# 蔵 Kura

**The Premium Native Client for Kemono**

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white) ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white) ![Material 3](https://img.shields.io/badge/Material%203-7C4DFF?style=for-the-badge&logo=materialdesign&logoColor=white)

Unlock a seamless browsing experience with **Kura** (蔵 - "Storehouse"). Built for speed, aesthetics, and organization, Kura transforms how you discover and archive content.

---

## ✨ Features

### 🎨 Immersive Discovery
*   **Unified Search**: Search for creators by name or ID with a debounced, lag-free interface.
*   **Rich Creator Profiles**: View detailed stats, banners, announcements, tags, and linked accounts (now with tile-based navigation).
*   **Dynamic Layouts**: Switch between "Compact" grids for speed or "Comfortable" lists for detail.
*   **Advanced Sorting & Filtering**: 
    *   Sort by Popularity, Date Indexed, Date Updated, or Alphabetical (Asc/Desc).
    *   Filter by Service (Patreon, Fanbox, Fantia, Discord, etc.) with color-coded toggles.
*   **Optimized Performance**: efficiently handles 100k+ artists using stream parsing and batch caching.

### 📥 Intelligent Archiving
*   **Inline Downloads**: Automatically detects and downloads images/GIFs embedded within post text.
*   **Bulk Actions**: Select multiple posts to download in batch.
*   **Organized Downloads**: Files are automatically sorted into `Downloads/Kemono/<Artist>/<PostTitle>/`.
*   **Background Manager**: Robust background processing via WorkManager with persistent retry logic.
*   **Enhanced Media Support**: 
    *   **Discord**: Full support for viewing queued Discord archives (Channels, Embeds, Attachments).
    *   **PSD & Archives**: Native preview for PSDs and card-based handling for ZIP/RAR/7z files.
    *   **Audio**: Integrated audio player for music and voice clips.

### 🖼️ Beautiful Gallery
*   **Artist-Centric View**: Offline collection grouped by Artist for easy browsing.
*   **Zoomable Viewer**: High-performance image viewer with zoom, pan, and swipe gestures.
*   **Deep Integration**: Navigate directly from a downloaded image to the original post.

### 🛠️ Power User Tools
*   **Favorites System**: Track your top creators.
*   **DDoS-Guard Bypass**: Built-in session handling—no external browser needed.
*   **Theming**: Full support for Light, Dark, and System themes.
*   **Cache Management**: Built-in tools to monitor and clear storage.
*   **In-App Updates**: Auto-update mechanism via GitHub Releases. (might be broken?)

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
