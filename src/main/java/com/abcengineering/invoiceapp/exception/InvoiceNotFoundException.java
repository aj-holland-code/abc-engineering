package com.abcengineering.invoiceapp.exception;

public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(Integer id) {

        super("Invoice not found with id: " + id);
    }
}
