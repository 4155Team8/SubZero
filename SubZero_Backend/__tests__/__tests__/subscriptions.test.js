const request = require("supertest");
const express = require("express");

// ── Mock dependencies ────────────────────────────────────────────────────────

jest.mock("../db");
jest.mock("../subscriptionModel");
jest.mock("jsonwebtoken");

const db  = require("../db");
const jwt = require("jsonwebtoken");
const {
    createSubscription,
    getSubscriptionsByUser,
    updateSubscription,
    deleteSubscription
} = require("../subscriptionModel");

const subscriptionRoutes = require("../subscriptionRoutes");
const app = express();
app.use(express.json());
app.use("/subscriptions", subscriptionRoutes);

// ── Helpers ──────────────────────────────────────────────────────────────────

const mockUser = { id: 1, email: "test@example.com" };
const authHeader = { Authorization: "Bearer mock_token" };

const mockSub = {
    id: 1,
    name: "Netflix",
    cost: 15.99,
    category: "Streaming",
    billing_cycle: "Monthly (30 days)"
};

// Make jwt.verify return our mock user for all authenticated requests
beforeEach(() => {
    jest.clearAllMocks();
    jwt.verify.mockReturnValue(mockUser);
});

// ── Auth middleware ───────────────────────────────────────────────────────────

describe("Auth middleware", () => {

    test("401 - rejects requests with no token", async () => {
        const res = await request(app).get("/subscriptions");
        expect(res.status).toBe(401);
        expect(res.body.error).toMatch(/missing or invalid token/i);
    });

    test("401 - rejects requests with invalid token", async () => {
        jwt.verify.mockImplementation(() => { throw new Error("invalid"); });

        const res = await request(app)
            .get("/subscriptions")
            .set("Authorization", "Bearer bad_token");

        expect(res.status).toBe(401);
        expect(res.body.error).toMatch(/expired or invalid/i);
    });
});

// ── GET /subscriptions ────────────────────────────────────────────────────────

describe("GET /subscriptions", () => {

    test("200 - returns user subscriptions", async () => {
        getSubscriptionsByUser.mockResolvedValue([mockSub]);

        const res = await request(app)
            .get("/subscriptions")
            .set(authHeader);

        expect(res.status).toBe(200);
        expect(res.body).toHaveLength(1);
        expect(res.body[0].name).toBe("Netflix");
        expect(getSubscriptionsByUser).toHaveBeenCalledWith(mockUser.id);
    });

    test("200 - returns empty array when user has no subscriptions", async () => {
        getSubscriptionsByUser.mockResolvedValue([]);

        const res = await request(app)
            .get("/subscriptions")
            .set(authHeader);

        expect(res.status).toBe(200);
        expect(res.body).toHaveLength(0);
    });
});

// ── GET /subscriptions/categories ─────────────────────────────────────────────

describe("GET /subscriptions/categories", () => {

    test("200 - returns global and user categories", async () => {
        const mockCategories = [
            { id: 1, name: "Streaming",  user_id: null },
            { id: 8, name: "Utilities",  user_id: 1 },
        ];
        db.query.mockResolvedValue([mockCategories]);

        const res = await request(app)
            .get("/subscriptions/categories")
            .set(authHeader);

        expect(res.status).toBe(200);
        expect(res.body).toHaveLength(2);
    });
});

// ── GET /subscriptions/billing-cycles ─────────────────────────────────────────

describe("GET /subscriptions/billing-cycles", () => {

    test("200 - returns billing cycles", async () => {
        const mockCycles = [
            { id: 1, name: "Monthly (30 days)", user_id: null },
            { id: 2, name: "Yearly",            user_id: null },
        ];
        db.query.mockResolvedValue([mockCycles]);

        const res = await request(app)
            .get("/subscriptions/billing-cycles")
            .set(authHeader);

        expect(res.status).toBe(200);
        expect(res.body).toHaveLength(2);
    });
});

// ── POST /subscriptions ───────────────────────────────────────────────────────

describe("POST /subscriptions", () => {

    test("201 - creates a subscription successfully", async () => {
        createSubscription.mockResolvedValue({ id: 1, ...mockSub });

        const res = await request(app)
            .post("/subscriptions")
            .set(authHeader)
            .send({ name: "Netflix", cost: 15.99, category_id: 1, billing_cycle_id: 4 });

        expect(res.status).toBe(201);
        expect(res.body.name).toBe("Netflix");
        expect(createSubscription).toHaveBeenCalledWith(
            expect.objectContaining({ user_id: mockUser.id, name: "Netflix" })
        );
    });

    test("400 - rejects missing name", async () => {
        const res = await request(app)
            .post("/subscriptions")
            .set(authHeader)
            .send({ cost: 15.99, category_id: 1, billing_cycle_id: 4 });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/name is required/i);
    });

    test("400 - rejects cost of zero or less", async () => {
        const res = await request(app)
            .post("/subscriptions")
            .set(authHeader)
            .send({ name: "Netflix", cost: -5, category_id: 1, billing_cycle_id: 4 });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/positive number/i);
    });

    test("400 - rejects missing category_id", async () => {
        const res = await request(app)
            .post("/subscriptions")
            .set(authHeader)
            .send({ name: "Netflix", cost: 15.99, billing_cycle_id: 4 });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/category_id/i);
    });

    test("400 - rejects missing billing_cycle_id", async () => {
        const res = await request(app)
            .post("/subscriptions")
            .set(authHeader)
            .send({ name: "Netflix", cost: 15.99, category_id: 1 });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/billing_cycle_id/i);
    });
});

// ── POST /subscriptions/categories/custom ─────────────────────────────────────

describe("POST /subscriptions/categories/custom", () => {

    test("201 - creates a custom category", async () => {
        db.query
            .mockResolvedValueOnce([[]])                    // no duplicate
            .mockResolvedValueOnce([{ insertId: 8 }]);     // insert

        const res = await request(app)
            .post("/subscriptions/categories/custom")
            .set(authHeader)
            .send({ name: "Utilities" });

        expect(res.status).toBe(201);
        expect(res.body.name).toBe("Utilities");
        expect(res.body.user_id).toBe(mockUser.id);
    });

    test("409 - rejects duplicate category name", async () => {
        db.query.mockResolvedValueOnce([[{ id: 1 }]]); // duplicate found

        const res = await request(app)
            .post("/subscriptions/categories/custom")
            .set(authHeader)
            .send({ name: "Streaming" });

        expect(res.status).toBe(409);
        expect(res.body.error).toMatch(/already exists/i);
    });

    test("400 - rejects empty name", async () => {
        const res = await request(app)
            .post("/subscriptions/categories/custom")
            .set(authHeader)
            .send({ name: "" });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/name is required/i);
    });
});

// ── POST /subscriptions/billing-cycles/custom ────────────────────────────────

describe("POST /subscriptions/billing-cycles/custom", () => {

    test("201 - creates a custom billing cycle", async () => {
        db.query
            .mockResolvedValueOnce([[]])                   // no duplicate
            .mockResolvedValueOnce([{ insertId: 7 }]);    // insert

        const res = await request(app)
            .post("/subscriptions/billing-cycles/custom")
            .set(authHeader)
            .send({ name: "Every 2 months" });

        expect(res.status).toBe(201);
        expect(res.body.name).toBe("Every 2 months");
    });

    test("409 - rejects duplicate billing cycle", async () => {
        db.query.mockResolvedValueOnce([[{ id: 1 }]]); // duplicate found

        const res = await request(app)
            .post("/subscriptions/billing-cycles/custom")
            .set(authHeader)
            .send({ name: "Monthly (30 days)" });

        expect(res.status).toBe(409);
        expect(res.body.error).toMatch(/already exists/i);
    });
});

// ── PUT /subscriptions/:id ────────────────────────────────────────────────────

describe("PUT /subscriptions/:id", () => {

    test("200 - updates a subscription successfully", async () => {
        updateSubscription.mockResolvedValue({ message: "Updated", id: 1 });

        const res = await request(app)
            .put("/subscriptions/1")
            .set(authHeader)
            .send({ name: "Netflix Premium", cost: 22.99, category_id: 1, billing_cycle_id: 4 });

        expect(res.status).toBe(200);
        expect(res.body.message).toBe("Updated");
        expect(updateSubscription).toHaveBeenCalledWith("1", mockUser.id, expect.any(Object));
    });

    test("403 - rejects update on another user's subscription", async () => {
        updateSubscription.mockRejectedValue(new Error("Subscription not found or unauthorized"));

        const res = await request(app)
            .put("/subscriptions/99")
            .set(authHeader)
            .send({ name: "Netflix", cost: 15.99, category_id: 1, billing_cycle_id: 4 });

        expect(res.status).toBe(403);
        expect(res.body.error).toMatch(/unauthorized/i);
    });

    test("400 - rejects invalid cost on update", async () => {
        const res = await request(app)
            .put("/subscriptions/1")
            .set(authHeader)
            .send({ name: "Netflix", cost: 0, category_id: 1, billing_cycle_id: 4 });

        expect(res.status).toBe(400);
        expect(res.body.error).toMatch(/positive number/i);
    });
});

// ── DELETE /subscriptions/:id ─────────────────────────────────────────────────

describe("DELETE /subscriptions/:id", () => {

    test("200 - deletes a subscription successfully", async () => {
        deleteSubscription.mockResolvedValue({ message: "Deleted", id: 1 });

        const res = await request(app)
            .delete("/subscriptions/1")
            .set(authHeader);

        expect(res.status).toBe(200);
        expect(res.body.message).toBe("Deleted");
        expect(deleteSubscription).toHaveBeenCalledWith("1", mockUser.id);
    });

    test("403 - rejects delete on another user's subscription", async () => {
        deleteSubscription.mockRejectedValue(new Error("Subscription not found or unauthorized"));

        const res = await request(app)
            .delete("/subscriptions/99")
            .set(authHeader);

        expect(res.status).toBe(403);
        expect(res.body.error).toMatch(/unauthorized/i);
    });
});
