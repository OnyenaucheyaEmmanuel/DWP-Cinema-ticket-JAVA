package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketServiceImplTest {

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl();
    }

    @Test
    void shouldPurchaseValidAdultTickets() {
        assertDoesNotThrow(() ->
                ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2)
                )
        );
    }

    @Test
    void shouldRejectInvalidAccountId() {
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        0L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
                )
        );
    }

    @Test
    void shouldRejectPurchaseWithoutAdult() {
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)
                )
        );
    }

    @Test
    void shouldRejectMoreThanTwentyFiveTickets() {
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26)
                )
        );
    }

    @Test
    void shouldRejectMoreInfantsThanAdults() {
        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
                )
        );
    }

    @Test
    void shouldAllowMixedValidPurchase() {
        assertDoesNotThrow(() ->
                ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3),
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
                )
        );
    }
}