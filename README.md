# nimbusdesk-api-gateway

Edge API Gateway for the [NimbusDesk](https://github.com/eliangilsierra/nimbusdesk-infra) system: routes every external request to the right backend service (resolved via Eureka) and validates the JWT issued by `nimbusdesk-identity-service` before letting a request through (see [ADR-0002](https://github.com/eliangilsierra/nimbusdesk-infra/blob/develop/adr/0002-validacion-jwt-en-el-gateway.md)).

## Stack

- Java 17 · Spring Boot 3.2.4 · Spring Cloud Gateway (WebFlux) 2023.0.1
- `spring-cloud-starter-netflix-eureka-client`
- `jjwt` for JWT verification

## Routes

| Path prefix | Forwarded to |
|---|---|
| `/api/identity/**` | `nimbusdesk-identity-service` |
| `/api/bookings/**` | `nimbusdesk-booking-service` |
| `/api/partner-bridge/**` | `nimbusdesk-partner-bridge-service` |

Public (no JWT required): `/api/identity/auth/login`, `/api/identity/auth/register`, `/actuator/health` — everything else requires `Authorization: Bearer <token>`.

## Configuration

| Env var | Default | Purpose |
|---|---|---|
| `EUREKA_URI` | `http://localhost:8761/eureka/` | Discovery server |
| `JWT_SECRET` | dev placeholder in `application.yml` | Must match the secret used by `nimbusdesk-identity-service` to sign tokens |

## Run locally

Requires `nimbusdesk-discovery-server` running first.

```bash
mvn spring-boot:run
```

Or with Docker:

```bash
docker build -t nimbusdesk-api-gateway .
docker run -p 8080:8080 -e EUREKA_URI=http://host.docker.internal:8761/eureka/ nimbusdesk-api-gateway
```

## How it fits in

Part of the NimbusDesk v1 system. See [`nimbusdesk-infra`](https://github.com/eliangilsierra/nimbusdesk-infra) for the full architecture and its `docker-compose.yml` to run the whole stack together.
