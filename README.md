# Kemono Android App

A native Android application for browsing content from kemono.cr, built with Kotlin and Jetpack Compose.

## Features

- **Creator Browser**: Browse and search through creators
- **Post Viewer**: View posts with images and attachments
- **Favorites**: Save favorite creators locally with Room database
- **Downloads**: Background downloads using WorkManager
- **Search**: Real-time search functionality
- **DDoS-Guard Bypass**: Automatic handling of DDoS-Guard protection

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Local Storage**: Room Database
- **Background Tasks**: WorkManager
- **Navigation**: Jetpack Navigation Compose

## Setup

1. Clone the repository
2. Open in Android Studio (Hedgehog or later)
3. Sync Gradle files
4. Run on device or emulator (API 24+)

## API Authentication

The app uses a special `Accept: text/css` header to bypass DDoS-Guard protection. On first launch:

1. Go to Settings (⚙️ icon)
2. Tap "Initialize DDoS-Guard Cookies"
3. Wait for success message
4. (Optional) Add your session cookie for authenticated access

## Project Structure

```
app/src/main/java/com/example/kemono/
├── data/
│   ├── local/          # Room database and DAOs
│   ├── model/          # Data models
│   ├── remote/         # Retrofit API interface
│   └── repository/     # Repository layer
├── di/                 # Hilt dependency injection modules
├── ui/                 # Compose UI screens and ViewModels
│   ├── creators/       # Creator list screen
│   ├── favorites/      # Favorites screen
│   ├── posts/          # Post list and detail screens
│   ├── settings/       # Settings screen
│   └── theme/          # Material 3 theme
└── worker/             # WorkManager background tasks
```

## Building

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## Requirements

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK API 34
- Minimum SDK: API 24 (Android 7.0)

## License

Private project - All rights reserved

## Notes

- This app is for personal use only
- Respects kemono.cr's API rate limits and caching headers
- Uses proper DDoS-Guard cookie handling for legitimate access
