INSERT INTO customer
    (customer_id, email, password_hash, full_name, date_of_birth, phone, status)
VALUES
    (1001, 'aarav.sharma@example.com', '$2a$10$0/cQhY32hrmzpdYxWUJjl.8nNZ40rprG21Gtl18j7Dk5RFB1Ii5LS',
     'Aarav Sharma', DATE '1988-03-15', '+91-9000001001', 'ACTIVE'),
    (1002, 'meera.iyer@example.com', '$2a$10$0/cQhY32hrmzpdYxWUJjl.8nNZ40rprG21Gtl18j7Dk5RFB1Ii5LS',
     'Meera Iyer', DATE '1990-07-22', '+91-9000001002', 'ACTIVE'),
    (1003, 'rohan.verma@example.com', '$2a$10$0/cQhY32hrmzpdYxWUJjl.8nNZ40rprG21Gtl18j7Dk5RFB1Ii5LS',
     'Rohan Verma', DATE '1995-11-02', '+91-9000001003', 'ACTIVE');

SELECT setval(
    pg_get_serial_sequence('customer', 'customer_id'),
    (SELECT COALESCE(MAX(customer_id), 1) FROM customer),
    true
);

INSERT INTO customer_policy
    (policy_number, customer_id, product_id, start_date, maturity_date, premium_frequency,
     premium_amount, sum_insured, total_premium_payable, status)
SELECT 'HD-POL-1001-001', c.customer_id, p.product_id, CURRENT_DATE,
       (CURRENT_DATE + INTERVAL '240 months')::date, 'MONTHLY',
       1250.00, 5000000.00, 300000.00, 'ACTIVE'
FROM customer c
JOIN insurance_product p ON p.product_code = 'HD-TERM-001'
WHERE c.customer_id = 1001;

INSERT INTO customer_policy
    (policy_number, customer_id, product_id, start_date, maturity_date, premium_frequency,
     premium_amount, sum_insured, total_premium_payable, status)
SELECT 'HD-POL-1002-001', c.customer_id, p.product_id, CURRENT_DATE,
       (CURRENT_DATE + INTERVAL '12 months')::date, 'YEARLY',
       18000.00, 1000000.00, 18000.00, 'ACTIVE'
FROM customer c
JOIN insurance_product p ON p.product_code = 'HD-HEALTH-001'
WHERE c.customer_id = 1002;

WITH installment_seed (policy_number, installment_number, month_offset, amount, paid_amount, payment_status) AS (
    VALUES
        ('HD-POL-1001-001', 1, 0, 1250.00, 1250.00, 'PAID'),
        ('HD-POL-1001-001', 2, 1, 1250.00, 1250.00, 'PAID'),
        ('HD-POL-1001-001', 3, 2, 1250.00, 0.00, 'PENDING'),
        ('HD-POL-1002-001', 1, 0, 18000.00, 18000.00, 'PAID')
)
INSERT INTO premium_installment
    (policy_id, installment_number, due_date, grace_period_end_date,
     scheduled_amount, paid_amount, payment_status)
SELECT p.policy_id, s.installment_number,
       (CURRENT_DATE + s.month_offset * INTERVAL '1 month')::date,
       (CURRENT_DATE + s.month_offset * INTERVAL '1 month' + INTERVAL '15 days')::date,
       s.amount, s.paid_amount, s.payment_status
FROM installment_seed s
JOIN customer_policy p ON p.policy_number = s.policy_number;

WITH payment_seed (policy_number, installment_number, transaction_reference, amount_paid, payment_method) AS (
    VALUES
        ('HD-POL-1001-001', 1, 'HD-DEMO-PAY-1001-01', 1250.00, 'UPI'),
        ('HD-POL-1001-001', 2, 'HD-DEMO-PAY-1001-02', 1250.00, 'CARD'),
        ('HD-POL-1002-001', 1, 'HD-DEMO-PAY-1002-01', 18000.00, 'BANK_TRANSFER')
)
INSERT INTO premium_payment
    (installment_id, transaction_reference, payment_date, amount_paid, penalty_amount, payment_method)
SELECT i.installment_id, s.transaction_reference, CURRENT_TIMESTAMP,
       s.amount_paid, 0.00, s.payment_method
FROM payment_seed s
JOIN customer_policy p ON p.policy_number = s.policy_number
JOIN premium_installment i
    ON i.policy_id = p.policy_id
   AND i.installment_number = s.installment_number;

INSERT INTO ai_recommendation_request
    (customer_id, insurance_goals, preferred_term, preferred_policy_type, calculated_age,
     model_name, model_version, prompt_version)
SELECT c.customer_id, 'Long-term life protection and family security', 240, 'TERM',
       EXTRACT(YEAR FROM age(CURRENT_DATE, c.date_of_birth))::integer,
       'gpt-4o-mini', 'configured', 'v1'
FROM customer c
WHERE c.customer_id = 1001;

INSERT INTO ai_recommendation_request
    (customer_id, insurance_goals, preferred_term, preferred_policy_type, calculated_age,
     model_name, model_version, prompt_version)
SELECT c.customer_id, 'Long-term savings for future financial goals', 120, 'SAVINGS',
       EXTRACT(YEAR FROM age(CURRENT_DATE, c.date_of_birth))::integer,
       'gpt-4o-mini', 'configured', 'v1'
FROM customer c
WHERE c.customer_id = 1003;

WITH recommendation_seed (customer_id, insurance_goals, product_code, rank_order, recommendation_reason, suitability) AS (
    VALUES
        (1001, 'Long-term life protection and family security', 'HD-TERM-001', 1,
         'Provides long-term life cover for family financial security.', 'HIGH'),
        (1001, 'Long-term life protection and family security', 'HD-HEALTH-001', 2,
         'Adds health coverage alongside the primary life protection.', 'MEDIUM'),
        (1003, 'Long-term savings for future financial goals', 'HD-SAV-001', 1,
         'Supports disciplined savings over a long-term horizon.', 'HIGH')
)
INSERT INTO ai_recommendation_item
    (recommendation_request_id, product_id, rank_order, recommendation_reason, suitability)
SELECT r.recommendation_request_id, p.product_id, s.rank_order,
       s.recommendation_reason, s.suitability
FROM recommendation_seed s
JOIN ai_recommendation_request r
    ON r.customer_id = s.customer_id
   AND r.insurance_goals = s.insurance_goals
JOIN insurance_product p ON p.product_code = s.product_code;
