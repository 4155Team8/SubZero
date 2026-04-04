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