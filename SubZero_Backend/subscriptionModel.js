const db = require("./db");

async function createSubscription(data) {
    const { name, cost, user_id, category_id, billing_cycle_id, renewal_date } = data;

    const [result] = await db.query(
        "INSERT INTO subscription (name, cost, user_id, category_id, billing_cycle_id, renewal_date) VALUES (?, ?, ?, ?, ?, ?)",
        [name, cost, user_id, category_id, billing_cycle_id, renewal_date]
    );

    return { id: result.insertId, ...data };
}

// Only returns subscriptions belonging to the given user
async function getSubscriptionsByUser(user_id) {
    const [rows] = await db.query(
        `SELECT
            s.id,
            s.name,
            s.cost,
            s.created_at,
            s.renewal_date,
            s.updated_at,
            s.category_id,
            s.billing_cycle_id,
            c.name  AS category,
            bc.name AS billing_cycle
         FROM subscription s
         JOIN category     c  ON s.category_id     = c.id
         JOIN billingcycle bc ON s.billing_cycle_id = bc.id
         WHERE s.user_id = ?
         ORDER BY s.created_at DESC`,
        [user_id]
    );
    return rows;
}

async function clearRemindersForUser(user_id) {
    const [result] = await db.query(
        "DELETE FROM reminders WHERE user_id = ?",
    [user_id]
    );

    if (result.affectedRows === 0) {
        throw new Error("Reminders not found.")
    }

    return { message: "Deleted all." };
}

// Only updates if the subscription belongs to the user
async function updateSubscription(id, user_id, data) {
    const { name, cost, category_id, billing_cycle_id, renewal_date } = data;

    const [result] = await db.query(
        `UPDATE subscription
         SET name=?, cost=?, category_id=?, billing_cycle_id=?, renewal_date=?
         WHERE id=? AND user_id=?`,
        [name, cost, category_id, billing_cycle_id, renewal_date ?? null, id, user_id]
    );

    if (result.affectedRows === 0) {
        throw new Error("Subscription not found or unauthorized");
    }

    return { message: "Updated", id, ...data };
}

// Only deletes if the subscription belongs to the user
async function deleteSubscription(id, user_id) {
    const [result] = await db.query(
        "DELETE FROM subscription WHERE id=? AND user_id=?",
        [id, user_id]
    );

    if (result.affectedRows === 0) {
        throw new Error("Subscription not found or unauthorized");
    }

    return { message: "Deleted", id };
}

// Returns reminders for a user where the renewal is within 7 days
const getSubscriptionNeedingReminder = async (userId) => {
    const query = `
        SELECT r.id,
               r.subscription_id,
               r.name,
               r.description,
               r.reminder_date,
               r.sent_at,
               r.created_at
        FROM reminders r
        JOIN subscription s ON r.subscription_id = s.id
        WHERE s.is_active = TRUE
          AND s.user_id = ?
          AND (r.sent_at IS NULL OR r.sent_at < CURDATE())
        ORDER BY r.reminder_date ASC
    `;
    const [results] = await db.query(query, [userId]);
    return results;
};

// Finds subscriptions renewing within 3 days and generates reminders for them
const generateReminders = async (userId) => {
    const [userRows] = await db.query(
        "SELECT reminders_enabled FROM users WHERE id = ?",
        [userId]
    );

    if (!userRows[0]?.reminders_enabled) return [];

    const [subscriptions] = await db.query(
        `SELECT s.id,
                s.name,
                s.cost,
                s.renewal_date
         FROM subscription s
         WHERE s.is_active = TRUE
           AND s.user_id = ?
           AND s.renewal_date <= DATE_ADD(CURDATE(), INTERVAL 3 DAY)
           AND s.renewal_date >= CURDATE()
           AND (s.last_reminded_at IS NULL OR DATE(s.last_reminded_at) < CURDATE())`,
        [userId]
    );

    const generatedReminders = [];

    for (const sub of subscriptions) {
        const formattedDate = new Date(sub.renewal_date).toLocaleDateString();
        const daysUntil = Math.ceil(
            (new Date(sub.renewal_date) - new Date()) / (1000 * 60 * 60 * 24)
        );

        const name        = sub.name;
        const description = `Your ${sub.name} subscription renews in ${daysUntil} day${daysUntil !== 1 ? "s" : ""} on ${formattedDate} — $${sub.cost}`;

        // Fixed: columns and values are now correctly aligned
        await db.query(
            `INSERT INTO reminders (subscription_id, user_id, reminder_date, name, description)
             VALUES (?, ?, CURDATE(), ?, ?)`,
            [sub.id, userId, name, description]
        );

        await db.query(
            "UPDATE subscription SET last_reminded_at = NOW() WHERE id = ?",
            [sub.id]
        );

        generatedReminders.push({ subscription_id: sub.id, name, description });
    }

    return generatedReminders;
};

// Runs generateReminders for every user who has reminders enabled.
// Called by the daily server-side scheduler in index.js.
const generateRemindersForAllUsers = async () => {
    const [users] = await db.query(
        "SELECT id FROM users WHERE reminders_enabled = 1"
    );

    let total = 0;
    for (const user of users) {
        const reminders = await generateReminders(user.id);
        total += reminders.length;
    }
    return total;
};

module.exports = {
    createSubscription,
    getSubscriptionsByUser,
    updateSubscription,
    deleteSubscription,
    getSubscriptionNeedingReminder,
    generateReminders,
    generateRemindersForAllUsers,
    clearRemindersForUser
};