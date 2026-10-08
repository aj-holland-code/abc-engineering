package com.abcengineering.invoiceapp.controller;

import com.abcengineering.invoiceapp.dto.CreateSupplierRequest;
import com.abcengineering.invoiceapp.dto.UpdateSupplierRequest;
import com.abcengineering.invoiceapp.exception.SupplierNotFoundException;
import com.abcengineering.invoiceapp.model.Supplier;
import com.abcengineering.invoiceapp.service.SupplierService;
import com.abcengineering.invoiceapp.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierController.class)
public class SupplierControllerTest {

    @MockitoBean
    private SupplierService supplierService;

    @Autowired
    private MockMvc mockMvc;

    private final String textContentType = "text/plain;charset=UTF-8";
    private final String uri = "/api/suppliers/{id}";
    private final String getURI = "/api/suppliers";

    private final String companyName = "Jeff's Wiring Services";
    private final String companyAddress = "12 Bishop's Lane, Grimsby, GR1 2FE";
    private final String contactName = "Larry Kolarich";
    private final String contactEmail = "lrkolarich@gmail.com";
    private final String contactTelephone = "01987234567";


    @Nested
    class SupplierRetrievalTests {

        @Test
        @DisplayName("getSupplier returns 404 when supplier not found")
        void getSupplierWhereSupplierNotFound() throws Exception {
            final Integer supplierId = 30;
            final String expectedExceptionMessage = "Supplier not found with ID: " + supplierId;

            when(supplierService.getSupplier(supplierId))
                    .thenThrow(new SupplierNotFoundException(supplierId));

            // Obtain and inspect the response
            // Debug code to be removed once understanding is clear
            MvcResult result = mockMvc.perform(get(uri, supplierId))
                    .andExpect(status().isNotFound())
                    .andReturn();
            MockHttpServletResponse response = result.getResponse();
            System.out.println("\n\nResponse status: " + response.getStatus());
            System.out.println("Response type: " + response.getContentType());
            System.out.println("Response string: " + response.getContentAsString());

            mockMvc.perform(get(uri, supplierId))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(textContentType))
                    .andExpect(content().string(expectedExceptionMessage));
        }


        @Test
        @DisplayName("getSupplier returns Supplier")
        void getSupplierWhereSupplierFound() throws Exception {
            final Integer supplierId = 7;

            Supplier supplier = TestDataFactory.createSupplier(companyName);
            supplier.setId(supplierId);
            supplier.setActive(true);
            supplier.setCompanyAddress(companyAddress);
            supplier.setContactName(contactName);
            supplier.setContactEmail(contactEmail);
            supplier.setContactTelephone(contactTelephone);

            when(supplierService.getSupplier(supplierId))
                    .thenReturn(supplier);

            // Debug code for learning - remove later
            MvcResult result = mockMvc.perform(get(uri, supplierId)
            ).andReturn();
            MockHttpServletResponse response = result.getResponse();
            System.out.println("\n\nResponse status: " + response.getStatus());
            System.out.println("Response type: " + response.getContentType());
            System.out.println("Response string: " + response.getContentAsString());

            // Required validation
            mockMvc.perform(get(uri, supplierId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(supplierId))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.companyName").value(companyName))
                    .andExpect(jsonPath("$.companyAddress").value(companyAddress))
                    .andExpect(jsonPath("$.contactName").value(contactName))
                    .andExpect(jsonPath("$.contactEmail").value(contactEmail))
                    .andExpect(jsonPath("$.contactTelephone").value(contactTelephone));
        }


        @Test
        @DisplayName("getAllSuppliers returns empty list")
        void testGetAllSuppliersNoSuppliersFound() throws Exception {

            when(supplierService.getAllSuppliers())
                    .thenReturn(List.of());

            // $ - root of JSON
            // jsonPath("$" - return entire JSON response
            mockMvc.perform(get(getURI))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }


        @Test
        @DisplayName("getAllSuppliers returns all suppliers")
        void getAllSuppliersWhereSuppliersFound() throws Exception {

            final String companyName2 = "Castle Lane Supplies";
            final String companyName3 = "Burke & Co.";

            // Set up data to return
            Supplier supplier1 = TestDataFactory.createSupplier(companyName);
            supplier1.setId(11);

            Supplier supplier2 = TestDataFactory.createSupplier(companyName2);
            supplier2.setId(12);

            Supplier supplier3 = TestDataFactory.createSupplier(companyName3);
            supplier3.setId(13);

            List<Supplier> suppliers = List.of(supplier1, supplier2, supplier3);

            // Configure bean to return the data
            when(supplierService.getAllSuppliers())
                    .thenReturn(suppliers);

            // Check:
            // HTTP response is 200
            // Content of JSON array is JSON.
            // Array contains three elements.
            // The three array elements contain the correct company ids and names.
            mockMvc.perform(get(getURI))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0]id").value(11))
                    .andExpect(jsonPath("$[0]companyName").value(companyName))
                    .andExpect(jsonPath("$[1]id").value(12))
                    .andExpect(jsonPath("$[1]companyName").value(companyName2))
                    .andExpect(jsonPath("$[2]id").value(13))
                    .andExpect(jsonPath("$[2]companyName").value(companyName3));
        }
    } // end of SupplierRetrievalTests


    @Nested
    class SupplierCreationTests {

        @Test
        @DisplayName("createSupplier successfully saves supplier")
        void testCreateSupplierWhereSupplierCreated() throws Exception {

            final Integer supplierId = 54;
            final String postURI = "/api/suppliers";

            // Output from createSupplier
            Supplier supplier = TestDataFactory.createSupplier(companyName);
            supplier.setId(supplierId);
            supplier.setCompanyAddress(companyAddress);
            supplier.setContactName(contactName);
            supplier.setContactEmail(contactEmail);
            supplier.setContactTelephone(contactTelephone);


            // createSupplier should return the above output.
            // The CreateSupplierRequest object is generated by Spring
            // when it deserialises the JSON.
            // Hence, use any() here, so that Mockito won't discriminate on
            // which instance of the object of that type it gets on this call.
            // It returns supplier in any case.
            when(supplierService.createSupplier(any(CreateSupplierRequest.class)))
                    .thenReturn(supplier);

            // Sends the simulated HTTP request to the HTTP endpoint.
            // Spring processes the JSON, turning it into the CreateSupplierRequest object.
            // That object is then the input to SupplierController.createSupplier().
            mockMvc.perform(post(postURI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "companyName": "%s",
                                        "companyAddress": "%s",
                                        "contactName": "%s",
                                        "contactEmail": "%s",
                                        "contactTelephone": "%s"
                                    }
                                    """.formatted(
                                    companyName,
                                    companyAddress,
                                    contactName,
                                    contactEmail,
                                    contactTelephone)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(supplierId))
                    .andExpect(jsonPath("$.companyName").value(companyName))
                    .andExpect(jsonPath("$.companyAddress").value(companyAddress))
                    .andExpect(jsonPath("$.contactName").value(contactName))
                    .andExpect(jsonPath("$.contactEmail").value(contactEmail))
                    .andExpect(jsonPath("$.contactTelephone").value(contactTelephone))
                    .andExpect(jsonPath("$.active").value(true));

            // Create the Captor for the CreateSupplierRequest object.
            // This is to allow additional checks to make sure that what
            // createSupplier receives as input is correct.
            ArgumentCaptor<CreateSupplierRequest> captor =
                    ArgumentCaptor.forClass(CreateSupplierRequest.class);

            // The Captor allows inspection of the actual CreateSupplierRequest
            // object that was passed to createSupplier.
            verify(supplierService).createSupplier(captor.capture());

            // Retrieve the CreateSupplierRequest object passed to createService.
            CreateSupplierRequest capturedRequest = captor.getValue();

            // Now check the various fields are populated as expected.
            assertEquals(companyName, capturedRequest.getCompanyName());
            assertEquals(companyAddress, capturedRequest.getCompanyAddress());
            assertEquals(contactName, capturedRequest.getContactName());
            assertEquals(contactEmail, capturedRequest.getContactEmail());
            assertEquals(contactTelephone, capturedRequest.getContactTelephone());
        }

        @Test
        @DisplayName("createSupplier rejects blank company name")
        void createSupplierRejectsBlankCompanyName() throws Exception {

            String jsonRequestBody = """
                    {
                        "companyName": "",
                        "companyAddress": "12 The Grange, Bourneville, Birmingham",
                        "contactName": "Bill Drummond",
                        "contactEmail": "billdrummond@gmail.com",
                        "contactTelephone": "01456456457"
                    }
                    """;

            mockMvc.perform(post(getURI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequestBody))
                    .andExpect(status().isBadRequest());

            verify(supplierService, never())
                    .createSupplier(any(CreateSupplierRequest.class));
        }


        @Test
        @DisplayName("createSupplier rejects blank company address")
        void createSupplierRejectsBlankCompanyAddress() throws Exception {

            String jsonRequestBody = """
                    {
                        "companyName": "Arthur Brown Cables",
                        "companyAddress": "",
                        "contactName": "Bill Drummond",
                        "contactEmail": "billdrummond@gmail.com",
                        "contactTelephone": "01456456457"
                    }
                    """;

            mockMvc.perform(post(getURI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequestBody))
                    .andExpect(status().isBadRequest());

            verify(supplierService, never())
                    .createSupplier(any(CreateSupplierRequest.class));
        }
    } // end of SupplierCreationTests


    @Nested
    class SupplierUpdateTests {

        @Test
        @DisplayName("updateSupplier successfully updates Supplier")
        void updateSupplierWhereSupplierFound() throws Exception {

            final Integer supplierId = 90;
            Supplier supplier = new Supplier(companyName, companyAddress, contactName,
                    contactEmail, contactTelephone);
            supplier.setId(supplierId);

            when(supplierService.updateSupplier(eq(supplierId),
                    any(UpdateSupplierRequest.class)))
                    .thenReturn(supplier);

            mockMvc.perform(put(uri, supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "companyName": "%s",
                                        "companyAddress": "%s",
                                        "contactName": "%s",
                                        "contactEmail": "%s",
                                        "contactTelephone": "%s"
                                    }
                                    """.formatted(
                                    companyName,
                                    companyAddress,
                                    contactName,
                                    contactEmail,
                                    contactTelephone)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(supplierId))
                    .andExpect(jsonPath("$.companyName").value(companyName))
                    .andExpect(jsonPath("$.companyAddress").value(companyAddress))
                    .andExpect(jsonPath("$.contactName").value(contactName))
                    .andExpect(jsonPath("$.contactEmail").value(contactEmail))
                    .andExpect(jsonPath("$.contactTelephone").value(contactTelephone));

            ArgumentCaptor<UpdateSupplierRequest> captor = ArgumentCaptor.forClass(UpdateSupplierRequest.class);

            // Check updateSupplier called on mock service bean
            // with the correct supplier Id and the captured UpdateSupplierRequest.
            verify(supplierService).updateSupplier(eq(supplierId),
                    captor.capture());

            UpdateSupplierRequest capturedRequest = captor.getValue();

            assertEquals(companyName, capturedRequest.getCompanyName());
            assertEquals(companyAddress, capturedRequest.getCompanyAddress());
            assertEquals(contactName, capturedRequest.getContactName());
            assertEquals(contactEmail, capturedRequest.getContactEmail());
            assertEquals(contactTelephone, capturedRequest.getContactTelephone());
        }


        @Test
        @DisplayName("updateSupplier returns 404 when supplier not found")
        void updateSupplierWhereSupplierNotFound() throws Exception {

            final Integer supplierId = 24;

            when(supplierService.updateSupplier(
                    eq(supplierId), any(UpdateSupplierRequest.class)))
                    .thenThrow(new SupplierNotFoundException(supplierId));

            MvcResult result = mockMvc.perform(put(uri, supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "companyName": "Renamed Widgets Ltd",
                                        "companyAddress": "25 Thomas Aquinas Avenue, Broadway, Gloucestershire, GL37 7RT"
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(textContentType))
                    .andReturn();

            MockHttpServletResponse response = result.getResponse();
            System.out.println("\n\nResponse status: " + response.getStatus());
            System.out.println("Response type: " + response.getContentType());
            System.out.println("Response string: " + response.getContentAsString());
        }


        @Test
        @DisplayName("updateSupplier rejects blank company name")
        void updateSupplierRejectsBlankCompanyName() throws Exception {

            final Integer supplierId = 41;

            String jsonRequestBody = """
                    {
                        "companyName": "",
                        "companyAddress": "12 The Grange, Bourneville, Birmingham",
                        "contactName": "Bill Drummond",
                        "contactEmail": "billdrummond@gmail.com",
                        "contactTelephone": "01456456457"
                    }
                    """;

            mockMvc.perform(put(uri, supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequestBody))
                    .andExpect(status().isBadRequest());

            verify(supplierService, never())
                    .updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class));
        }


        @Test
        @DisplayName("updateSupplier rejects blank company address")
        void updateSupplierRejectsBlankCompanyAddress() throws Exception {

            final Integer supplierId = 41;

            String jsonRequestBody = """
                    {
                        "companyName": "Arthur Brown Cables",
                        "companyAddress": "",
                        "contactName": "Bill Drummond",
                        "contactEmail": "billdrummond@gmail.com",
                        "contactTelephone": "01456456457"
                    }
                    """;

            mockMvc.perform(put(uri, supplierId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequestBody))
                    .andExpect(status().isBadRequest());

            verify(supplierService, never())
                    .updateSupplier(eq(supplierId), any(UpdateSupplierRequest.class));
        }
    } // end of SupplierUpdateTests


    @Nested
    class SupplierActivationTests {
        @Test
        @DisplayName("deactivateSupplier returns 404 when supplier not found")
        void deactivateSupplierWhereSupplierNotFound() throws Exception {
            final Integer supplierId = 46;
            final String expectedExceptionMessage = "Supplier not found with ID: " + supplierId;

            when(supplierService.deactivateSupplier(supplierId))
                    .thenThrow(new SupplierNotFoundException(supplierId));

            mockMvc.perform(delete(uri, supplierId))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(expectedExceptionMessage));

            verify(supplierService).deactivateSupplier(supplierId);
        }


        @Test
        @DisplayName("deactivateSupplier keeps supplier active when supplier has outstanding invoices")
        void deactivateSupplierWhereHasOutstandingInvoices() throws Exception {
            final Integer supplierId = 21;

            when(supplierService.deactivateSupplier(supplierId))
                    .thenReturn(false);

            mockMvc.perform(delete(uri, supplierId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));

            verify(supplierService).deactivateSupplier(supplierId);
        }


        @Test
        @DisplayName("deactivateSupplier successfully deactivates supplier")
        void deactivateSupplierWhereSupplierHasNoOutstandingInvoices() throws Exception {
            final Integer supplierId = 29;

            when(supplierService.deactivateSupplier(supplierId))
                    .thenReturn(true);

            mockMvc.perform(delete(uri, supplierId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            verify(supplierService).deactivateSupplier(supplierId);
        }


        @Test
        @DisplayName("reactivateSupplier returns 404 when supplier not found")
        void reactivateSupplierWhereSupplierNotFound() throws Exception {
            final Integer supplierId = 26;
            final String expectedExceptionMessage = "Supplier not found with ID: " + supplierId;

            // Do Throw test now method has void return type.
            doThrow(new SupplierNotFoundException(supplierId))
                    .when(supplierService).reactivateSupplier(supplierId);

            mockMvc.perform(patch(uri, supplierId))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(expectedExceptionMessage));

            verify(supplierService).reactivateSupplier(supplierId);
        }


        @Test
        @DisplayName("reactivateSupplier sets supplier to active and saves it")
        void reactivateSupplierWhereSupplierFound() throws Exception {
            final Integer supplierId = 31;

            // Do Nothing test now method has void return type.
            doNothing().when(supplierService).reactivateSupplier(supplierId);

            mockMvc.perform(patch(uri, supplierId))
                    .andExpect(status().isOk());

            verify(supplierService).reactivateSupplier(supplierId);
        }
    } // end of SupplierActivationTests
}