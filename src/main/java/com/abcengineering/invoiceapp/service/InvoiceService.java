package com.abcengineering.invoiceapp.service;

import com.abcengineering.invoiceapp.dto.CancelInvoiceRequest;
import com.abcengineering.invoiceapp.dto.CreateInvoiceRequest;
import com.abcengineering.invoiceapp.dto.UpdateInvoiceRequest;
import com.abcengineering.invoiceapp.exception.InvoiceNotFoundException;
import com.abcengineering.invoiceapp.exception.SupplierNotFoundException;
import com.abcengineering.invoiceapp.model.Invoice;
import com.abcengineering.invoiceapp.model.Payment;
import com.abcengineering.invoiceapp.model.Supplier;
import com.abcengineering.invoiceapp.repository.InvoiceRepository;
import com.abcengineering.invoiceapp.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {

    private final PaymentService paymentService;
    private final InvoiceRepository invoiceRepository;
    private final SupplierRepository supplierRepository;

    private static final Logger logger
            = LoggerFactory.getLogger(InvoiceService.class);

    public InvoiceService(PaymentService paymentService, InvoiceRepository invoiceRepository,
        SupplierRepository supplierRepository) {
        this.paymentService = paymentService;
        this.invoiceRepository = invoiceRepository;
        this.supplierRepository = supplierRepository;
    }


    /**
     * Create an invoice.
     *
     * @param invoiceRequest the details of invoice to be created
     * @return the created {@link Invoice} entity
     * @throws SupplierNotFoundException if no supplier exists with the supplied ID in invoiceRequest
     */
    public Invoice createInvoice(CreateInvoiceRequest invoiceRequest) {
        // Get the Supplier first
        Supplier supplier = supplierRepository.findById(invoiceRequest.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException(invoiceRequest.getSupplierId()));

        logger.info("Creating invoice for supplier {}", supplier.getId());

        // Use it create the Invoice
        Invoice invoice = new Invoice(supplier, invoiceRequest.getSupplierInvoiceRef(),
                invoiceRequest.getInvoiceDate(), invoiceRequest.getDueDate(),
                invoiceRequest.getInvoiceAmount());

        Invoice createdInvoice = invoiceRepository.save(invoice);

        logger.info("Invoice successfully created with ID {}", createdInvoice.getId());
        return createdInvoice;
    }


    /**
     * Gets invoice details.
     *
     * @param invoiceId the ID of the invoice to get
     * @return the {@link Invoice} entity associated with the supplied ID
     * @throws InvoiceNotFoundException if no invoice exists with the supplied ID
     */
    public Invoice getInvoice(Integer invoiceId) {
        return invoiceRepository.findById(invoiceId).orElseThrow(
                () -> new InvoiceNotFoundException(invoiceId));
    }


    /**
     * Get all invoices, regardless of payment or cancellation status.
     *
     * @return a list of all invoices, or an empty list if no invoices exist
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }


    /**
     * Get all invoices that are not cancelled for the supplier.
     *
     * @param supplierId the ID of the supplier
     * @return a list of all non-cancelled invoices for the supplier, or an empty list if no such invoices exist
     */
    public List<Invoice> getNonCancelledInvoicesForSupplier(Integer supplierId) {
        return invoiceRepository.findBySupplierIdAndCancelledAtIsNull(supplierId);
    }


    /**
     * Checks if the supplier has any outstanding invoices; i.e. invoices that
     * are not cancelled and where the amount due has not been fully paid.
     *
     * @param supplierId the ID of the supplier
     * @return {@code true} if the supplier has at least one non-cancelled invoices with payment still owing;
     *         {@code false} if the supplier has no non-cancelled invoices, or all non-cancelled invoices
     *         have been paid in full.
     */
    public boolean supplierHasOutstandingInvoices(Integer supplierId) {
        List<Invoice> nonCancelledInvoices = getNonCancelledInvoicesForSupplier(supplierId);

        if (nonCancelledInvoices.isEmpty()) {
            logger.debug("Supplier {} has no outstanding invoices", supplierId);
            return false;
        }

        for (Invoice invoice : nonCancelledInvoices) {
            List<Payment> nonCancelledPayments = paymentService.getNonCancelledPaymentsForInvoice(invoice.getId());
            BigDecimal totalPaidForInvoice = BigDecimal.valueOf(0.0);

            for (Payment payment : nonCancelledPayments) {
                totalPaidForInvoice = totalPaidForInvoice.add(payment.getPaymentAmount());
            }

            // If the amount paid is less than the invoice amount
            // there is at least one outstanding invoice, so
            // there's no need to continue processing the others.
            if (totalPaidForInvoice.compareTo(invoice.getInvoiceAmount()) < 0) {
                logger.debug("Supplier {} has outstanding invoices", supplierId);
                return true;
            }
        }

        logger.debug("Supplier {} has no outstanding invoices", supplierId);
        return false;
    }


    /**
     * Update an invoice.
     *
     * @param invoiceId the ID of the invoice to update
     * @param updateRequest the update details
     * @return the updated {@link Invoice} entity
     * @throws InvoiceNotFoundException if no invoice exists with the supplied ID
     */
    public Invoice updateInvoice(Integer invoiceId, UpdateInvoiceRequest updateRequest) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        logger.info("Updating invoice {}", invoiceId);

        invoice.setSupplierInvoiceRef( updateRequest.getSupplierInvoiceRef());
        invoice.setInvoiceDate( updateRequest.getInvoiceDate());
        invoice.setDueDate( updateRequest.getDueDate());
        invoice.setInvoiceAmount(updateRequest.getInvoiceAmount());

        Invoice updatedInvoice = invoiceRepository.save(invoice);

        logger.info("Invoice {} successfully updated", invoiceId);
        return updatedInvoice;
    }


    /**
     * Cancel an invoice.
     *
     * @param invoiceId the ID of the invoice to cancel
     * @param cancellationRequest the cancellation details
     * @return {@code true} if the invoice successfully cancelled in this operation;
     *         {@code false} if the invoice was already cancelled
     * @throws InvoiceNotFoundException if no invoice exists with the supplied ID
     */
    public boolean cancelInvoice(Integer invoiceId,
                                 CancelInvoiceRequest cancellationRequest) {
        // Get existing invoice details.
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        logger.info("Attempting to cancel invoice {}", invoiceId);

        // Reject an already cancelled invoice.
        if (invoice.getCancelledAt() != null) {
            logger.warn("Invoice {} already cancelled", invoiceId);
            return false;
        }

        invoice.setCancelledAt(LocalDateTime.now());
        invoice.setCancellationReason(cancellationRequest.getCancellationReason());

        invoiceRepository.save(invoice);
        logger.info("Invoice {} successfully cancelled", invoiceId);

        return true;
    }
}
