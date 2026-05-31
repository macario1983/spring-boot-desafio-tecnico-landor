package br.com.coupon.domain;

import br.com.coupon.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Coupon")
class CouponTest {

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("deve criar cupom publicado com status ACTIVE")
        void shouldCreatePublishedCoupon() {
            Coupon coupon = Coupon.create(
                "PROMO1",
                "Desconto de boas-vindas",
                new BigDecimal("10.0"),
                LocalDateTime.now().plusDays(30),
                true
            );

            assertThat(coupon.getCode()).isEqualTo("PROMO1");
            assertThat(coupon.getDescription()).isEqualTo("Desconto de boas-vindas");
            assertThat(coupon.getDiscountValue()).isEqualByComparingTo("10.0");
            assertThat(coupon.isPublished()).isTrue();
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.ACTIVE);
            assertThat(coupon.isRedeemed()).isFalse();
            assertThat(coupon.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("deve criar cupom não publicado com status INACTIVE")
        void shouldCreateUnpublishedCoupon() {
            Coupon coupon = Coupon.create(
                "CUPOM2",
                "Desconto inativo",
                new BigDecimal("5.0"),
                LocalDateTime.now().plusDays(15),
                false
            );

            assertThat(coupon.isPublished()).isFalse();
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.INACTIVE);
        }

        @Test
        @DisplayName("deve sanitizar código removendo caracteres especiais")
        void shouldSanitizeCode() {
            Coupon coupon = Coupon.create(
                "PRO-MO1",
                "Com caracteres especiais",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                true
            );

            assertThat(coupon.getCode()).isEqualTo("PROMO1");
            assertThat(coupon.getCode()).hasSize(6);
        }

        @Test
        @DisplayName("deve converter código para uppercase")
        void shouldUppercaseCode() {
            Coupon coupon = Coupon.create(
                "promo1",
                "Código minúsculo",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                true
            );

            assertThat(coupon.getCode()).isEqualTo("PROMO1");
        }

        @Test
        @DisplayName("deve lançar exceção quando código após sanitização não tem 6 caracteres")
        void shouldThrowWhenCodeNotSixCharsAfterSanitization() {
            assertThatThrownBy(() -> Coupon.create(
                "PRO-MO12",
                "Muito longo",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                true
            ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Código do cupom deve ter exatamente 6 caracteres alfanuméricos após remoção de caracteres especiais");
        }

        @Test
        @DisplayName("deve lançar exceção quando desconto é nulo")
        void shouldThrowWhenDiscountValueIsNull() {
            assertThatThrownBy(() -> Coupon.create(
                "PROMO1",
                "Sem desconto",
                null,
                LocalDateTime.now().plusDays(1),
                true
            ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Valor do desconto deve ser no mínimo 0.5");
        }

        @Test
        @DisplayName("deve lançar exceção quando desconto é menor que 0.5")
        void shouldThrowWhenDiscountBelowMinimum() {
            assertThatThrownBy(() -> Coupon.create(
                "PROMO1",
                "Desconto baixo",
                new BigDecimal("0.49"),
                LocalDateTime.now().plusDays(1),
                true
            ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Valor do desconto deve ser no mínimo 0.5");
        }

        @Test
        @DisplayName("deve aceitar desconto exatamente 0.5")
        void shouldAcceptDiscountExactlyAtMinimum() {
            Coupon coupon = Coupon.create(
                "PROMO1",
                "Desconto mínimo",
                new BigDecimal("0.5"),
                LocalDateTime.now().plusDays(1),
                true
            );

            assertThat(coupon.getDiscountValue()).isEqualByComparingTo("0.5");
        }

        @Test
        @DisplayName("deve lançar exceção quando data de expiração é nula")
        void shouldThrowWhenExpirationDateIsNull() {
            assertThatThrownBy(() -> Coupon.create(
                "PROMO1",
                "Sem expiração",
                new BigDecimal("10.0"),
                null,
                true
            ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Data de expiração não pode estar no passado");
        }

        @Test
        @DisplayName("deve lançar exceção quando data de expiração está no passado")
        void shouldThrowWhenExpirationDateInPast() {
            assertThatThrownBy(() -> Coupon.create(
                "PROMO1",
                "Expirado",
                new BigDecimal("10.0"),
                LocalDateTime.now().minusDays(1),
                true
            ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Data de expiração não pode estar no passado");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve marcar cupom como deletado e status DELETED")
        void shouldSoftDeleteCoupon() {
            Coupon coupon = Coupon.create(
                "PROMO1",
                "Para deletar",
                new BigDecimal("10.0"),
                LocalDateTime.now().plusDays(30),
                true
            );

            coupon.delete();

            assertThat(coupon.isDeleted()).isTrue();
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.DELETED);
        }

        @Test
        @DisplayName("deve lançar exceção ao deletar cupom já deletado")
        void shouldThrowWhenDeletingAlreadyDeleted() {
            Coupon coupon = Coupon.create(
                "PROMO1",
                "Já deletado",
                new BigDecimal("10.0"),
                LocalDateTime.now().plusDays(30),
                true
            );
            coupon.delete();

            assertThatThrownBy(coupon::delete)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cupom já foi excluído");
        }
    }
}
