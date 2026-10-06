package com.abcengineering.invoiceapp.repository;

import com.abcengineering.invoiceapp.model.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.context.TestConstructor.AutowireMode.ALL;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = ALL)
public class SupplierRepositoryTest {

    @Autowired
    private SupplierRepository supplierRepository;

    SupplierRepositoryTest(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }


    @Nested
    class FindByIdTests {

        @Test
        @DisplayName("findById returns supplier with compulsory fields")
        void findByIdReturnsSupplierWithExpectedFields() {
            final String companyName = "Bob's Widgets";
            final String companyAddress = "32 Newlands Road, Templeton Peck, Leicester, L53 4AT";

            Supplier supplier = new Supplier(companyName, companyAddress);
            Supplier savedSupplier = supplierRepository.save(supplier);
            Optional<Supplier> result = supplierRepository.findById(savedSupplier.getId());

            assertTrue(result.isPresent());
            Supplier retrievedSupplier = result.get();

            assertEquals(companyName, retrievedSupplier.getCompanyName());
            assertEquals(companyAddress, retrievedSupplier.getCompanyAddress());
            assertNotNull(retrievedSupplier.getId());
            assertTrue(retrievedSupplier.isActive());
            assertNull(retrievedSupplier.getContactEmail());
            assertNull(retrievedSupplier.getContactName());
            assertNull(retrievedSupplier.getContactTelephone());
        }


        @Test
        @DisplayName("findById returns supplier with all fields")
        void findByIdReturnsSupplierWithOptionalFields() {
            final String companyName = "Bob's Widgets";
            final String companyAddress = "32 Newlands Road, Templeton Peck, Leicester, L53 4AT";
            final String contactName = "Jeff Smith";
            final String contactEmail = "jeffsmith@bobswidgets.com";
            final String contactPhone = "01567345456";

            Supplier supplier = new Supplier(companyName, companyAddress,
                    contactName, contactEmail, contactPhone);
            Supplier savedSupplier = supplierRepository.save(supplier);
            Optional<Supplier> result = supplierRepository.findById(savedSupplier.getId());

            assertTrue(result.isPresent());
            Supplier retrievedSupplier = result.get();

            assertEquals(companyName, retrievedSupplier.getCompanyName());
            assertEquals(companyAddress, retrievedSupplier.getCompanyAddress());
            assertNotNull(retrievedSupplier.getId());
            assertTrue(retrievedSupplier.isActive());
            assertEquals(contactName, retrievedSupplier.getContactName());
            assertEquals(contactEmail, retrievedSupplier.getContactEmail());
            assertEquals(contactPhone, retrievedSupplier.getContactTelephone());
        }


        @Test
        @DisplayName("findById returns empty for non-existent id")
        void findSupplierByIdNotFound() {
            Optional<Supplier> result = supplierRepository.findById(999999);
            assertTrue(result.isEmpty());
        }
    } // end of FindByIdTests


    @Nested
    class FindAllTests {

        @Test
        @DisplayName("findAll returns empty list when supplier table empty")
        void findAllReturnsEmptyListWhenNoSuppliersExist() {
            List<Supplier> result = supplierRepository.findAll();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("findAll returns expected suppliers")
        void findAllReturnsAllSuppliers() {
            String[] companyNames = {"Supplier One", "Supplier Two", "Supplier Three"};
            String[] addresses = {"Address One", "Address Two", "Address Three"};
            String[] names = {"Bill", null, "Barrington"};
            String[] emails = {null, null, "barringtonaqualung@gmail.com"};
            String[] phoneNums = {null, "01245897456", "0231131232131"};

            List<Supplier> createdSuppliers = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                createdSuppliers.add(createSupplier(companyNames[i], addresses[i], names[i],
                        emails[i], phoneNums[i]));
            }

            List<Supplier> persistedSuppliers = supplierRepository.findAll();
            assertEquals(3, persistedSuppliers.size());

            for (int i = 0; i < 3; i++) {
                Supplier supplierExpected = createdSuppliers.get(i);
                Supplier supplierActual = persistedSuppliers.get(i);

                assertEquals(supplierExpected.getCompanyName(), supplierActual.getCompanyName());
                assertEquals(supplierExpected.getCompanyAddress(), supplierActual.getCompanyAddress());
                assertEquals(supplierExpected.getContactName(), supplierActual.getContactName());
                assertEquals(supplierExpected.getContactEmail(), supplierActual.getContactEmail());
                assertEquals(supplierExpected.getContactTelephone(), supplierActual.getContactTelephone());
            }
        }
    } // end of FindAllTests


    // Helper method to create Supplier entities
    private Supplier createSupplier(String companyName, String companyAddress,
                                    String contactName, String contactEmail,
                                    String contactPhone) {

        Supplier supplier = new Supplier(companyName, companyAddress,
                contactName, contactEmail, contactPhone);

        return supplierRepository.save(supplier);
    }
}