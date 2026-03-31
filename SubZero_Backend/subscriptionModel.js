const db = require("./db");

async function createSubscription(data){
    const {name, cost, user_id, category_id, billing_cycle_id} = data;

    const [result] = await db.query(
        "INSERT INTO subscription (name, cost, user_id, category_id, billing_cycle_id) VALUES (?, ?, ?, ?, ?)",
        [name, cost, user_id, category_id, billing_cycle_id]
    );

    return {id: result.insertId, ...data};
}

async function getSubscription(){
    const [rows] = await db.query("SELECT * FROM subscription");
    return rows;
}

async function updateSubscription(id, data){
    const {name, cost} = data;
    await db.query(
        "UPDATE subscription SET name=?, cost=? WHERE id=?",
        [name, cost, id]
    );
    return {message:"Updated"};
}

async function deleteSubscription(id){
    await db.query(
        "DELETE FROM subscription WHERE id=?",
        [id]
    );
    return {message:"Deleted"};
}
const getSubscriptionNeedingReminder= async ()=>{
    const query='SELECT id, name, cost, renewal_date, reminder_days_before FROM subscription WHERE is_active=TRUE AND renewal_date <= DATE_ADD(CURDATE(), INTERVAL reminder_days_before DAY) AND (last_reminded_at is NULL OR last_reminded_at < CURDATE())';
    const [results]= await db.query(query);
    return results;

};
    
module.exports = {
    createSubscription,
    getSubscription,
    updateSubscription,
    deleteSubscription,
    getSubscriptionNeedingReminder
};