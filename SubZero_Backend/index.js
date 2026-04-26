require("dotenv").config();
const express = require("express");
const app = express();
app.use(express.json());

const authRoutes         = require("./authRoutes");
const subscriptionRoutes = require("./subscriptionRoutes");
const reminderRoutes     = require("./reminderRoutes");
const profileRoutes      = require("./profileRoutes");
const { generateRemindersForAllUsers } = require("./subscriptionModel");

app.use("/profile",       profileRoutes);
app.use("/auth",          authRoutes);
app.use("/subscriptions", subscriptionRoutes);
app.use("/reminders",     reminderRoutes);

const TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000;

async function runDailyReminders() {
    try {
        const count = await generateRemindersForAllUsers();
        console.log(`[Reminders] ${new Date().toISOString()} — generated ${count} reminder(s)`);
    } catch (err) {
        console.error("[Reminders] Scheduler error:", err.message);
    }
}

// Run once on startup, then every 24 hours
runDailyReminders();
setInterval(runDailyReminders, TWENTY_FOUR_HOURS);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});