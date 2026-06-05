-- PostgreSQL database triggers for WASAC/REG Utility Billing System
-- Auto-installed on startup via PostgresTriggerInitializer

-- Trigger function: insert notification on bill generation
CREATE OR REPLACE FUNCTION notify_on_bill_insert()
RETURNS TRIGGER AS $$
DECLARE
    customer_name VARCHAR(150);
BEGIN
    SELECT full_names INTO customer_name FROM customers WHERE id = NEW.customer_id;

    INSERT INTO notifications (customer_id, bill_id, type, message, sent, created_at)
    VALUES (
        NEW.customer_id,
        NEW.id,
        'BILL_GENERATED',
        'Dear ' || customer_name || ', Your utility bill of ' || NEW.total_amount || ' FRW has been successfully processed.',
        FALSE,
        NOW()
    );

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bill_notification ON bills;
CREATE TRIGGER trg_bill_notification
    AFTER INSERT ON bills
    FOR EACH ROW
    EXECUTE FUNCTION notify_on_bill_insert();

-- Trigger function: on full payment, update bill status and notify customer
CREATE OR REPLACE FUNCTION notify_on_full_payment()
RETURNS TRIGGER AS $$
DECLARE
    customer_name VARCHAR(150);
    bill_total DECIMAL(14,2);
    bill_paid DECIMAL(14,2);
    bill_customer_id BIGINT;
    bill_id_val BIGINT;
BEGIN
    SELECT total_amount, amount_paid, customer_id, id
    INTO bill_total, bill_paid, bill_customer_id, bill_id_val
    FROM bills WHERE id = NEW.bill_id;

    -- Include the new payment row; bill.amount_paid may not be updated yet by the application
    IF (bill_paid + NEW.amount_paid) >= bill_total THEN
        UPDATE bills
        SET status = 'PAID',
            amount_paid = bill_paid + NEW.amount_paid,
            outstanding_balance = 0
        WHERE id = NEW.bill_id;

        SELECT full_names INTO customer_name FROM customers WHERE id = bill_customer_id;

        INSERT INTO notifications (customer_id, bill_id, type, message, sent, created_at)
        VALUES (
            bill_customer_id,
            bill_id_val,
            'BILL_PAID',
            'Dear ' || customer_name || ', Your utility bill of ' || bill_total || ' FRW has been successfully processed.',
            FALSE,
            NOW()
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_full_payment_notification ON payments;
CREATE TRIGGER trg_full_payment_notification
    AFTER INSERT ON payments
    FOR EACH ROW
    EXECUTE FUNCTION notify_on_full_payment();
