package com.abcengineering.invoiceapp.repository;


import com.abcengineering.invoiceapp.model.Invoice;
import com.abcengineering.invoiceapp.model.Payment;
import com.abcengineering.invoiceapp.model.Supplier;
import com.abcengineering.invoiceapp.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = ALL)
public class PaymentRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    final String companyName = "Bassett Technical Services";
    final LocalDate paymentDate = LocalDate.now();
    final BigDecimal paymentAmount = BigDecimal.valueOf(35);
    final String paymentMethod = "BACS";
    final String paymentReference = "ABC-1001";
    final LocalDateTime cancelledAt = LocalDateTime.now();
    final String cancellationReason = "Incorrect amount listed";

    PaymentRepositoryTest(SupplierRepository supplierRepository,
                          InvoiceRepository invoiceRepository,
                          PaymentRepository paymentRepository) {
        this.supplierRepository = supplierRepository;
        this.invoiceRepository =  invoiceRepository;
        this.paymentRepository =  paymentRepository;
    }

    @Nested
    class FindPaymentsByIdTests {

        @Test
        @DisplayName("findById returns empty for non-existent id")
        void findByIdWherePaymentNotFound() {
            Optional<Payment> payment = paymentRepository.findById(500);
            assertTrue(payment.isEmpty());
        }


        @Test
        @DisplayName("findById returns payment with mandatory fields only populated")
        void findByIdReturnsPaymentInvoice() {

            // Payment - requires Invoice - requires Supplier
            Supplier supplier = TestDataFactory.createSupplier(companyName);
            Supplier savedSupplier = supplierRepository.save(supplier);

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setSupplier(savedSupplier);
            Invoice savedInvoice = invoiceRepository.save(invoice);

            Payment payment = new Payment(savedInvoice, paymentDate, paymentAmount,
                    paymentMethod, paymentReference);
            Payment savedPayment = paymentRepository.save(payment);

            Optional<Payment> returnedPayment = paymentRepository.findById(savedPayment.getId());
            assertTrue(returnedPayment.isPresent());

            Payment retrievedPayment = returnedPayment.get();
            assertNotNull(retrievedPayment.getId());
            assertEquals(savedPayment.getInvoice().getId(), retrievedPayment.getInvoice().getId());
            assertEquals(paymentDate, retrievedPayment.getPaymentDate());
            assertEquals(paymentAmount, retrievedPayment.getPaymentAmount());
            assertEquals(paymentMethod, retrievedPayment.getPaymentMethod());
            assertEquals(paymentReference, retrievedPayment.getPaymentReference());
            assertNull(retrievedPayment.getCancellationDateTime());
            assertNull(retrievedPayment.getCancellationReason());
        }


        @Test
        @DisplayName("findById returns payment with all fields populated")
        void findByIdReturnsCancelledPayment() {

            // Payment - requires Invoice - requires Supplier
            Supplier supplier = TestDataFactory.createSupplier(companyName);
            Supplier savedSupplier = supplierRepository.save(supplier);

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setSupplier(savedSupplier);
            Invoice savedInvoice = invoiceRepository.save(invoice);

            Payment payment = new Payment(savedInvoice, paymentDate, paymentAmount,
                    paymentMethod, paymentReference);
            payment.setCancellationDateTime(cancelledAt);
            payment.setCancellationReason(cancellationReason);
            Payment savedPayment = paymentRepository.save(payment);

            Optional<Payment> returnedPayment = paymentRepository.findById(savedPayment.getId());
            assertTrue(returnedPayment.isPresent());

            Payment retrievedPayment = returnedPayment.get();
            assertNotNull(retrievedPayment.getId());
            assertEquals(savedPayment.getInvoice().getId(), retrievedPayment.getInvoice().getId());
            assertEquals(paymentDate, retrievedPayment.getPaymentDate());
            assertEquals(paymentAmount, retrievedPayment.getPaymentAmount());
            assertEquals(paymentMethod, retrievedPayment.getPaymentMethod());
            assertEquals(paymentReference, retrievedPayment.getPaymentReference());
            assertEquals(cancelledAt, retrievedPayment.getCancellationDateTime());
            assertEquals(cancellationReason, retrievedPayment.getCancellationReason());
        }
    } // end of FindPaymentsByIdTests


    @Nested
    class FindNonCancelledPaymentsForInvoiceTests {

        @Test
        @DisplayName("findByInvoice finds no payments")
        void findByInvoiceIdNoPaymentsFound() {

            Supplier supplier = TestDataFactory.createSupplier(companyName);
            Supplier savedSupplier = supplierRepository.save(supplier);

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setSupplier(savedSupplier);
            Invoice savedInvoice = invoiceRepository.save(invoice);

            List<Payment> noncancelledPayments
                    = paymentRepository.findByInvoiceIdAndCancellationDateTimeIsNull(invoice.getId());

            assertTrue(noncancelledPayments.isEmpty());
        }

        @Test
        @DisplayName("findByInvoice finds single active payment")
        void findByInvoiceIdAndCancellationDateTimeIsNullReturnsActivePayment() {

            Supplier supplier = TestDataFactory.createSupplier(companyName);
            Supplier savedSupplier = supplierRepository.save(supplier);

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setSupplier(savedSupplier);
            Invoice savedInvoice = invoiceRepository.save(invoice);

            Payment payment = new Payment(savedInvoice, paymentDate, paymentAmount,
                    paymentMethod, paymentReference);
            Payment expectedPayment = paymentRepository.save(payment);

            List<Payment> noncancelledPayments
                    = paymentRepository.findByInvoiceIdAndCancellationDateTimeIsNull(invoice.getId());

            assertEquals(1, noncancelledPayments.size());

            Payment actualPayment = noncancelledPayments.getFirst();

            assertEquals(expectedPayment.getInvoice().getId(), actualPayment.getInvoice().getId());
            assertEquals(expectedPayment.getPaymentReference(), actualPayment.getPaymentReference());
            assertEquals(paymentMethod, actualPayment.getPaymentMethod());
            assertEquals(paymentAmount, actualPayment.getPaymentAmount());
            assertEquals(paymentDate, actualPayment.getPaymentDate());
            assertNull(actualPayment.getCancellationDateTime());
            assertNull(actualPayment.getCancellationReason());
        }


        @Test
        @DisplayName("findByInvoice returns active payments, excludes cancelled payment")
        void findByInvoiceIdExcludesCancelledPaymentAndUnrelatedPayment() {

            Supplier supplier1 = TestDataFactory.createSupplier(companyName);
            Supplier savedSupplier1 = supplierRepository.save(supplier1);

            Invoice invoice1 = TestDataFactory.createInvoice(companyName);
            invoice1.setSupplier(savedSupplier1);
            Invoice savedInvoice1 = invoiceRepository.save(invoice1);

            // Create a second supplier with an invoice.
            // This allows creation of a payment for that invoice that is
            // unrelated to the invoice searched against, thus enabling
            // a test to ensure the irrelevant payment is not returned.
            final String otherCompanyName = "Jack Grant Ltd.";
            Supplier supplier2 = TestDataFactory.createSupplier(otherCompanyName);
            Supplier savedSupplier2 = supplierRepository.save(supplier2);

            Invoice invoice2 = TestDataFactory.createInvoice(otherCompanyName);
            invoice2.setSupplier(savedSupplier2);
            Invoice savedInvoice2 = invoiceRepository.save(invoice2);

            // Create the payments for the supplier/invoice of interest
            Payment activePayment1 = new Payment(savedInvoice1, paymentDate, paymentAmount,
                    paymentMethod, paymentReference);

            final LocalDate otherPaymentDate = LocalDate.of(2026, 9, 12);
            final BigDecimal otherPaymentAmount = BigDecimal.valueOf(90);
            final String otherPaymentMethod = "Cash";
            final String otherPaymentReference = "ABC-02";

            Payment activePayment2 = new Payment(savedInvoice1, otherPaymentDate,
                    otherPaymentAmount, otherPaymentMethod, otherPaymentReference);

            Payment cancelledPayment = new Payment(savedInvoice1, paymentDate,
                    paymentAmount, paymentMethod, "ABC-03");
            cancelledPayment.setCancellationReason("Goods returned");
            cancelledPayment.setCancellationDateTime(LocalDateTime.now());

            // Save the payments for invoice 1
            Payment savedPayment1 = paymentRepository.save(activePayment1);
            Payment savedPayment2 = paymentRepository.save(activePayment2);
            Payment savedPayment3 = paymentRepository.save(cancelledPayment);

            // Create a payment irrelevant to invoice 1
            Payment unrelatedPayment = new Payment(savedInvoice2, paymentDate,
                    paymentAmount, paymentMethod, "ABC-04");

            // Save the payment for invoice 2
            Payment savedPayment4 = paymentRepository.save(unrelatedPayment);

            List<Payment> returnedPaymentsForInvoice
                    = paymentRepository.findByInvoiceIdAndCancellationDateTimeIsNull(invoice1.getId());

            assertEquals(2, returnedPaymentsForInvoice.size());

            // Values common to both payments checked here
            for (Payment payment : returnedPaymentsForInvoice) {
                assertEquals(invoice1.getId(), payment.getInvoice().getId());
                assertNull(payment.getCancellationDateTime());
                assertNull(payment.getCancellationReason());
                assertNotNull(payment.getId());
            }

            // Other fields are distinct in each payment.
            // The order of the payments in the returned list is not predictable.
            // Therefore, need to search in the list for the values.

            assertThat(returnedPaymentsForInvoice)
                    .extracting(Payment::getId)
                    .containsExactlyInAnyOrder(
                            savedPayment1.getId(),
                            savedPayment2.getId());

            assertThat(returnedPaymentsForInvoice)
                    .extracting(Payment::getPaymentDate)
                    .containsExactlyInAnyOrder(paymentDate, otherPaymentDate);

            assertThat(returnedPaymentsForInvoice)
                    .extracting(Payment::getPaymentAmount)
                    .containsExactlyInAnyOrder(paymentAmount, otherPaymentAmount);

            assertThat(returnedPaymentsForInvoice)
                    .extracting(Payment::getPaymentMethod)
                    .containsExactlyInAnyOrder(paymentMethod, otherPaymentMethod);

            assertThat(returnedPaymentsForInvoice)
                    .extracting(Payment::getPaymentReference)
                    .containsExactlyInAnyOrder(paymentReference, otherPaymentReference);
        }
    } // end of FindNonCancelledPaymentsForInvoiceTests
}
