const express = require("express");
const router = express.Router();
const jwt = require("jsonwebtoken");
const crypto = require("crypto");
const bcrypt = require("bcrypt");
const { registerUser, loginUser, deleteUser } = require("./userModel");
const { sendPasswordResetEmail } = require("./emailService");
const db = require("./db");
const { verifyToken } = require("./authMiddleware");

const JWT_SECRET    = process.env.JWT_SECRET;
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || "7d";
const SALT_ROUNDS   = 10;

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// ── POST /auth/register ───────────────────────────────────────────────────────
router.post("/register", async (req, res) => {
    const { email, password } = req.body;

    if (!email || !isValidEmail(email)) {
        return res.status(400).json({ error: "A valid email is required" });
    }
    if (!password || typeof password !== "string" || password.length < 8) {
        return res.status(400).json({ error: "Password must be at least 8 characters" });
    }

    try {
        const user = await registerUser({ email, password });
        res.status(201).json({ message: "User registered successfully", user });
    } catch (err) {
        if (err.message === "Email is already registered") {
            return res.status(409).json({ error: err.message });
        }
        res.status(500).json({ error: err.message });
    }
});

// ── POST /auth/login ──────────────────────────────────────────────────────────
router.post("/login", async (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ error: "Email and password are required" });
    }

    try {
        const user = await loginUser({ email, password });

        const token = jwt.sign(
            { id: user.id, email: user.email },
            JWT_SECRET,
            { expiresIn: JWT_EXPIRES_IN }
        );

        res.json({ message: "Login successful", token, user });
    } catch (err) {
        if (err.message === "Invalid email or password") {
            return res.status(401).json({ error: err.message });
        }
        res.status(500).json({ error: err.message });
    }
});

// ── POST /auth/forgot-password ────────────────────────────────────────────────
// Accepts an email, generates a reset token, sends the email.
// Always returns 200 regardless of whether the email exists (security best practice).
router.post("/forgot-password", async (req, res) => {
    const { email } = req.body;

    if (!email || !isValidEmail(email)) {
        return res.status(400).json({ error: "A valid email is required" });
    }

    try {
        const [rows] = await db.query(
            "SELECT id FROM users WHERE email = ?",
            [email]
        );

        // Always respond with 200 — don't reveal whether the email exists
        if (rows.length === 0) {
            return res.status(200).json({ message: "If an account exists, a reset email has been sent" });
        }

        const user = rows[0];

        // Invalidate any existing unused tokens for this user
        await db.query(
            "UPDATE password_reset_tokens SET used = 1 WHERE user_id = ? AND used = 0",
            [user.id]
        );

        // Generate a secure random token
        const token     = crypto.randomBytes(32).toString("hex");
        const expiresAt = new Date(Date.now() + 60 * 60 * 1000); // 1 hour from now

        await db.query(
            "INSERT INTO password_reset_tokens (user_id, token, expires_at) VALUES (?, ?, ?)",
            [user.id, token, expiresAt]
        );

        await sendPasswordResetEmail(email, token);

        res.status(200).json({ message: "If an account exists, a reset email has been sent" });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ── POST /auth/reset-password ─────────────────────────────────────────────────
// Accepts the token + new password, validates, updates the hash.
router.post("/reset-password", async (req, res) => {
    const { token, password } = req.body;

    if (!token) {
        return res.status(400).json({ error: "Reset token is required" });
    }
    if (!password || password.length < 8) {
        return res.status(400).json({ error: "Password must be at least 8 characters" });
    }

    try {
        const [rows] = await db.query(
            `SELECT prt.user_id, prt.expires_at, prt.used
             FROM password_reset_tokens prt
             WHERE prt.token = ?`,
            [token]
        );

        if (rows.length === 0) {
            return res.status(400).json({ error: "Invalid or expired reset link" });
        }

        const resetRow = rows[0];

        if (resetRow.used) {
            return res.status(400).json({ error: "This reset link has already been used" });
        }

        if (new Date() > new Date(resetRow.expires_at)) {
            return res.status(400).json({ error: "This reset link has expired" });
        }

        // Hash the new password
        const password_hash = await bcrypt.hash(password, SALT_ROUNDS);

        // Update user password
        await db.query(
            "UPDATE users SET password_hash = ? WHERE id = ?",
            [password_hash, resetRow.user_id]
        );

        // Mark token as used
        await db.query(
            "UPDATE password_reset_tokens SET used = 1 WHERE token = ?",
            [token]
        );

        res.status(200).json({ message: "Password reset successfully" });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});


router.post("/new-email", verifyToken, async (req, res) => {
    const { email } = req.body
    const id = req.user.id
    console.log("updating email for user id:", id, "to:", email);

    if (!email || typeof email !== "string" || email.trim() === "") {
        return res.status(400).json({ error: "Email is required" });
    }

    try {
        const user = await updateEmail({ email: email.trim(), id });
        if (!user) return res.status(404).json({ error: "User not found" });
        res.status(200).json({ message: "Email updated successfully", user });
    } catch (err) {
            console.error(err)
            res.status(500).json({ error: err.message });
    }
})

async function updateEmail({ email, id }) {
    const [result] = await db.query(
        "UPDATE users SET email = ? WHERE id = ?",
        [email, id]
    );
    if (result.affectedRows === 0) return null;
    return { email, id };
}

// POST new pass
router.post("/new-password", verifyToken, async (req, res) => {
    const { password } = req.body
    const id = req.user.id

    if (!password || typeof password !== "string" || password.trim() === "") {
        return res.status(400).json({ error: "Password is required" });
    }

    try {
        const password_hash = await bcrypt.hash(password, SALT_ROUNDS);
        const user = await updatePassword({ password_hash, id });
        if (!user) return res.status(404).json({ error: "User not found" });
        res.status(200).json({ message: "Password updated successfully", user });
    } catch (err) {
            console.error(err)
            res.status(500).json({ error: err.message });
    }
})


// DELETE account
router.delete("/delete-account", verifyToken, async (req, res) => {
  const id = req.user.id;

  try {
    const result = await deleteUser({ id });
    res.status(200).json(result);
  } catch (err) {
    if (err.message === "User not found") {
      return res.status(404).json({ error: err.message });
    }
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});
async function updatePassword({ password_hash, id }) {
    const [result] = await db.query(
        "UPDATE users SET password_hash = ? WHERE id = ?",
        [password_hash, id]
    );
    if (result.affectedRows === 0) return null;
    return { password_hash, id };
}

module.exports = router;