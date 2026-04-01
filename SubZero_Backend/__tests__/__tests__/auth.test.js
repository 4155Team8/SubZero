const request = require("supertest");
const express = require("express");

// ── Mock dependencies before importing routes ────────────────────────────────

jest.mock("../db");
jest.mock("../emailService");
jest.mock("bcrypt");
jest.mock("jsonwebtoken");

const db           = require("../db");
const bcrypt       = require("bcrypt");
const jwt          = require("jsonwebtoken");
const { sendPasswordResetEmail } = require("../emailService");

// Build a minimal Express app with just the auth router
const authRoutes = require("../authRoutes");
const app = express();
app.use(express.json());
app.use("/auth", authRoutes);

// ── Helpers ──────────────────────────────────────────────────────────────────

const mockUser = { id: 1, email: "test@example.com", password_hash: "hashed_pw" };

// ── /auth/register ───────────────────────────────────────────────────────────

describe("POST /auth/register", () => {

    beforeEach(() => jest.clearAllMocks());

    test("201 - registers a new user successfully", async () => {
        db.query
            .mockResolvedValueOnce([[]])                          // no existing user
            .mockResolvedValueOnce([{ insertId: 1 }]);           // insert result
        bcrypt.hash.mockResolvedValue("hashed_pw");

        const res = await request(app)
            .post("/auth/register")
            .send({ email: "test@example.com", password: "password123" });

        expect(res.status).toBe(201);
        expect(res.body.user.email).toBe("test@example.com");
    });

    test("400 - rejects invalid email", async () => {
        const res = await request(app)
            .post("/auth/register")
            .send({ email: "notanemail", password: "password123" });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/valid email/i);
    });

    test("400 - rejects password shorter than 8 characters", async () => {
        const res = await request(app)
            .post("/auth/register")
            .send({ email: "test@example.com", password: "short" });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/8 characters/i);
    });

    test("409 - rejects duplicate email", async () => {
        db.query.mockResolvedValueOnce([[mockUser]]); // existing user found

        const res = await request(app)
            .post("/auth/register")
            .send({ email: "test@example.com", password: "password123" });

        expect(res.status).toBe(409);
        expect(res.body.error).toMatch(/already registered/i);
    });
});

// ── /auth/login ───────────────────────────────────────────────────────────────

describe("POST /auth/login", () => {

    beforeEach(() => jest.clearAllMocks());

    test("200 - logs in successfully and returns token", async () => {
        db.query
            .mockResolvedValueOnce([[mockUser]])   // user found
            .mockResolvedValueOnce([{}]);          // last_login update
        bcrypt.compare.mockResolvedValue(true);
        jwt.sign.mockReturnValue("mock_token");

        const res = await request(app)
            .post("/auth/login")
            .send({ email: "test@example.com", password: "password123" });

        expect(res.status).toBe(200);
        expect(res.body.token).toBe("mock_token");
        expect(res.body.user.email).toBe("test@example.com");
    });

    test("400 - rejects missing email or password", async () => {
        const res = await request(app)
            .post("/auth/login")
            .send({ email: "test@example.com" });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/required/i);
    });

    test("401 - rejects wrong password", async () => {
        db.query.mockResolvedValueOnce([[mockUser]]);
        bcrypt.compare.mockResolvedValue(false);

        const res = await request(app)
            .post("/auth/login")
            .send({ email: "test@example.com", password: "wrongpassword" });

        expect(res.status).toBe(401);
        expect(res.body.error).toMatch(/invalid email or password/i);
    });

    test("401 - rejects non-existent email", async () => {
        db.query.mockResolvedValueOnce([[]]); // no user found

        const res = await request(app)
            .post("/auth/login")
            .send({ email: "nobody@example.com", password: "password123" });

        expect(res.status).toBe(401);
        expect(res.body.error).toMatch(/invalid email or password/i);
    });
});

// ── /auth/forgot-password ─────────────────────────────────────────────────────

describe("POST /auth/forgot-password", () => {

    beforeEach(() => jest.clearAllMocks());

    test("200 - returns same message whether email exists or not", async () => {
        db.query.mockResolvedValueOnce([[]]); // email not found

        const res = await request(app)
            .post("/auth/forgot-password")
            .send({ email: "nobody@example.com" });

        expect(res.status).toBe(200);
        expect(res.body.message).toMatch(/if an account exists/i);
        expect(sendPasswordResetEmail).not.toHaveBeenCalled();
    });

    test("200 - sends reset email when account exists", async () => {
        db.query
            .mockResolvedValueOnce([[mockUser]])  // user found
            .mockResolvedValueOnce([{}])          // invalidate old tokens
            .mockResolvedValueOnce([{ insertId: 1 }]); // insert new token
        sendPasswordResetEmail.mockResolvedValue();

        const res = await request(app)
            .post("/auth/forgot-password")
            .send({ email: "test@example.com" });

        expect(res.status).toBe(200);
        expect(sendPasswordResetEmail).toHaveBeenCalledWith(
            "test@example.com",
            expect.any(String)
        );
    });

    test("400 - rejects invalid email format", async () => {
        const res = await request(app)
            .post("/auth/forgot-password")
            .send({ email: "bademail" });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/valid email/i);
    });
});

// ── /auth/reset-password ──────────────────────────────────────────────────────

describe("POST /auth/reset-password", () => {

    beforeEach(() => jest.clearAllMocks());

    const validToken = "validtoken123";
    const futureExpiry = new Date(Date.now() + 60 * 60 * 1000); // 1 hour from now

    test("200 - resets password successfully", async () => {
        db.query
            .mockResolvedValueOnce([[{ user_id: 1, expires_at: futureExpiry, used: 0 }]]) // token valid
            .mockResolvedValueOnce([{}])  // update password
            .mockResolvedValueOnce([{}]); // mark token used
        bcrypt.hash.mockResolvedValue("new_hashed_pw");

        const res = await request(app)
            .post("/auth/reset-password")
            .send({ token: validToken, password: "newpassword123" });

        expect(res.status).toBe(200);
        expect(res.body.message).toMatch(/reset successfully/i);
    });

    test("400 - rejects missing token", async () => {
        const res = await request(app)
            .post("/auth/reset-password")
            .send({ password: "newpassword123" });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/token is required/i);
    });

    test("400 - rejects password shorter than 8 characters", async () => {
        const res = await request(app)
            .post("/auth/reset-password")
            .send({ token: validToken, password: "short" });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/8 characters/i);
    });

    test("400 - rejects invalid token", async () => {
        db.query.mockResolvedValueOnce([[]]); // token not found

        const res = await request(app)
            .post("/auth/reset-password")
            .send({ token: "badtoken", password: "newpassword123" });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/invalid or expired/i);
    });

    test("400 - rejects already used token", async () => {
        db.query.mockResolvedValueOnce([[{ user_id: 1, expires_at: futureExpiry, used: 1 }]]);

        const res = await request(app)
            .post("/auth/reset-password")
            .send({ token: validToken, password: "newpassword123" });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/already been used/i);
    });

    test("400 - rejects expired token", async () => {
        const pastExpiry = new Date(Date.now() - 1000); // already expired
        db.query.mockResolvedValueOnce([[{ user_id: 1, expires_at: pastExpiry, used: 0 }]]);

        const res = await request(app)
            .post("/auth/reset-password")
            .send({ token: validToken, password: "newpassword123" });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/expired/i);
    });
});
