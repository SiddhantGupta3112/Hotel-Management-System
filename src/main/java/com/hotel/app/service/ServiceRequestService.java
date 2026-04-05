package com.hotel.app.service;

import com.hotel.app.entity.Booking;
import com.hotel.app.entity.Service;
import com.hotel.app.entity.ServiceRequest;
import com.hotel.app.repository.ServiceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for the hotel service request system.
 *
 * Flow:
 *   Customer places request (PENDING)
 *       → Staff marks in-progress (IN_PROGRESS)
 *       → Staff marks served     (SERVED)  → SERVICE_CHARGES row created → appears in bill
 *       OR
 *       → Staff cancels          (CANCELLED) → nothing written to SERVICE_CHARGES → no billing
 */
public class ServiceRequestService {

    private final ServiceRepository repo;

    public ServiceRequestService() {
        this.repo = new ServiceRepository();
    }

    // ─────────────────────────────────────────────────────────
    // Customer-facing
    // ─────────────────────────────────────────────────────────

    /**
     * Finds the most recent CONFIRMED or CHECKED_IN booking for this user.
     * Returns empty if the customer has no active booking — in which case
     * the Services screen should show the "no active booking" message.
     */
    public Optional<Booking> getActiveBooking(long userId) {
        return repo.findActiveBookingForUser(userId);
    }

    /** Returns all available services (is_available = 1), sorted by category. */
    public List<Service> getAllServices() {
        return repo.findAllServices();
    }

    /**
     * Places a service request for a booking.
     *
     * Business rules enforced here:
     *   1. Booking must be CONFIRMED or CHECKED_IN — no requests for checked-out
     *      or cancelled bookings.
     *   2. Quantity must be at least 1.
     *
     * Throws IllegalStateException if the booking is ineligible.
     * Throws IllegalArgumentException if quantity is invalid.
     * Returns false (and logs the error) if the DB insert fails.
     */
    public boolean placeRequest(long bookingId, String bookingStatus,
                                long serviceId, int quantity, String notes) {

        if (!isEligibleForServices(bookingStatus)) {
            throw new IllegalStateException(
                    "Services can only be requested for CONFIRMED or CHECKED_IN bookings.\n" +
                            "This booking is: " + bookingStatus);
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        return repo.saveRequest( bookingId,  serviceId, quantity, notes);
    }

    /**
     * Returns all requests for a specific booking (all statuses).
     * Used by the customer to see their own request history.
     */
    public List<ServiceRequest> getRequestsForBooking(long bookingId) {
        return repo.findRequestsByBooking(bookingId);
    }

    // ─────────────────────────────────────────────────────────
    // Staff-facing
    // ─────────────────────────────────────────────────────────

    /**
     * Returns all PENDING and IN_PROGRESS requests across all bookings.
     * This is the staff queue — only admin and staff roles see this screen.
     */
    public List<ServiceRequest> getPendingRequests() {
        return repo.findActiveRequests();
    }

    /**
     * Staff acknowledges the request and is working on it.
     * Only transitions from PENDING → IN_PROGRESS.
     * Returns false if the request is already in another state.
     */
    public boolean markInProgress(long requestId, long staffUserId) {
        return repo.markInProgress(requestId, staffUserId);
    }

    /**
     * Staff marks the request as done. This is the key billing trigger:
     *   - SERVICE_REQUESTS.status → SERVED
     *   - SERVICE_CHARGES row inserted (quantity × base_price)
     *
     * Both DB writes happen in a single transaction. If either fails,
     * nothing is committed, so there can never be a SERVED status
     * without a corresponding charge record.
     *
     * Returns false if the request was not found, already in a terminal
     * state, or if the transaction failed.
     */
    public boolean markServed(long requestId, long staffUserId) {
        return repo.markServed( requestId,  staffUserId);
    }

    /**
     * Staff cancels the request (e.g. service unavailable, guest not in room).
     *   - SERVICE_REQUESTS.status → CANCELLED
     *   - Nothing written to SERVICE_CHARGES
     *
     * Because no SERVICE_CHARGES row is created, cancellation has
     * zero impact on the guest's bill.
     *
     * Works on both PENDING and IN_PROGRESS requests.
     */
    public boolean markCancelled(long requestId) {
        return repo.markCancelled( requestId);
    }

    // ─────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────

    /**
     * A booking is eligible for service requests if it is CONFIRMED
     * (booked but not yet arrived — valid for pre-arrival requests such
     * as airport transfer) or CHECKED_IN (guest is in the hotel).
     */
    private boolean isEligibleForServices(String bookingStatus) {
        return List.of("APPROVED","CHECKIN_PENDING","CHECKED_IN","CHECKOUT_PENDING")
                .contains(bookingStatus);
    }
}
