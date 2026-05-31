package br.com.coupon.repository;

import br.com.coupon.domain.Coupon;
import br.com.coupon.domain.CouponRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({CouponRepositoryAdapter.class, CouponPersistenceMapperImpl.class})
@DisplayName("CouponRepository")
class CouponRepositoryTest {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private EntityManager entityManager;

    private Coupon savedCoupon;

    @BeforeEach
    void setUp() {
        Coupon coupon = Coupon.create(
            "PROMO1",
            "Teste de persistência",
            new BigDecimal("10.0"),
            LocalDateTime.now().plusDays(30),
            true
        );
        savedCoupon = couponRepository.save(coupon);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("deve encontrar cupom por ID")
    void shouldFindCouponById() {
        Optional<Coupon> result = couponRepository.findById(savedCoupon.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("PROMO1");
        assertThat(result.get().isDeleted()).isFalse();
    }

    @Test
    @DisplayName("não deve encontrar cupom deletado devido ao @SQLRestriction")
    void shouldNotFindDeletedCoupon() {
        savedCoupon.delete();
        couponRepository.save(savedCoupon);
        entityManager.flush();
        entityManager.clear();

        Optional<Coupon> result = couponRepository.findById(savedCoupon.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deve persistir cupom com ID gerado")
    void shouldGenerateIdOnSave() {
        Coupon coupon = Coupon.create(
            "CUPOM2",
            "Outro cupom",
            new BigDecimal("5.0"),
            LocalDateTime.now().plusDays(15),
            false
        );

        Coupon result = couponRepository.save(coupon);

        assertThat(result.getId()).isNotNull();
    }
}
