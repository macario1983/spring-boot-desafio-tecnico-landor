package br.com.coupon.service;

import br.com.coupon.domain.Coupon;
import br.com.coupon.domain.CouponRepository;
import br.com.coupon.dto.CouponRequest;
import br.com.coupon.dto.CouponResponse;
import br.com.coupon.exception.BusinessException;
import br.com.coupon.mapper.CouponMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public CouponService(CouponRepository couponRepository, CouponMapper couponMapper) {
        this.couponRepository = couponRepository;
        this.couponMapper = couponMapper;
    }

    @Transactional
    public CouponResponse create(CouponRequest request) {
        Coupon coupon = Coupon.create(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published()
        );
        coupon = couponRepository.save(coupon);
        return couponMapper.toResponse(coupon);
    }

    @Transactional
    public void delete(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cupom não encontrado"));
        coupon.delete();
        couponRepository.save(coupon);
    }
}
