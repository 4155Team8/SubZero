Separate branch for me (Jovan) to test the API endpoints and add functionality to the DB as needed.
Eventually will merge with master to incorporate screens.


Seed for subscriptions:

INSERT INTO subscription (name, cost, user_id, category_id, billing_cycle_id)
VALUES 
('Netflix', 12.99, 3, 1, 1), 
('Spotify', 9.99, 3, 1, 1), 
('Amazon Prime', 14.99, 3, 1, 2), 
('Gym Membership', 35.00, 3, 2, 1),
('Adobe Creative Cloud', 52.99, 3, 3, 1);
