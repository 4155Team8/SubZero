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

const generateReminders = async () => {
    // subscriptions that need reminders
    

    const subscriptions = await getSubscriptionNeedingReminder();
    console.log("DEBUG subscriptions:", subscriptions)

    const generatedReminders= [];
    for (const sub of subscriptions )
    {
        // insert into reminders table
        const insertQuery='INSERT INTO reminders (subscription_id, reminder_date) VALUES(?,CURDATE())';
        await db.query(insertQuery,[sub.id]);
        // update last_reminded_at
        const updateQuery='UPDATE subscription SET last_reminded_at = NOW() WHERE id=?';
        await db.query(updateQuery, [sub.id]);
        const formattedDate = new Date(sub.renewal_date).toLocaleDateString();
        //store for output
        generatedReminders.push({
            subscription_id: sub.id,
            name: sub.name,
            message: `Reminder: ${sub.name} renews on ${formattedDate}`
        });
        
    }
    return generatedReminders;
};
    
module.exports = {
    createSubscription,
    getSubscription,
    updateSubscription,
    deleteSubscription,
    getSubscriptionNeedingReminder,
    generateReminders
};