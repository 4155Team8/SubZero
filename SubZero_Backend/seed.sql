USE subzero_db;

INSERT IGNORE INTO billingcycle (name, user_id) VALUES
    ('Daily',             NULL),
    ('Weekly',            NULL),
    ('Biweekly',          NULL),
    ('Monthly (30 days)', NULL),
    ('Yearly',            NULL),
    ('Custom',            NULL);

INSERT IGNORE INTO category (name, user_id) VALUES
    ('Streaming',      NULL),
    ('Social',         NULL),
    ('Financial',      NULL),
    ('Food',           NULL),
    ('Media Creation', NULL),
    ('Mental Health',  NULL),
    ('Fitness',        NULL),
    ('Education',      NULL),
    ('Productivity',   NULL),
    ('Gaming',         NULL),
    ('Custom',         NULL);

UPDATE users SET monthly_budget = 150.00 WHERE id = 1;

-- ── User 1 subscriptions ──────────────────────────────────────────────────────
-- category_id:      1=Streaming, 2=Social, 3=Financial, 5=Media Creation, 7=Fitness
-- billing_cycle_id: 3=Biweekly, 4=Monthly (30 days), 5=Yearly
INSERT INTO subscription (name, cost, user_id, category_id, billing_cycle_id, renewal_date) VALUES
    ('Netflix',               12.99, 1, 1, 4, '2026-05-22'),
    ('Spotify',                9.99, 1, 1, 4, '2026-05-22'),
    ('Amazon Prime',          14.99, 1, 1, 4, '2026-05-22'),
    ('Gym Membership',        35.00, 1, 7, 3, '2026-05-05'),
    ('Adobe Creative Cloud',  52.99, 1, 5, 5, '2027-04-21');

-- ── User 1 reminders ─────────────────────────────────────────────────────────
INSERT INTO reminders (user_id, name, subscription_id, reminder_date, description) VALUES
    (1, 'Netflix',              1, '2026-05-19', 'Your Netflix payment is coming up soon.'),
    (1, 'Spotify',              2, '2026-05-19', 'Spotify Premium renewal coming soon.'),
    (1, 'Amazon Prime',         3, '2026-05-19', 'Amazon Prime renewal coming soon.'),
    (1, 'Gym Membership',       4, '2026-05-02', 'Gym membership renewal coming soon.'),
    (1, 'Adobe Creative Cloud', 5, '2027-04-18', 'Adobe Creative Cloud annual renewal coming soon.');

-- ── User 1 monthly spend history (last 6 months) ─────────────────────────────
-- Reflects approximate monthly cost of active subscriptions per month
INSERT IGNORE INTO monthly_spend_history (user_id, year, month, total_spend) VALUES
    (1, 2025, 11, 37.97),   -- Nov: Netflix + Spotify + Amazon
    (1, 2025, 12, 37.97),   -- Dec: Netflix + Spotify + Amazon
    (1, 2026,  1, 72.97),   -- Jan: + Gym added
    (1, 2026,  2, 72.97),   -- Feb
    (1, 2026,  3, 72.97),   -- Mar
    (1, 2026,  4, 125.96);  -- Apr: + Adobe Creative Cloud