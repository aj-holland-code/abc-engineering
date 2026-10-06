package com.abcengineering.invoiceapp.controller;

import com.abcengineering.invoiceapp.dto.CreateSupplierRequest;
import com.abcengineering.invoiceapp.dto.UpdateSupplierRequest;
import com.abcengineering.invoiceapp.model.Supplier;
import com.abcengineering.invoiceapp.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * Creates a supplier.
     *
     * @param createRequest the details of the supplier to create
     * @return the created supplier
     */
    @PostMapping
    public Supplier createSupplier(
           @Valid @RequestBody CreateSupplierRequest createRequest) {
        return supplierService.createSupplier(createRequest);
    }


    /**
     * Gets a supplier by ID.
     *
     * @param supplierId the ID of the supplier to retrieve
     * @return the requested supplier
     */
    @GetMapping("/{id}")
    public Supplier getSupplier(@PathVariable("id") Integer supplierId) {
        return supplierService.getSupplier(supplierId);
    }


    /**
     * Gets all suppliers.
     *
     * @return  a list of {@link Supplier} objects, or an empty list if none exist
     */
    @GetMapping
    public List<Supplier> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }


    /**
     * Updates a supplier.
     *
     * @return the updated supplier details.
     */
    @PutMapping("/{id}")
    public Supplier updateSupplier(
             @PathVariable("id") Integer supplierId,
             @Valid @RequestBody UpdateSupplierRequest updateRequest) {
        return supplierService.updateSupplier(supplierId, updateRequest);
    }


    /**
     * Deactivates a supplier.
     *
     * @param supplierId the ID of the supplier to be deactivated
     * @return {@code true} if the supplier is successfully deactivated in this operation;
     *         {@code false} if the supplier was already deactivated
     */
    @DeleteMapping("/{id}")
    public boolean deactivateSupplier(@PathVariable("id") Integer supplierId) {
        return supplierService.deactivateSupplier(supplierId);
    }


    /**
     * Reactivates a supplier.
     *
     * @param supplierId the ID of the supplier to be reactivated
     * @return {@code true} if the supplier is successfully reactivated
     */
    @PatchMapping("/{id}")
    public boolean reactivateSupplier(@PathVariable("id") Integer supplierId) {
        return supplierService.reactivateSupplier(supplierId);
    }
}
