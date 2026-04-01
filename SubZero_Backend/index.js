require("dotenv").config();
const express = require("express");
const app = express();
app.use(express.json());

const authRoutes         = require("./authRoutes");
const subscriptionRoutes = require("./subscriptionRoutes");

app.use("/auth",          authRoutes);
app.use("/subscriptions", subscriptionRoutes);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});