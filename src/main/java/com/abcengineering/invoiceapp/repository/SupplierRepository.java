package com.abcengineering.invoiceapp.repository;

import com.abcengineering.invoiceapp.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository
        <Supplier, Integer> {

}
