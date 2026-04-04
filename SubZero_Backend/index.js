require("dotenv").config();
const express = require("express");
const app = express();
app.use(express.json());

const {
  createSubscription,
  getSubscription,
  updateSubscription,
  deleteSubscription,
  getSubscriptionNeedingReminder
} = require("./subscriptionModel");
const authRoutes         = require("./authRoutes");
const subscriptionRoutes = require("./subscriptionRoutes");

app.use("/auth",          authRoutes);
app.use("/subscriptions", subscriptionRoutes);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});

app.get('/reminders', async (req,res)=>{
  try{
    const data= await getSubscriptionNeedingReminder();
    res.json(data);

  }
  catch(err){
    console.error(err);
    res.status(500).json({error: err.message});
  }
});
