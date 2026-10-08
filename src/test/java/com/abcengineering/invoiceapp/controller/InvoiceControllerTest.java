package com.abcengineering.invoiceapp.controller;

import com.abcengineering.invoiceapp.dto.CancelInvoiceRequest;
import com.abcengineering.invoiceapp.dto.CreateInvoiceRequest;
import com.abcengineering.invoiceapp.dto.UpdateInvoiceRequest;
import com.abcengineering.invoiceapp.exception.InvoiceNotFoundException;
import com.abcengineering.invoiceapp.exception.SupplierNotFoundException;
import com.abcengineering.invoiceapp.model.Invoice;
import com.abcengineering.invoiceapp.service.InvoiceService;
import com.abcengineering.invoiceapp.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
public class InvoiceControllerTest {

    @MockitoBean
    private InvoiceService invoiceService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String INVOICE_URI = "/api/invoices";
    private final String ID_INVOICE_URI = "/api/invoices/{id}";
    private final String invoiceRefTooLong =
            "You purchased this item from us a few months ago - I forget the exact date, but one of your accounts representatives called us to place the order before their lunch break. I can't believe I haven't got more details, but there we are. I hope I have nonetheless got enough info here for you to go on.";

    @Nested
    class InvoiceRetrievalTests {

        @Test
        @DisplayName("getInvoice returns 404 when invoice not found")
        void getInvoiceWhereInvoiceNotFound() throws Exception {
            final Integer invoiceId = 129;
            final String expectedExceptionMessage = "Invoice not found with ID: " + invoiceId;

            when(invoiceService.getInvoice(invoiceId))
                    .thenThrow(new InvoiceNotFoundException(invoiceId));

            mockMvc.perform(get(ID_INVOICE_URI, invoiceId))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(TestDataFactory.TEXT_CONTENT_TYPE))
                    .andExpect(content().string(expectedExceptionMessage));
        }

        @Test
        @DisplayName("getInvoice returns invoice")
        void getInvoice() throws Exception {
            final Integer invoiceId = 130;
            final String companyName = "Balthus Enterprises";

            Invoice returnedInvoice = TestDataFactory.createInvoice(companyName);
            returnedInvoice.setId(invoiceId);

            when(invoiceService.getInvoice(invoiceId))
                    .thenReturn(returnedInvoice);

            mockMvc.perform(get(ID_INVOICE_URI, invoiceId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(invoiceId))
                    .andExpect(jsonPath("$.supplierInvoiceRef").value(returnedInvoice.getSupplierInvoiceRef()))
                    .andExpect(jsonPath("$.invoiceDate").value(returnedInvoice.getInvoiceDate().toString()))
                    .andExpect(jsonPath("$.dueDate").value(returnedInvoice.getDueDate().toString()))
                    .andExpect(jsonPath("$.invoiceAmount").value(returnedInvoice.getInvoiceAmount()))
                    .andExpect(jsonPath("$.cancelledAt").value(nullValue()))
                    .andExpect(jsonPath("$.cancellationReason").value(nullValue()));
        }


        @Test
        @DisplayName("getAllInvoices returns empty list")
        void getAllInvoicesNoInvoicesFound() throws Exception {

            when(invoiceService.getAllInvoices())
                    .thenReturn(List.of());

            mockMvc.perform(get(INVOICE_URI))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }


        @Test
        @DisplayName("getAllInvoices returns invoices")
        void getAllInvoices() throws Exception {

            final String companyName = "FUD Telemarketing";
            Invoice[] invoices = new Invoice[2];
            invoices[0] = TestDataFactory.createInvoice(companyName);
            invoices[0].setId(10);
            invoices[1] = TestDataFactory.createInvoice(companyName);
            invoices[1].setSupplierInvoiceRef("INV-REF_02");
            invoices[1].setId(11);

            when(invoiceService.getAllInvoices()).
                    thenReturn(List.of(invoices));

            // Check two invoices returned.
            // Also check invoice ref (distinct) and invoice amount (representative)
            // for each of the two invoices, but not all the other fields.
            mockMvc.perform(get(INVOICE_URI))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0]id").value(invoices[0].getId()))
                    .andExpect(jsonPath("$[0]supplierInvoiceRef").value(invoices[0].getSupplierInvoiceRef()))
                    .andExpect(jsonPath("$[0]invoiceAmount").value(invoices[0].getInvoiceAmount()))
                    .andExpect(jsonPath("$[1]id").value(invoices[1].getId()))
                    .andExpect(jsonPath("$[1]supplierInvoiceRef").value(invoices[1].getSupplierInvoiceRef()))
                    .andExpect(jsonPath("$[1]invoiceAmount").value(invoices[1].getInvoiceAmount()));
        }
    } // end of InvoiceRetrievalTests


    @Nested
    class InvoiceCreationTests {

        @Test
        @DisplayName("createInvoice successfully creates and returns invoice")
        void createInvoice() throws Exception {

            final String companyName = "Balthus Enterprises";
            final Integer supplierId = 86;
            final Integer invoiceId = 101;
            final String supplierInvoiceRef = "ABC-86";
            final BigDecimal invoiceAmount = BigDecimal.valueOf(145);
            final LocalDate invoiceDate = LocalDate.now();
            final LocalDate dueDate = LocalDate.of(2027, 4, 5);

            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setSupplierId(supplierId);
            request.setSupplierInvoiceRef(supplierInvoiceRef);
            request.setInvoiceAmount(invoiceAmount);
            request.setInvoiceDate(invoiceDate);
            request.setDueDate(dueDate);

            Invoice returnedInvoice = TestDataFactory.createInvoice(companyName);
            returnedInvoice.setId(invoiceId);
            returnedInvoice.getSupplier().setId(supplierId);
            returnedInvoice.setSupplierInvoiceRef(supplierInvoiceRef);
            returnedInvoice.setInvoiceAmount(invoiceAmount);
            returnedInvoice.setInvoiceDate(invoiceDate);
            returnedInvoice.setDueDate(dueDate);

            when(invoiceService.createInvoice(any(CreateInvoiceRequest.class)))
                    .thenReturn(returnedInvoice);

            mockMvc.perform(post(INVOICE_URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(returnedInvoice.getId()))
                    .andExpect(jsonPath("$.supplier.id").value(returnedInvoice.getSupplier().getId()))
                    .andExpect(jsonPath("$.supplierInvoiceRef").value(returnedInvoice.getSupplierInvoiceRef()))
                    .andExpect(jsonPath("$.invoiceDate").value(returnedInvoice.getInvoiceDate().toString()))
                    .andExpect(jsonPath("$.invoiceAmount").value(returnedInvoice.getInvoiceAmount()))
                    .andExpect(jsonPath("$.dueDate").value(returnedInvoice.getDueDate().toString()));

            ArgumentCaptor<CreateInvoiceRequest> captor =
                    ArgumentCaptor.forClass(CreateInvoiceRequest.class);

            verify(invoiceService).createInvoice(captor.capture());

            CreateInvoiceRequest capturedRequest = captor.getValue();

            assertEquals(request.getSupplierId(), capturedRequest.getSupplierId());
            assertEquals(request.getSupplierInvoiceRef(), capturedRequest.getSupplierInvoiceRef());
            assertEquals(request.getInvoiceDate(), capturedRequest.getInvoiceDate());
            assertEquals(request.getInvoiceAmount(), capturedRequest.getInvoiceAmount());
            assertEquals(request.getDueDate(), capturedRequest.getDueDate());
        }


        @Test
        @DisplayName("createInvoice with an invoice ref over 100 chars")
        void createInvoiceWhereInvoiceReferenceTooLong() throws Exception {

            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setSupplierInvoiceRef("ABC-10");
            request.setInvoiceAmount(BigDecimal.valueOf(150));
            request.setInvoiceDate(LocalDate.now());
            request.setDueDate(LocalDate.now());

            // Ensure the invoice reference is too long
            request.setSupplierInvoiceRef(invoiceRefTooLong);

            mockMvc.perform(post(INVOICE_URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Check invalid request never reaches InvoiceService
            verify(invoiceService, never()).createInvoice(any(CreateInvoiceRequest.class));
        }

        @Test
        @DisplayName("createInvoice without an invoice amount")
        void createInvoiceWithMissingMandatoryField() throws Exception {

            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setSupplierInvoiceRef("ABC-10");
            request.setInvoiceDate(LocalDate.now());
            request.setDueDate(LocalDate.now());

            mockMvc.perform(post(INVOICE_URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Check invalid request never reaches InvoiceService
            verify(invoiceService, never()).createInvoice(any(CreateInvoiceRequest.class));
        }


        @Test
        @DisplayName("createInvoice returns 404 as supplier not found")
        void createInvoiceWhereSupplierNotFound() throws Exception {

            final Integer supplierId = 14;
            final String expectedExceptionMessage = "Supplier not found with ID: " + supplierId;

            CreateInvoiceRequest request = new CreateInvoiceRequest();
            request.setSupplierId(supplierId);
            request.setSupplierInvoiceRef("ABC-10");
            request.setInvoiceDate(LocalDate.now());
            request.setDueDate(LocalDate.now());
            request.setInvoiceAmount(BigDecimal.valueOf(125));

            when(invoiceService.createInvoice(any(CreateInvoiceRequest.class)))
                    .thenThrow(new SupplierNotFoundException(supplierId));

            mockMvc.perform(post(INVOICE_URI)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(TestDataFactory.TEXT_CONTENT_TYPE))
                    .andExpect(content().string(expectedExceptionMessage));
        }
    } // end of InvoiceCreationTests

    @Nested
    class UpdateInvoiceTests {

        @Test
        @DisplayName("updateInvoice successfully updates and returns invoice")
        void updateInvoice() throws Exception {
            final Integer invoiceId = 35;
            final String supplierInvoiceRef = "ABC-1000";
            final String companyName = "Dave Bledsoe Services";
            UpdateInvoiceRequest request = TestDataFactory.createUpdateInvoiceRequest(supplierInvoiceRef);
            request.setDueDate(LocalDate.of(2027, 5, 1));

            Invoice returnedInvoice = TestDataFactory.createInvoice(companyName);
            returnedInvoice.setId(invoiceId);
            returnedInvoice.setDueDate(request.getDueDate());
            returnedInvoice.setInvoiceAmount(request.getInvoiceAmount());
            returnedInvoice.setSupplierInvoiceRef(supplierInvoiceRef);
            returnedInvoice.setInvoiceDate(request.getInvoiceDate());
            returnedInvoice.getSupplier().setCompanyName(companyName);

            when(invoiceService.updateInvoice(eq(invoiceId), any(UpdateInvoiceRequest.class)))
                    .thenReturn(returnedInvoice);

            mockMvc.perform(put(ID_INVOICE_URI, invoiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(returnedInvoice.getId()))
                    .andExpect(jsonPath("$.supplier.id").value(returnedInvoice.getSupplier().getId()))
                    .andExpect(jsonPath("$.supplierInvoiceRef").value(returnedInvoice.getSupplierInvoiceRef()))
                    .andExpect(jsonPath("$.invoiceDate").value(returnedInvoice.getInvoiceDate().toString()))
                    .andExpect(jsonPath("$.invoiceAmount").value(returnedInvoice.getInvoiceAmount()))
                    .andExpect(jsonPath("$.dueDate").value(returnedInvoice.getDueDate().toString()));

            ArgumentCaptor<UpdateInvoiceRequest> captor =
                    ArgumentCaptor.forClass(UpdateInvoiceRequest.class);

            verify(invoiceService).updateInvoice(eq(invoiceId), captor.capture());

            UpdateInvoiceRequest capturedRequest = captor.getValue();

            assertEquals(request.getSupplierInvoiceRef(), capturedRequest.getSupplierInvoiceRef());
            assertEquals(request.getInvoiceDate(), capturedRequest.getInvoiceDate());
            assertEquals(request.getInvoiceAmount(), capturedRequest.getInvoiceAmount());
            assertEquals(request.getDueDate(), capturedRequest.getDueDate());
        }


        @Test
        @DisplayName("updateInvoice returns 404 when invoice not found")
        void updateInvoiceWhereInvoiceNotFound() throws Exception {
            final Integer invoiceId = 129;
            final String expectedExceptionMessage = "Invoice not found with ID: " + invoiceId;

            UpdateInvoiceRequest request = new UpdateInvoiceRequest();
            request.setSupplierInvoiceRef("ABC-110");
            request.setDueDate(LocalDate.now());
            request.setInvoiceDate(LocalDate.now());
            request.setInvoiceAmount(BigDecimal.valueOf(130));

            when(invoiceService.updateInvoice(eq(invoiceId), any(UpdateInvoiceRequest.class)))
                    .thenThrow(new InvoiceNotFoundException(invoiceId));

            mockMvc.perform(put(ID_INVOICE_URI, invoiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(TestDataFactory.TEXT_CONTENT_TYPE))
                    .andExpect(content().string(expectedExceptionMessage));
        }

        @Test
        @DisplayName("updateInvoice with an invoice ref over 100 chars")
        void updateInvoiceWhereInvoiceReferenceTooLong() throws Exception {

            final Integer invoiceId = 101;
            UpdateInvoiceRequest request = new UpdateInvoiceRequest();
            request.setSupplierInvoiceRef("ABC-10");
            request.setInvoiceDate(LocalDate.now());
            request.setInvoiceAmount(BigDecimal.valueOf(150));
            request.setDueDate(LocalDate.now());

            // Ensure the invoice reference is too long
            request.setSupplierInvoiceRef(invoiceRefTooLong);

            mockMvc.perform(put(ID_INVOICE_URI, invoiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Check invalid request never reaches InvoiceService
            verify(invoiceService, never()).updateInvoice(eq(invoiceId), any(UpdateInvoiceRequest.class));
        }


        @Test
        @DisplayName("updateInvoice without an invoice amount")
        void updateInvoiceWithMissingMandatoryField() throws Exception {

            final Integer invoiceId = 54;
            UpdateInvoiceRequest request = new UpdateInvoiceRequest();
            request.setSupplierInvoiceRef("ABC-100");
            request.setInvoiceDate(LocalDate.now());
            request.setDueDate(LocalDate.now());

            mockMvc.perform(put(ID_INVOICE_URI, invoiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Check invalid request never reaches InvoiceService
            verify(invoiceService, never()).updateInvoice(eq(invoiceId), any(UpdateInvoiceRequest.class));
        }
    } // end of UpdateInvoiceTests


    @Nested
    class CancelInvoiceTests {

        @Test
        @DisplayName("cancelInvoice without a cancellation reason")
        void cancelInvoiceWithMissingMandatoryField() throws Exception {
            final Integer invoiceId = 45;
            CancelInvoiceRequest request = new CancelInvoiceRequest();

            mockMvc.perform(patch(ID_INVOICE_URI, invoiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            // Check invalid request never reaches the service method.
            verify(invoiceService, never()).cancelInvoice(eq(invoiceId), any(CancelInvoiceRequest.class));
        }


        @Test
        @DisplayName("cancelInvoice returns 404 when invoice not found")
        void cancelInvoiceWhereInvoiceNotFound() throws Exception {
            final Integer invoiceId = 129;
            final String expectedExceptionMessage = "Invoice not found with ID: " + invoiceId;
            final String cancellationReason = "Incorrect product sent";

            CancelInvoiceRequest request = new CancelInvoiceRequest();
            request.setCancellationReason(cancellationReason);

            when(invoiceService.cancelInvoice(eq(invoiceId), any(CancelInvoiceRequest.class)))
                    .thenThrow(new InvoiceNotFoundException(invoiceId));

            mockMvc.perform(patch(ID_INVOICE_URI, invoiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(TestDataFactory.TEXT_CONTENT_TYPE))
                    .andExpect(content().string(expectedExceptionMessage));

            // Check the service was reached with the correct invoice id.
            verify(invoiceService).cancelInvoice(eq(invoiceId), any(CancelInvoiceRequest.class));
        }


        @Test
        @DisplayName("cancelInvoice cannot cancel already cancelled invoice")
        void cancelInvoiceWhereInvoiceAlreadyCancelled() throws Exception {

            final Integer invoiceId = 55;
            final String cancellationReason = "Goods returned";
            CancelInvoiceRequest request = new CancelInvoiceRequest();
            request.setCancellationReason(cancellationReason);

            when(invoiceService.cancelInvoice(eq(invoiceId), any(CancelInvoiceRequest.class)))
                    .thenReturn(false);

            mockMvc.perform(patch(ID_INVOICE_URI, invoiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }

        @Test
        @DisplayName("cancelInvoice successfully cancels invoice")
        void cancelInvoice() throws Exception {

            final Integer invoiceId = 60;
            final String cancellationReason = "Items damaged";
            CancelInvoiceRequest request = new CancelInvoiceRequest();
            request.setCancellationReason(cancellationReason);

            when(invoiceService.cancelInvoice(eq(invoiceId), any(CancelInvoiceRequest.class)))
                    .thenReturn(true);

            mockMvc.perform(patch(ID_INVOICE_URI, invoiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            ArgumentCaptor<CancelInvoiceRequest> captor =
                    ArgumentCaptor.forClass(CancelInvoiceRequest.class);

            // Confirm cancelInvoice was called (in perform() step, above)
            // with invoiceId.
            // Also ensures the CancelInvoiceRequest object it was  passed is captured.
            verify(invoiceService).cancelInvoice(eq(invoiceId), captor.capture());

            CancelInvoiceRequest capturedRequest = captor.getValue();
            assertEquals(cancellationReason, capturedRequest.getCancellationReason());
        }
    } // end of CancelInvoiceTests
}
