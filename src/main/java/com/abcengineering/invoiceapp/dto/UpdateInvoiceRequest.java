package com.abcengineering.invoiceapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
public class UpdateInvoiceRequest {

    @NotBlank
    @Size(max = 100)
    private String supplierInvoiceRef;

    @NotNull
    private LocalDate invoiceDate;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    private BigDecimal invoiceAmount;
}