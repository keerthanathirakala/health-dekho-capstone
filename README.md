# Health Dekho Insurance

Spring Boot 3.5.7 and React application for policy registration, premium payment tracking and AI-assisted product recommendations. See [BMAD](BMAD.md) for workflows, architecture and data.

## Prerequisites

- Java 21 and Maven 3.9+
- Node.js compatible with the installed Vite version (Node 20.19+ or 22.12+) and npm
- PostgreSQL with permission to create a database and run Flyway migrations
- AWS credentials with Bedrock Converse/model invocation permissions and access to the configured model and its cross-region inference profile. Check model availability and access in your AWS account; charges may apply.

## Setup

1. Create a PostgreSQL database, for example:

   ```sql
   CREATE DATABASE healthdekho;
   ```

2. Set environment variables in the backend process. Example in PowerShell (replace placeholders; do not commit credentials):

   ```powershell
   $env:DB_URL = 'jdbc:postgresql://localhost:5432/healthdekho'
   $env:DB_USERNAME = 'postgres'
   $env:DB_PASSWORD = '<database password>'
   $env:JWT_SECRET = '<strong secret of at least 32 bytes>'
   $env:AWS_ACCESS_KEY_ID = '<access key>'
   $env:AWS_SECRET_ACCESS_KEY = '<secret key>'
   # For temporary credentials only:
   $env:AWS_SESSION_TOKEN = '<session token>'
   ```

   Omit `AWS_SESSION_TOKEN` for long-lived credentials. The backend defaults to port 8080 and frontend origin `http://localhost:5173`; override using `SERVER_PORT` and `FRONTEND_ORIGIN`. The database defaults are for local development only. Replace the default JWT secret outside local development.

3. Start the backend from `backend`:

   ```powershell
   mvn spring-boot:run
   ```

   Flyway creates the schema and applies the existing product and demo data migrations on startup. These migrations also create demo customer accounts; this project does not provide customer signup. Use a valid existing customer's email and password, or provision a customer with a BCrypt password hash in your local database. Do not edit an already-applied Flyway migration.

4. In a separate terminal, start the frontend:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

   Open `http://localhost:5173`. The frontend defaults to `http://localhost:8080`; set `VITE_API_URL` before starting Vite if the backend URL differs.

The AI model is `us.anthropic.claude-sonnet-4-6`, configured under `spring.ai.bedrock.converse.chat.options.model`. The service uses Spring AI's `ChatClient`; the AWS access key, secret key and optional session token come from environment variables. Provider access, IAM permissions, inference profile availability, valid credentials, and network connectivity are required for an actual recommendation. Check server logs and AWS account configuration if the provider fails.

## Postman collection outline

Set collection variables `baseUrl` = `http://localhost:8080`, `token`, `policyId`, `productId`, and `installmentId`. Send `Authorization: Bearer {{token}}` on all requests except login and `Content-Type: application/json` on POSTs.

1. **Login**: `POST {{baseUrl}}/auth/login` with `{"email":"<customer email>","password":"<password>"}`. Copy the response `token` to the collection variable.
2. **Dashboard**: `GET {{baseUrl}}/dashboard`.
3. **Available products**: `GET {{baseUrl}}/products/available`. Copy a `productId`.
4. **Register policy**: `POST {{baseUrl}}/policies/register` with `{"productId":1}` using an eligible product ID. Copy the returned `policy.policyId`.
5. **My policies**: `GET {{baseUrl}}/policies/my`.
6. **Search policies**: `GET {{baseUrl}}/policies/search?policyName=health&page=0&size=10&sort=policyNumber,asc`. Optional filters: `policyNumber`, `policyName`, `policyType`, `status`; combine filters to narrow results. Supported sorts: `createdAt`, `startDate`, `maturityDate`, `policyNumber`, `status`, `product.policyName`. Response contains `content`, `page`, `size`, `totalElements`, `totalPages`. Pages start at zero; size is limited to 100. Existing `sortBy` and `direction` parameters remain supported.
7. **Installments**: `GET {{baseUrl}}/installments/policy/{{policyId}}`. Copy an unpaid `installmentId`.
8. **Record payment**: `POST {{baseUrl}}/payments/pay` with `{"installmentId":1,"amount":100.00,"paymentMethod":"UPI"}` using the installment ID and an amount no greater than the remaining principal. The response includes principal as `amountPaid` and a separate `penaltyAmount`. A 2% penalty on the scheduled amount is applied once per installment on the first payment after the grace period, rounded half-up to two decimals. The demo records the transaction and penalty; it does not charge a payment instrument.
9. **Payment history**: `GET {{baseUrl}}/payments/history` or `GET {{baseUrl}}/payments/policy/{{policyId}}`.
10. **AI recommendations**: `POST {{baseUrl}}/recommendations/generate` with `{"dateOfBirth":"1988-03-15","insuranceGoals":"Family protection","preferredTerm":240,"preferredPolicyType":"TERM"}`. Use the authenticated customer's actual date of birth. `preferredTerm` is required and in months; `preferredPolicyType` may be omitted. Results are restricted to eligible catalog products.

## Checks

From `backend`, run `mvn test` with AWS environment variables set (tests use an in-memory database and do not call Bedrock). From `frontend`, run `npm run build` and `npm run lint`.
