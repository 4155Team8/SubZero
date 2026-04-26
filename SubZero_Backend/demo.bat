@echo off
setlocal

REM ============================================================
REM  SubZero — Demo Setup & Start
REM  Initialises MySQL, seeds demo data, then starts the server.
REM  Run from the SubZero_Backend directory.
REM ============================================================

REM -- Read credentials from .env so they stay in one place ----
for /f "tokens=1,2 delims==" %%A in (.env) do (
    if "%%A"=="DB_USER"     set DB_USER=%%B
    if "%%A"=="DB_PASSWORD" set DB_PASSWORD=%%B
    if "%%A"=="DB_NAME"     set DB_NAME=%%B
    if "%%A"=="DB_HOST"     set DB_HOST=%%B
    if "%%A"=="DB_PORT"     set DB_PORT=%%B
)

REM Defaults if .env keys are missing
if "%DB_USER%"==""     set DB_USER=root
if "%DB_HOST%"==""     set DB_HOST=localhost
if "%DB_PORT%"==""     set DB_PORT=3306
if "%DB_NAME%"==""     set DB_NAME=subzero_db

set MYSQL_CMD=mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% --batch

echo.
echo  ===================================
echo   SubZero  ^|  Demo Setup
echo  ===================================
echo.

REM ── Step 1 : Start MySQL ────────────────────────────────────
echo [1/4] Starting MySQL service...
REM Try common Windows service names in order
sc query MySQL80 >nul 2>&1 && net start MySQL80 >nul 2>&1 && goto :mysql_ok
sc query MySQL   >nul 2>&1 && net start MySQL   >nul 2>&1 && goto :mysql_ok
sc query MySQL57 >nul 2>&1 && net start MySQL57 >nul 2>&1 && goto :mysql_ok
sc query MySQL84 >nul 2>&1 && net start MySQL84 >nul 2>&1 && goto :mysql_ok
echo  MySQL service not found or already running — continuing.

:mysql_ok
REM Verify we can actually connect
%MYSQL_CMD% -e "SELECT 1;" >nul 2>&1
if errorlevel 1 (
    echo.
    echo  ERROR: Cannot connect to MySQL.
    echo  Check that MySQL is running and that DB_USER / DB_PASSWORD in .env are correct.
    echo.
    pause
    exit /b 1
)
echo  MySQL is up.

REM ── Step 2 : Apply schema ───────────────────────────────────
echo.
echo [2/4] Applying schema...
%MYSQL_CMD% < schema.sql
if errorlevel 1 (
    echo  ERROR: schema.sql failed.
    pause
    exit /b 1
)
echo  Schema applied.

REM ── Step 3 : Seed demo data ─────────────────────────────────
echo.
echo [3/4] Seeding demo data...

REM Create demo user accounts first (bcrypt hash generated via Node)
node scripts\create_demo_users.js
if errorlevel 1 (
    echo  ERROR: Could not create demo users.
    pause
    exit /b 1
)

REM Seed lookup tables + User 1 subscriptions / reminders
%MYSQL_CMD% < seed.sql
if errorlevel 1 (
    echo  ERROR: seed.sql failed.
    pause
    exit /b 1
)

REM Seed User 2 subscriptions / reminders
%MYSQL_CMD% < seed2.sql
if errorlevel 1 (
    echo  ERROR: seed2.sql failed.
    pause
    exit /b 1
)

echo  Demo data seeded.

REM ── Step 4 : Start server ───────────────────────────────────
echo.
echo [4/4] Starting SubZero server...
echo.
echo  ┌─────────────────────────────────────────┐
echo  │  Server  :  http://localhost:3000        │
echo  │                                          │
echo  │  Account 1                               │
echo  │    Email   :  demo1@subzero.com          │
echo  │    Password:  Demo1234!                  │
echo  │                                          │
echo  │  Account 2                               │
echo  │    Email   :  demo2@subzero.com          │
echo  │    Password:  Demo1234!                  │
echo  │                                          │
echo  │  Press Ctrl+C to stop.                   │
echo  └─────────────────────────────────────────┘
echo.
node index.js

endlocal
