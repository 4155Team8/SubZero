USE subzero_db;

INSERT INTO subscription (name, cost, user_id, category_id, billing_cycle_id)
VALUES 
('Netflix', 12.99, 2, 1, 4), 
('Spotify', 9.99, 2, 1, 4), 
('Amazon Prime', 14.99, 2, 1, 4), 
('Gym Membership', 35.00, 2, 2, 3),
('Adobe Creative Cloud', 52.99, 2, 3, 5);

INSERT INTO reminders (name, subscription_id, reminder_date, description, user_id)
VALUES
('Netflix', 6, '2026-04-25', 'Your netflix payment is coming up soon.', 2), 
('Spotify', 7, '2026-04-25', 'Spotify premium renewal coming soon.', 2), 
('Amazon Prime', 8, '2026-04-10', 'Amazon Prime renewal coming soon.', 2), 
('Gym Membership', 9, '2026-04-25', 'Annual fee for the gym coming soon.', 2), 
('Adobe Creative Cloud', 10, '2026-04-25', 'Adobe CC is increasing in price in one month.', 2);