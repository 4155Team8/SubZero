const express = require("express");
const router = express.Router();
const { verifyToken } = require("./authMiddleware");
const {
    createSubscription,
    getSubscriptionsByUser,
    updateSubscription,
    deleteSubscription,
    getSubscriptionNeedingReminder,
    generateReminders,
    clearRemindersForUser

} = require("./subscriptionModel");

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

// DELETE /reminders/clear-all
router.delete("/clear-all", verifyToken, async (req, res) => {
    const id = req.user.id;

    try {
        const result = await clearRemindersForUser(id);
        res.status(200).json(result);
    } catch (err) {
        if (err.message === "Reminders not found") {
            return res.status(404).json({ error: err.message });
        }

        console.error(err);
        res.status(500).json({ error: err.message });
    }
});
module.exports = router;