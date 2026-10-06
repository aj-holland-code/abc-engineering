package com.abcengineering.invoiceapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSupplierRequest {

    @NotBlank
    private String companyName;

    @NotBlank
    private String companyAddress;

    private String contactName;
    private String contactEmail;
    private String contactTelephone;
}
