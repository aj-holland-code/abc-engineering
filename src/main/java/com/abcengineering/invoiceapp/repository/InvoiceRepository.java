package com.abcengineering.invoiceapp.repository;

import com.abcengineering.invoiceapp.model.Invoice;
import com.abcengineering.invoiceapp.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository
        extends JpaRepository<Invoice, Integer> {

    /**
     * Find all invoices that are not cancelled for the supplier.
     *
     * @param supplierId the Id of the supplier
     * @return a list of all non-cancelled invoices for the supplier, or an empty list if no such invoices exist
     */
    List<Invoice> findBySupplierIdAndCancelledAtIsNull(Integer supplierId);
}
