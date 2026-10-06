package com.abcengineering.invoiceapp.service;

import com.abcengineering.invoiceapp.dto.CancelInvoiceRequest;
import com.abcengineering.invoiceapp.dto.CreateInvoiceRequest;
import com.abcengineering.invoiceapp.dto.UpdateInvoiceRequest;
import com.abcengineering.invoiceapp.exception.InvoiceNotFoundException;
import com.abcengineering.invoiceapp.exception.SupplierNotFoundException;
import com.abcengineering.invoiceapp.model.Invoice;
import com.abcengineering.invoiceapp.model.Payment;
import com.abcengineering.invoiceapp.model.Supplier;
import com.abcengineering.invoiceapp.repository.InvoiceRepository;
import com.abcengineering.invoiceapp.repository.SupplierRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTest {

    @Mock
    InvoiceRepository invoiceRepository;

    @Mock
    SupplierRepository supplierRepository;

    @Mock
    PaymentService paymentService;

    @InjectMocks
    InvoiceService invoiceService;

    @Nested
    class GetInvoiceTests {

        private final String companyName = "Holton Engineering Supplies";

        @Test
        @DisplayName("getInvoice returns invoice")
        void getInvoiceWhereInvoiceFound() {
            Supplier supplier = TestDataFactory.createSupplier(companyName);
            final Integer invoiceId = 15;
            final String invoiceRef = "ABC-01";
            LocalDate invoiceDate = LocalDate.now();
            LocalDate dueDate = LocalDate.of(2026, 12, 4);
            BigDecimal invoiceAmount = BigDecimal.valueOf(345.67);

            Invoice expectedInvoice = new Invoice(supplier,
                    invoiceRef, invoiceDate, dueDate, invoiceAmount);
            expectedInvoice.setId(invoiceId);

            when(invoiceRepository.findById(invoiceId))
                    .thenReturn(Optional.of(expectedInvoice));

            Invoice actualInvoice = invoiceService.getInvoice(invoiceId);

            assertSame(expectedInvoice, actualInvoice);
            assertEquals(invoiceId, actualInvoice.getId());
            assertEquals(invoiceRef, actualInvoice.getSupplierInvoiceRef());
            assertEquals(invoiceDate, actualInvoice.getInvoiceDate());
            assertEquals(dueDate, actualInvoice.getDueDate());
            assertEquals(invoiceAmount, actualInvoice.getInvoiceAmount());
        }


        @Test
        @DisplayName("getInvoice throws exception")
        void getInvoiceWhereInvoiceNotFound() {
            final Integer invoiceId = 12;
            when(invoiceRepository.findById(invoiceId))
                    .thenReturn(Optional.empty());

            assertThrows(InvoiceNotFoundException.class,
                    () -> invoiceService.getInvoice(invoiceId));
        }


        @Test
        @DisplayName("getlAllInvoices returns list when invoices exist")
        void getAllInvoicesWhereInvoicesFound() {
            final Integer supplierId = 20;
            Supplier supplier = TestDataFactory.createSupplier("David Thrower Accountancy Services");
            supplier.setId(supplierId);

            final String baseRef = "ABC-";
            final long baseAmount = 100;
            final int arraySize = 4;
            LocalDate dueDate = LocalDate.of(2027, 1, 15);
            Invoice[] invoices = new Invoice[arraySize];
            List<Invoice> expectedInvoices = new ArrayList<>();

            for (int i = 0; i < arraySize; i++) {
                invoices[i] = new Invoice(supplier, baseRef + i,
                        LocalDate.now(), dueDate, BigDecimal.valueOf(baseAmount + i));
                expectedInvoices.add(invoices[i]);
            }

            when(invoiceRepository.findAll())
                    .thenReturn(expectedInvoices);

            List<Invoice> actualInvoices = invoiceService.getAllInvoices();
            assertEquals(expectedInvoices.size(), actualInvoices.size());
            assertSame(expectedInvoices.getFirst(), actualInvoices.getFirst());
            assertSame(expectedInvoices.get(1), actualInvoices.get(1));
            assertSame(expectedInvoices.get(2), actualInvoices.get(2));
            assertSame(expectedInvoices.get(3), actualInvoices.get(3));
        }


        @Test
        @DisplayName("getAllInvoices returns empty when no invoices exist")
        void getAllInvoicesWhereNoneFound() {
            when(invoiceRepository.findAll())
                    .thenReturn(List.of());

            List<Invoice> invoices = invoiceService.getAllInvoices();
            assertTrue(invoices.isEmpty());
        }
    } // end of GetInvoiceTests

    @Nested
    class UpdateInvoiceTests {

        @Test
        @DisplayName("updateInvoice returns updated invoice")
        void updateInvoiceWhereInvoiceFound() {
            final Integer supplierId = 23;
            Supplier supplier = TestDataFactory.createSupplier("DEC Engineering");
            supplier.setId(supplierId);
            final Integer invoiceId = 15;
            final String invoiceRef = "ABC-01";
            LocalDate invoiceDate = LocalDate.now();
            LocalDate dueDate = LocalDate.of(2026, 12, 4);
            BigDecimal invoiceAmount = BigDecimal.valueOf(345.67);

            Invoice existingInvoice = new Invoice(supplier, invoiceRef,
                    invoiceDate, dueDate, invoiceAmount);

            UpdateInvoiceRequest request = new UpdateInvoiceRequest();
            request.setSupplierInvoiceRef("ABC-12");
            request.setDueDate(LocalDate.now());
            request.setInvoiceDate(LocalDate.now());
            request.setInvoiceAmount(BigDecimal.valueOf(1300));

            when(invoiceRepository.findById(invoiceId))
                    .thenReturn(Optional.of(existingInvoice));

            when(invoiceRepository.save(any(Invoice.class)))
                    .thenReturn(existingInvoice);

            Invoice updatedInvoice = invoiceService.updateInvoice(invoiceId, request);
            verify(invoiceRepository).save(existingInvoice);

            assertEquals(request.getSupplierInvoiceRef(), updatedInvoice.getSupplierInvoiceRef());
            assertEquals(request.getInvoiceDate(), updatedInvoice.getInvoiceDate());
            assertEquals(request.getDueDate(), updatedInvoice.getDueDate());
            assertEquals(request.getInvoiceAmount(), updatedInvoice.getInvoiceAmount());
        }


        @Test
        @DisplayName("updateInvoice throws exception when invoice not found")
        void updateInvoiceWhereInvoiceNotFound() {
            final Integer invoiceId = 100;

            UpdateInvoiceRequest request = new UpdateInvoiceRequest();
            request.setSupplierInvoiceRef("ABC-10");
            request.setDueDate(LocalDate.now());
            request.setInvoiceDate(LocalDate.now());
            request.setInvoiceAmount(BigDecimal.valueOf(1200));

            when(invoiceRepository.findById(invoiceId))
                    .thenReturn(Optional.empty());

            assertThrows(InvoiceNotFoundException.class,
                    () -> invoiceService.updateInvoice(invoiceId, request));

            verify(invoiceRepository, never()).save(any(Invoice.class));
        }
    } // end of UpdateInvoiceTests


    @Nested
    class CreateInvoiceTests {

        @Test
        @DisplayName("createInvoice returns invoice")
        void createInvoice() {
            final Integer supplierId = 43;
            final String companyName = "Dave Bledsoe Vehicle Hire";

            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setSupplierId(supplierId);
            request.setSupplierInvoiceRef("ABC-12");
            request.setInvoiceDate(LocalDate.now());
            request.setDueDate(LocalDate.of(2027, 12, 3));
            request.setInvoiceAmount(BigDecimal.valueOf(200));

            // The Invoice the mocked repository will return
            Supplier supplier = TestDataFactory.createSupplier(companyName);
            supplier.setId(supplierId);

            Invoice expectedInvoice = new Invoice(supplier,
                    request.getSupplierInvoiceRef(),
                    request.getInvoiceDate(), request.getDueDate(),
                    request.getInvoiceAmount());

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.of(supplier));

            when(invoiceRepository.save(any(Invoice.class)))
                    .thenReturn(expectedInvoice);

            Invoice actualInvoice = invoiceService.createInvoice(request);

            ArgumentCaptor<Invoice> captor =
                    ArgumentCaptor.forClass(Invoice.class);

            verify(invoiceRepository).save(captor.capture());

            Invoice capturedInvoice = captor.getValue();

            assertSame(expectedInvoice, actualInvoice);
            assertEquals(request.getSupplierId(), capturedInvoice.getSupplier().getId());
            assertEquals(request.getSupplierInvoiceRef(), capturedInvoice.getSupplierInvoiceRef());
            assertEquals(request.getInvoiceDate(), capturedInvoice.getInvoiceDate());
            assertEquals(request.getDueDate(), capturedInvoice.getDueDate());
            assertEquals(request.getInvoiceAmount(), capturedInvoice.getInvoiceAmount());
        }


        @Test
        @DisplayName("createInvoice throws an exception when supplier not found")
        void createInvoiceWhereSupplierNotFound() {
            final Integer supplierId = 21;

            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setSupplierId(supplierId);
            request.setSupplierInvoiceRef("ABC-10");
            request.setDueDate(LocalDate.now());
            request.setInvoiceDate(LocalDate.now());
            request.setInvoiceAmount(BigDecimal.valueOf(30));

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.empty());

            assertThrows(SupplierNotFoundException.class,
                    () -> invoiceService.createInvoice(request));

            verify(invoiceRepository, never()).save(any(Invoice.class));
        }
    } // end of CreateInvoiceTests


    @Nested
    class GetNonCancelledInvoicesTests {

        @Test
        @DisplayName("getNonCancelledInvoices returns invoices")
        void getNonCancelledInvoicesForSupplierWhereInvoicesFound() {

            final Integer supplierId = 28;
            Supplier supplier = TestDataFactory.createSupplier("ADT Alarms");
            final String invoiceRef = "ABC-";

            LocalDate invoiceDate = LocalDate.now();
            LocalDate dueDate = LocalDate.of(2026, 12, 4);
            BigDecimal invoiceAmount = BigDecimal.valueOf(500);
            final int arraySize = 5;


            List<Invoice> expectedInvoices = new ArrayList<>();
            for (int i = 0; i < arraySize; i++) {
                expectedInvoices.add(new Invoice(supplier,
                        invoiceRef + i,
                        invoiceDate, dueDate, invoiceAmount));
            }

            when(invoiceRepository.findBySupplierIdAndCancelledAtIsNull(supplierId))
                    .thenReturn(expectedInvoices);

            List<Invoice> actualInvoices = invoiceService.getNonCancelledInvoicesForSupplier(supplierId);

            assertEquals(arraySize, actualInvoices.size());

            for (int i = 0; i < arraySize; i++) {
                assertSame(expectedInvoices.get(i), actualInvoices.get(i));
            }
        }


        @Test
        @DisplayName("getNonCancelledInvoicesForSupplier returns empty list")
        void getNonCancelledInvoicesForSupplierWhereNoneFound() {
            final Integer supplierId = 46;

            when(invoiceRepository.findBySupplierIdAndCancelledAtIsNull(supplierId))
                    .thenReturn(List.of());

            List<Invoice> returnedInvoices = invoiceService.getNonCancelledInvoicesForSupplier(supplierId);

            assertTrue(returnedInvoices.isEmpty());
        }

    } // end of GetNonCancelledInvoicesTests


    @Nested
    class SupplierHasOutstandingInvoicesTests {

        @Test
        @DisplayName("supplierHasOutstandingInvoices finds no invoices")
        void supplierHasOutstandingInvoicesNoInvoices() {

            final Integer supplierId = 30;
            when(invoiceRepository.findBySupplierIdAndCancelledAtIsNull(supplierId))
                    .thenReturn(List.of());

            boolean hasOutstandingInvoices = invoiceService.supplierHasOutstandingInvoices(supplierId);

            assertFalse(hasOutstandingInvoices);
        }

        @Test
        @DisplayName("supplierHasOutstandingInvoices finds no outstanding invoices")
        void supplierHadOutstandingInvoicesAllInvoicesPaid() {
            final Integer invoiceId = 50;
            final Integer supplierId = 19;
            final String companyName = "Tormarton Tents";
            Invoice paidInvoice = TestDataFactory.createInvoice(companyName);

            // Set the ids
            paidInvoice.getSupplier().setId(supplierId);
            paidInvoice.setId(invoiceId);

            // Payment amount matches what's on the associated invoice
            Payment payment = TestDataFactory.createPayment(companyName, 65);

            when(invoiceRepository.findBySupplierIdAndCancelledAtIsNull(supplierId))
                    .thenReturn(List.of(paidInvoice));

            when(paymentService.getNonCancelledPaymentsForInvoice(invoiceId))
                    .thenReturn(List.of(payment));

            boolean hasOutstandingInvoices = invoiceService.supplierHasOutstandingInvoices(supplierId);

            verify(invoiceRepository).findBySupplierIdAndCancelledAtIsNull(supplierId);
            verify(paymentService).getNonCancelledPaymentsForInvoice(invoiceId);
            assertFalse(hasOutstandingInvoices);
        }

        @Test
        @DisplayName("supplierHasOutstandingInvoices finds outstanding invoice")
        void supplierHasOutstandingInvoices() {
            final Integer invoiceId = 50;
            final Integer supplierId = 19;
            final String companyName = "Tormarton Tents";
            Invoice paidInvoice = TestDataFactory.createInvoice(companyName);

            // Set the ids
            paidInvoice.getSupplier().setId(supplierId);
            paidInvoice.setId(invoiceId);

            // Payment amount matches what's on the associated invoice
            Payment payment = TestDataFactory.createPayment(companyName, 25);

            when(invoiceRepository.findBySupplierIdAndCancelledAtIsNull(supplierId))
                    .thenReturn(List.of(paidInvoice));

            when(paymentService.getNonCancelledPaymentsForInvoice(invoiceId))
                    .thenReturn(List.of(payment));

            boolean hasOutstandingInvoices = invoiceService.supplierHasOutstandingInvoices(supplierId);

            verify(invoiceRepository).findBySupplierIdAndCancelledAtIsNull(supplierId);
            verify(paymentService).getNonCancelledPaymentsForInvoice(invoiceId);
            assertTrue(hasOutstandingInvoices);
        }
    } // end of SupplierHasOutstandingInvoicesTests


    @Nested
    class CancelInvoiceTests {

        @Test
        @DisplayName("cancelInvoice throws exception")
        void cancelInvoiceWhereInvoiceNotFound() {
            final Integer invoiceId = 23;

            CancelInvoiceRequest request = new CancelInvoiceRequest();
            request.setCancellationReason("Goods returned");

            when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.empty());

            assertThrows(InvoiceNotFoundException.class,
                    () -> invoiceService.cancelInvoice(invoiceId, request));

            verify(invoiceRepository).findById(invoiceId);
            verify(invoiceRepository, never()).save(any(Invoice.class));
        }

        @Test
        @DisplayName("cancelInvoice fails as invoice already cancelled")
        void cancelInvoiceWhereInvoiceFoundButAlreadyCancelled() {
            final Integer invoiceId = 24;
            final String cancellationReason = "Goods faulty";

            CancelInvoiceRequest request = new CancelInvoiceRequest();
            request.setCancellationReason(cancellationReason);

            Invoice cancelledInvoice = TestDataFactory.createInvoice("Dave Bledsoe Vehicle Hire");
            cancelledInvoice.setCancellationReason(cancellationReason);
            cancelledInvoice.setCancelledAt(LocalDateTime.now());

            when(invoiceRepository.findById(invoiceId))
                    .thenReturn(Optional.of(cancelledInvoice));

            boolean invoiceCancelled = invoiceService.cancelInvoice(invoiceId, request);

            assertFalse(invoiceCancelled);
            verify(invoiceRepository).findById(invoiceId);
            verify(invoiceRepository, never()).save(any(Invoice.class));
        }

        @Test
        @DisplayName("cancelInvoice cancels the invoice")
        void cancelInvoiceWhereInvoiceFound() {

            final Integer invoiceId = 99;
            final String cancellationReason = "Services not provided";

            CancelInvoiceRequest request = new CancelInvoiceRequest();
            request.setCancellationReason(cancellationReason);

            Invoice invoice = TestDataFactory.createInvoice("Dave Bledsoe Vehicle Hire");


            when(invoiceRepository.findById(invoiceId))
                    .thenReturn(Optional.of(invoice));

            boolean invoiceCancelled = invoiceService.cancelInvoice(invoiceId, request);

            assertTrue(invoiceCancelled);
            assertNotNull(invoice.getCancelledAt());
            assertEquals(cancellationReason, invoice.getCancellationReason());

            verify(invoiceRepository).findById(invoiceId);
            verify(invoiceRepository).save(invoice);
        }
    } // end of CancelInvoiceTests
}
