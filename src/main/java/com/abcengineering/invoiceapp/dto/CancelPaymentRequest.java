package com.abcengineering.invoiceapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelPaymentRequest {

    @NotBlank
    private String cancellationReason;
}
