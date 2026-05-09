# Smart Supply — Android Mobile Application

A corporate procurement and supply chain management app for Android, built in Kotlin. It allows employees to submit service requests across categories (Kitchen, Stationary, Furniture, Events), track their order status in real time, and have those requests reviewed and priced by an administrator — all backed by Firebase.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Architecture](#3-architecture)
4. [Package Structure](#4-package-structure)
5. [Screens and Navigation Flow](#5-screens-and-navigation-flow)
6. [Data Models](#6-data-models)
7. [Firebase Integration](#7-firebase-integration)
8. [Key Components](#8-key-components)
9. [Service Categories and Sub-Services](#9-service-categories-and-sub-services)
10. [UI and Design System](#10-ui-and-design-system)
11. [Build Configuration](#11-build-configuration)

---

## 1. Project Overview

Smart Supply is a B2B mobile application that digitises the internal supply request workflow for corporate organisations. Instead of handling procurement requests through email or paper, employees raise requests through the app and administrators process them.

**Core workflow:**
1. Employee registers / logs in
2. Selects a service category and sub-service from the home screen or the New Request form
3. Fills in contact details, quantity, location, and required date
4. Submits — the request appears immediately in My Requests with status **Pending**
5. An administrator opens the request, sets a price (PKR), and marks it **Completed** or **Rejected**
6. The employee sees the updated status and price in real time

**Target market:** Corporate offices in Pakistan (PKR currency).

---

## 2. Technology Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Platform | Android (native) |
| Min SDK | API 24 — Android 7.0 Nougat |
| Target SDK | API 36 |
| Build System | Gradle 9.2.1 with Kotlin DSL (.kts) |
| Android Gradle Plugin | 9.0.1 |
| UI | XML layouts, View Binding |
| UI Components | Material3 (Theme.Material3.DayNight.NoActionBar) |
| Authentication | Firebase Authentication (email/password) |
| Database | Cloud Firestore (NoSQL, real-time) |
| Firebase BOM | 34.0.0 |
| Architecture Pattern | MVVM (Model — ViewModel — View) |

---

## 3. Architecture

The project follows the **MVVM (Model-View-ViewModel)** pattern, which separates concerns into three layers:

```
┌─────────────────────────────────────────────────┐
│  VIEW LAYER  (Activities)                        │
│  HomeActivity, OrdersActivity, NewRequestActivity│
│  OrderDetailActivity, ProfileActivity, etc.      │
│  Observe LiveData / call ViewModel methods       │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│  VIEWMODEL LAYER                                 │
│  MainViewModel                                   │
│  Holds LiveData, processes business logic,       │
│  calls Repository methods                        │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│  MODEL / REPOSITORY LAYER                        │
│  OrderRepository — all Firestore order CRUD      │
│  AuthRepository  — Firebase Auth + user profile  │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│  FIREBASE BACKEND                                │
│  Firebase Auth — identity management             │
│  Cloud Firestore — orders and user collections   │
└─────────────────────────────────────────────────┘
```

**Key architectural decisions:**
- **No custom backend / REST API.** Firebase SDKs are called directly from the Android app. Firebase Security Rules (on the Firebase Console) enforce data access control.
- **Firestore real-time listeners** (`addSnapshotListener`) are used in `OrdersActivity` so the list updates live without the user refreshing.
- **Callbacks over coroutines** — all async Firebase calls use `onSuccess`/`onError` lambda callbacks for simplicity.
- **Repository pattern** isolates all Firebase calls. Activities never touch Firestore directly.

---

## 4. Package Structure

```
com.example.mobileapplicationsmartsupply/
│
├── auth/
│   ├── model/
│   │   └── AuthRepository.kt       Firebase Auth + Firestore user profile ops
│   └── view/
│       ├── SplashActivity.kt        Entry point, checks auth state, redirects
│       ├── LoginActivity.kt         Email/password login
│       ├── RegisterActivity.kt      Account creation + Firestore profile write
│       └── ForgotPasswordActivity.kt  Password reset email
│
├── main/
│   ├── model/
│   │   └── OrderRepository.kt      All Firestore CRUD for orders collection
│   ├── view/
│   │   ├── HomeActivity.kt          Dashboard with category shortcuts
│   │   ├── OrdersActivity.kt        My Requests list with real-time listener
│   │   ├── NewRequestActivity.kt    Request form (category → sub-service → details)
│   │   ├── OrderDetailActivity.kt   Order detail + admin price/status actions
│   │   ├── ProfileActivity.kt       User profile display + logout
│   │   ├── OrderAdapter.kt          RecyclerView adapter for order cards
│   │   ├── BottomNavHelper.kt       Singleton: sets up bottom navigation across screens
│   │   └── LoadingDialog.kt         Programmatic loading spinner dialog
│   └── viewmodel/
│       └── MainViewModel.kt         LiveData for home screen stats and recent orders
│
├── data/
│   └── model/
│       ├── Order.kt                 Order data class (mirrors Firestore document)
│       └── User.kt                  User data class (mirrors Firestore document)
│
├── BaseActivity.kt                  Abstract base — handles system window insets
└── MainActivity.kt                  Navigation host (legacy entry point)
```

---

## 5. Screens and Navigation Flow

```
SplashActivity (LAUNCHER)
        │
        ├─ not logged in ──► LoginActivity ──► RegisterActivity
        │                          │               │
        │                          └───────────────┘
        │                          ForgotPasswordActivity
        │
        └─ logged in ────► HomeActivity
                                │
                    ┌───────────┼──────────────┐
                    │           │              │
              (category      (New        (bottom nav)
               shortcut)   Request btn)
                    │           │
                    └─────┬─────┘
                          │
                   NewRequestActivity
                   (category → sub-service → form → submit)
                          │
                    (on success)
                          │
                   OrdersActivity  ◄── bottom nav "Request"
                   (My Requests list, real-time)
                          │
                   (tap an order)
                          │
                   OrderDetailActivity
                   (view full details, admin: set price, mark complete/reject)

                   ProfileActivity ◄── bottom nav "Profile"
                   (view profile, logout)
```

**Navigation rules:**
- Bottom navigation uses `FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP` so re-tapping a tab does not stack duplicate activities.
- Back navigation uses `onBackPressedDispatcher.onBackPressed()` (API 33+ compatible).
- Logout clears the entire back stack with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` before starting LoginActivity.

---

## 6. Data Models

### Order

Stored in the Firestore `orders` collection. Each document maps to this class:

```kotlin
data class Order(
    val id: String = "",          // Firestore document ID (set on read via .copy())
    val userId: String = "",      // Firebase Auth UID of the submitter
    val requestId: String = "",   // Human-readable ID, e.g. "REQ-84732"
    val title: String = "",       // e.g. "Kitchen Services"
    val category: String = "",    // "Kitchen" | "Stationary" | "Furniture" | "Events"
    val subService: String = "",  // e.g. "Daily Catering"
    val description: String = "", // "Request by Name — phone"
    val quantity: Int = 0,        // No. of units / people
    val address: String = "",     // Delivery/service location
    val date: String = "",        // Required date (DD / MM / YYYY)
    val price: String = "",       // "PKR —" until admin sets it, then "PKR 5000"
    val status: String = ""       // "Pending" | "Completed" | "Rejected"
)
```

**Firestore write:** `OrderRepository.addOrder()` writes a `hashMapOf` explicitly (not `toObject`) plus a `createdAt` Timestamp field used for ordering. `subService` is included in the map.

**Firestore read:** `doc.toObject(Order::class.java)?.copy(id = doc.id)` — all fields with defaults means missing fields (e.g. `subService` on old records) safely deserialise to `""`.

### User

Stored in the Firestore `users` collection, document ID = Firebase Auth UID:

```kotlin
data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val company: String = ""
)
```

---

## 7. Firebase Integration

### Authentication (Firebase Auth)
- **Provider:** Email/password only.
- **Register:** `createUserWithEmailAndPassword()` → on success, writes user profile document to `users/{uid}` in Firestore.
- **Login:** `signInWithEmailAndPassword()`.
- **Session persistence:** Firebase SDK handles session persistence automatically; `SplashActivity` checks `auth.currentUser != null` to decide where to route on launch.
- **Logout:** `auth.signOut()` then clears the activity stack.
- **Password reset:** `sendPasswordResetEmail()` sends a reset link to the user's email.

### Cloud Firestore

**Collections:**

| Collection | Document ID | Description |
|---|---|---|
| `users` | Firebase Auth UID | User profile created at registration |
| `orders` | Auto-generated | One document per service request |

**Order queries:**
```
orders
  .where("userId", "==", currentUserId)   -- user sees only their own orders
  .orderBy("createdAt", DESCENDING)        -- newest first
```

**Real-time listener (`OrdersActivity`):**
```kotlin
collection
    .whereEqualTo("userId", currentUserId())
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .addSnapshotListener { snapshot, _ -> ... }
```
The listener is registered in `onStart()` and removed in `onStop()` to prevent memory leaks and unnecessary reads when the activity is not visible.

**One-shot fetch (`HomeActivity`, `OrderDetailActivity`):**
Uses `.get()` with `addOnSuccessListener` / `addOnFailureListener` callbacks.

**Note:** The `orderBy("createdAt")` query requires a Firestore **composite index** on `(userId ASC, createdAt DESC)`. If this index does not exist in the Firebase Console, the query silently returns zero results. The index must be created manually in the Firebase Console under Firestore → Indexes.

---

## 8. Key Components

### BaseActivity
All activities extend `BaseActivity` instead of `AppCompatActivity`. It calls `WindowCompat.setDecorFitsSystemWindows(window, true)` **after** `super.onCreate()` to prevent app content from being drawn behind the system status bar — necessary because Material3 v1.13+ automatically enables edge-to-edge during `super.onCreate()`.

### BottomNavHelper (Singleton Object)
Manages the three-tab bottom navigation bar (Home, Request, Profile) present on all main screens. `setup(activity, activeTab)` highlights the active tab in gold and wires up click listeners to launch the correct activity with `CLEAR_TOP | SINGLE_TOP` flags.

### OrderAdapter (RecyclerView.Adapter)
Binds a `List<Order>` to `item_order.xml` cards. Each card shows:
- Title (e.g. "Kitchen Services") + sub-service in gold (hidden if empty, for backward compatibility with old records)
- Gray description line (requester name/phone)
- Status badge (gold = Pending, green = Completed, red = Rejected)
- Price in gold
- Request ID and date in the footer

Card border colour changes to match status (gold/green/red border drawable).

### LoadingDialog
A programmatic `AlertDialog` with a custom gold `ProgressBar` and a message. No XML layout required. Called with `LoadingDialog.show(context, message)` and dismissed with `LoadingDialog.dismiss()`. Used on every network operation to block the UI during Firestore calls.

### NewRequestActivity — Form Validation Chain
Uses a `when` expression to check fields in order and short-circuit on the first failure:
1. Category selected
2. Sub-service selected
3. Full name not empty
4. Phone not empty
5. Email not empty and valid format (`Patterns.EMAIL_ADDRESS`)
6. Location not empty
7. Quantity not empty
8. Date not empty
9. Date valid format (DD/MM/YYYY, separators flexible, year ≥ 2024)

Only when all pass does it call `saveRequest()`.

---

## 9. Service Categories and Sub-Services

When a user selects a category, a "Service Type" dropdown appears in the form. Tapping it opens a radio-button dialog. The selected sub-service is saved to Firestore as `subService` and displayed in gold on the order card in My Requests.

| Kitchen | Stationary | Furniture | Events |
|---|---|---|---|
| Daily Catering | Office Supplies | Office Chairs | Conference Setup |
| Tea & Beverages | Printing & Photocopying | Desks & Tables | Corporate Dinner |
| Snacks & Refreshments | Filing & Storage | Storage Cabinets | Training Workshop |
| Pantry Restocking | Printer Cartridges | Reception Furniture | Product Launch |
| Kitchen Equipment | Desk Accessories | Partitions & Dividers | Team Building |

---

## 10. UI and Design System

**Theme:** `Theme.Material3.DayNight.NoActionBar` — forced dark/brown at runtime by setting `bg_dark` as the window background.

**Colour palette:**

| Token | Value | Usage |
|---|---|---|
| `bg_dark` | Deep dark brown (`#1A0F00` approx) | Screen backgrounds, status bar |
| `bg_card` | Slightly lighter brown | Card backgrounds, nav bar |
| `gold` | `#FFC107` approx | Headings, active icons, accents, price |
| `white` | `#FFFFFF` | Body text, input text |
| `hint_gray` | Medium gray | Placeholder text, secondary info |
| `active_green` | Green | Completed status |
| `status_rejected` | Red | Rejected status |

**Key drawables:**
- `bg_gold_button` / `bg_gold_button_pill` — gold filled button backgrounds
- `bg_input_field` — dark rounded rectangle for text inputs
- `bg_card_gold_border` — card with gold border (Pending orders, Personal Info card)
- `bg_card_green_border` / `bg_card_red_border` — Completed / Rejected order cards
- `bg_category_icon` — default dark tile for category icons
- `bg_status_pending` / `bg_status_complete` / `bg_status_rejected` — status badge backgrounds

**Bottom navigation:** Custom `bottom_nav.xml` layout included via `<include>` in all main screen layouts. Three tabs: Home (`ic_home`), Request (`ic_request`), Profile (`ic_profile`). Active tab icon tinted gold by `BottomNavHelper`.

---

## 11. Build Configuration

**`app/build.gradle.kts`:**
```kotlin
plugins {
    alias(libs.plugins.android.application)     // AGP via version catalog
    id("com.google.gms.google-services")        // Firebase plugin
}

android {
    namespace = "com.example.mobileapplicationsmartsupply"
    compileSdk { version = release(36) }
    defaultConfig {
        applicationId = "com.example.mobileapplicationsmartsupply"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures { viewBinding = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)                    // Material3
    implementation(libs.androidx.recyclerview)

    // Firebase (BOM manages all versions)
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
}
```

**`google-services.json`:** Must be present in `app/` (downloaded from Firebase Console → Project Settings → Android app). Contains the project's API keys, project ID, and app ID. Not committed to version control.

**`values-v35/themes.xml`:** Overrides the theme on API 35+ (Android 15) to opt out of the system's automatic edge-to-edge enforcement (`windowOptOutEdgeToEdgeEnforcement = true`), combined with `BaseActivity`'s inset handling for a consistent layout on all API levels.
