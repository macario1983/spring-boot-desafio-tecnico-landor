package br.com.coupon.controller;

import br.com.coupon.domain.Coupon;
import br.com.coupon.domain.CouponRepository;
import br.com.coupon.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CouponController")
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Nested
    @DisplayName("POST /coupons")
    class Create {

        @Test
        @DisplayName("deve retornar 201 com cupom criado")
        void shouldReturn201WhenValid() throws Exception {
            String body = """
                {
                    "code": "PROMO1",
                    "description": "Desc",
                    "discountValue": 10.0,
                    "expirationDate": "2099-12-31T23:59:59",
                    "published": true
                }
                """;

            mockMvc.perform(post("/coupons")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("PROMO1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("deve retornar 400 quando campos obrigatórios ausentes")
        void shouldReturn400WhenMissingRequiredFields() throws Exception {
            String body = """
                {
                    "code": "",
                    "description": "",
                    "discountValue": 0.5,
                    "expirationDate": "2099-12-31T23:59:59",
                    "published": true
                }
                """;

            mockMvc.perform(post("/coupons")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Erro de validação"));
        }

        @Test
        @DisplayName("deve retornar 400 quando desconto abaixo do mínimo")
        void shouldReturn400WhenDiscountBelowMinimum() throws Exception {
            String body = """
                {
                    "code": "PROMO1",
                    "description": "Desc",
                    "discountValue": 0.1,
                    "expirationDate": "2099-12-31T23:59:59",
                    "published": true
                }
                """;

            mockMvc.perform(post("/coupons")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Erro de validação"));
        }
    }

    @Nested
    @DisplayName("DELETE /coupons/{id}")
    class Delete {

        private UUID existingId;

        @BeforeEach
        void setUp() {
            Coupon coupon = Coupon.create(
                "DELTE1", "Para deletar", new BigDecimal("10.0"),
                LocalDateTime.now().plusDays(30), true
            );
            existingId = couponRepository.save(coupon).getId();
        }

        @Test
        @DisplayName("deve retornar 204 ao deletar cupom existente")
        void shouldReturn204WhenDeletingExisting() throws Exception {
            mockMvc.perform(delete("/coupons/{id}", existingId))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("deve retornar 422 ao deletar cupom já deletado")
        void shouldReturn422WhenCouponAlreadyDeleted() throws Exception {
            couponService.delete(existingId);

            mockMvc.perform(delete("/coupons/{id}", existingId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Erro de negócio"));
        }

        @Test
        @DisplayName("deve retornar 422 ao deletar cupom inexistente")
        void shouldReturn422WhenCouponNotFound() throws Exception {
            UUID randomId = UUID.randomUUID();

            mockMvc.perform(delete("/coupons/{id}", randomId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Erro de negócio"))
                .andExpect(jsonPath("$.message").value("Cupom não encontrado"));
        }
    }
}
