package br.com.coupon.dto;

import br.com.coupon.domain.CouponStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        String description,
        BigDecimal discountValue,
        LocalDateTime expirationDate,
        CouponStatus status,
        boolean published,
        boolean redeemed
) {
}
