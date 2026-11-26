# Health Dekho: Business Model, Architecture and Data

## Business Model

Health Dekho is an insurance management application that helps customers discover eligible insurance products, register policies, track premiums and obtain AI-assisted recommendations.

Objectives:
- Give customers a single dashboard for their policies and premium obligations.
- Make eligible products easier to find and register.
- Record installment payments, including late penalties.
- Help customers compare eligible products using recommendations; recommendations are informational, not financial advice.

Core workflows:
1. A customer signs in and views their dashboard. They browse eligible products and register a product, creating a customer policy and installment schedule. "Registration" refers to policy registration; there is no customer signup endpoint.
2. The customer selects a policy and records payments against its installments. Partial payments are supported. The first payment after the grace period incurs a separate 2% penalty on the scheduled amount, rounded to two decimal places. This application records payments; it does not integrate with a payment gateway.
3. The customer supplies their date of birth, goals, preferred term and optional policy type. The service sends eligible product information to AWS Bedrock through Spring AI, validates the returned choices against the catalog and records the request and results.
4. The customer searches their own registered policies by name or number, with pagination and sorting.

## Architecture

```text
React frontend (Vite, React Router)
        |
        | HTTPS/HTTP JSON, JWT bearer token
        v
Spring Security (JWT, CORS)
        |
        v
Spring Boot controllers
        |
        v
Services (eligibility, registration, payments, recommendations)
        |                                |
        v                                v
JPA repositories                 Spring AI ChatClient
        |                                |
        v                                v
PostgreSQL <--- Flyway             AWS Bedrock Converse
```

The backend uses Spring Boot 3.5.7, JPA and Flyway. Controllers validate requests; services enforce customer ownership and business rules. Payment updates lock the installment row inside a transaction, preventing concurrent payments from applying the same penalty twice. The JWT filter authenticates requests and CORS permits the configured frontend origin. Spring AI sends recommendation context to Bedrock in `us-east-1`; AWS credentials and model access must be configured separately. Recommendation requests may remain recorded without items if the provider call fails.

## Data

Flyway migrations define the schema and initial product/demo data. Key tables are:

| Table | Purpose and relationships |
| --- | --- |
| `customer` | Customer identity, contact, date of birth, status and password hash. |
| `insurance_product` | Product catalog: type, term, frequency, premium, coverage and age eligibility. |
| `customer_policy` | A customer's registered product and policy terms; references `customer` and `insurance_product`. |
| `premium_installment` | Scheduled premium, due date, grace-period end, principal paid and status; references `customer_policy`. |
| `premium_payment` | Transaction reference, timestamp, principal paid, separate penalty and method; references `premium_installment`. |
| `ai_recommendation_request` | Customer goals, preferences, calculated age and model/prompt metadata; references `customer`. |
| `ai_recommendation_item` | Ranked suggested product, reason and suitability; references a recommendation request and product. |

`amount_paid` is principal only; `penalty_amount` is separate. Their sum is the amount recorded for that payment. The due date is not itself the penalty threshold: the penalty applies only after `grace_period_end_date`, using the backend server's local date.
