# BikeManager

A modern Kotlin Multiplatform (KMP) application for tracking motorcycle maintenance schedules and history.

## Overview

BikeManager helps motorcycle enthusiasts track their bikes' maintenance needs and history. Track maintenance by kilometers or engine hours, manage multiple bikes, and never miss an important service interval.

## Features

- **Multi-bike Management**: Track multiple motorcycles with individual profiles
- **Flexible Tracking**: Monitor maintenance by kilometers or engine hours
- **Maintenance Scheduling**: Create and track maintenance tasks (oil changes, tire replacements, etc.)
- **Maintenance History**: Keep a complete history of completed services
- **Firebase Sync**: Cloud synchronization across all your devices
- **Google Sign-In**: Secure authentication with Google account
- **Theme Support**: Light and dark mode with system preference detection
- **Modern Navigation**: Type-safe navigation with Navigation Compose

## Tech Stack

### Core Technologies
- **Kotlin Multiplatform (KMP)**: Shared business logic across Android and iOS
- **Kotlin 2.3.0**: Latest Kotlin features and improvements
- **Compose Multiplatform 1.10.0**: Modern declarative UI framework

### Android
- **Jetpack Compose**: Native Android UI with Material 3 design
- **Navigation Compose 2.9.1**: Type-safe navigation
- **Android Gradle Plugin 8.9.1**: Latest build tools
- **Min SDK 24** | **Target SDK 35** | **Compile SDK 36**

### iOS
- **SwiftUI Integration**: Native iOS UI components
- **iOS 14+**: Modern iOS capabilities

### Architecture & Patterns
- **Clean Architecture**: Separation of concerns with domain, data, and presentation layers
- **MVVM**: ViewModel pattern for UI state management
- **Repository Pattern**: Abstraction of data sources
- **Use Cases**: Single-responsibility business logic components
- **Dependency Injection**: Koin 4.1.1 for DI management

### Backend & Data
- **Firebase**: Cloud backend and real-time sync
  - Firebase Authentication (via GitLive KMP SDK 2.4.0)
  - Firebase Realtime Database (via GitLive KMP SDK 2.4.0)
- **SQLDelight 2.2.1**: Local database with type-safe SQL
- **Kotlinx Serialization 1.8.1**: JSON serialization/deserialization

### Networking & Async
- **Ktor 3.4.0**: HTTP client for API calls
- **Kotlinx Coroutines 1.10.2**: Asynchronous programming
- **Kotlinx DateTime 0.7.1**: Cross-platform date/time handling

### Authentication
- **KMPAuth 2.3.1**: Cross-platform Google authentication
- **Google Sign-In**: OAuth 2.0 authentication flow

### UI & Resources
- **Compose BOM 2026.01.00**: Latest Compose dependencies
- **Coil 2.7.0**: Image loading for Compose
- **Material 3**: Modern Material Design components
- **Compose Resources**: XML-based localized strings

### Development Tools
- **Napier 2.7.1**: Cross-platform logging
- **Kover 0.9.4**: Code coverage reporting
- **JUnit 4.13.2**: Unit testing framework
- **Turbine 1.2.1**: Flow testing utilities

## Project Structure

```
BikeManager/
├── androidApp/          # Android-specific code
│   └── src/
│       ├── main/        # Android app entry point
│       ├── debug/       # Debug-specific resources (google-services.json)
│       └── release/     # Release-specific resources
├── shared/              # Shared KMP module
│   └── src/
│       ├── commonMain/  # Shared Kotlin code
│       │   ├── domain/  # Business logic & use cases
│       │   ├── data/    # Repositories & data sources
│       │   └── ui/      # Shared UI components
│       ├── androidMain/ # Android-specific implementations
│       ├── iosMain/     # iOS-specific implementations
│       └── commonTest/  # Shared unit tests
└── iosApp/              # iOS-specific code (SwiftUI)
```

## License

Copyright (c) 2026 BikeManager

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.