package com.abcengineering.invoiceapp.controller;

import com.abcengineering.invoiceapp.dto.CancelInvoiceRequest;
import com.abcengineering.invoiceapp.dto.CreateInvoiceRequest;
import com.abcengineering.invoiceapp.dto.UpdateInvoiceRequest;
import com.abcengineering.invoiceapp.model.Invoice;
import com.abcengineering.invoiceapp.model.Supplier;
import com.abcengineering.invoiceapp.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {

        this.invoiceService = invoiceService;
    }

    /**
     * Creates an invoice.
     *
     * @param invoiceRequest the details of the invoice to create
     * @return the created invoice
     */
    @PostMapping
    public Invoice createInvoice(@Valid @RequestBody CreateInvoiceRequest invoiceRequest) {

        return invoiceService.createInvoice(invoiceRequest);
    }


    /**
     * Gets an invoice by ID.
     *
     * @param invoiceId the ID of the invoice to retrieve
     */
    @GetMapping("/{id}")
    public Invoice getInvoice(@PathVariable("id") Integer invoiceId) {

        return invoiceService.getInvoice(invoiceId);
    }


    /**
     * Gets all invoices.
     *
     * @return  a list of {@link Invoice} objects, or an empty list if none exist
     */
    @GetMapping
    public List<Invoice> getAllInvoices() {

        return invoiceService.getAllInvoices();
    }



    /**
     * Updates a invoice.
     *
     * @return the updated invoice details.
     */
    @PutMapping("/{id}")
    public Invoice updateInvoice(@PathVariable("id") Integer invoiceId,
                                 @Valid @RequestBody UpdateInvoiceRequest updateRequest) {

        return invoiceService.updateInvoice(invoiceId, updateRequest);
    }


    /**
     * Cancels an invoice.
     *
     * @param invoiceId the ID of the invoice to be cancelled
     * @param cancelRequest the details of the cancellation
     * @return {@code true} if the invoice was successfully cancelled in this operation;
     *         {@code false} if the invoice was already cancelled
     */
    @PatchMapping("/{id}")
    public boolean cancelInvoice(@PathVariable("id") Integer invoiceId,
                                 @Valid @RequestBody CancelInvoiceRequest cancelRequest) {

        return invoiceService.cancelInvoice(invoiceId, cancelRequest);
    }
}
