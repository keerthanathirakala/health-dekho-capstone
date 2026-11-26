ALTER TABLE customer_policy
    ADD CONSTRAINT ck_customer_policy_date_range
        CHECK (maturity_date > start_date),
    ADD CONSTRAINT ck_customer_policy_premium_frequency
        CHECK (premium_frequency IN ('MONTHLY', 'QUARTERLY', 'HALF_YEARLY', 'YEARLY'));

ALTER TABLE premium_installment
    ADD CONSTRAINT ck_installment_grace_period
        CHECK (grace_period_end_date >= due_date),
    ADD CONSTRAINT ck_installment_paid_amount
        CHECK (paid_amount <= scheduled_amount);

ALTER TABLE ai_recommendation_item
    ADD CONSTRAINT ck_recommendation_item_suitability
        CHECK (suitability IN ('HIGH', 'MEDIUM', 'LOW'));

CREATE INDEX idx_customer_policy_product ON customer_policy(product_id);
CREATE INDEX idx_recommendation_item_product ON ai_recommendation_item(product_id);
