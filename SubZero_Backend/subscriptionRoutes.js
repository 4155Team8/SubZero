const express = require("express");
const router = express.Router();
const jwt = require("jsonwebtoken");
const db = require("./db");
const {
    createSubscription,
    getSubscriptionsByUser,
    updateSubscription,
    deleteSubscription,
    clearRemindersForUser
} = require("./subscriptionModel");

const JWT_SECRET = process.env.JWT_SECRET;

// Auth middleware
function authenticate(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        return res.status(401).json({ error: "Missing or invalid token" });
    }
    const token = authHeader.split(" ")[1];
    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        req.user = decoded;
        next();
    } catch (err) {
        return res.status(401).json({ error: "Token expired or invalid" });
    }
}

// GET /subscriptions/categories
// Returns global categories + custom ones for this user
router.get("/categories", authenticate, async (req, res) => {
    try {
        const [rows] = await db.query(
            `SELECT id, name, 
                CASE WHEN user_id IS NULL THEN 0 ELSE 1 END AS is_custom
             FROM category
             WHERE user_id IS NULL OR user_id = ?
             ORDER BY is_custom ASC, name ASC`,
            [req.user.id]
        );
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// POST /subscriptions/categories/custom
// Create a custom category for the logged-in user
router.post("/categories/custom", authenticate, async (req, res) => {
    const { name } = req.body;
    if (!name || typeof name !== "string" || name.trim() === "") {
        return res.status(400).json({ error: "Category name is required" });
    }
    const trimmed = name.trim();
    const [existing] = await db.query(
        "SELECT id, user_id FROM category WHERE name = ? AND (user_id IS NULL OR user_id = ?)",
        [trimmed, req.user.id]
    );
    if (existing.length > 0) {
        return res.json({ id: existing[0].id, name: trimmed, is_custom: existing[0].user_id !== null });
    }
    try {
        const [result] = await db.query(
            "INSERT INTO category (name, user_id) VALUES (?, ?)",
            [trimmed, req.user.id]
        );
        res.status(201).json({ id: result.insertId, name: trimmed, is_custom: true });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// GET /subscriptions/billing-cycles
// Returns global billing cycles + custom ones for this user
router.get("/billing-cycles", authenticate, async (req, res) => {
    try {
        const [rows] = await db.query(
            `SELECT id, name,
                CASE WHEN user_id IS NULL THEN 0 ELSE 1 END AS is_custom
             FROM billingcycle
             WHERE user_id IS NULL OR user_id = ?
             ORDER BY is_custom ASC, id ASC`,
            [req.user.id]
        );
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// POST /subscriptions/billing-cycles/custom
// Create a custom billing cycle for the logged-in user
router.post("/billing-cycles/custom", authenticate, async (req, res) => {
    const { name } = req.body;
    if (!name || typeof name !== "string" || name.trim() === "") {
        return res.status(400).json({ error: "Billing cycle name is required" });
    }
    const trimmed = name.trim();
    const [existing] = await db.query(
        "SELECT id, user_id FROM billingcycle WHERE name = ? AND (user_id IS NULL OR user_id = ?)",
        [trimmed, req.user.id]
    );
    if (existing.length > 0) {
        return res.json({ id: existing[0].id, name: trimmed, is_custom: existing[0].user_id !== null });
    }
    try {
        const [result] = await db.query(
            "INSERT INTO billingcycle (name, user_id) VALUES (?, ?)",
            [trimmed, req.user.id]
        );
        res.status(201).json({ id: result.insertId, name: trimmed, is_custom: true });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// GET /subscriptions
router.get("/", authenticate, async (req, res) => {
    try {
        const subscriptions = await getSubscriptionsByUser(req.user.id);

        // Fetch user budget
        const [userRows] = await db.query(
            "SELECT monthly_budget FROM users WHERE id = ?",
            [req.user.id]
        );
        const monthly_budget = parseFloat(userRows[0]?.monthly_budget) || 0;

        // Build last 6 months of spend from subscription data
        const now = new Date();
        const monthlySpend = [];
        for (let i = 5; i >= 0; i--) {
            const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
            const year = d.getFullYear();
            const month = d.getMonth() + 1; // 1-12

            const [histRows] = await db.query(
                "SELECT total_spend FROM monthly_spend_history WHERE user_id = ? AND year = ? AND month = ?",
                [req.user.id, year, month]
            );

            const monthLabel = d.toLocaleString("default", { month: "short" });
            monthlySpend.push({
                year,
                month,
                month_label: monthLabel,
                total_spend: histRows.length > 0 ? parseFloat(histRows[0].total_spend) : 0
            });
        }

        res.json({ subscriptions, monthly_budget, monthly_spend: monthlySpend });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// POST /subscriptions
router.post("/", authenticate, async (req, res) => {
    const { name, cost, category_id, billing_cycle_id } = req.body;
    if (!name || typeof name !== "string" || name.trim() === "") {
        return res.status(400).json({ error: "Subscription name is required" });
    }
    if (!cost || isNaN(cost) || Number(cost) <= 0) {
        return res.status(400).json({ error: "Cost must be a positive number" });
    }
    if (!category_id) return res.status(400).json({ error: "category_id is required" });
    if (!billing_cycle_id) return res.status(400).json({ error: "billing_cycle_id is required" });
    try {
        const subscription = await createSubscription({
            name: name.trim(),
            cost: Number(cost),
            user_id: req.user.id,
            category_id,
            billing_cycle_id,
            renewal_date: req.body.renewal_date ?? null
        });
        res.status(201).json(subscription);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// PUT /subscriptions/:id
router.put("/:id", authenticate, async (req, res) => {
    const { id } = req.params;
    const { name, cost, category_id, billing_cycle_id } = req.body;
    if (!name || typeof name !== "string" || name.trim() === "") {
        return res.status(400).json({ error: "Subscription name is required" });
    }
    if (!cost || isNaN(cost) || Number(cost) <= 0) {
        return res.status(400).json({ error: "Cost must be a positive number" });
    }
    try {
        const result = await updateSubscription(id, req.user.id, {
            name: name.trim(),
            cost: Number(cost),
            category_id,
            billing_cycle_id
        });
        res.json(result);
    } catch (err) {
        if (err.message === "Subscription not found or unauthorized") {
            return res.status(403).json({ error: err.message });
        }
        res.status(500).json({ error: err.message });
    }
});

// DELETE /subscriptions/:id
router.delete("/:id", authenticate, async (req, res) => {
    try {
        const result = await deleteSubscription(req.params.id, req.user.id);
        res.json(result);
    } catch (err) {
        if (err.message === "Subscription not found or unauthorized") {
            return res.status(403).json({ error: err.message });
        }
        res.status(500).json({ error: err.message });
    }
});

// POST /subscriptions/monthly-spend
// Upsert the recorded spend for a given year/month (called when a subscription renews)
router.post("/monthly-spend", authenticate, async (req, res) => {
    const { year, month, total_spend } = req.body;
    if (!year || !month || total_spend === undefined) {
        return res.status(400).json({ error: "year, month, and total_spend are required" });
    }
    if (month < 1 || month > 12) {
        return res.status(400).json({ error: "month must be 1-12" });
    }
    try {
        await db.query(
            `INSERT INTO monthly_spend_history (user_id, year, month, total_spend)
             VALUES (?, ?, ?, ?)
             ON DUPLICATE KEY UPDATE total_spend = VALUES(total_spend), updated_at = CURRENT_TIMESTAMP`,
            [req.user.id, year, month, Number(total_spend)]
        );
        res.json({ message: "Monthly spend recorded", year, month, total_spend: Number(total_spend) });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});



module.exports = router;
