package br.com.coupon.repository;

import br.com.coupon.domain.Coupon;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CouponPersistenceMapper {

    CouponEntity toEntity(Coupon coupon);

    Coupon toDomain(CouponEntity entity);
}
