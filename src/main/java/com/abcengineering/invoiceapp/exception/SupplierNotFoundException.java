package com.abcengineering.invoiceapp.exception;

public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(Integer id) {

        super("Supplier not found with ID: " + id);
    }
}
