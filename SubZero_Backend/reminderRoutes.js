const express = require("express");
const router = express.Router();
const { verifyToken } = require("./authMiddleware");
const { getSubscriptionNeedingReminder, generateReminders } = require("./subscriptionModel");

router.get("/", verifyToken, async (req, res) => {
    try {
        const data = await getSubscriptionNeedingReminder(req.user.id);
        res.json(data);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: err.message });
    }
});

router.get("/generate", verifyToken, async (req, res) => {
    try {
        const reminders = await generateReminders(req.user.id);
        res.json(reminders);
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Failed to generate reminders" });
    }
});

module.exports = router;