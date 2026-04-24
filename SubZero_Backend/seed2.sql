USE subzero_db;

-- ── User 2 budget ─────────────────────────────────────────────────────────────
UPDATE users SET monthly_budget = 200.00 WHERE id = 2;

-- ── User 2 subscriptions ──────────────────────────────────────────────────────
-- Global billing/category IDs are the same as seeded in seed.sql
-- category_id:      1=Streaming, 2=Social, 3=Financial, 5=Media Creation, 7=Fitness
-- billing_cycle_id: 3=Biweekly, 4=Monthly (30 days), 5=Yearly
INSERT INTO subscription (name, cost, user_id, category_id, billing_cycle_id, renewal_date) VALUES
    ('Netflix',               12.99, 2, 1, 4, '2026-05-10'),
    ('Spotify',                9.99, 2, 1, 4, '2026-05-20'),
    ('Amazon Prime',          14.99, 2, 1, 4, '2026-05-18'),
    ('Gym Membership',        35.00, 2, 7, 3, '2026-05-05'),
    ('Adobe Creative Cloud',  52.99, 2, 5, 5, '2027-04-18');

-- ── User 2 reminders ─────────────────────────────────────────────────────────
-- subscription IDs for user 2 start at 6 (after user 1's 5 subs)
INSERT INTO reminders (name, subscription_id, reminder_date, description) VALUES
    ('Netflix',              6,  '2026-05-07', 'Your Netflix payment is coming up soon.'),
    ('Spotify',              7,  '2026-05-17', 'Spotify Premium renewal coming soon.'),
    ('Amazon Prime',         8,  '2026-05-15', 'Amazon Prime renewal coming soon.'),
    ('Gym Membership',       9,  '2026-05-02', 'Gym membership renewal coming soon.'),
    ('Adobe Creative Cloud', 10, '2027-04-15', 'Adobe Creative Cloud annual renewal coming soon.');

-- ── User 2 monthly spend history (last 6 months) ─────────────────────────────
INSERT IGNORE INTO monthly_spend_history (user_id, year, month, total_spend) VALUES
    (2, 2025, 11, 37.97),   -- Nov: Netflix + Spotify + Amazon
    (2, 2025, 12, 37.97),   -- Dec
    (2, 2026,  1, 72.97),   -- Jan: + Gym added
    (2, 2026,  2, 72.97),   -- Feb
    (2, 2026,  3, 72.97),   -- Mar
    (2, 2026,  4, 125.96);  -- Apr: + Adobe Creative Cloud