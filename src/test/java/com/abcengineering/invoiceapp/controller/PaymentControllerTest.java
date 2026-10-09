package com.abcengineering.invoiceapp.controller;

import com.abcengineering.invoiceapp.dto.CancelInvoiceRequest;
import com.abcengineering.invoiceapp.dto.CancelPaymentRequest;
import com.abcengineering.invoiceapp.dto.CreateInvoiceRequest;
import com.abcengineering.invoiceapp.dto.CreatePaymentRequest;
import com.abcengineering.invoiceapp.dto.UpdateInvoiceRequest;
import com.abcengineering.invoiceapp.exception.InvoiceNotFoundException;
import com.abcengineering.invoiceapp.exception.PaymentNotFoundException;
import com.abcengineering.invoiceapp.model.Invoice;
import com.abcengineering.invoiceapp.model.Payment;
import com.abcengineering.invoiceapp.service.PaymentService;
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
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

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

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @MockitoBean
    PaymentService paymentService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String GET_URI = "/api/payments/{id}";
    private static final String EXCEPTION_GET_URI = "/api/payments/";
    private static final String PATCH_URI = "/api/invoices/{id}/payments/{paymentId}";
    private static final String EXCEPTION_PATCH_URI = "/api/invoices/";
    private static final String PAYMENTS_SLASH_URI = "/payments/";
    private static final String PAYMENTS_URI = "/payments";
    private static final String POST_URI = "/api/invoices/{id}/payments";
    private static final String EXCEPTION_POST_URI = EXCEPTION_PATCH_URI;

    @Nested
    class GetPaymentTests {

        private final Integer paymentId = 10;

        @Test
        @DisplayName("getPayment throws 404 exception")
        void getPaymentWhereInvoiceNotFound() throws Exception {

            final String expectedExceptionMessage = "Payment not found with ID: " + paymentId;

            when(paymentService.getPayment(paymentId))
                    .thenThrow(new PaymentNotFoundException(paymentId));

            mockMvc.perform(get(GET_URI, paymentId))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(TestDataFactory.PROBLEM_TYPE))
                    .andExpect(jsonPath("$.title")
                            .value(TestDataFactory.PROBLEM_TITLE))
                    .andExpect(jsonPath("$.status").value(
                            MockHttpServletResponse.SC_NOT_FOUND))
                    .andExpect(jsonPath("$.detail").value(expectedExceptionMessage))
                    .andExpect(jsonPath("$.instance").value(EXCEPTION_GET_URI + paymentId));
        }


        @Test
        @DisplayName("getPayment returns payment")
        void getPayment() throws Exception {

            final String companyName = "HHC Transport Ltd.";
            final LocalDate paymentDate = LocalDate.now();
            BigDecimal paymentAmount = BigDecimal.valueOf(120);
            String paymentMethod = "CHAPS";
            String paymentReference = "HHCT-ABC-1000";

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            Payment expectedPayment = new Payment(invoice,
                    paymentDate, paymentAmount, paymentMethod, paymentReference);
            expectedPayment.setId(paymentId);

            when(paymentService.getPayment(paymentId))
                    .thenReturn(expectedPayment);

            mockMvc.perform(get(GET_URI, paymentId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(paymentId))
                    .andExpect(jsonPath("$.paymentDate").value(paymentDate.toString()))
                    .andExpect(jsonPath("$.paymentAmount").value(paymentAmount))
                    .andExpect(jsonPath("$.paymentMethod").value(paymentMethod))
                    .andExpect(jsonPath("$.paymentReference").value(paymentReference))
                    .andExpect(jsonPath("$.cancellationDateTime").value(nullValue()))
                    .andExpect(jsonPath("$.cancellationReason").value(nullValue()));
        }
    } // end of GetPaymentTests


    @Nested
    class CancelPaymentTests {

        private final Integer invoiceId = 15;
        private final Integer paymentId = 30;
        private final String cancellationReason = "Payment made in error";
        private final String companyName = "Hercules Parrot Services";

        @Test
        @DisplayName("cancelPayment fails as payment not found")
        void cancelPaymentWherePaymentNotFound() throws Exception {

            final String expectedExceptionMessage = "Payment not found with ID: " + paymentId;

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setId(invoiceId);

            CancelPaymentRequest request = new CancelPaymentRequest();
            request.setCancellationReason(cancellationReason);

            when(paymentService.cancelPayment(eq(paymentId), any(CancelPaymentRequest.class)))
                    .thenThrow(new PaymentNotFoundException(paymentId));

            mockMvc.perform(patch(PATCH_URI, invoiceId, paymentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(TestDataFactory.PROBLEM_TYPE))
                    .andExpect(jsonPath("$.title")
                            .value(TestDataFactory.PROBLEM_TITLE))
                    .andExpect(jsonPath("$.status").value(
                            MockHttpServletResponse.SC_NOT_FOUND))
                    .andExpect(jsonPath("$.detail").value(expectedExceptionMessage))
                    .andExpect(jsonPath("$.instance")
                            .value(EXCEPTION_PATCH_URI + invoiceId + PAYMENTS_SLASH_URI + paymentId));

            ArgumentCaptor<CancelPaymentRequest> captor =
                    ArgumentCaptor.forClass(CancelPaymentRequest.class);

            verify(paymentService).cancelPayment(eq(paymentId), captor.capture());

            assertEquals(
                    request.getCancellationReason(),
                    captor.getValue().getCancellationReason());
        }


        @Test
        @DisplayName("cancelPayment rejected for blank cancellation reason")
        void cancelPaymentWhereNoCancellationReasonSupplied() throws Exception {

            CancelPaymentRequest request = new CancelPaymentRequest();

            mockMvc.perform(patch(PATCH_URI, invoiceId, paymentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isBadRequest());

            verify(paymentService, never()).cancelPayment(eq(paymentId), any(CancelPaymentRequest.class));
        }



        @Test
        @DisplayName("cancelPayment fails as payment has already been cancelled")
        void cancelPaymentWherePaymentAlreadyCancelled() throws Exception {

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setId(invoiceId);

            CancelPaymentRequest request = new CancelPaymentRequest();
            request.setCancellationReason(cancellationReason);

            when(paymentService.cancelPayment(eq(paymentId), any(CancelPaymentRequest.class)))
                    .thenReturn(false);

            mockMvc.perform(patch(PATCH_URI, invoiceId, paymentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("false"));
        }


        @Test
        @DisplayName("cancelPayment successfully cancels payment")
        void cancelPayment() throws Exception {

            CancelPaymentRequest request = new CancelPaymentRequest();
            request.setCancellationReason(cancellationReason);

            when(paymentService.cancelPayment(eq(paymentId), any(CancelPaymentRequest.class)))
                    .thenReturn(true);

            mockMvc.perform(patch(PATCH_URI, invoiceId, paymentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            ArgumentCaptor<CancelPaymentRequest> captor =
                    ArgumentCaptor.forClass(CancelPaymentRequest.class);

            verify(paymentService).cancelPayment(eq(paymentId), captor.capture());

            CancelPaymentRequest capturedRequest = captor.getValue();
            assertEquals(cancellationReason, capturedRequest.getCancellationReason());
        }
    } // end of CancelPaymentTests

    @Nested
    class CreatePaymentTests {

        private final Integer invoiceId = 54;
        private final Integer paymentId = 12;
        private final String companyName = "JF Ballard & Sons";
        private final LocalDate paymentDate = LocalDate.now();
        private final BigDecimal paymentAmount = BigDecimal.valueOf(100);
        private final String paymentMethod = "BACS";
        private final String paymentReference = "JFBS-ABC-200";


        @Test
        @DisplayName("createPayment with missing payment amount")
        void createPaymentWithNoPaymentAmount() throws Exception {

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setId(invoiceId);

            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setInvoiceId(invoiceId);
            request.setPaymentDate(paymentDate);
            request.setPaymentMethod(paymentMethod);
            request.setPaymentReference(paymentReference);

            mockMvc.perform(post(POST_URI, request)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(paymentService, never()).createPayment(any(CreatePaymentRequest.class));
        }


        @Test
        @DisplayName("createPayment with payment method over 30 character limit")
        void createPaymentWherePaymentMethodTooLong() throws Exception {

            final String longPaymentMethod = "A method of payment that is excessively long for the field on the DTO";
            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setId(invoiceId);

            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setInvoiceId(invoiceId);
            request.setPaymentDate(paymentDate);
            request.setPaymentMethod(longPaymentMethod);
            request.setPaymentReference(paymentReference);

            mockMvc.perform(post(POST_URI, request)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(paymentService, never()).createPayment(any(CreatePaymentRequest.class));
        }


        @Test
        @DisplayName("createPayment with payment reference over 50 character limit")
        void createPaymentWherePaymentReferenceTooLong() throws Exception {

            final String longPaymentReference =
                    "A payment reference that exceeds the maximum allowed characters for the field in CreatePaymentRequest";
            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setId(invoiceId);

            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setInvoiceId(invoiceId);
            request.setPaymentDate(paymentDate);
            request.setPaymentMethod(paymentMethod);
            request.setPaymentReference(longPaymentReference);

            mockMvc.perform(post(POST_URI, request)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isBadRequest());

            verify(paymentService, never()).createPayment(any(CreatePaymentRequest.class));
        }


        @Test
        @DisplayName("createPayment fails as invoice not found")
        void createPaymentWhereInvoiceNotFound() throws Exception {
            final String expectedExceptionMessage = "Invoice not found with ID: " + invoiceId;

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setId(invoiceId);

            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setInvoiceId(invoiceId);
            request.setPaymentDate(paymentDate);
            request.setPaymentAmount(paymentAmount);
            request.setPaymentMethod(paymentMethod);
            request.setPaymentReference(paymentReference);

            when(paymentService.createPayment(any(CreatePaymentRequest.class)))
                    .thenThrow(new InvoiceNotFoundException(invoiceId));


            mockMvc.perform(post(POST_URI, invoiceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(
                            MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type").value(TestDataFactory.PROBLEM_TYPE))
                    .andExpect(jsonPath("$.title")
                            .value(TestDataFactory.PROBLEM_TITLE))
                    .andExpect(jsonPath("$.status").value(
                            MockHttpServletResponse.SC_NOT_FOUND))
                    .andExpect(jsonPath("$.detail").value(expectedExceptionMessage))
                    .andExpect(jsonPath("$.instance")
                            .value(EXCEPTION_POST_URI + invoiceId + PAYMENTS_URI));
        }

        @Test
        @DisplayName("createPayment creates and returns payment")
        void createPayment() throws Exception {

            Invoice invoice = TestDataFactory.createInvoice(companyName);
            invoice.setId(invoiceId);

            CreatePaymentRequest request = new CreatePaymentRequest();
            request.setInvoiceId(invoiceId);
            request.setPaymentDate(paymentDate);
            request.setPaymentAmount(paymentAmount);
            request.setPaymentMethod(paymentMethod);
            request.setPaymentReference(paymentReference);

            Payment payment = new Payment(invoice, paymentDate, paymentAmount,
                    paymentMethod, paymentReference);
            payment.setId(paymentId);

            when(paymentService.createPayment(any(CreatePaymentRequest.class)))
                    .thenReturn(payment);

            mockMvc.perform(post(POST_URI, request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(paymentId))
                    .andExpect(jsonPath("$.paymentDate").value(paymentDate.toString()))
                    .andExpect(jsonPath("$.paymentAmount").value(paymentAmount))
                    .andExpect(jsonPath("$.paymentMethod").value(paymentMethod))
                    .andExpect(jsonPath("$.paymentReference").value(paymentReference));

            ArgumentCaptor<CreatePaymentRequest> captor
                    = ArgumentCaptor.forClass(CreatePaymentRequest.class);

            verify(paymentService).createPayment(captor.capture());

            CreatePaymentRequest capturedRequest = captor.getValue();

            assertEquals(request.getInvoiceId(), capturedRequest.getInvoiceId());
            assertEquals(request.getPaymentDate(), capturedRequest.getPaymentDate());
            assertEquals(request.getPaymentAmount(), capturedRequest.getPaymentAmount());
            assertEquals(request.getPaymentMethod(), capturedRequest.getPaymentMethod());
            assertEquals(request.getPaymentReference(), capturedRequest.getPaymentReference());
        }
    } // end of CreatePaymentTests
}
