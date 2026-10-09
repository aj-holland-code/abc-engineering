package com.abcengineering.invoiceapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ProblemDetail;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PROBLEM_TITLE = "Not Found";
    private static final String PROBLEM_TYPE = "about:blank";

    @ExceptionHandler(SupplierNotFoundException.class)
    public ProblemDetail handleSupplierNotFound(SupplierNotFoundException ex) {
        logger.warn("{}", ex.getMessage());

        // Use the static factory method to create a ProblemDetail
        // for the JSON response.
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());

        problem.setType(URI.create(PROBLEM_TYPE));
        problem.setTitle(PROBLEM_TITLE);

        return problem;
    }

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ProblemDetail handleInvoiceNotFound(InvoiceNotFoundException ex) {
        logger.warn("{}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());

        problem.setType(URI.create(PROBLEM_TYPE));
        problem.setTitle(PROBLEM_TITLE);

        return problem;
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ProblemDetail handlePaymentNotFound(PaymentNotFoundException ex) {
        logger.warn("{}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());

        problem.setType(URI.create(PROBLEM_TYPE));
        problem.setTitle(PROBLEM_TITLE);

        return problem;
    }
}
