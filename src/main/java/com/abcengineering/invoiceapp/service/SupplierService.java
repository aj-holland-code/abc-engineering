package com.abcengineering.invoiceapp.service;

import com.abcengineering.invoiceapp.dto.CreateSupplierRequest;
import com.abcengineering.invoiceapp.dto.UpdateSupplierRequest;
import com.abcengineering.invoiceapp.exception.InvoiceNotFoundException;
import com.abcengineering.invoiceapp.exception.SupplierNotFoundException;
import com.abcengineering.invoiceapp.model.Invoice;
import com.abcengineering.invoiceapp.model.Supplier;
import com.abcengineering.invoiceapp.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final InvoiceService invoiceService;


    public SupplierService(SupplierRepository supplierRepository, InvoiceService invoiceService) {

        this.supplierRepository = supplierRepository;
        this.invoiceService = invoiceService;
    }


    /**
     * Create a supplier.
     *
     * @param request the details of supplier to be created
     * @return the created {@link Supplier} entity
     */
    public Supplier createSupplier(CreateSupplierRequest request) {
        Supplier supplier = new Supplier(request.getCompanyName(), request.getCompanyAddress());

        if (request.getContactName() != null) {
            supplier.setContactName(request.getContactName());
        }

        if (request.getContactEmail() != null) {
            supplier.setContactEmail(request.getContactEmail());
        }

        if (request.getContactTelephone() != null) {
            supplier.setContactTelephone(request.getContactTelephone());
        }

        supplier.setActive(true);
        return supplierRepository.save(supplier);
    }


    /**
     * Gets a supplier's details.
     *
     * @param supplierId the ID of the supplier to get
     * @return the {@link Supplier} entity associated with the supplied ID
     * @throws SupplierNotFoundException if no supplier exists with the supplied ID
     */
    public Supplier getSupplier(Integer supplierId) {
        return supplierRepository.findById(supplierId).orElseThrow(
                () -> new SupplierNotFoundException(supplierId));
    }

    /**
     * Gets details of all suppliers, active and inactive.
     *
     * @return a list of all invoices, or an empty list if no invoices exist
     */
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }


    /**
     * Updates supplier's details.
     *
     * @param supplierId the ID of the supplier to update
     * @return the updated {@link Supplier} entity associated with the supplied ID
     */
    public Supplier updateSupplier(Integer supplierId, UpdateSupplierRequest request) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));

        supplier.setCompanyName(request.getCompanyName());
        supplier.setCompanyAddress(request.getCompanyAddress());
        supplier.setContactName(request.getContactName());
        supplier.setContactEmail(request.getContactEmail());
        supplier.setContactTelephone(request.getContactTelephone());
        return supplierRepository.save(supplier);
    }


    /**
     * Deactivates a supplier.
     *
     * @param supplierId the ID of the supplier to be deactivated
     * @return {@code true} if the supplier has been successfully deactivated;
     *         {@code false} if the supplier cannot be deactivated due to outstanding invoices
     * @throws SupplierNotFoundException if no supplier exists with the supplied ID
     */
    public boolean deactivateSupplier(Integer supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));

        // A supplier cannot be deactivated while they have outstanding invoices
        if (invoiceService.supplierHasOutstandingInvoices(supplierId)) {
            return false;
        }

        supplier.setActive(false);
        supplierRepository.save(supplier);

        return true;
    }

    /**
     * Reactivates a supplier.
     *
     * @param supplierId the ID of the supplier to be deactivated
     * @throws SupplierNotFoundException if no supplier exists with the supplied ID
     */
    public void reactivateSupplier(Integer supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));

        supplier.setActive(true);
        supplierRepository.save(supplier);
    }
}
