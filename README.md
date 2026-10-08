## Repository Structure

This repository is organized as a monorepo containing two decoupled projects:

```text
├── android/          # Android Studio application (Mobile client)
├── server/           # Java Spring Boot backend service (REST API)
├── .gitignore        # Unified Git ignore rules
└── README.md         # Project documentation and onboarding guide
```

---

## Prerequisites

Before getting started, ensure you have the following installed on your machine:

- **Git**: [Download Git](https://git-scm.com/)
- **Java Development Kit (JDK)**: Version 17+
- **Android Studio**: Ladybug / Hedgehog or newer (includes Android SDK & Emulator)
- **IntelliJ IDEA**: Ultimate or Community Edition
- **Database**: PostgreSQL hosted on Supabase.

---

## Getting Started

### 1. Clone the Repository

Clone the full repository to your local machine:

```bash
git clone https://github.com/WSU-4110/Pending
cd Pending
```

> **Important**: Do **not** open the root folder directly in your IDEs. Android Studio and IntelliJ should open their respective subdirectories (`android/` and `server/`) independently to avoid Gradle build conflicts.

---

### 2. Android App Setup (`android/`)

1. Launch **Android Studio**.
2. Select **Open** (or **File > Open**).
3. Navigate to and select the **`android`** folder inside the cloned repo.
4. Allow Android Studio to:
   - Run the initial Gradle Sync.
   - Automatically generate `local.properties` pointing to your local Android SDK.
5. Create an Android Virtual Device (AVD) via the **Device Manager** or plug in a physical device with USB debugging enabled.
6. Press **Run** (`Shift + F10`) to build and deploy the app.

> **Network Note**: When testing against the local Spring Boot backend from an Android Emulator, use `http://10.0.2.2:<port>` instead of `http://localhost:<port>`.

---

### 3. Backend Server Setup (`server/`)

1. Launch **IntelliJ IDEA**.
2. Select **Open** (or **File > Open**).
3. Navigate to and select the **`server`** folder inside the cloned repo.
4. When prompted, open as a **Project** (IntelliJ will auto-detect the `build.gradle` or `pom.xml`).
5. Ensure the Project SDK is configured correctly:
   - Go to **File > Project Structure > Project > SDK**.
   - Select your installed JDK version.
6. Configure local properties / secrets:
   - Create your local configuration file (e.g., `src/main/resources/application-local.properties` or `.env`) for api/secret keys

---