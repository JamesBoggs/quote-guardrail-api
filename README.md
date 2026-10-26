# Quote Guardrail API

A small REST service in Java and Spring Boot that checks a sales quote against pricing policy and returns a decision: **APPROVE**, **ROUTE_TO_VP** or **BLOCK**, with the reasons.

It is the approval step from [Margin Retention](https://jamesboggs.online/marginguard), rebuilt as a standalone Java service. I built it to get up to speed on Spring Boot; my production work has been in Python and SQL.

[![CI](https://github.com/JamesBoggs/quote-guardrail-api/actions/workflows/ci.yml/badge.svg)](https://github.com/JamesBoggs/quote-guardrail-api/actions/workflows/ci.yml)

**Live demo:** [quote-guardrail-api-production.up.railway.app](https://quote-guardrail-api-production.up.railway.app) (opens the interactive API docs: pick an endpoint, click **Try it out**, then **Execute**)

## The rules

| Check | Result |
|---|---|
| Any line priced below cost | BLOCK |
| Quote gross margin below the hard floor (22%) | BLOCK |
| Quote gross margin below the margin floor (28%) | ROUTE_TO_VP |
| Any line discount above the customer tier's cap (Standard 10%, Preferred 15%, Key 20%) | ROUTE_TO_VP |
| Otherwise | APPROVE |

Thresholds live in `application.yml`, so policy changes don't need a code change. Every decision is saved, so there is an audit trail.

## Endpoints

| Method | Path | What it does |
|---|---|---|
| POST | `/api/quotes/evaluate` | Price a quote and return the decision |
| GET | `/api/quotes` | The 20 most recent decisions |
| GET | `/api/quotes/{id}` | One past decision |
| GET | `/api/products?line=` | Product catalog, optionally by line |
| GET | `/api/products/{sku}` | One product |
| GET | `/api/customers` | Customers and their tiers |
| GET | `/api/policy` | Current floors and caps |

Interactive docs (Swagger UI): `/swagger-ui/index.html`. The root URL redirects there.

### Example

```bash
curl -X POST http://localhost:8080/api/quotes/evaluate \
  -H "Content-Type: application/json" \
  -d '{"customerId":"brightline","lines":[{"sku":"SKU-110","quantity":4,"discountPct":15}]}'
```

```json
{
  "quoteId": 1,
  "customerId": "brightline",
  "customerTier": "PREFERRED",
  "decision": "ROUTE_TO_VP",
  "reasons": ["Quote margin of 25.6% is below the floor of 28%."],
  "revenue": 2312.00,
  "cost": 1720.00,
  "grossMargin": 592.00,
  "marginPct": 25.6,
  "lines": [{ "sku": "SKU-110", "name": "Condensing unit, 3-ton", "quantity": 4,
              "listPrice": 680.00, "discountPct": 15, "netPrice": 578.00,
              "revenue": 2312.00, "cost": 1720.00, "marginPct": 25.6 }]
}
```

Try 8% for an APPROVE and 25% for a BLOCK.

## How it's built

```
controller/   REST endpoints (@RestController)          -> JSON in and out
service/      GuardrailService: loads data, saves decisions
rules/        GuardrailRules: the pricing logic, plain Java, no Spring
repository/   Spring Data JPA interfaces                 -> SQL generated for you
model/        @Entity classes (tables) and enums
dto/          Request and response records
exception/    @RestControllerAdvice: errors as clean JSON (404, 400)
config/       GuardrailPolicy: thresholds bound from application.yml
```

- **Java 21, Spring Boot 3.5**, Spring Web, Spring Data JPA, Bean Validation
- **H2** in-memory database, seeded from `data.sql` at startup (demo HVAC catalog, not real prices)
- **BigDecimal** for all money, so there are no floating-point rounding errors
- **Tests:** JUnit unit tests on the rules, plus MockMvc tests that call the real endpoints

## Run it locally

Requires Java 21 and Maven (IntelliJ IDEA Community bundles both).

```bash
mvn spring-boot:run      # starts on http://localhost:8080
mvn test                 # runs the tests
```

Or with Docker:

```bash
docker build -t quote-guardrail-api .
docker run -p 8080:8080 quote-guardrail-api
```

## Deploy (Railway)

1. In Railway: **New Project → Deploy from GitHub repo**, pick this repo. Railway finds the `Dockerfile`; `railway.json` sets the health check.
2. Open the service → **Settings → Networking → Generate Domain** to get a public `*.up.railway.app` URL.
3. Optional, to keep usage low: **Settings → Serverless** (the app sleeps when idle and wakes on the next request).

When generating the domain, set the target port to 8080.

The app reads Railway's `PORT` variable automatically (`server.port: ${PORT:8080}`), and the JVM is capped to fit the free plan's 0.5 GB of memory.
