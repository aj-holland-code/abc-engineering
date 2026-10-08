package com.abcengineering.invoiceapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SupplierNotFoundException.class)
    public ResponseEntity<String> handleSupplierNotFound(SupplierNotFoundException ex) {
        logger.warn("{}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<String> handleInvoiceNotFound(InvoiceNotFoundException ex) {
        logger.warn("{}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<String> handlePaymentNotFound(PaymentNotFoundException ex) {
        logger.warn("{}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
}
