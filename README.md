# 蔵 Kura

**A Half-Decent Native Client for Kemono**

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white) ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white) ![Material 3](https://img.shields.io/badge/Material%203-7C4DFF?style=for-the-badge&logo=materialdesign&logoColor=white)

**Kura** (蔵) is a native Android client for Kemono. It provides a mobile interface for content discovery, metadata indexing, and media archival via the Kemono API.

**Note**: First of all I made this for my own use and I'm not a professional developer. This is a work in progress and has a lot of room for improvement. Updates can and will break the app or some functions. Use at your own risk!
---

## Features

### Discovery
*   **Unified Search**: Search for creators by name or ID with a debounced, lag-free interface.
*   **Rich Creator Profiles**: View detailed stats, banners, announcements, tags, and linked accounts with tile-based navigation.
*   **Dynamic Layouts**: Switch between compact grids for speed or comfortable lists for detail.
*   **Advanced Sorting & Filtering**: Sort by popularity, date, or name; filter by service with color-coded toggles.
*   **Optimized Performance**: Efficiently handles 100k+ artists using stream parsing and batch caching.

### Intelligent Archiving
*   **Inline Downloads**: Automatically detects and downloads images/GIFs embedded within post text.
*   **Bulk Actions**: Select multiple posts to download in batch.
*   **Organized Downloads**: Files are automatically sorted into `Downloads/Kemono/<Artist>/<PostTitle>/`.
*   **Background Manager**: Robust background processing via WorkManager with persistent retry logic.
*   **Archive and Media Support**: Full support for Discord archives, ZIP/RAR/7z handling, and an integrated audio player.

### Gallery
*   **Artist-Centric View**: Offline collection grouped by artist for easy browsing.
*   **Zoomable Viewer**: High-performance image viewer with zoom, pan, and swipe gestures.
*   **Deep Integration**: Navigate directly from a downloaded image to the original post.

### Power User Tools
*   **DDoS-Guard Bypass**: Built-in session handling without an external browser.
*   **Cache Management**: Tools to monitor and clear storage.

---

## Tech Stack

Kura is built with these technologies:

*   **Architecture**: MVVM + Clean Architecture for separation of concerns.
*   **UI**: 100% Jetpack Compose (Material 3) for a fluid interface.
*   **Network**: Retrofit + OkHttp with custom interceptors for cookie handling.
*   **Image Loading**: Coil with aggressive caching (500MB disk cache).
*   **Persistence**: Room Database for offline data and DataStore for preferences.
*   **Concurrency**: Kotlin Coroutines + Flow for reactive state management.
*   **Background Work**: WorkManager for reliable download tasks.
*   **Dependency Injection**: Hilt for a modular and testable structure.

---

## Get Started

### Installation

1.  Visit the [Releases Page](https://github.com/AntarcDev/Kura/releases).
2.  Download the latest `kura*.apk`.
3.  Install on an Android device (7.0+).
4.  **Initialize**: Navigate to **Settings** -> **Initialize DDoS-Guard** to set up your session. (optional)

### Development

1.  **Prerequisites**: Android Studio Hedgehog+ and JDK 17+.
2.  **Clone the Repository**:
    ```bash
    git clone https://github.com/AntarcDev/Kura.git
    ```
3.  **Open**: Import into Android Studio and sync Gradle.
4.  **Build & Run**: Select the `app` configuration and run on a device or emulator.

*Made with 🖕 by Antarc*
