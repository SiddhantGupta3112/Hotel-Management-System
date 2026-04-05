package com.hotel.app.service;

import com.hotel.app.entity.Invoice;
import com.hotel.app.entity.Payment;
import com.hotel.app.entity.ServiceCharge;
import com.hotel.app.repository.BookingRepository;
import com.hotel.app.repository.InvoiceRepository;
import com.hotel.app.repository.PaymentRepository;
import com.hotel.app.service.BookingService.Result;

import java.util.List;

public class BillingService {

    private final InvoiceRepository invoiceRepo = new InvoiceRepository();
    private final PaymentRepository paymentRepo = new PaymentRepository();
    private final BookingRepository bookingRepo = new BookingRepository();


    public Result<Void> generateInvoice(long bookingId) {
        var bookingOpt = bookingRepo.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            return Result.failure("Booking not found.");
        }

        if (!"CHECKED_OUT".equals(bookingOpt.get().getBookingStatus())) {
            return Result.failure("Invoice can only be generated after guest has checked out.");
        }

        if (invoiceRepo.invoiceExists(bookingId)) {
            return Result.failure("An invoice already exists for this booking.");
        }

        boolean success = invoiceRepo.generate(bookingId);
        return success ? Result.success(null) : Result.failure("Error generating invoice record.");
    }


    public Result<Void> recordPayment(long bookingId, long methodId, double amount) {
        if (amount <= 0) {
            return Result.failure("Payment amount must be greater than zero.");
        }

        // Basic check to see if booking exists before taking money
        if (bookingRepo.findById(bookingId).isEmpty()) {
            return Result.failure("Cannot record payment for non-existent booking.");
        }

        boolean success = paymentRepo.save(bookingId, methodId, amount);
        return success ? Result.success(null) : Result.failure("Database error while recording payment.");
    }

    public List<ServiceCharge> getServiceChargesForBooking(long bookingId) {
        return invoiceRepo.findChargesWithDetailsByBookingId(bookingId);
    }


    public double getTotalPaidForBooking(long bookingId) {
        return invoiceRepo.getTotalPaidForBooking(bookingId);
    }


    public List<Invoice> getAllInvoices() {
        return invoiceRepo.findAll();
    }


    public List<Invoice> getInvoicesForCustomer(long customerId) {
        return invoiceRepo.findByCustomerId(customerId);
    }


    public List<Payment> getPaymentsForBooking(long bookingId) {
        return paymentRepo.findByBookingId(bookingId);
    }


    public List<String[]> getPaymentMethods() {
        return paymentRepo.findAllPaymentMethods();
    }
}