package br.com.coupon.service;

import br.com.coupon.domain.Coupon;
import br.com.coupon.domain.CouponRepository;
import br.com.coupon.domain.CouponStatus;
import br.com.coupon.dto.CouponRequest;
import br.com.coupon.dto.CouponResponse;
import br.com.coupon.exception.BusinessException;
import br.com.coupon.mapper.CouponMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponService couponService;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("deve criar cupom e retornar resposta")
        void shouldCreateCouponAndReturnResponse() {
            CouponRequest request = new CouponRequest(
                "PROMO1", "Desc", new BigDecimal("10.0"),
                LocalDateTime.now().plusDays(30), true
            );
            Coupon savedCoupon = Coupon.create("PROMO1", "Desc", new BigDecimal("10.0"),
                LocalDateTime.now().plusDays(30), true);
            CouponResponse expectedResponse = new CouponResponse(
                UUID.randomUUID(), "PROMO1", "Desc", new BigDecimal("10.0"),
                savedCoupon.getExpirationDate(), CouponStatus.ACTIVE, true, false
            );

            when(couponRepository.save(any())).thenReturn(savedCoupon);
            when(couponMapper.toResponse(any())).thenReturn(expectedResponse);

            CouponResponse actual = couponService.create(request);

            assertThat(actual).isEqualTo(expectedResponse);
            verify(couponRepository).save(any());
            verify(couponMapper).toResponse(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve deletar cupom existente")
        void shouldDeleteExistingCoupon() {
            UUID id = UUID.randomUUID();
            Coupon coupon = Coupon.create("PROMO1", "Desc", new BigDecimal("10.0"),
                LocalDateTime.now().plusDays(30), true);

            when(couponRepository.findById(id)).thenReturn(Optional.of(coupon));

            couponService.delete(id);

            verify(couponRepository).findById(id);
            verify(couponRepository).save(any());
        }

        @Test
        @DisplayName("deve lançar exceção quando cupom não encontrado")
        void shouldThrowWhenCouponNotFound() {
            UUID id = UUID.randomUUID();
            when(couponRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.delete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cupom não encontrado");

            verify(couponRepository, never()).save(any());
        }
    }
}
