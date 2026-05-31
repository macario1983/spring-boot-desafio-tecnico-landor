package br.com.coupon.mapper;

import br.com.coupon.domain.Coupon;
import br.com.coupon.dto.CouponResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    CouponResponse toResponse(Coupon coupon);
}
