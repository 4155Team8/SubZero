const db = require("./db");
const bcrypt = require("bcrypt");

const SALT_ROUNDS = 10;

async function registerUser({ email, password }) {
  // Check if email already in use
  const [existing] = await db.query(
    "SELECT id FROM users WHERE email = ?",
    [email]
  );
  if (existing.length > 0) {
    throw new Error("Email is already registered");
  }

  const password_hash = await bcrypt.hash(password, SALT_ROUNDS);

  const [result] = await db.query(
    "INSERT INTO users (email, password_hash) VALUES (?, ?)",
    [email, password_hash]
  );

  return { id: result.insertId, email };
}

async function loginUser({ email, password }) {
  const [rows] = await db.query(
    "SELECT id, email, password_hash FROM users WHERE email = ?",
    [email]
  );

  if (rows.length === 0) {
    throw new Error("Invalid email or password");
  }

  const user = rows[0];
  const isMatch = await bcrypt.compare(password, user.password_hash);

  if (!isMatch) {
    throw new Error("Invalid email or password");
  }

  // Update last_login timestamp
  await db.query(
    "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?",
    [user.id]
  );

  return { id: user.id, email: user.email };
}

module.exports = { registerUser, loginUser };
