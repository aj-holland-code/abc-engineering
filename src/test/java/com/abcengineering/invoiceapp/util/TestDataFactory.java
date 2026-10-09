package com.abcengineering.invoiceapp.util;

import com.abcengineering.invoiceapp.dto.UpdateInvoiceRequest;
import com.abcengineering.invoiceapp.model.Invoice;
import com.abcengineering.invoiceapp.model.Payment;
import com.abcengineering.invoiceapp.model.Supplier;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestDataFactory {

    private static final String companyAddress = "21 Main Street, Lincoln, LN12 1TY";

    public static final String TEXT_CONTENT_TYPE = "text/plain;charset=UTF-8";
    public static final String PROBLEM_TITLE = "Not Found";
    public static final String PROBLEM_TYPE = "about:blank";

    public static Supplier createSupplier(String companyName) {

        return new Supplier(companyName, companyAddress);
    }

    public static Supplier createSupplierWithContactDetails(String companyName) {

        Supplier supplier = createSupplier(companyName);
        supplier.setContactName("Ian Barnes");
        supplier.setContactEmail("ian.barnes@gmail.com");
        supplier.setContactTelephone("01435239764");
        return supplier;
    }


    public static Invoice createInvoice(String companyName) {

        return new Invoice(createSupplier(companyName),
                "INV-REF-01",
                LocalDate.now(),
                LocalDate.of(2027, 3, 4),
                BigDecimal.valueOf(65));
    }

    public static UpdateInvoiceRequest createUpdateInvoiceRequest(String supplierInvoiceRef) {
        UpdateInvoiceRequest request = new UpdateInvoiceRequest();
        request.setSupplierInvoiceRef(supplierInvoiceRef);
        request.setDueDate(LocalDate.now());
        request.setInvoiceDate(LocalDate.now());
        request.setInvoiceAmount(BigDecimal.valueOf(130));
        return request;
    }


    public static Payment createPayment(String companyName, long paymentAmount) {
        return new Payment(createInvoice(companyName),
                LocalDate.now(),
                BigDecimal.valueOf(paymentAmount),
                "BACS",
                "ABC-1001");
    }
}