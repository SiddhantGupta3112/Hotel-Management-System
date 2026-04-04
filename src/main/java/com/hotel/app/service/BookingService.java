package com.hotel.app.service;

import com.hotel.app.entity.Booking;
import com.hotel.app.entity.Customer;
import com.hotel.app.repository.BookingRepository;
import com.hotel.app.repository.CustomerRepository;
import com.hotel.app.repository.RoomRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BookingService{

    private final BookingRepository bookingRepo = new BookingRepository();
    private final CustomerRepository customerRepo= new CustomerRepository();
    private final RoomRepository roomRepo = new RoomRepository();

    public static class Result<T>{
        private final T data;
        private final String errorMessage;
        private final boolean success;

        private Result(T data, String errorMessage, boolean success){
             this.data = data;
             this.errorMessage = errorMessage;
             this.success = success;
        }

        public static <T> Result<T> success(T data) { return new Result<>(data, null, true); }
        public static <T> Result<T> failure(String msg) { return new Result<>(null, msg, false); }

        public T getData() { return data; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isSuccess() { return success; }
    }

    public Result<Long> makeBooking(long customerId, long roomId, LocalDate checkIn, LocalDate checkOut){

        if(checkIn == null || checkOut == null)  return Result.failure("Dates cannot be null.");
        if(!checkOut.isAfter(checkIn))  return Result.failure("Check-out must be after Check-in.");
        if(checkIn.isBefore(LocalDate.now()))  return Result.failure("Cannot book in the past.");

        // Check if room exists and is currently AVAILABLE
        var roomOpt = roomRepo.findbyid(roomId);
        if(roomOpt.isEmpty() || !"AVAILABLE".equals(roomOpt.get().getStatus())){
            return Result.failure("Room is not available for booking.");
        }

        // Check for overlapping APPROVED bookings
        if(bookingRepo.hasOverlappingBooking(roomId, checkIn, checkOut, null)){
            return Result.failure("Room is already reserved for these dates.");
        }

        long bookingId = bookingRepo.save(customerId, roomId, checkIn, checkOut);
        return bookingId != -1 ? Result.success(bookingId) : Result.failure("Database error creating booking.");
    }


    public Result<Void> requestCheckIn(long bookingId, long customerId) {
        Optional<Booking> bOpt = bookingRepo.findById(bookingId);
        if (bOpt.isEmpty()) return Result.failure("Booking not found.");

        Booking b = bOpt.get();
        if (b.getCustomerId() != customerId) return Result.failure("Security Alert: Unauthorized access.");
        if (!b.canRequestCheckIn()) return Result.failure("Cannot request check-in at this time.");

        return bookingRepo.updateStatus(bookingId, "CHECKIN_PENDING")
                ? Result.success(null) : Result.failure("Update failed.");
    }

    public Result<Void> requestCheckOut(long bookingId, long customerId) {
        Optional<Booking> bOpt = bookingRepo.findById(bookingId);
        if (bOpt.isEmpty()) return Result.failure("Booking not found.");

        Booking b = bOpt.get();
        if (b.getCustomerId() != customerId) return Result.failure("Unauthorized access.");
        if (!b.canRequestCheckOut()) return Result.failure("Not currently checked in.");

        return bookingRepo.updateStatus(bookingId, "CHECKOUT_PENDING")
                ? Result.success(null) : Result.failure("Update failed.");
    }

    public Result<Void> cancelBooking(long bookingId, long customerId) {
        Optional<Booking> bOpt = bookingRepo.findById(bookingId);
        if (bOpt.isEmpty()) return Result.failure("Booking not found.");

        Booking b = bOpt.get();
        if (b.getCustomerId() != customerId) return Result.failure("Unauthorized access.");
        if (!b.canCancel()) return Result.failure("Booking cannot be cancelled in current state.");

        boolean success;
        if ("APPROVED".equals(b.getBookingStatus())) {
            success = bookingRepo.updateStatusAndRoomStatus(bookingId, "CANCELLED", b.getRoomId(), "AVAILABLE");
        } else {
            success = bookingRepo.updateStatus(bookingId, "CANCELLED");
        }
        return success ? Result.success(null) : Result.failure("Cancellation failed.");
    }

    // Admin methods

    public Result<Void> approveBooking(long bookingId) {
        Booking b = bookingRepo.findById(bookingId).orElse(null);
        if (b == null || !"PENDING".equals(b.getBookingStatus())) return Result.failure("Invalid booking state.");

        // Re-check room availability right before approval
        var room = roomRepo.findbyid(b.getRoomId()).orElse(null);
        if (room == null || !"AVAILABLE".equals(room.getStatus())) {
            return Result.failure("Room is no longer available.");
        }

        return bookingRepo.updateStatusAndRoomStatus(bookingId, "APPROVED", b.getRoomId(), "RESERVED")
                ? Result.success(null) : Result.failure("Approval failed.");
    }

    public Result<Void> rejectBooking(long bookingId) {
        return bookingRepo.updateStatus(bookingId, "REJECTED")
                ? Result.success(null) : Result.failure("Rejection failed.");
    }

    public Result<Void> approveCheckIn(long bookingId) {
        Booking b = bookingRepo.findById(bookingId).orElse(null);
        if (b == null || !"CHECKIN_PENDING".equals(b.getBookingStatus())) return Result.failure("Not awaiting check-in.");

        return bookingRepo.updateStatusAndRoomStatus(bookingId, "CHECKED_IN", b.getRoomId(), "OCCUPIED")
                ? Result.success(null) : Result.failure("Check-in approval failed.");
    }


    public Result<Void> approveCheckOut(long bookingId) {
        Booking b = bookingRepo.findById(bookingId).orElse(null);
        if (b == null || !"CHECKOUT_PENDING".equals(b.getBookingStatus())) return Result.failure("Not awaiting check-out.");

        if (bookingRepo.updateStatusAndRoomStatus(bookingId, "CHECKED_OUT", b.getRoomId(), "AVAILABLE")) {
            customerRepo.addLoyaltyPoints(b.getCustomerId(), 10);
            return Result.success(null);
        }
        return Result.failure("Check-out failed.");
    }

    public Result<Void> rejectCheckIn(long bookingId) {
        return bookingRepo.updateStatus(bookingId, "APPROVED") ? Result.success(null) : Result.failure("Update failed.");
    }

    public Result<Void> rejectCheckOut(long bookingId) {
        return bookingRepo.updateStatus(bookingId, "CHECKED_IN") ? Result.success(null) : Result.failure("Update failed.");
    }

    public List<Booking> getAllBookings() { return bookingRepo.findAll(); }
    public List<Booking> getBookingsByStatus(String status) { return bookingRepo.findByStatus(status); }
    public List<Booking> getBookingsForCustomer(long customerId) { return bookingRepo.findByCustomerId(customerId); }
    public Optional<Booking> getActiveBooking(long customerId) { return bookingRepo.findActiveByCustomerId(customerId); }
    public Optional<Customer> getCustomerByUserId(long userId) { return customerRepo.findByUserId(userId); }

}