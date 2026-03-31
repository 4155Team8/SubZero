const express = require('express');
const app = express();
app.use(express.json());

const {
  createSubscription,
  getSubscription,
  updateSubscription,
  deleteSubscription,
  getSubscriptionNeedingReminder
} = require("./subscriptionModel");

const authRoutes = require("./authRoutes");
app.use("/auth", authRoutes);

app.post("/subscriptions", async (req, res) => {
  const { name, cost, user_id, category_id, billing_cycle_id } = req.body;

  if (!name || typeof name !== "string") {
    return res.status(400).json({ error: "Name is required and must be a string" });
  }
  if (!cost || typeof cost !== "number" || cost <= 0) {
    return res.status(400).json({ error: "Cost must be a positive number" });
  }
  if (!user_id || !category_id || !billing_cycle_id) {
    return res.status(400).json({ error: "User, Category, and Billing Cycle IDs are required" });
  }
  try {
    const result = await createSubscription(req.body);
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get("/subscriptions", async (req, res) => {
  const data = await getSubscription();
  res.json(data);
});

app.put("/subscriptions/:id", async (req, res) => {
  const { name, cost, user_id, category_id, billing_cycle_id } = req.body;

  if (!name || typeof name !== "string") {
    return res.status(400).json({ error: "Name is required and must be a string" });
  }
  if (!cost || typeof cost !== "number" || cost <= 0) {
    return res.status(400).json({ error: "Cost must be a positive number" });
  }
  if (!user_id || !category_id || !billing_cycle_id) {
    return res.status(400).json({ error: "User, Category, and Billing Cycle IDs are required" });
  }
  try {
    const result = await updateSubscription(req.params.id, req.body);
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.delete("/subscriptions/:id", async (req, res) => {
  const result = await deleteSubscription(req.params.id);
  res.json(result);
});

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
