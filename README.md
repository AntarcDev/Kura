# 蔵 Kura

**The Premium Native Client for Kemono**

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white) ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white) ![Material 3](https://img.shields.io/badge/Material%203-7C4DFF?style=for-the-badge&logo=materialdesign&logoColor=white)

Unlock a seamless browsing experience with **Kura** (蔵 - "Storehouse"). Built for speed, aesthetics, and organization, Kura transforms how you discover and archive content.

---

## ✨ Features

### 🎨 Immersive Discovery
*   **Smart Search**: Instantly find creators with real-time filtering by name and service.
*   **Dynamic Layouts**: Switch between "Compact" grids for speed or "Comfortable" lists for detail.
*   **Advanced Sorting**: Sort by Name, Updated Date, or Popularity to find exactly what you want.
*   **Tag Filtering**: Filter posts by tags to find specific content within a creator's feed.
*   **Infinite Scrolling**: Seamlessly browse through thousands of posts without interruption.

### 📥 Intelligent Archiving
*   **Organized Downloads**: Forget messy folders. Kura automatically sorts every file into `Downloads/Kemono/<Artist>/<PostTitle>/`.
*   **Background Manager**: Queue up downloads and let Kura handle the rest with robust background processing via WorkManager.
*   **Media Support**: Download images, videos, and attachments with ease.
*   **Resumable Downloads**: Downloads automatically retry on failure and persist across app restarts.

### 🖼️ Beautiful Gallery
*   **Artist-Centric View**: Your offline collection is automatically grouped by Artist, making it easy to browse your favorite creators.
*   **Zoomable Viewer**: Inspect every detail with a high-performance, gesture-supported image viewer (zoom, pan, swipe).
*   **Deep Integration**: Navigate directly from a downloaded image to the original post or creator profile.

### 🛠️ Power User Tools
*   **Favorites System**: Keep your top creators just a tap away.
*   **DDoS-Guard Bypass**: Built-in handling for protection cookies—no browser gymnastics required.
*   **Theming**: Full support for Light, Dark, and System themes with dynamic color support.
*   **Cache Management**: Built-in tools to manage storage usage and clear cache.

---

## 🔧 Under the Hood

Kura is built with modern Android engineering standards, ensuring stability and performance:

*   **Architecture**: MVVM + Clean Architecture for separation of concerns and testability.
*   **UI**: 100% Jetpack Compose (Material 3) for a fluid, reactive user interface.
*   **Network**: Retrofit + OkHttp with custom interceptors for cookie handling and caching.
*   **Image Loading**: Coil with aggressive caching (500MB disk cache) and crossfade animations.
*   **Persistence**: Room Database for offline data and DataStore for user preferences.
*   **Concurrency**: Kotlin Coroutines + Flow for asynchronous operations and reactive state management.
*   **Background Work**: WorkManager for reliable, persistent download tasks.
*   **Dependency Injection**: Hilt for modular and testable code structure.

---

## 📦 Get Started

### 📲 Install the Premade APK

For users who just want to use the app:

1.  Go to the [Releases Page](https://github.com/AntarcDev/Kura/releases).
2.  Download the latest `app-release.apk`.
3.  Install it on your Android device (Android 7.0+).
4.  **Initialize**: Go to **Settings** -> **Initialize DDoS-Guard** to set up your session.

### 🏗️ Build it Yourself

For developers who want to contribute or build from source:

1.  **Prerequisites**:
    *   Android Studio Hedgehog or newer.
    *   JDK 17 or newer.
2.  **Clone the Repository**:
    ```bash
    git clone https://github.com/AntarcDev/Kura.git
    ```
3.  **Open in Android Studio**: Let Gradle sync dependencies.
4.  **Build**:
    *   Select the `app` configuration.
    *   Click the **Run** button (green arrow) or run `./gradlew assembleDebug`.
5.  **Run**: The app will launch on your connected device or emulator.

---

*Made with 🖕 by Antarc*
