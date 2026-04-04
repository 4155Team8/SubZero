const db = require("./db");

async function createSubscription(data) {
    const { name, cost, user_id, category_id, billing_cycle_id } = data;

    const [result] = await db.query(
        "INSERT INTO subscription (name, cost, user_id, category_id, billing_cycle_id) VALUES (?, ?, ?, ?, ?)",
        [name, cost, user_id, category_id, billing_cycle_id]
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
            s.updated_at,
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

// Only updates if the subscription belongs to the user
async function updateSubscription(id, user_id, data) {
    const { name, cost, category_id, billing_cycle_id } = data;

    const [result] = await db.query(
        `UPDATE subscription
         SET name=?, cost=?, category_id=?, billing_cycle_id=?
         WHERE id=? AND user_id=?`,
        [name, cost, category_id, billing_cycle_id, id, user_id]
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
const getSubscriptionNeedingReminder= async ()=>{
    const query='SELECT id, name, cost, renewal_date, reminder_days_before FROM subscription WHERE is_active=TRUE AND renewal_date <= DATE_ADD(CURDATE(), INTERVAL reminder_days_before DAY) AND (last_reminded_at is NULL OR last_reminded_at < CURDATE())';
    const [results]= await db.query(query);
    return results;

};
    
module.exports = {
    createSubscription,
    getSubscriptionsByUser,
    updateSubscription,
    deleteSubscription,
    getSubscriptionNeedingReminder
};