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
public class CreatePaymentRequest {

    @NotNull
    private Integer invoiceId;

    @NotNull
    private LocalDate paymentDate;

    @NotNull
    private BigDecimal paymentAmount;

    @NotBlank
    @Size(max = 30)
    private String paymentMethod;

    @NotBlank
    @Size(max = 50)
    private String paymentReference;
}
