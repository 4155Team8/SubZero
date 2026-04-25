const express = require("express");
const router = express.Router();
const { verifyToken } = require("./authMiddleware");
const db = require("./db");


// PUT /profile/budget — update monthly budget for the logged-in user
router.put("/budget", verifyToken, async (req, res) => {
    const { monthly_budget } = req.body;
    if (monthly_budget === undefined || isNaN(monthly_budget) || Number(monthly_budget) < 0) {
        return res.status(400).json({ error: "monthly_budget must be a non-negative number" });
    }
    try {
        await db.query(
            "UPDATE users SET monthly_budget = ? WHERE id = ?",
            [Number(monthly_budget), req.user.id]
        );
        res.json({ message: "Budget updated successfully", monthly_budget: Number(monthly_budget) });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});
router.get("/", verifyToken, async (req, res) => {
    try {
        const [rows] = await db.query(
            "SELECT email, name, created_at, reminders_enabled, monthly_budget FROM users WHERE id = ?",
            [req.user.id]
        );
        res.json({
            id: req.user.id,
            email: rows[0].email,
            name: rows[0].name,
            created_at: rows[0].created_at,
            reminders_enabled: rows[0].reminders_enabled,
            monthly_budget: parseFloat(rows[0].monthly_budget) || 0
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

router.post("/name", verifyToken, async (req, res) => {
    const { name } = req.body
    const email = req.user.email

    if (!name || typeof name !== "string" || name.trim() === "") {
        return res.status(400).json({ error: "Name is required" });
    }

    try {
        const user = await updateName({ name: name.trim(), email });
        if (!user) return res.status(404).json({ error: "User not found" });
        res.status(200).json({ message: "Name updated successfully", user });
    } catch (err) {
            console.error(err)  // add this
            res.status(500).json({ error: err.message });
    }
})

async function updateName({ name, email }) {
    const [result] = await db.query(
        "UPDATE users SET name = ? WHERE email = ?",
        [name, email]
    );
    if (result.affectedRows === 0) return null;
    return { name, email };
}





module.exports = router;