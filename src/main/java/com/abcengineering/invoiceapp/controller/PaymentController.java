package com.abcengineering.invoiceapp.controller;

import com.abcengineering.invoiceapp.dto.CancelPaymentRequest;
import com.abcengineering.invoiceapp.dto.CreatePaymentRequest;
import com.abcengineering.invoiceapp.model.Payment;
import com.abcengineering.invoiceapp.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {

        this.paymentService = paymentService;
    }


    /**
     * Creates a payment.
     *
     * @param paymentRequest the details of the payment to create
     * @return the created payment
     */
    @PostMapping("/api/invoices/{id}/payments")
    public Payment createPayment(@Valid @RequestBody CreatePaymentRequest paymentRequest) {

        return paymentService.createPayment(paymentRequest);
    }


    /**
     * Gets a payment by ID.
     *
     * @param paymentId the ID of the payment to retrieve
     */
    @GetMapping("/api/payments/{id}")
    public Payment getPayment(@PathVariable("id") Integer paymentId) {

        return paymentService.viewPayment(paymentId);
    }

    /**
     * Cancels a payment.
     *
     * @param paymentId the ID of the payment to be cancelled
     * @param cancellationRequest the details of the cancellation
     * @return {@code true} if the payment was successfully cancelled in this operation;
     *         {@code false} if the payment was already cancelled
     */
    @PatchMapping("/api/invoices/{id}/payments/{paymentId}")
    public boolean cancelPayment(@PathVariable("paymentId") Integer paymentId,
        @Valid @RequestBody CancelPaymentRequest cancellationRequest) {

        return paymentService.cancelPayment(paymentId, cancellationRequest);
    }
}
