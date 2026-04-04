// __mocks__/db.js
// Jest auto-mock for the database pool.
// All tests use jest.mock('../db') which picks this up automatically.

const db = {
    query: jest.fn(),
};

module.exports = db;
