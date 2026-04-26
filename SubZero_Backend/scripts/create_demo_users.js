/**
 * Creates two demo accounts if they don't already exist.
 * Run before seeding subscription/reminder data so that
 * the user IDs (1 and 2) referenced in seed.sql are present.
 *
 * Demo credentials
 *   demo1@subzero.com / Demo1234!
 *   demo2@subzero.com / Demo1234!
 */

const bcrypt  = require('bcrypt');
const mysql   = require('mysql2/promise');
const path    = require('path');
require('dotenv').config({ path: path.join(__dirname, '..', '.env') });

async function main() {
    const conn = await mysql.createConnection({
        host:     process.env.DB_HOST     || 'localhost',
        port:     parseInt(process.env.DB_PORT) || 3306,
        user:     process.env.DB_USER     || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME     || 'subzero_db',
    });

    const hash = await bcrypt.hash('Demo1234!', 10);

    // INSERT IGNORE keeps the script idempotent — safe to re-run
    await conn.execute(
        `INSERT IGNORE INTO users (id, name, email, password_hash, reminders_enabled)
         VALUES
           (1, 'Alex Demo', 'demo1@subzero.com', ?, TRUE),
           (2, 'Sam Demo',  'demo2@subzero.com', ?, TRUE)`,
        [hash, hash]
    );

    console.log('  Demo users ready.');
    await conn.end();
}

main().catch(err => {
    console.error('  ERROR creating demo users:', err.message);
    process.exit(1);
});
