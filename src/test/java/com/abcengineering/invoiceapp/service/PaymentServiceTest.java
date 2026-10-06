package com.abcengineering.invoiceapp.service;

import com.abcengineering.invoiceapp.dto.CancelPaymentRequest;
import com.abcengineering.invoiceapp.dto.CreatePaymentRequest;
import com.abcengineering.invoiceapp.exception.InvoiceNotFoundException;
import com.abcengineering.invoiceapp.exception.PaymentNotFoundException;
import com.abcengineering.invoiceapp.model.Invoice;
import com.abcengineering.invoiceapp.model.Payment;
import com.abcengineering.invoiceapp.repository.InvoiceRepository;
import com.abcengineering.invoiceapp.repository.PaymentRepository;
import com.abcengineering.invoiceapp.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    InvoiceRepository invoiceRepository;

    @Mock
    PaymentRepository paymentRepository;

    @InjectMocks
    PaymentService paymentService;

    @Nested
    class GetPaymentsTests {

        private final Integer paymentId = 1000;
        private final String companyName = "Holton Engineering Supplies;";


        @Test
        @DisplayName("viewPayment throws exception")
        void viewPaymentWherePaymentNotFound() {

            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.empty());

            assertThrows(PaymentNotFoundException.class,
                    () -> paymentService.viewPayment(paymentId));
        }


        @Test
        @DisplayName("viewPayment returns payment")
        void viewPayment() {

            final LocalDate paymentDate = LocalDate.now();
            final BigDecimal paymentAmount = BigDecimal.valueOf(100);
            final String paymentMethod = "CHAPS";
            final String paymentReference = "HES-ABC-99";

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            Payment payment = new Payment(invoice, paymentDate,
                    paymentAmount, paymentMethod, paymentReference);
            payment.setId(paymentId);

            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.of(payment));

            Payment actualPayment = paymentService.viewPayment(paymentId);

            assertSame(payment, actualPayment);
            assertEquals(paymentId, actualPayment.getId());
            assertEquals(paymentDate, actualPayment.getPaymentDate());
            assertEquals(paymentAmount, actualPayment.getPaymentAmount());
            assertEquals(paymentMethod, actualPayment.getPaymentMethod());
            assertEquals(paymentReference, actualPayment.getPaymentReference());
        }

        @Test
        @DisplayName("getNonCancelledPayments returns empty list")
        void getNonCancelledPaymentsForInvoiceWhereNoneFound() {

            final Integer invoiceId = 12;
            when(paymentRepository.findByInvoiceIdAndCancellationDateTimeIsNull(invoiceId))
                    .thenReturn(List.of());

            List<Payment> returnedPayments = paymentService.getNonCancelledPaymentsForInvoice(invoiceId);
            assertTrue(returnedPayments.isEmpty());
        }


        @Test
        @DisplayName("getNonCancelledPayments returns payments")
        void getNonCancelledPaymentsForInvoice() {

            // Create invoice first
            final Integer invoiceId = 120;
            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setId(invoiceId);

            // Create some payments to go with that invoice
            final int SIZE = 3;
            final LocalDate[] paymentDates = {
                    LocalDate.of(2026, 9, 1),
                    LocalDate.of(2026, 9, 2),
                    LocalDate.of(2026, 9, 3)
            };

            final BigDecimal[] paymentAmounts = {
                    BigDecimal.valueOf(100),
                    BigDecimal.valueOf(200),
                    BigDecimal.valueOf(300)
            };
            final String[] paymentMethods = {"CHAPS", "BACS", "Faster Service"};
            final String[] paymentReferences = {"HES-ABC-100",
                    "HES-ABC-200", "HES-ABC-300"};
            final Integer[] paymentIds = {10, 20, 30};

            Payment[] payments = new Payment[SIZE];

            for (int i = 0; i < SIZE; i++) {
                payments[i] = new Payment(invoice, paymentDates[i],
                    paymentAmounts[i], paymentMethods[i], paymentReferences[i]);
                payments[i].setId(paymentIds[i]);
            }

            when(paymentRepository.findByInvoiceIdAndCancellationDateTimeIsNull(invoiceId))
                    .thenReturn(List.of(payments));

            List<Payment> returnedPayments = paymentService.getNonCancelledPaymentsForInvoice(invoiceId);

            assertEquals(SIZE, returnedPayments.size());

            for (int i = 0; i < SIZE; i++) {
                Payment payment = returnedPayments.get(i);
                assertEquals(paymentIds[i], payment.getId());
                assertEquals(paymentDates[i], payment.getPaymentDate());
                assertEquals(paymentAmounts[i], payment.getPaymentAmount());
                assertEquals(paymentMethods[i], payment.getPaymentMethod());
                assertEquals(paymentReferences[i], payment.getPaymentReference());
            }
        }
    } // end of GetPaymentsTests


    @Nested
    class CancelPaymentTests {

        final Integer paymentId = 50;
        final String companyName = "Hawksworth Skips";

        @Test
        @DisplayName("cancelPayment throws exception")
        void cancelPaymentWherePaymentNotFound() {

            CancelPaymentRequest request = new CancelPaymentRequest();
            request.setCancellationReason("Goods not delivered");

            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.empty());

            assertThrows(PaymentNotFoundException.class,
                    () -> paymentService.cancelPayment(paymentId, request));

            verify(paymentRepository).findById(paymentId);
            verify(paymentRepository, never()).save(any(Payment.class));
        }


        @Test
        @DisplayName("cancelPayment returns false")
        void cancelPaymentWherePaymentAlreadyCancelled() {
            CancelPaymentRequest request = new CancelPaymentRequest();
            request.setCancellationReason("Services not rendered");

            final LocalDate paymentDate = LocalDate.now();
            final BigDecimal paymentAmount = BigDecimal.valueOf(100);
            final String paymentMethod = "CHAPS";
            final String paymentReference = "HES-ABC-99";

            Invoice invoice = TestDataFactory.createInvoice(companyName);

            Payment payment = new Payment(invoice, paymentDate, paymentAmount,
                    paymentMethod, paymentReference);
            payment.setCancellationReason("Order cancelled");
            payment.setCancellationDateTime(LocalDateTime.now());

            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.of(payment));

            boolean paymentCancelled = paymentService.cancelPayment(paymentId, request);

            assertFalse(paymentCancelled);
            verify(paymentRepository).findById(paymentId);
            verify(paymentRepository, never()).save(any(Payment.class));
        }


        @Test
        @DisplayName("cancelPayment successfully cancels payment")
        void cancelPayment() {

            final LocalDate paymentDate = LocalDate.now();
            final BigDecimal paymentAmount = BigDecimal.valueOf(50);
            final String paymentMethod = "BACS";
            final String paymentReference = "HES-ABC-110";
            final String cancellationReason = "Payment made in error";

            CancelPaymentRequest request = new CancelPaymentRequest();
            request.setCancellationReason(cancellationReason);

            Invoice invoice = TestDataFactory.createInvoice(companyName);

            Payment payment = new Payment(invoice, paymentDate, paymentAmount,
                    paymentMethod, paymentReference);

            when(paymentRepository.findById(paymentId))
                    .thenReturn(Optional.of(payment));

            boolean paymentCancelled = paymentService.cancelPayment(paymentId, request);

            verify(paymentRepository).findById(paymentId);
            verify(paymentRepository).save(payment);

            assertTrue(paymentCancelled);
            assertEquals(cancellationReason, payment.getCancellationReason());
            assertNotNull(payment.getCancellationDateTime());
        }
    }   // end of  CancelPaymentTests


    @Nested
    class CreatePaymentTests {

        final String companyName = "Dave Bledsoe Catering Services";
        final Integer invoiceId = 50;


        @Test
        @DisplayName("createPayment fails as invoice not found")
        void createPaymentWhereInvoiceNotFound() {

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setId(invoiceId);

            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setInvoiceId(invoiceId);

            when(invoiceRepository.findById(invoiceId))
                    .thenReturn(Optional.empty());

            assertThrows(InvoiceNotFoundException.class,
                    () -> paymentService.createPayment(request));

            verify(invoiceRepository).findById(invoiceId);
            verify(paymentRepository, never()).save(any(Payment.class));
        }


        @Test
        @DisplayName("createPayment creates and returns a payment")
        void createPayment() {

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setId(invoiceId);

            final BigDecimal paymentAmount = BigDecimal.valueOf(300);
            final LocalDate paymentDate = LocalDate.now();
            final String paymentMethod = "BACS";
            final String paymentReference = "DBCS-ABC-105";
            final Integer paymentId = 20;

            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setInvoiceId(invoiceId);
            request.setPaymentAmount(paymentAmount);
            request.setPaymentDate(paymentDate);
            request.setPaymentMethod(paymentMethod);
            request.setPaymentReference(paymentReference);

            Payment expectedPayment = new Payment(invoice, paymentDate,
                    paymentAmount, paymentMethod, paymentReference);
            expectedPayment.setId(paymentId);

            when(invoiceRepository.findById(invoiceId))
                    .thenReturn(Optional.of(invoice));

            when(paymentRepository.save(any(Payment.class)))
                    .thenReturn(expectedPayment);

            Payment returnedPayment = paymentService.createPayment(request);

            // Check what is returned matches what was set up to be returned
            assertSame(expectedPayment, returnedPayment);

            ArgumentCaptor<Payment> captor =
                    ArgumentCaptor.forClass(Payment.class);

            verify(paymentRepository).save(captor.capture());

            // This is the payment object createPayment
            // itself sets up.
            Payment capturedPayment = captor.getValue();

            assertSame(invoice, capturedPayment.getInvoice());
            assertEquals(paymentDate, capturedPayment.getPaymentDate());
            assertEquals(paymentAmount, capturedPayment.getPaymentAmount());
            assertEquals(paymentMethod, capturedPayment.getPaymentMethod());
            assertEquals(paymentReference, capturedPayment.getPaymentReference());
            assertNull(capturedPayment.getCancellationDateTime());
            assertNull(capturedPayment.getCancellationReason());
        }
    } // end of CreatePaymentTests
}
