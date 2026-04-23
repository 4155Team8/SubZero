USE subzero_db;

-- user_id is NULL for global options (available to all users)
INSERT IGNORE INTO billingcycle (name, user_id) VALUES
    ('Daily',              NULL),
    ('Weekly',             NULL),
    ('Biweekly',           NULL),
    ('Monthly (30 days)',  NULL),
    ('Yearly',             NULL),
    ('Custom',             NULL);

INSERT IGNORE INTO category (name, user_id) VALUES
    ('Streaming',      NULL),
    ('Social',         NULL),
    ('Financial',      NULL),
    ('Food',           NULL),
    ('Media Creation', NULL),
    ('Mental Health',  NULL),
    ('Custom',         NULL);

INSERT INTO subscription (name, cost, user_id, category_id, billing_cycle_id)
VALUES 
('Netflix', 12.99, 1, 1, 4), 
('Spotify', 9.99, 1, 1, 4), 
('Amazon Prime', 14.99, 1, 1, 4), 
('Gym Membership', 35.00, 1, 2, 3),
('Adobe Creative Cloud', 52.99, 1, 3, 5);

INSERT INTO reminders (name, subscription_id, reminder_date, description, user_id)
VALUES
('Netflix', 1, '2026-04-25', 'Your netflix payment is coming up soon.', 1), 
('Spotify', 2, '2026-04-25', 'Spotify premium renewal coming soon.', 1), 
('Amazon Prime', 3, '2026-04-10', 'Amazon Prime renewal coming soon.', 1), 
('Gym Membership', 4, '2026-04-25', 'Annual fee for the gym coming soon.', 1), 
('Adobe Creative Cloud', 5, '2026-04-25', 'Adobe CC is increasing in price in one month.', 1);