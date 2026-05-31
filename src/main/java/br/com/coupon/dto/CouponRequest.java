package br.com.coupon.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponRequest(@NotBlank(message = "Código é obrigatório") String code,
                            @NotBlank(message = "Descrição é obrigatória") String description,
                            @NotNull(message = "Valor do desconto é obrigatório") @DecimalMin(value = "0.5", message = "Valor do desconto deve ser no mínimo 0.5") BigDecimal discountValue,
                            @NotNull(message = "Data de expiração é obrigatória") LocalDateTime expirationDate,
                            boolean published) {
}
