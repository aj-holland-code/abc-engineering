package com.abcengineering.invoiceapp.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(Integer paymentId) {

        super("Payment not found with id: " + paymentId);
    }
}
