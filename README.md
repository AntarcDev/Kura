<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="128" />
  <h1>Kura 蔵</h1>
  <p><strong>A Native Client for Kemono (It's trying its best) (；￣Д￣)</strong></p>

  <p>
    <img src="https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white" alt="Jetpack Compose" />
    <img src="https://img.shields.io/badge/Material%203-7C4DFF?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material 3" />
    <br/>
    <img src="https://img.shields.io/badge/Release-v0.8_Pre--Release-orange?style=for-the-badge" alt="v0.8" />
  </p>
</div>

---

### *Welcome to the Archives! (≧◡≦)*

**Kura** is a native Android client for Kemono. It *attempts* to provide a fluid, high-performance mobile interface. It works on my machine, at least.

> [!IMPORTANT]
> **⚠ Pre-Release Build (v0.8)**
> **Warning**: Versions prior to v0.8 are completely broken. The API changed. They are dead. Let them go.
> This project is developed by one guy and a slightly hallucinations-prone AI. Expect bugs. Embrace them.

---

## 📸 Visual Tour

| Login & Sync | Creator Grid | Post Details |
|:---:|:---:|:---:|
| ![Login Sync](screenshots/login_sync.png) | ![Creator Grid](screenshots/creator_grid.png) | ![Post Details](screenshots/post_details.png) |
| *Native Account Integration* | *Customizable Grid Layouts* | *Rich Metadata & Media* |

| Light Mode | Search & History | Settings |
|:---:|:---:|:---:|
| ![Light Mode](screenshots/light_mode.png) | ![Search History](screenshots/search.png) | ![Settings](screenshots/settings.png) |
| *Material 3 Light Theme* | *History & Advanced Filters* | *Data & Cache Control* |

---

## ✨ Features (o^▽^o)

### 🔐 Native Account & Sync
*   **Seamless Login**: Log directly into your Kemono account within the app.
*   **Cloud Sync**: Manually synchronize your favorite **Creators** and **Posts** between the app and the website via Settings or Profile.
*   **Favorites Management**: Add or remove favorites locally, and push changes to your account instantly.

### 🎨 Personalization
*   **Dynamic Layouts**: Choose between **List** or **Grid** views for Creator profiles.
*   **Grid Density**: Control the size of items (Small, Medium, Large) to fit more content or see more detail.
*   **Autoplay Control**: Toggle GIF autoplay to save data or improve performance.

### 🔍 Discovery
*   **Unified Search**: Search for creators by name or ID with a lag-free, debounced interface.
*   **Search History**: Quickly access your recently searched terms.
*   **Rich Profiles**: View detailed stats, banners, announcements, tags, and linked accounts.
*   **Advanced Filtering**: Filter by Service (Patreon, Fanbox, etc.) with color-coded badges, sort by Popularity/Date/Name.

### 💾 Intelligent Archiving
*   **Inline Downloads**: Automatically detects and downloads images/GIFs embedded in posts.
*   **Bulk Actions**: Long-press to select multiple posts and download them in batch.
*   **Organized Storage**: Files are sorted automatically: `Downloads/Kura/<Artist>/<PostTitle>/`.
*   **Background Manager**: Robust background downloading via `WorkManager` with retry logic.
*   **Archive Support**: Native handling for ZIP/RAR/7z archives and embedded audio players.

### 🚀 Performance & Tools
*   **Optimized Core**: Efficiently handles 100k+ artists using stream parsing and aggressive caching (Low-Res First loading).
*   **DDoS-Guard Bypass**: Built-in session handling to bypass protection without an external browser.
*   **Cache Management**: Monitor network/media cache usage and clear them independently to free up space.
*   **Crash Reporting**: Optional anonymous crash logging to help improve stability.

---

## 🛠 Under the Hood (Don't Look Too Closely (*/ω＼))

*   **Architecture**: MVVM + Clean Architecture styling.
*   **UI**: 100% Jetpack Compose (Material 3).
*   **Network**: Retrofit + OkHttp (Custom Interceptors).
*   **Image Loading**: Coil (Video & GIF support).
*   **Persistence**: Room Database (Offline Cache) + DataStore (Settings).
*   **Async**: Kotlin Coroutines + Flow.
*   **DI**: Hilt.

---

## 🚀 Get Started

### Installation
1.  Visit the [Releases Page](https://github.com/AntarcDev/Kura/releases).
2.  Download the latest `kura-v0.8.apk`.
3.  Install on Android (7.0+).
4.  **Optional**: Go to **Settings -> Account** to log in and sync your favorites!

### Development
1.  **Clone**: `git clone https://github.com/AntarcDev/Kura.git`
2.  **Open**: Android Studio Hedgehog+ (JDK 17).
3.  **Build**: Run `app` configuration.

---

<div align="center">
  <p>Developed by <strong>Antarc</strong>.</p>
  <p><i>Luna: "I fixed it!" (Narrator: She broke it).</i></p>
</div>
