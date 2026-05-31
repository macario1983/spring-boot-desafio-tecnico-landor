package br.com.coupon.domain;

import br.com.coupon.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Coupon {

    private UUID id;
    private String code;
    private String description;
    private BigDecimal discountValue;
    private LocalDateTime expirationDate;
    private CouponStatus status;
    private boolean published;
    private boolean redeemed;
    private boolean deleted;

    private Coupon(String code, String description, BigDecimal discountValue, LocalDateTime expirationDate, boolean published) {
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.published = published;
        this.status = published ? CouponStatus.ACTIVE : CouponStatus.INACTIVE;
        this.redeemed = false;
        this.deleted = false;
    }

    public Coupon(UUID id, String code, String description, BigDecimal discountValue,
                  LocalDateTime expirationDate, CouponStatus status,
                  boolean published, boolean redeemed, boolean deleted) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.status = status;
        this.published = published;
        this.redeemed = redeemed;
        this.deleted = deleted;
    }

    public static Coupon create(String code, String description, BigDecimal discountValue, LocalDateTime expirationDate, boolean published) {
        validateDiscountValue(discountValue);
        validateExpirationDate(expirationDate);
        String sanitizedCode = sanitizeCode(code);
        return new Coupon(sanitizedCode, description, discountValue, expirationDate, published);
    }

    public void delete() {
        if (this.deleted) {
            throw new BusinessException("Cupom já foi excluído");
        }
        this.deleted = true;
        this.status = CouponStatus.DELETED;
    }

    private static String sanitizeCode(String code) {
        String sanitized = code.replaceAll("[^a-zA-Z0-9]", "");
        if (sanitized.length() != 6) {
            throw new BusinessException("Código do cupom deve ter exatamente 6 caracteres alfanuméricos após remoção de caracteres especiais");
        }
        return sanitized.toUpperCase();
    }

    private static void validateDiscountValue(BigDecimal discountValue) {
        if (discountValue == null || discountValue.compareTo(new BigDecimal("0.5")) < 0) {
            throw new BusinessException("Valor do desconto deve ser no mínimo 0.5");
        }
    }

    private static void validateExpirationDate(LocalDateTime expirationDate) {
        if (expirationDate == null || expirationDate.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Data de expiração não pode estar no passado");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public CouponStatus getStatus() {
        return status;
    }

    public boolean isPublished() {
        return published;
    }

    public boolean isRedeemed() {
        return redeemed;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
