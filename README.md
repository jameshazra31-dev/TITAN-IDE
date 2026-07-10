<div align="center">

# Titan IDE

**A Production-Ready Native Android IDE built entirely with Kotlin & Jetpack Compose**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Gradle](https://img.shields.io/badge/Gradle-8.10-02303A?logo=gradle&logoColor=white)](https://gradle.org)
[![AGP](https://img.shields.io/badge/AGP-8.5.1-3DDC84?logo=android&logoColor=white)](https://developer.android.com/studio)
[![Min SDK](https://img.shields.io/badge/Min_SDK-29-FF6D00?logo=android&logoColor=white)](https://developer.android.com/about/versions/android-10)
[![Target SDK](https://img.shields.io/badge/Target_SDK-35-34A853?logo=android&logoColor=white)](https://developer.android.com/about/versions/android-15)
[![Architecture](https://img.shields.io/badge/Architecture-Clean_MVVM-00C7B7)](#architecture)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**24 Modules | MVVM + Clean Architecture | Hilt DI | Room DB | AI-Powered Coding**

</div>

---

## Overview

Titan IDE is a fully-featured, native Android Integrated Development Environment designed for on-the-go development directly from your Android device. It brings a desktop-class coding experience to mobile, complete with a powerful code editor, AI-assisted coding, integrated terminal, Git support, build system, and 20+ developer tools.

Built from the ground up with **Jetpack Compose** and following **Clean Architecture** principles across **24 modular Gradle modules**, Titan IDE is engineered for performance, scalability, and maintainability.

---

## Screenshots

> *Coming soon â€” pull requests with screenshots are welcome!*

---

## Features

### Core IDE

| Feature | Description |
|---------|-------------|
| **Code Editor** | Multi-tab editor with syntax highlighting, auto-save, find & replace, file tree sidebar, breadcrumb navigation, and status bar |
| **File Manager** | Full file system browser with search, hidden file toggle, and directory navigation |
| **Project Manager** | Create, import, pin, and manage projects with 11 built-in project templates |
| **Terminal** | Multi-session terminal emulator with built-in command handler (ls, pwd, clear, help, etc.) |
| **Git Integration** | Status, commits, branches, remote management, and stash â€” all from within the app |
| **Build System** | Gradle-based build runner with real-time colored log output and progress tracking |

### AI-Powered Development

| Feature | Description |
|---------|-------------|
| **AI Chat** | Conversational AI assistant integrated directly into the IDE |
| **Code Generation** | Generate code snippets from natural language descriptions |
| **11 AI Providers** | OpenAI, Claude, Gemini, Mistral, Ollama, Groq, DeepSeek, Cohere, Together AI, Perplexity, OpenRouter |
| **Fully Dynamic** | Configure Base URL, API Key, and Model â€” no source code changes needed |
| **Encrypted Storage** | API keys encrypted at rest using AES/GCM via Android Keystore |

### Developer Tools

| Tool | Description |
|------|-------------|
| **Logcat** | Real-time Android log viewer with level filters and search |
| **Debugger** | Variables, breakpoints, threads, memory, and network inspection tabs |
| **APK Tools** | Analyzer, Signer, Zipalign, Manifest viewer, Resource viewer, Certificate viewer |
| **XML Designer** | Visual XML layout designer with Design/Text/Preview tabs and component palette |
| **Plugin System** | Extensible plugin architecture with marketplace |
| **Tools Suite** | Markdown viewer, Image viewer, PDF viewer, SQLite browser, JSON formatter, Color picker, Regex tester, HTTP/REST client, WebSocket client, and more |

### Settings & Customization

- **30+ configurable settings** across Editor, Appearance, Terminal, Git, and Security
- **4 theme modes**: Light, Dark, AMOLED, and Dynamic Colors (Material You)
- **Font size, tab size, word wrap, line numbers** â€” fully customizable editor
- **Biometric lock** for app security
- **DataStore-backed preferences** with persistent storage

---

## Architecture

Titan IDE follows **Clean Architecture** with **MVVM** presentation pattern across a highly modularized codebase.

```
TitanIDE/
â”œâ”€â”€ app/                          # Application entry point, DI modules, navigation, theme
â”œâ”€â”€ core/
â”‚   â”œâ”€â”€ common/                   # Base classes, utilities, extensions, constants
â”‚   â”œâ”€â”€ database/                 # Room database (8 entities, 7 DAOs, TypeConverters)
â”‚   â”œâ”€â”€ datastore/                # DataStore preferences (25+ keys)
â”‚   â”œâ”€â”€ network/                  # Network connectivity monitor
â”‚   â”œâ”€â”€ security/                 # AES/GCM encryption, Biometric authentication
â”‚   â””â”€â”€ ai/                       # AI HTTP client, DTOs, provider mapper
â”œâ”€â”€ domain/
â”‚   â”œâ”€â”€ model/                    # Domain models (Project, FileNode, AIChat, etc.)
â”‚   â”œâ”€â”€ repository/               # Repository interfaces
â”‚   â””â”€â”€ usecase/                  # Use cases (GetProjects, CreateProject, SendAI, CloneRepo)
â”œâ”€â”€ data/
â”‚   â”œâ”€â”€ local/                    # Local data source (DAO aggregator)
â”‚   â”œâ”€â”€ remote/                   # Remote data source
â”‚   â””â”€â”€ repository/               # Repository implementations
â””â”€â”€ feature/
    â”œâ”€â”€ editor/                   # Code editor screen + viewmodel
    â”œâ”€â”€ filemanager/              # File manager screen + viewmodel
    â”œâ”€â”€ projectmanager/           # Project manager screen + viewmodel
    â”œâ”€â”€ terminal/                 # Terminal screen + viewmodel
    â”œâ”€â”€ git/                      # Git operations screen
    â”œâ”€â”€ buildsystem/              # Build system screen
    â”œâ”€â”€ settings/                 # Settings screen + viewmodel
    â”œâ”€â”€ logcat/                   # Logcat viewer screen
    â”œâ”€â”€ debugger/                 # Debugger screen
    â”œâ”€â”€ apktools/                 # APK tools screen
    â”œâ”€â”€ xmldesigner/              # XML designer screen
    â”œâ”€â”€ ai/                       # AI chat screen + viewmodel
    â”œâ”€â”€ plugins/                  # Plugin system screen
    â””â”€â”€ tools/                    # Developer tools suite screen
```

### Data Flow

```
UI (Compose) â†’ ViewModel (State/Event/Action) â†’ UseCase â†’ Repository â†’ DataSource
                                        â†‘
                                 Room / DataStore / Network
```

### Key Design Patterns

- **BaseViewModel<S, E, A>** â€” Generic base with State, Event, and Action type parameters
- **Resource<T>** â€” Sealed class for loading/success/error state management
- **Result<T>** â€” Domain-layer operation result wrapper
- **Hilt Dependency Injection** â€” @Module, @Binds, @Provides across all layers
- **Repository Pattern** â€” Interface in domain, implementation in data layer

---

## Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 1.9.24 |
| **UI Framework** | Jetpack Compose with Material 3 |
| **Build System** | Gradle 8.10, AGP 8.5.1 |
| **Dependency Injection** | Hilt 2.51.1 |
| **Database** | Room 2.6.1 |
| **Preferences** | DataStore Preferences 1.1.1 |
| **Networking** | Retrofit 2.11.0 + OkHttp 4.12.0 |
| **Serialization** | kotlinx-serialization-json 1.6.3 |
| **Image Loading** | Coil Compose 2.6.0 |
| **Logging** | Timber 5.0.1 |
| **Navigation** | Navigation Compose 2.7.7 |
| **Async** | Kotlin Coroutines + Flow |
| **Security** | Android Keystore (AES/GCM), Biometric API |
| **Git** | JGit / ProcessBuilder |
| **Code Editor** | Sora Editor |
| **Min SDK** | 29 (Android 10) |
| **Target SDK** | 35 (Android 15) |
| **JVM Target** | 17 |

---

## Project Templates

Titan IDE supports creating new projects from **11 built-in templates**:

| # | Template | Description |
|---|----------|-------------|
| 1 | Empty Project | Blank project with basic Gradle setup |
| 2 | Kotlin Project | Kotlin-based Android project |
| 3 | Compose Project | Jetpack Compose starter project |
| 4 | MVVM Project | Model-View-ViewModel architecture template |
| 5 | Clean Architecture | Full layered architecture (data/domain/presentation) |
| 6 | Java Project | Java-based Android project |
| 7 | Library Module | Android library module template |
| 8 | Navigation Project | Navigation Compose template with multi-screen setup |
| 9 | Room Project | Room database integrated project |
| 10 | Retrofit Project | Retrofit networking integrated project |
| 11 | Hilt Project | Hilt dependency injection setup |

Each template generates actual project files with real code â€” not placeholders.

---

## Database Schema

Room database with **8 entities** and **7 DAOs**:

| Entity | Description |
|--------|-------------|
| `ProjectEntity` | Project metadata, path, language, build system |
| `AIProviderEntity` | AI provider config (type, base URL, encrypted API key, model) |
| `AIChatEntity` | Chat sessions linked to providers |
| `AIChatMessageEntity` | Individual messages within chats |
| `RecentFileEntity` | Recently opened files with timestamps |
| `BookmarkEntity` | Code bookmarks with file paths and line numbers |
| `TerminalHistoryEntity` | Terminal command history |
| `GitAccountEntity` | Git account credentials and configuration |

---

## Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** or higher
- **Android SDK** with API Level 35 installed
- A physical Android device running **Android 10+** (recommended) or an emulator

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/<your-username>/TitanIDE.git
cd TitanIDE

# Open in Android Studio
# File â†’ Open â†’ Select the TitanIDE directory

# Build the project
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Build Variants

| Variant | Minification | Description |
|---------|-------------|-------------|
| `debug` | Disabled | Development builds with full logging |
| `release` | R8/ProGuard enabled | Production builds with obfuscation and resource shrinking |

---

## AI Provider Setup

Titan IDE supports **11 AI providers** with zero-code configuration:

1. Open **Settings â†’ AI Providers**
2. Tap **Add Provider**
3. Select provider type (OpenAI, Claude, Gemini, etc.)
4. Enter your **Base URL**, **API Key**, and **Model name**
5. Start chatting or generating code!

> API keys are encrypted using AES/GCM with the Android Keystore before being stored in the local database. Your keys never leave your device.

---

## Security

- **AES/GCM Encryption** â€” All sensitive data (API keys, tokens) encrypted at rest via Android Keystore
- **Biometric Authentication** â€” Optional app lock with fingerprint/face recognition
- **No Hardcoded APIs** â€” All AI provider endpoints are user-configurable
- **Scoped Storage** â€” Proper Android storage permissions with legacy fallback
- **ProGuard/R8** â€” Release builds are fully obfuscated

---

## Module Dependency Graph

```
app
 â”œâ”€â”€ core:common          (shared by all modules)
 â”œâ”€â”€ core:database        (Room)
 â”œâ”€â”€ core:datastore       (DataStore)
 â”œâ”€â”€ core:network         (Connectivity)
 â”œâ”€â”€ core:security        (Crypto + Biometric)
 â”œâ”€â”€ core:ai              (AI Client + DTOs)
 â”œâ”€â”€ domain               (Models + UseCases + Repo Interfaces)
 â”‚    â””â”€â”€ data:local      (LocalDataSource)
 â”‚    â””â”€â”€ data:remote     (RemoteDataSource)
 â”‚    â””â”€â”€ data:repository (Repo Implementations)
 â”œâ”€â”€ feature:editor
 â”œâ”€â”€ feature:filemanager
 â”œâ”€â”€ feature:projectmanager
 â”œâ”€â”€ feature:terminal
 â”œâ”€â”€ feature:git
 â”œâ”€â”€ feature:buildsystem
 â”œâ”€â”€ feature:settings
 â”œâ”€â”€ feature:logcat
 â”œâ”€â”€ feature:debugger
 â”œâ”€â”€ feature:apktools
 â”œâ”€â”€ feature:xmldesigner
 â”œâ”€â”€ feature:ai
 â”œâ”€â”€ feature:plugins
 â””â”€â”€ feature:tools
```

---

## Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Code Style

- Follow official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use **Compose best practices** (state hoisting, immutable state, etc.)
- Maintain **Clean Architecture** boundaries â€” no feature module imports from another feature module
- All new settings must use **DataStore Preferences**, not SharedPreferences

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with Kotlin and Jetpack Compose**

Made for developers who code on the go.

</div>
