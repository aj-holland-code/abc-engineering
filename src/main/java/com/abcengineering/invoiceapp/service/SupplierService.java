package com.abcengineering.invoiceapp.service;

import com.abcengineering.invoiceapp.dto.CreateSupplierRequest;
import com.abcengineering.invoiceapp.dto.UpdateSupplierRequest;
import com.abcengineering.invoiceapp.exception.SupplierNotFoundException;
import com.abcengineering.invoiceapp.model.Supplier;
import com.abcengineering.invoiceapp.repository.SupplierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final InvoiceService invoiceService;

    private static final Logger logger
            = LoggerFactory.getLogger(SupplierService.class);


    public SupplierService(SupplierRepository supplierRepository, InvoiceService invoiceService) {

        this.supplierRepository = supplierRepository;
        this.invoiceService = invoiceService;
    }


    /**
     * Create a supplier.
     *
     * @param supplierRequest the details of supplier to be created
     * @return the created {@link Supplier} entity
     */
    public Supplier createSupplier(CreateSupplierRequest supplierRequest) {
        logger.info("Creating supplier '{}'", supplierRequest.getCompanyName());

        Supplier supplier = new Supplier(supplierRequest.getCompanyName(), supplierRequest.getCompanyAddress());

        if (supplierRequest.getContactName() != null) {
            supplier.setContactName(supplierRequest.getContactName());
        }

        if (supplierRequest.getContactEmail() != null) {
            supplier.setContactEmail(supplierRequest.getContactEmail());
        }

        if (supplierRequest.getContactTelephone() != null) {
            supplier.setContactTelephone(supplierRequest.getContactTelephone());
        }

        supplier.setActive(true);
        Supplier savedSupplier = supplierRepository.save(supplier);

        logger.info("Supplier successfully created with ID {}", savedSupplier.getId());
        return savedSupplier;
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
     * @param updateRequest the updated details for the supplier
     * @return the updated {@link Supplier} entity associated with the supplied ID
     */
    public Supplier updateSupplier(Integer supplierId, UpdateSupplierRequest updateRequest) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(supplierId));

        logger.info("Updating supplier {}", supplierId);

        supplier.setCompanyName(updateRequest.getCompanyName());
        supplier.setCompanyAddress(updateRequest.getCompanyAddress());
        supplier.setContactName(updateRequest.getContactName());
        supplier.setContactEmail(updateRequest.getContactEmail());
        supplier.setContactTelephone(updateRequest.getContactTelephone());
        Supplier updatedSupplier = supplierRepository.save(supplier);

        logger.info("Supplier {} updated successfully", supplierId);
        return updatedSupplier;
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

        logger.info("Attempting to deactivate supplier {}", supplierId);

        // A supplier cannot be deactivated while they have outstanding invoices
        if (invoiceService.supplierHasOutstandingInvoices(supplierId)) {
            logger.warn("Supplier {} cannot be deactivated due to outstanding invoices", supplierId);
            return false;
        }

        supplier.setActive(false);
        supplierRepository.save(supplier);
        logger.info("Supplier {} successfully deactivated", supplierId);
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

        logger.info("Attempting to reactivate supplier {}", supplierId);

        supplier.setActive(true);
        supplierRepository.save(supplier);
        logger.info("Supplier {} successfully reactivated", supplierId);
    }
}
