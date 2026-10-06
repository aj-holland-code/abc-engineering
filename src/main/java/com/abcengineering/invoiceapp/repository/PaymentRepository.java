package com.abcengineering.invoiceapp.repository;

import com.abcengineering.invoiceapp.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {


    /**
     * Gets details of all non-cancelled payments associated with the invoice.
     *
     * @param invoiceId the ID of the invoice for payments to be retrieved for
     * @return a list of all non-cancelled payments, or an empty list if none exist.
     */
    List<Payment> findByInvoiceIdAndCancellationDateTimeIsNull(Integer invoiceId);
}
