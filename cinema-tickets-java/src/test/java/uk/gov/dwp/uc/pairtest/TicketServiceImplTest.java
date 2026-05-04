package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private TicketPaymentService paymentService;
    private SeatReservationService seatService;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        paymentService = mock(TicketPaymentService.class);
        seatService = mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(paymentService, seatService);
    }


    @Test
    void shouldPurchaseAdultTicketsSuccessfully() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2)
        );

        verify(paymentService).makePayment(1L, 50);
        verify(seatService).reserveSeat(1L, 2);
    }

    @Test
    void shouldHandleMixedTicketPurchaseCorrectly() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 3),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        );

        verify(paymentService).makePayment(1L, 95); // (2*25 + 3*15)
        verify(seatService).reserveSeat(1L, 5);     // adults + children
    }

    @Test
    void shouldAllowInfantsWhenAccompaniedByAdults() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
        );

        verify(paymentService).makePayment(1L, 50);
        verify(seatService).reserveSeat(1L, 2);
    }

    @Test
    void shouldHandleDuplicateTicketTypesCorrectly() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3)
        );

        verify(paymentService).makePayment(1L, 125);
        verify(seatService).reserveSeat(1L, 5);
    }

    @Test
    void shouldAllowMaximumTicketLimit() {
        ticketService.purchaseTickets(
                1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 25)
        );

        verify(paymentService).makePayment(1L, 625);
        verify(seatService).reserveSeat(1L, 25);
    }



    @Test
    void shouldRejectInvalidAccountId() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(
                        0L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)
                )
        );

        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    void shouldRejectNullTicketRequests() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, (TicketTypeRequest[]) null)
        );

        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    void shouldRejectEmptyTicketRequests() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L)
        );

        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    void shouldRejectNullTicketRequestElement() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(
                        1L,
                        null
                )
        );

        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    void shouldRejectZeroTicketQuantity() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0)
                )
        );

        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    void shouldRejectNegativeTicketQuantity() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1)
                )
        );

        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    void shouldRejectMoreThanTwentyFiveTickets() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26)
                )
        );

        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    void shouldRejectChildTicketsWithoutAdult() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)
                )
        );

        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    void shouldRejectInfantTicketsWithoutAdult() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
                )
        );

        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    void shouldRejectMoreInfantsThanAdults() {
        assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(
                        1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
                )
        );

        verifyNoInteractions(paymentService, seatService);
    }
}