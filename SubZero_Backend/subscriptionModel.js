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

module.exports = {
    createSubscription,
    getSubscription,
    updateSubscription,
    deleteSubscription
};