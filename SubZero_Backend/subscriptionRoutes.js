const express = require("express");
const router = express.Router();
const jwt = require("jsonwebtoken");
const db = require("./db");
const {
    createSubscription,
    getSubscriptionsByUser,
    updateSubscription,
    deleteSubscription
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
        res.json(subscriptions);
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
            billing_cycle_id
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

// GET /subscriptions/reminders
router.get("/reminders", authenticate, async (req, res) => {
    try {
        const data = await require("./subscriptionModel").getSubscriptionNeedingReminder();
        res.json(data);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

// GET /subscriptions/generate-reminders
router.get("/generate-reminders", authenticate, async (req, res) => {
    try {
        const reminders = await require("./subscriptionModel").generateReminders();
        res.json(reminders);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Failed to generate reminders" });
    }
});
// GET /subscriptions/redundant
router.get("/redundant",authenticate, async(req,res)=>{
  try{

    const userId=req.user.id;
    const data= await require("./subscriptionModel").getRedundantSubscriptions(userId);

    res.json(data);


  } catch(err){
    res.status(500).json({error: err.message});
  }
});

module.exports = router;
