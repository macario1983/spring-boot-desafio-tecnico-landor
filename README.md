# Projeto Coupon

API REST para gestão de cupons — criação e exclusão lógica (soft delete).\
\
Arquitetura: **Domain-Driven Design tático** com modelo de domínio puro (sem anotações JPA) e modelo de persistência separado. O fluxo segue a estrutura de camadas: `controller → service → domain/repository`.

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.6 |
| Spring Data JPA | — (gerido pelo parent) |
| H2 Database | — (runtime, em memória) |
| MapStruct | 1.6.3 |
| SpringDoc OpenAPI | 3.0.2 |
| Maven Wrapper | — (empacotado no projeto) |

## Como rodar

```bash
# Compilar
./mvnw compile

# Executar
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Endpoints

### Criar cupom

```http
POST /coupons
Content-Type: application/json

{
  "code": "PROMO1",
  "description": "Desconto de boas-vindas",
  "discountValue": 10.0,
  "expirationDate": "2026-12-31T23:59:59",
  "published": true
}
```

**Resposta:** `201 Created`

```json
{
  "id": "a1b2c3d4-...",
  "code": "PROMO1",
  "description": "Desconto de boas-vindas",
  "discountValue": 10.0,
  "expirationDate": "2026-12-31T23:59:59",
  "status": "ACTIVE",
  "published": true,
  "redeemed": false
}
```

### Excluir cupom (soft delete)

```http
DELETE /coupons/{id}
```

**Resposta:** `204 No Content`

O cupom é marcado como `deleted = true` com status `DELETED` — não é removido do banco.

## Regras de negócio

| Regra | Local |
|---|---|
| Código deve ter exatamente 6 caracteres alfanuméricos (após remoção de caracteres especiais) | `Coupon.sanitizeCode()` |
| Código é convertido para uppercase automaticamente | `Coupon.sanitizeCode()` |
| Valor do desconto mínimo: 0.5 | `Coupon.validateDiscountValue()` |
| Data de expiração não pode estar no passado | `Coupon.validateExpirationDate()` |
| Cupom publicado nasce com status `ACTIVE`; não publicado, `INACTIVE` | Construtor de `Coupon` |
| Não é possível excluir um cupom já excluído | `Coupon.delete()` |
| DELETE é soft delete — marca `deleted = true`, status `DELETED` | `Coupon.delete()` |
| Consultas ignoram cupons deletados via `@SQLRestriction("deleted = false")` | `CouponEntity` |

## Estrutura de pacotes

```
br.com.coupon
├── CouponApplication.java        # Bootstrap Spring Boot
├── config/                       # Configurações (@Configuration)
├── controller/
│   └── CouponController.java     # REST: POST /coupons, DELETE /coupons/{id}
├── domain/
│   ├── Coupon.java               # POJO puro — regras de negócio, sem anotações JPA
│   ├── CouponStatus.java         # Enum: ACTIVE, INACTIVE, DELETED
│   └── CouponRepository.java     # Interface do domínio — contrato do repositório
├── dto/
│   ├── CouponRequest.java        # Record — entrada validada
│   └── CouponResponse.java       # Record — saída
├── exception/
│   ├── BusinessException.java    # RuntimeException para erros de negócio
│   ├── ErrorResponse.java        # Record — corpo padronizado de erro
│   └── GlobalExceptionHandler.java # @ControllerAdvice
├── mapper/
│   └── CouponMapper.java         # MapStruct — Coupon → CouponResponse
├── repository/
│   ├── CouponEntity.java         # @Entity JPA — modelo de persistência
│   ├── CouponJpaRepository.java  # Spring Data JPA — (package-private)
│   ├── CouponRepositoryAdapter.java # Implementa CouponRepository do domínio
│   └── CouponPersistenceMapper.java # MapStruct — Coupon ↔ CouponEntity
└── service/
    └── CouponService.java        # Casos de uso (depende só do domínio)
```

## Respostas de erro

Todos os erros seguem o formato:

```json
{
  "status": 422,
  "error": "Erro de negócio",
  "message": "Cupom já foi excluído",
  "timestamp": "2026-05-31T14:00:00"
}
```

| Cenário | HTTP | `error` |
|---|---|---|
| Erro de negócio (`BusinessException`) | 422 | `Erro de negócio` |
| Validação de entrada (`@Valid` falhou) | 400 | `Erro de validação` |
| Erro inesperado | 500 | `Erro interno do servidor` |

## Banco de dados

H2 em memória. Console disponível em:

```
http://localhost:8080/h2-console
```

Configuração em `application.yml`:
- **JDBC URL:** `jdbc:h2:mem:coupondb`
- **Usuário:** `sa`
- **Senha:** *(vazia)*

O DDL é gerado automaticamente via `spring.jpa.hibernate.ddl-auto: update`.

## Documentação da API

Swagger UI disponível em:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI spec em `/api-docs`.

## Health check

Actuator expõe health em:

```
http://localhost:8080/actuator/health
```

## Decisões de design

- **Records para DTOs** — `CouponRequest` e `CouponResponse` são records Java 21, imutáveis e concisos.
- **DDD tático — repositório do domínio** — A interface `CouponRepository` vive no pacote `domain` e opera sobre objetos `Coupon` puros. A implementação `CouponRepositoryAdapter` fica no pacote `repository`, encapsulando `CouponJpaRepository` (Spring Data) + `CouponPersistenceMapper` (MapStruct). O service só enxerga a interface do domínio.
- **DDD tático — domínio separado da persistência** — `Coupon` é um POJO puro sem anotações JPA; `CouponEntity` existe exclusivamente no pacote `repository` como modelo de persistência. `CouponPersistenceMapper` (MapStruct) faz a conversão bidirecional.
- **Factory method estático** — `Coupon.create()` encapsula validações e sanitização antes de instanciar, em vez de deixar regras espalhadas no service.
- **Mensagens em português** — validações e exceções retornam mensagens em pt-BR.
- **MapStruct** — gera o código de mapeamento entidade → DTO em tempo de compilação, sem reflection.
- **Soft delete com `boolean` primitivo** — `deleted`, `published` e `redeemed` são `boolean` (não `Boolean`). Começam como `false` por padrão, dispensando inicialização manual e evitando `NullPointerException`.
- **`findByIdAndDeletedFalse`** — queries ignoram registros deletados sem precisar de `@Where` ou filtro global.
