# SubZero

An Android application for tracking personal subscriptions, monitoring monthly spending, and receiving renewal reminders.

---

## Current Functionality

### Authentication
- User registration with email and password
- Login with JWT session persistence
- Forgot password flow (sends reset email)
- Logout and session clearing

### Insights
- Displays total monthly spend across all subscriptions
- Groups subscriptions by category with per-category totals
- Normalises costs across billing cycles (daily, weekly, biweekly, monthly, yearly) to a monthly equivalent
- Donut chart visualisation of spending by category
- Sorted category breakdown with individual subscription rows

### Alerts
- Loads upcoming and past subscription renewal reminders
- Displays reminder name, description, and relative date (e.g. "Tomorrow", "In 3 days", "2 days ago")
- Shows count of active alerts

### Profile
- Displays user name, email, and member since date
- Shows total number of active subscriptions
- Notifications enabled/disabled status

### Personal Info
- Update display name
- Update email address (session is updated on success)
- Update password with strength validation (length, uppercase, lowercase, digit, special character)

---

## Setup Guide

### Prerequisites
- Android Studio
- JDK 17+
- Node.js (for the backend server)
- An Android emulator or physical device running API 26+ (although the medium phone API that comes with android studio works best)

### Backend
The app connects to a local Node.js server. The emulator reaches it via `10.0.2.2:3000`.

1. Navigate to your backend directory
2. Install dependencies:
   ```
   npm install
   ```
3. Start the server:
   ```
   node index.js
   ```
   The server must be running on port **3000** before launching the app.

## SQL
For this project, we use MySQL and our schema is modeled off of that.
All one has to do is download MySQL Server 8.0 or later, install it,
run it, and use the schema and seed files as a source, and with the node
backend running, the app will work as intended.

### Android App
1. Open the project in Android Studio
2. Let Gradle sync complete
3. Run the app on an emulator (the base URL is hardcoded to `10.0.2.2:3000` for emulator use)
4. If running on a physical device, update `BASE_URL` in `ApiClient.kt` to your machine's local IP address

### Running Tests
Tests are located in `app/src/test/` and use JUnit4 + Robolectric. Run them via:
In Android Studio, right-click the `test` directory -> **Run Tests**


## Directory

```
app/src/
├── main/
│   └── java/com/example/subzero/
│       │
│       │
│       │
│       │
│       ├── global/                         # Shared utilities
│       │   ├── ApiCalls.kt                 # Wrapper functions for all authenticated API calls
│       │   └── Utility.kt                  # Date formatting and relative time helpers
│       │
│       ├── views/                          # Custom views
│       │   └── DonutChartView.kt           # Canvas-drawn donut chart
│       │
│       ├── MainActivity.kt                 # Login screen
│       ├── RegisterActivity.kt             # Registration screen
│       ├── ForgotPasswordActivity.kt       # Password reset screen
│       ├── InsightsActivity.kt             # Spending insights and subscription breakdown
│       ├── AlertsActivity.kt               # Renewal reminders list
│       ├── ProfileActivity.kt              # User profile overview
│       ├── PersonalInfoActivity.kt         # Edit name, email, password
│       ├── SessionManager.kt               # JWT token storage via SharedPreferences
│       ├── FirstFragment.kt                # Default navigation fragment (scaffold)
│       ├── ApiClient.kt                    # Retrofit instance and test injection
│       ├── ApiService.kt                   # Endpoint definitions and request/response models
│       └── AuthRepository.kt               # Login and registration logic
│
└── test/
    └── java/com/example/subzero/
        │
        ├── MainActivityTest.kt             # Login input validation
        ├── RegisterActivityTest.kt         # Password strength and registration validation
        ├── ForgotPasswordActivityTest.kt   # Email validation for password reset
        ├── SessionManagerTest.kt           # Session save, retrieve, clear
        ├── InsightsActivityTest.kt         # Billing normalisation, grouping, sorting
        ├── ApiCallsResponseTest.kt         # Profile response mapping and fallback values
        ├── AuthRepositoryTest.kt           # Login/register success and error handling
        ├── UtilityTest.kt                  # Date formatting and timeAgo logic
        └── DonutChartViewTest.kt           # Slice storage and rendering state
```
