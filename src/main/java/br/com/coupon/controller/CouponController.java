package br.com.coupon.controller;

import br.com.coupon.dto.CouponRequest;
import br.com.coupon.dto.CouponResponse;
import br.com.coupon.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }
}