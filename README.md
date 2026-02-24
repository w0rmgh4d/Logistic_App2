# Log Truck - Mobile Driver Application

A specialized Android application designed for logistics drivers and field personnel. This app serves as the mobile component of the 2NDJLSU Logistics Support Unit, allowing real-time communication between drivers and administrators.

## 🚀 Key Features

### 🗺️ Advanced Mapping & Navigation
- **Real-time Map Integration**: Uses OSMDroid (OpenStreetMap) to provide live mapping without proprietary API costs.
- **Driver Pinpointing**: Real-time tracking of the driver's current location with a dedicated "Locate Me" button.
- **Destination Markers**: Clear visual markers for dispatch targets.
- **External GPS Support**: One-tap transition to Google Maps for professional turn-by-turn voice navigation.

### 📦 Dispatch Management
- **Acceptance Workflow**: Drivers receive new dispatches and can review details (truck, supplies, destination) before accepting.
- **Live Status Updates**: Update the admin dashboard in real-time with statuses like "Ongoing", "Stop Over", or "Delayed".
- **Delivery Confirmation**: Capture receiver names and take/upload "Proof of Delivery" photos directly to Cloudinary.

### ⚠️ Safety & Reporting
- **Emergency SOS**: A dedicated emergency screen to report urgent issues immediately to the command center.
- **Delay Reporting**: Specifically report traffic, mechanical issues, or other delays with estimated time impacts.
- **Stop-Over Logging**: Keep administrators informed during rest periods or refueling.

### 🛠️ Technical Highlights
- **Persistent Login**: Uses Firebase Authentication to keep drivers logged in across app restarts.
- **Modern UI**: Built entirely with **Jetpack Compose** following Material 3 design guidelines.
- **Asynchronous Data**: Uses Kotlin Coroutines and StateFlow for reactive, lag-free data updates from Firebase Firestore.
- **Optimized Boot**: Custom splash screen implementation using the Android 12+ SplashScreen API with a tailored 2-second branding duration.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Navigation**: Jetpack Navigation Compose
- **Backend**: Firebase Auth & Cloudinary (Image Storage)
- **Database**: Firebase Firestore (Real-time NoSQL)
- **Maps**: OSMDroid (OpenStreetMap)
- **Image Loading**: Coil
- **Architecture**: MVVM (Model-View-ViewModel)

## 📁 Project Structure

```text
app/src/main/java/com/example/logistic_app/
├── data/model/          # Data classes (Dispatch, Personnel, SupplyItem)
├── navigation/          # NavGraph and Screen route definitions
├── ui/
│   ├── components/      # Reusable UI (Map, Custom Buttons, Photo Boxes)
│   ├── screens/         # Feature screens (Dispatch, Login, Profile, SOS)
│   ├── theme/           # Color schemes, Typography, and Shapes
│   └── viewmodel/       # Business logic and Firebase integration
└── utils/               # Helpers (Cloudinary, formatters)
```

## ⚙️ Setup & Installation

1. **Prerequisites**:
   - Android Studio Ladybug or newer.
   - A `google-services.json` file in the `app/` directory (connected to your Firebase project).
2. **Permissions**:
   - The app requires **Internet** and **Fine Location** permissions to function correctly.
3. **Build**:
   - Sync Gradle to download dependencies (OSMDroid, Firebase, etc.).
   - Build and run on a physical device for the best GPS experience.

## 📝 Usage
1. **Login**: Use your assigned personnel credentials.
2. **View Dispatch**: Check the main dashboard for active assignments.
3. **Navigate**: Use the map or the navigation button to find the target.
4. **Report**: Use the action chips (Delivered, Stop Over, Delay) to keep the system updated.

---
© 2025 APFLSC - Logistics Support Unit, Palawan
