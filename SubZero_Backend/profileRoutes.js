const express = require("express");
const router = express.Router();
const { verifyToken } = require("./authMiddleware");
const db = require("./db");

router.get("/", verifyToken, async (req, res) => {
    try {
        const [rows] = await db.query(
            "SELECT name, created_at, reminders_enabled FROM users WHERE id = ?",
            [req.user.id]
        );
        res.json({
            id: req.user.id,
            email: req.user.email,
            name: rows[0].name,
            created_at: rows[0].created_at,
            reminders_enabled: rows[0].reminders_enabled
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;