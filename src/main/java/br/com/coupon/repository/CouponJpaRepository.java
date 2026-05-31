package br.com.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface CouponJpaRepository extends JpaRepository<CouponEntity, UUID> {

    @Query("SELECT e FROM CouponEntity e WHERE e.id = :id AND e.deleted = false")
    Optional<CouponEntity> findActiveById(@Param("id") UUID id);
}
