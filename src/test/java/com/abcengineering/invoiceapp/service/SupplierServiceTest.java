package com.abcengineering.invoiceapp.service;

import com.abcengineering.invoiceapp.dto.CreateSupplierRequest;
import com.abcengineering.invoiceapp.dto.UpdateSupplierRequest;
import com.abcengineering.invoiceapp.exception.SupplierNotFoundException;
import com.abcengineering.invoiceapp.model.Supplier;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Initialise the mocks
@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private SupplierService supplierService;

    private final String companyName = "Bob's Widgets";
    private final String companyAddress = "32 Newlands Road, Leicester, LE1 3RT";
    private final String contactName = "Bob Fleming";
    private final String contactEmail = "bob.fleming@gmail.com";
    private final String contactPhone = "01456234561";


    @Nested
    class SupplierRetrievalTests {


        @Test
        @DisplayName("getSupplier returns Supplier")
        void getSupplierWhereSupplierFound() {

            Supplier supplier = new Supplier(companyName, companyAddress);

            when(supplierRepository.findById(56))
                    .thenReturn(Optional.of(supplier));

            Supplier result = supplierService.getSupplier(56);

            assertSame(supplier, result);
            assertEquals(companyName, result.getCompanyName());
            assertEquals(companyAddress, result.getCompanyAddress());
        }


        @Test
        @DisplayName("getSupplier returns empty")
        void getSupplierWhereSupplierNotFound() {
            when(supplierRepository.findById(76))
                    .thenReturn(Optional.empty());

            assertThrows(SupplierNotFoundException.class,
                    () -> supplierService.getSupplier(76));
        }


        @Test
        @DisplayName("getAllSuppliers returns empty list when no suppliers exist")
        void getAllSuppliersWhereNoneFound() {

            // List.of() - creates an empty list
            when(supplierRepository.findAll())
                    .thenReturn(List.of());

            List<Supplier> suppliers = supplierService.getAllSuppliers();
            assertTrue(suppliers.isEmpty());
        }


        @Test
        @DisplayName("getAllSuppliers returns list when suppliers exist")
        void getAllSuppliersWhereSuppliersFound() {
            final String companyName1 = "Fred's Plumbing Supplies";
            final String companyName2 = "BR Weavis Ltd.";
            final String companyName3 = "West Bromwich Components";

            List<Supplier> expectedSuppliers = new ArrayList<>();
            expectedSuppliers.add(TestDataFactory.createSupplier(companyName1));
            expectedSuppliers.add(TestDataFactory.createSupplier(companyName2));
            expectedSuppliers.add(TestDataFactory.createSupplierWithContactDetails(companyName3));

            when(supplierRepository.findAll()).thenReturn(expectedSuppliers);

            List<Supplier> actualSuppliers = supplierService.getAllSuppliers();
            assertEquals(expectedSuppliers.size(), actualSuppliers.size());
            assertSame(expectedSuppliers.getFirst(), actualSuppliers.getFirst());
            assertSame(expectedSuppliers.get(1), actualSuppliers.get(1));
            assertSame(expectedSuppliers.get(2), actualSuppliers.get(2));
        }
    } // end of SupplierRetrievalTests



    @Nested
    class CreateSupplierTests {


        @Test
        @DisplayName("createSupplier returns mandatory fields")
        void createSupplierMandatoryFieldsOnly() {
            // Set up the input object to createSupplier
            CreateSupplierRequest request = new CreateSupplierRequest();
            request.setCompanyName(companyName);
            request.setCompanyAddress(companyAddress);

            // The Supplier that the mocked repository will return
            Supplier savedSupplier = new Supplier(companyName, companyAddress);
            savedSupplier.setCompanyName(companyName);
            savedSupplier.setCompanyAddress(companyAddress);
            savedSupplier.setActive(true);

            // any() means whatever Supplier object save() is
            // called with, return savedSupplier
            when(supplierRepository.save(any(Supplier.class)))
                    .thenReturn(savedSupplier);

            // This is what we're really testing
            Supplier result = supplierService.createSupplier(request);

            // Inspect what the service passed to the repository.
            // This creates the container to capture only - nothing
            // has yet been captured.
            // But it is created to capture a Supplier argument
            // when Mockito sees one being passed to a mock.
            // This can be done AFTER the call to save() since
            // Mockito records what happens to the mock object
            // during its life. Therefore, it can retrieve the
            // argument it passed to save().
            // Mockito records every interaction independently,
            // so multiple calls to createSupplier would generate
            // the same savedSupplier reference if there were
            // a new result2, result3, etc reference assigned to
            // the result of those createSupplier calls.
            ArgumentCaptor<Supplier> captor =
                    ArgumentCaptor.forClass(Supplier.class);

            // This checks what happened in the mock repo (supplier).
            // It also checks that save was called.
            // It also captures the actual argument passed into it.
            // Note: the save has already taken place (in createSupplier).
            verify(supplierRepository).save(captor.capture());

            // This extracts the Supplier the captor captured.
            // This yields a reference ot the Supplier that the
            // real SupplierService constructed internally.
            Supplier capturedSupplier = captor.getValue();

            assertSame(savedSupplier, result);
            assertEquals(companyName, capturedSupplier.getCompanyName());
            assertEquals(companyAddress, capturedSupplier.getCompanyAddress());
            assertTrue(capturedSupplier.isActive());
        }


        @Test
        @DisplayName("createSupplier returns mandatory and optional fields")
        void createSupplierAllFields() {
            CreateSupplierRequest request = new CreateSupplierRequest();
            request.setCompanyName(companyName);
            request.setCompanyAddress(companyAddress);
            request.setContactEmail(contactEmail);
            request.setContactTelephone(contactPhone);
            request.setContactName(contactName);

            Supplier savedSupplier = new Supplier(companyName,
                    companyAddress, contactName, contactEmail,
                    contactPhone);
            savedSupplier.setActive(true);

            when(supplierRepository.save(any(Supplier.class)))
                    .thenReturn(savedSupplier);

            Supplier result = supplierService.createSupplier(request);

            ArgumentCaptor<Supplier> captor =
                    ArgumentCaptor.forClass(Supplier.class);
            verify(supplierRepository).save(captor.capture());
            Supplier capturedSupplier = captor.getValue();

            assertSame(savedSupplier, result);
            assertEquals(companyName, capturedSupplier.getCompanyName());
            assertEquals(companyAddress, capturedSupplier.getCompanyAddress());
            assertEquals(contactName, capturedSupplier.getContactName());
            assertEquals(contactEmail, capturedSupplier.getContactEmail());
            assertEquals(contactPhone, capturedSupplier.getContactTelephone());
            assertTrue(capturedSupplier.isActive());
        }
    } // end of CreateSupplierTests


    @Nested
    class UpdateSupplierTests {


        @Test
        @DisplayName("updateSupplier returns updated supplier")
        void updateSupplierWhereSupplierFound() {

            Supplier existingSupplier =
                    TestDataFactory.createSupplierWithContactDetails(companyName);

            UpdateSupplierRequest request = new UpdateSupplierRequest();

            request.setCompanyName("Llewellyn Crowthorpe's Electrical Supplies");
            request.setCompanyAddress("12 Barnaby Street, Ellington, Lancashire, L34 3WT");
            request.setContactName("Harry Hardwicke");
            request.setContactEmail("hhardwicke@gmail.com");
            request.setContactTelephone("01334567890");

            final Integer supplierId = 45;

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.of(existingSupplier));

            when(supplierRepository.save(any(Supplier.class)))
                    .thenReturn(existingSupplier);

            Supplier updatedSupplier = supplierService.updateSupplier(supplierId, request);

            // Verify that updateSupplier did call the save
            // for existingSupplier
            verify(supplierRepository).save(existingSupplier);

            assertEquals(request.getCompanyName(), updatedSupplier.getCompanyName());
            assertEquals(request.getCompanyAddress(), updatedSupplier.getCompanyAddress());
            assertEquals(request.getContactName(), updatedSupplier.getContactName());
            assertEquals(request.getContactEmail(), updatedSupplier.getContactEmail());
            assertEquals(request.getContactTelephone(), updatedSupplier.getContactTelephone());
        }


        @Test
        @DisplayName("updateSupplier throws exception when invoice not found")
        void updateSupplierWhereSupplierNotFound() {

            final Integer supplierId = 12;

            UpdateSupplierRequest request = new UpdateSupplierRequest();
            request.setCompanyName(companyName);
            request.setCompanyAddress(companyAddress);
            request.setContactName(contactName);
            request.setContactEmail(contactEmail);
            request.setContactTelephone(contactPhone);

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.empty());

            assertThrows(SupplierNotFoundException.class,
                    () -> supplierService.updateSupplier(supplierId, request));

            verify(supplierRepository, never())
                    .save(any(Supplier.class));
        }
    } // end of UpdateSupplierTests


    @Nested
    class SupplierActivationTests {


        @Test
        @DisplayName("reactivateSupplier throws exception when supplier not found")
        void reactivateSupplierWhereSupplierNotFound() {
            final Integer supplierId = 15;

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.empty());

            assertThrows(SupplierNotFoundException.class,
                    () -> supplierService.reactivateSupplier(supplierId));

            verify(supplierRepository, never()).save(any(Supplier.class));
        }


        @Test
        @DisplayName("reactivateSupplier sets supplier to active and saves it")
        void reactivateSupplierWhereSupplierFound() {
            final Integer supplierId = 20;

            Supplier supplier = TestDataFactory.createSupplier("Brian's Widgets");
            supplier.setActive(false);
            supplier.setId(supplierId);

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.of(supplier));

            supplierService.reactivateSupplier(supplierId);
            assertTrue(supplier.isActive());
            verify(supplierRepository).save(supplier);
        }


        @Test
        @DisplayName("deactivateSupplier throws exception when supplier not found")
        void deactivateSupplierWhereSupplierNotFound() {
            final Integer supplierId = 10;

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.empty());

            assertThrows(SupplierNotFoundException.class,
                    () -> supplierService.deactivateSupplier(supplierId));

            verify(invoiceService, never()).supplierHasOutstandingInvoices(supplierId);
            verify(supplierRepository, never()).save(any(Supplier.class));
        }


        @Test
        @DisplayName("deactivateSupplier keeps supplier active when supplier has outstanding invoices")
        void deactivateSupplierWhereSupplierHasOutstandingInvoices() {
            final Integer supplierId = 16;

            Supplier supplier = TestDataFactory.createSupplier(companyName);
            supplier.setActive(true);
            supplier.setId(supplierId);

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.of(supplier));

            when(invoiceService.supplierHasOutstandingInvoices(supplierId))
                    .thenReturn(true);

            // Call the service method
            boolean supplierDeactivated = supplierService.deactivateSupplier(supplierId);

            // Now verify return value
            assertFalse(supplierDeactivated);

            // Verify the deactivate method checked via the invoice
            // service for outstanding invoices for this supplier
            // as required.
            verify(invoiceService).supplierHasOutstandingInvoices(supplierId);

            // Verify that what should not happen has not happened.
            // Supplier should remain active and no save should happen.
            assertTrue(supplier.isActive());
            verify(supplierRepository, never()).save(any(Supplier.class));
        }


        @Test
        @DisplayName("deactivateSupplier successfully deactivates supplier")
        void deactivateSupplierWhereSupplierHasNoOutstandingInvoices() {
            final Integer supplierId = 22;

            Supplier supplier = TestDataFactory.createSupplier(companyName);
            supplier.setActive(true);
            supplier.setId(supplierId);

            when(supplierRepository.findById(supplierId))
                    .thenReturn(Optional.of(supplier));

            when(invoiceService.supplierHasOutstandingInvoices(supplierId))
                    .thenReturn(false);

            boolean supplierDeactivated = supplierService.deactivateSupplier(supplierId);
            assertTrue(supplierDeactivated);

            verify(invoiceService).supplierHasOutstandingInvoices(supplierId);

            assertFalse(supplier.isActive());
            verify(supplierRepository).save(supplier);
        }
    } // end of SupplierActivationTests
}
