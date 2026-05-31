package br.com.coupon.repository;

import br.com.coupon.domain.Coupon;
import br.com.coupon.domain.CouponRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class CouponRepositoryAdapter implements CouponRepository {

    private final CouponJpaRepository jpaRepository;
    private final CouponPersistenceMapper persistenceMapper;

    CouponRepositoryAdapter(CouponJpaRepository jpaRepository, CouponPersistenceMapper persistenceMapper) {
        this.jpaRepository = jpaRepository;
        this.persistenceMapper = persistenceMapper;
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity entity = persistenceMapper.toEntity(coupon);
        entity = jpaRepository.save(entity);
        return persistenceMapper.toDomain(entity);
    }

    @Override
    public Optional<Coupon> findById(UUID id) {
        return jpaRepository.findActiveById(id)
                .map(persistenceMapper::toDomain);
    }
}
