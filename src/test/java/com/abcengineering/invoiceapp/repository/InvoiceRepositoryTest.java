package com.abcengineering.invoiceapp.repository;


import com.abcengineering.invoiceapp.model.Invoice;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = ALL)
public class InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    InvoiceRepositoryTest(InvoiceRepository invoiceRepository,
                          SupplierRepository supplierRepository) {
        this.invoiceRepository = invoiceRepository;
        this.supplierRepository = supplierRepository;
    }

    @Nested
    class FindInvoicesByInvoiceIdTests {

        @Test
        @DisplayName("findById returns empty for non-existent id")
        void findByIdInvoiceNotFound() {
            Optional<Invoice> invoice = invoiceRepository.findById(232131);
            assertTrue(invoice.isEmpty());
        }

        @Test
        @DisplayName("findById returns invoice with mandatory fields only populated")
        void findByIdReturnsActiveInvoice() {
            // Need a supplier to create the invoice
            final String companyName = "Bert's Plumbing Supplies";
            final String companyAddress = "123 Any Street, Louth";
            Supplier supplier = new Supplier(companyName, companyAddress);
            Supplier savedSupplier = supplierRepository.save(supplier);

            final String invoiceRef = "BPS-1";
            final LocalDate invoiceDate = LocalDate.now();
            final LocalDate dueDate = LocalDate.of(2026, 12, 13);
            final BigDecimal invoiceAmount = BigDecimal.valueOf(120.56);

            Invoice invoice = new Invoice(savedSupplier, invoiceRef,
                    invoiceDate, dueDate, invoiceAmount);
            Invoice savedInvoice = invoiceRepository.save(invoice);
            Optional<Invoice> result = invoiceRepository.findById(savedInvoice.getId());

            assertTrue(result.isPresent());
            Invoice retrievedInvoice = result.get();

            assertNotNull(retrievedInvoice.getId());
            assertNull(retrievedInvoice.getCancellationReason());
            assertNull(retrievedInvoice.getCancelledAt());
            assertEquals(invoiceDate, retrievedInvoice.getInvoiceDate());
            assertEquals(dueDate, retrievedInvoice.getDueDate());
            assertEquals(invoiceAmount, retrievedInvoice.getInvoiceAmount());
            assertEquals(invoiceRef, retrievedInvoice.getSupplierInvoiceRef());
            assertEquals(savedSupplier.getId(), retrievedInvoice.getSupplier().getId());
        }

        @Test
        @DisplayName("findById returns invoice will all fields populated")
        void findByIdReturnsCancelledInvoice() {
            final String companyName = "Bert's Plumbing Supplies";
            final String companyAddress = "123 Any Street, Louth";
            Supplier supplier = new Supplier(companyName, companyAddress);
            Supplier savedSupplier = supplierRepository.save(supplier);

            final String invoiceRef = "BPS-2";
            final LocalDate invoiceDate = LocalDate.of(2026, 7, 23);
            final LocalDate dueDate = LocalDate.of(2026, 12, 13);
            final BigDecimal invoiceAmount = BigDecimal.valueOf(120.56);
            final String cancellationReason = "Ordered items returned to supplier";
            final LocalDateTime cancelledAt = LocalDateTime.now();

            Invoice invoice = new Invoice(savedSupplier, invoiceRef,
                    invoiceDate, dueDate, invoiceAmount);
            invoice.setCancellationReason(cancellationReason);
            invoice.setCancelledAt(cancelledAt);
            Invoice savedInvoice = invoiceRepository.save(invoice);

            Optional<Invoice> result = invoiceRepository.findById(savedInvoice.getId());

            assertTrue(result.isPresent());
            Invoice retrievedInvoice = result.get();

            assertNotNull(retrievedInvoice.getId());
            assertEquals(cancellationReason, retrievedInvoice.getCancellationReason());
            assertEquals(cancelledAt, retrievedInvoice.getCancelledAt());
            assertEquals(invoiceDate, retrievedInvoice.getInvoiceDate());
            assertEquals(dueDate, retrievedInvoice.getDueDate());
            assertEquals(invoiceAmount, retrievedInvoice.getInvoiceAmount());
            assertEquals(invoiceRef, retrievedInvoice.getSupplierInvoiceRef());
            assertEquals(savedSupplier.getId(), retrievedInvoice.getSupplier().getId());
        }
    } // end of FindByInvoiceIdTests

    @Nested
    class FindInvoicesBySupplierIdTests {

        final String companyName = "ANF Catering";
        final String invoiceRef = "ABC-01";
        final LocalDate invoiceDate = LocalDate.now();
        final LocalDate dueDate = LocalDate.of(2026, 11, 12);
        final BigDecimal invoiceAmount = BigDecimal.valueOf(124.67);

        @Test
        @DisplayName("findBySupplier returns only invoice for supplier, which is active")
        void findBySupplierIdAndCancelledAtIsNullActiveInvoiceOnly() {

            Supplier supplier = TestDataFactory.createSupplier(companyName);
            Supplier savedSupplier = supplierRepository.save(supplier);

            Invoice expectedInvoice = createInvoice(supplier,
                    invoiceRef, invoiceDate, dueDate, invoiceAmount);

            // Test the method
            List<Invoice> activeInvoices = invoiceRepository.findBySupplierIdAndCancelledAtIsNull(expectedInvoice.getSupplier().getId());

            assertEquals(1, activeInvoices.size());

            Invoice actualInvoice = activeInvoices.getFirst();

            assertEquals(expectedInvoice.getSupplier().getId(), actualInvoice.getSupplier().getId());
            assertEquals(expectedInvoice.getSupplierInvoiceRef(), actualInvoice.getSupplierInvoiceRef());
            assertNull(actualInvoice.getCancellationReason());
            assertNull(actualInvoice.getCancelledAt());
            assertEquals(expectedInvoice.getInvoiceDate(), actualInvoice.getInvoiceDate());
            assertEquals(expectedInvoice.getDueDate(), actualInvoice.getDueDate());
            assertEquals(expectedInvoice.getInvoiceAmount(), actualInvoice.getInvoiceAmount());
        }

        @Test
        @DisplayName("findBySupplier excludes cancelled invoices")
        void findBySupplierIdAndCancelledAtIsNullExcludeCancelledInvoices() {

            Supplier supplier = TestDataFactory.createSupplier(companyName);
            Supplier savedSupplier = supplierRepository.save(supplier);

            Invoice activeInvoice1 = createInvoice(savedSupplier,
                    invoiceRef, invoiceDate, dueDate, invoiceAmount);
            Invoice activeInvoice2 = createInvoice(savedSupplier,
                    "ABC-02", invoiceDate, dueDate, invoiceAmount);

            // Need to set the cancellation fields on this one
            // before saving it to the DB
            Invoice cancelledInvoice = new Invoice(supplier,
                    "ABC-CANC" , invoiceDate, dueDate, invoiceAmount);

            final String cancellationReason = "Invoice incorrectly issued";
            final LocalDateTime cancelledAt = LocalDateTime.now();
            cancelledInvoice.setCancellationReason(cancellationReason);
            cancelledInvoice.setCancelledAt(cancelledAt);
            Invoice inactiveInvoice = invoiceRepository.save(cancelledInvoice);

            // Data should be set now - see what's returned
            List<Invoice> returnedInvoicesForSupplier = invoiceRepository
                    .findBySupplierIdAndCancelledAtIsNull(savedSupplier.getId());

            // Should return the two non-cancelled invoices
            // and not return the cancelled invoice for this supplier
            assertEquals(2, returnedInvoicesForSupplier.size());

            // Take the supplier invoice references from the returned invoices
            // list and assert that they contain exactly these two supplier
            // invoice references, in any order.
            // This approach is necessary because the query to get the
            // invoices for the supplier has no ordering, therefore it
            // is not possible to know here whether the first invoice
            // in the list will be ABC-01 or ABC-02, but we do need
            // to know that both are in the list, irrespective of the order.
            //
            // extracting() goes through the list (returnedInvoicesForSupplier).
            // It applyies the method reference here, to get the supplier's
            // invoice ref.
            // containsExactlyInAnyOrder() ensures that there's only
            // the two refs and that the specified values must be present
            assertThat(returnedInvoicesForSupplier)
                    .extracting(Invoice::getSupplierInvoiceRef)
                    .containsExactlyInAnyOrder(invoiceRef, "ABC-02");

            // The other fields are identical in each invoice
            // so can be evaluated in any order.
            for (Invoice actual : returnedInvoicesForSupplier) {
                assertEquals(savedSupplier.getId(), actual.getSupplier().getId());
                assertEquals(invoiceDate, actual.getInvoiceDate());
                assertEquals(dueDate, actual.getDueDate());
                assertEquals(invoiceAmount, actual.getInvoiceAmount());
                assertNull(actual.getCancelledAt());
                assertNull(actual.getCancellationReason());
            }
        }
    } // end of FindInvoicesBySupplierIdTests

    @Nested
    class FindAllInvoicesTests {

        @Test
        @DisplayName("findAll returns empty list when invoice table empty")
        void findAllReturnsEmptyListWhenNoInvoicesExist() {
            List<Invoice> invoiceList = invoiceRepository.findAll();
            assertTrue(invoiceList.isEmpty());
            assertEquals(0, invoiceList.size());
        }

        @Test
        @DisplayName("findAll returns all invoices")
        void findAllReturnsAllInvoices() {
            // Set up five suppliers
            final String baseName = "Engineering Supplies";
            final String baseRef = "INV-";
            final int arraySize = 5;
            String[] invoiceRefs = new String[arraySize];
            LocalDate[] dueDates = new LocalDate[arraySize];
            BigDecimal[] invoiceAmounts = new BigDecimal[arraySize];

            String[] companyNames = new String[arraySize];

            for (int i = 0; i < arraySize; i++) {
                companyNames[i] = baseName + i;
                invoiceRefs[i] = baseRef + i;
                dueDates[i] = LocalDate.of(2026, 12, i + 1);
                invoiceAmounts[i] = BigDecimal.valueOf(100 + i);
            }

            Supplier[] suppliers = new Supplier[arraySize];
            Supplier[] savedSuppliers = new Supplier[arraySize];

            for (int i = 0; i < arraySize; i++) {
                suppliers[i] = TestDataFactory.createSupplier(companyNames[i]);
                savedSuppliers[i] = supplierRepository.save(suppliers[i]);
            }

            // Set up five invoices
            Invoice[] savedInvoices = new Invoice[5];
            for (int i = 0; i < arraySize; i++) {
                savedInvoices[i] = createInvoice(savedSuppliers[i],
                        invoiceRefs[i], LocalDate.now(),
                        dueDates[i], invoiceAmounts[i]);
            }

            List<Invoice> persistedInvoices = invoiceRepository.findAll();
            assertEquals(arraySize, persistedInvoices.size());

            for (int i = 0; i < arraySize; i++) {
                Invoice invoiceExpected = savedInvoices[i];
                Invoice invoiceActual = persistedInvoices.get(i);

                assertEquals(invoiceExpected.getId(), invoiceActual.getId());
                assertEquals(invoiceExpected.getCancellationReason(), invoiceActual.getCancellationReason());
                assertEquals(invoiceExpected.getCancelledAt(), invoiceActual.getCancelledAt());
                assertEquals(invoiceExpected.getDueDate(), invoiceActual.getDueDate());
                assertEquals(invoiceExpected.getInvoiceAmount(), invoiceActual.getInvoiceAmount());
                assertEquals(invoiceExpected.getInvoiceDate(), invoiceActual.getInvoiceDate());
                assertEquals(invoiceExpected.getSupplierInvoiceRef(), invoiceActual.getSupplierInvoiceRef());
                assertEquals(invoiceExpected.getSupplier().getId(), invoiceActual.getSupplier().getId());
            }
        }
    } // end of FindAllInvoicesTests


    // Helper method to create invoice entities.
    private Invoice createInvoice(Supplier supplier, String supplierInvoiceRef,
                LocalDate invoiceDate, LocalDate dueDate,
                BigDecimal invoiceAmount) {

        Invoice invoice = new Invoice(supplier, supplierInvoiceRef,
                invoiceDate, dueDate, invoiceAmount);

        return invoiceRepository.save(invoice);
    }
}
