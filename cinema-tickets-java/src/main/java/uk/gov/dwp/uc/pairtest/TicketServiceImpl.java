package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Objects;

public class TicketServiceImpl implements TicketService {

    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;
    private static final int MAX_TICKETS_PER_PURCHASE = 25;

    private final TicketPaymentService paymentService;
    private final SeatReservationService seatReservationService;

    // ✅ Constructor Injection (IMPORTANT)
    public TicketServiceImpl(TicketPaymentService paymentService,
                             SeatReservationService seatReservationService) {
        this.paymentService = paymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {

        validateAccountId(accountId);
        validateTicketRequests(ticketTypeRequests);

        PurchaseSummary summary = summariseTicketRequest(ticketTypeRequests);

        validateBusinessRules(summary);

        int totalAmount = calculateTotalAmount(summary);
        int totalSeats = calculateTotalSeats(summary);

        paymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    private void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Invalid account ID");
        }
    }

    private void validateTicketRequests(TicketTypeRequest... requests) {
        if (requests == null || requests.length == 0) {
            throw new InvalidPurchaseException("At least one ticket request is required");
        }

        for (TicketTypeRequest request : requests) {
            if (Objects.isNull(request)) {
                throw new InvalidPurchaseException("Ticket request cannot be null");
            }

            if (request.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException("Ticket quantity must be greater than zero");
            }
        }
    }

    private PurchaseSummary summariseTicketRequest(TicketTypeRequest... requests) {
        int adults = 0;
        int children = 0;
        int infants = 0;

        for (TicketTypeRequest request : requests) {
            switch (request.getTicketType()) {
                case ADULT:
                    adults += request.getNoOfTickets();
                    break;
                case CHILD:
                    children += request.getNoOfTickets();
                    break;
                case INFANT:
                    infants += request.getNoOfTickets();
                    break;
                default:
                    throw new InvalidPurchaseException("Unsupported ticket type");
            }
        }

        return new PurchaseSummary(adults, children, infants);
    }

    private void validateBusinessRules(PurchaseSummary summary) {
        int totalTickets = summary.getTotalTickets();

        if (totalTickets > MAX_TICKETS_PER_PURCHASE) {
            throw new InvalidPurchaseException("Maximum 25 tickets allowed per purchase");
        }

        if (summary.getAdultTickets() == 0 &&
                (summary.getChildTickets() > 0 || summary.getInfantTickets() > 0)) {
            throw new InvalidPurchaseException("Child and infant tickets require at least one adult");
        }

        if (summary.getInfantTickets() > summary.getAdultTickets()) {
            throw new InvalidPurchaseException("Each infant must be accompanied by an adult");
        }
    }

    private int calculateTotalAmount(PurchaseSummary summary) {
        return (summary.getAdultTickets() * ADULT_TICKET_PRICE)
                + (summary.getChildTickets() * CHILD_TICKET_PRICE);
    }

    private int calculateTotalSeats(PurchaseSummary summary) {
        return summary.getAdultTickets() + summary.getChildTickets();
    }

    private static final class PurchaseSummary {
        private final int adultTickets;
        private final int childTickets;
        private final int infantTickets;

        private PurchaseSummary(int adultTickets, int childTickets, int infantTickets) {
            this.adultTickets = adultTickets;
            this.childTickets = childTickets;
            this.infantTickets = infantTickets;
        }

        private int getAdultTickets() {
            return adultTickets;
        }

        private int getChildTickets() {
            return childTickets;
        }

        private int getInfantTickets() {
            return infantTickets;
        }

        private int getTotalTickets() {
            return adultTickets + childTickets + infantTickets;
        }
    }
}