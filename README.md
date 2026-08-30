<div align="center">

# ⚔️ Blade Launcher

**Next-Generation Mobile Launcher for Minecraft: Java Edition on Android**

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20%28API%2026%2B%29-orange?style=for-the-badge&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3E-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Java Runtimes](https://img.shields.io/badge/Java-8%20→%2026-ED8B00?style=for-the-badge&logo=openjdk)](https://openjdk.org)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg?style=for-the-badge)](LICENSE)

*Experience desktop Minecraft: Java Edition in the palm of your hand with an ergonomic mobile-first vertical interface, high-performance Vulkan/OpenGL render pipelines, instant JDK 8–26 installer, and integrated Modrinth & MC Mods ecosystem.*

---

</div>

## 📑 Table of Contents

- [✨ Key Highlights](#-key-highlights)
- [🎨 Design System & Aesthetics](#-design-system--aesthetics)
- [☕ Java Runtime Management (JDK 8–26)](#-java-runtime-management-jdk-826)
- [🧩 Modrinth & CurseForge Browser](#-modrinth--curseforge-browser)
- [🌐 MC Mods Updater Hub](#-mc-mods-updater-hub)
- [🛠️ Tech Stack & Architecture](#️-tech-stack--architecture)
- [🏗️ Project Structure](#️-project-structure)
- [🌐 Project Website](#-project-website)
- [🚀 Getting Started (Build & Run)](#-getting-started-build--run)
- [⚙️ Configuration & Environment](#️-configuration--environment)
- [🧪 Testing & Quality Assurance](#-testing--quality-assurance)
- [❓ Troubleshooting & FAQ](#-troubleshooting--faq)
- [📜 License & Compliance Notice](#-license--compliance-notice)

---

## ✨ Key Highlights

- **📱 Pojav-Style Vertical Mobile UX**: Ergonomic one-handed portrait interface with rapid launch controls anchored directly in the thumb zone. Seamlessly adapts to tablets and landscape mode via responsive breakpoint layouts.
- **☕ Built-in JDK Downloader (Java 8 up to Java 26)**: One-click installation of OpenJDK runtimes (8, 11, 17, 21, 25, 26) across ARM64, ARM32, and x86_64 architectures with automatic per-version JVM routing.
- **🔥 Milk White & Flame Orange Aesthetic**: Bespoke theme inspired by the modern **MC Mods Updater** ecosystem, featuring `#EC5E27` flame orange accents, silky milk-white surfaces, obsidian dark mode, and tactile pill-shaped smooth buttons.
- **🧩 1-Tap Mod & Resource Pack Installer**: Native Modrinth & CurseForge integration with fast category filtering, loader selectors (Fabric, Forge, NeoForge, Quilt, Cleanroom, Legacy Fabric), and direct download.
- **🌐 Integrated MC Mods Updater Hub**: Embedded webview environment equipped with quick source pills, back/forward history navigation, cache cleanup, and instant external browser dispatch.
- **⚡ Advanced Multi-Renderer Graphics Engine**: Hardware-accelerated Vulkan (Zink / Turnip / VirGL), ANGLE, and NG-GL4ES render backends tailored for smooth 60+ FPS gameplay.
- **🎮 Touch Controls & Gamepad Engine**: Fully custom virtual touch HUD with custom button textures, haptic feedback, gyroscope aim assist, and physical gamepad (Xbox / PlayStation / HID) mapping.
- **🛡️ Process Isolation Architecture**: Split-process architecture separating the launcher interface (`:main`) from the Minecraft JVM environment (`:game`) for extreme memory stability.

---

## 🎨 Design System & Aesthetics

Blade Launcher is engineered with a **Touch-First, Mobile-First** design doctrine that prioritizes usability, fluid animations, and visual polish.

```
┌─────────────────────────────────────────────────────────────┐
│                       BLADE LAUNCHER                        │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │  👤 Steve  [Microsoft / Offline]           🔄 Switch    │ │
│ └─────────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │                                                         │ │
│ │                  🔥 COMMUNITY NEWS & HUB                │ │
│ │                                                         │ │
│ └─────────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │  📦 Fabric 1.21.4 (Java 21)                  ⚙️ Settings │ │
│ │  ┌───────────────────────────────────────────────────┐  │ │
│ │  │             ▶  PLAY MINECRAFT                     │  │ │
│ │  └───────────────────────────────────────────────────┘  │ │
│ └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Color Tokens

| Token | Light Theme (Milk White) | Dark Theme (Obsidian Slate) | Purpose |
|---|---|---|---|
| **Primary Accent** | `#EC5E27` (Flame Orange) | `#EC5E27` (Flame Orange) | CTAs, Launch Buttons, Badges |
| **Primary Container** | `#FFDCD2` | `#3D1B0F` | Active Chips, Highlight Cards |
| **Surface Background** | `#FFFFFF` / `#FAF8F6` | `#0F1115` / `#15181E` | Background Canvas & Dialogs |
| **Card Containers** | `#F6F3F0` | `#1A1E26` / `#222731` | Elevation Cards & Lists |
| **Outlines & Dividers** | `#8B716A` | `#444B59` / `#282D37` | Subtle Separation Lines |

---

## ☕ Java Runtime Management (JDK 8–26)

Blade Launcher includes a complete Java catalog manager that downloads, verifies, and decompresses OpenJDK archives into native Android runtime environments:

| JDK Version | Status | Target Minecraft Releases | Recommended Mod Loaders |
|---|---|---|---|
| **Java 8** | LTS | Minecraft 1.0 – 1.16.5 | Forge 1.7.10–1.12.2, OptiFine |
| **Java 11** | LTS | Minecraft 1.16+ legacy & Babric | Babric, Legacy Fabric |
| **Java 17** | LTS | Minecraft 1.17 – 1.20.4 | Fabric, Forge 1.17+, Quilt |
| **Java 21** | LTS | Minecraft 1.20.5 – 1.21.x | Fabric, NeoForge, Cleanroom 0.4.4 |
| **Java 25** | Preview | Minecraft 26.x Snapshots / Cleanroom 0.5+ | Cleanroom 0.5.0-alpha+ |
| **Java 26** | Experimental | Next-Gen Minecraft & Research | Experimental JVM Flags |

### How It Works

1. Open **Settings** → **Java Runtime Manager**.
2. Tap **"Download JDK (8–26)"**.
3. Select your target Java release.
4. The launcher automatically downloads the architecture-specific `.tar.xz` binary, unpacks with `xz-java` and `RuntimesManager.installRuntime()`, configures `libawt_xawt.so` and `libfreetype.so`, and registers the runtime immediately.

---

## 🧩 Modrinth & CurseForge Browser

Browse and install modifications directly from within the launcher without opening an external browser:

- **Unified Asset Filtering**: Filter by Minecraft version, loader (Fabric, Forge, NeoForge, Quilt), and categories (Optimization, Technology, Magic, QoL).
- **One-Tap Mod Installation**: Downloads mod `.jar` files straight into your active version's `mods/` directory.
- **Resource Packs & Shaders**: Installs texture packs to `resourcepacks/` and shaders to `shaderpacks/` automatically.
- **Modpack Setup**: Instant extraction and configuration of `.mrpack` and CurseForge zip bundles.

---

## 🌐 MC Mods Updater Hub

The **MC Mods Updater** view provides an integrated embedded WebView designed to match the launcher's dark theme:

- **Quick Navigation Chips**: Instant access to Modrinth Mods, CurseForge, Modpacks, Resource Packs, and Shaders.
- **WebView Tools**: Full back/forward navigation history, reload button, external browser handoff, and progress feedback.
- **Touch-Friendly Controls**: Large 48dp+ interactive buttons and rounded surfaces following modern mobile guidelines.

---

## 🛠️ Tech Stack & Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    UI & APP LAYER (Process :main)           │
│  Jetpack Compose M3E  •  Navigation 3  •  StateFlow / MVVM  │
└──────────────────────────────┬──────────────────────────────┘
                               │ IPC / Activity Launch
┌──────────────────────────────▼──────────────────────────────┐
│                  GAME RUNTIME (Process :game)               │
│  VMActivity  •  LWJGL 3 Bridge  •  Virtual Input Controller │
└──────────────────────────────┬──────────────────────────────┘
                               │ JNI Bridge / POSIX fork
┌──────────────────────────────▼──────────────────────────────┐
│                    NATIVE & GRAPHICS LAYER                  │
│  libjvm.so  •  Mesa (Zink/Turnip)  •  ANGLE  •  GL4ES       │
└─────────────────────────────────────────────────────────────┘
```

### Technology Highlights

- **Language**: Kotlin 2.0+ (100% Coroutines & Flow)
- **UI Framework**: Android Jetpack Compose with Material 3 Expressive (`MaterialExpressiveTheme`)
- **Navigation**: Type-safe Navigation 3 (`NavDisplay`, `NavBackStack`)
- **Native Linker**: JNI bridge communicating with OpenJDK binaries & Android Bionic C runtime
- **Storage & State**: MMKV high-speed memory-mapped KV storage + Kotlinx Serialization
- **Image Pipeline**: Coil 3 with WebP, SVG, and animated GIF decoding
- **Networking**: Ktor Client CIO engine with asynchronous streaming downloads

---

## 🏗️ Project Structure

```
Blade-Launcher/                         # Repository root
├── app/                                 # Android project root (Gradle multi-module)
│   ├── settings.gradle.kts              # Includes: BladeLauncher, LWJGL, LayerController,
│   │                                     # ColorPicker, Terracotta
│   ├── BladeLauncher/                   # Main Android application module
│   │   ├── build.gradle.kts             # Module build config, signing, JRE packaging
│   │   ├── gradle.properties            # Branding, versioning, default debug signing
│   │   └── src/main/
│   │       ├── AndroidManifest.xml      # Manifest (Portrait Launcher + Landscape Game)
│   │       ├── java/com/sednium/bladelauncher/
│   │       │   ├── components/          # Core components (JRE metadata, sound, controllers)
│   │       │   ├── coroutine/           # TaskSystem async worker pool
│   │       │   ├── game/                # Game launching engine
│   │       │   │   ├── account/         # Microsoft OAuth & offline account managers
│   │       │   │   ├── download/        # Game files, assets & Modrinth/CurseForge APIs
│   │       │   │   ├── launch/          # GameLauncher JVM arguments & classpath builder
│   │       │   │   ├── multirt/         # JdkDownloadManager (JDK 8-26 catalog & installer)
│   │       │   │   └── version/         # Version metadata & modpack parsers
│   │       │   ├── setting/             # AllSettings MMKV configurations
│   │       │   ├── ui/                  # Jetpack Compose UI
│   │       │   │   ├── components/      # Tactile smooth buttons, dialogs, cards
│   │       │   │   ├── screens/         # Screens (LauncherScreen, DownloadScreen, etc.)
│   │       │   │   └── theme/           # Milk White / Obsidian Orange color palettes & Shapes
│   │       │   └── viewmodel/           # Reactive ViewModels & UI state holders
│   │       └── res/                     # Drawables, layouts, localized strings (20+ languages)
│   ├── LWJGL/                           # LWJGL3 GLFW bridge module
│   ├── LayerController/                 # Controller mapping & layout editor module
│   ├── ColorPicker/                     # Color picker library module
│   └── Terracotta/                      # Multiplayer tunneling module (P2P via EasyTier)
├── website/                             # Project website (static, light theme)
│   ├── index.html                       # Landing page
│   ├── docs.html                        # Documentation
│   ├── changelog.html                   # Build/change log
│   ├── styles.css / script.js
├── README.md
└── LICENSE
```

---

## 🌐 Project Website

The `website/` folder contains Blade Launcher's static project site — a landing page, documentation, and changelog, sharing one stylesheet and script:

- [`website/index.html`](website/index.html) — overview, feature highlights
- [`website/docs.html`](website/docs.html) — runtime, renderer, and account setup guides
- [`website/changelog.html`](website/changelog.html) — development build log

Open `website/index.html` directly in a browser to preview; all three pages link to each other via relative paths, so keep the folder's files together.

---

## 🚀 Getting Started (Build & Run)

### Prerequisites

- **Java Development Kit (JDK)**: JDK 17 or JDK 21
- **Android SDK**:
  - Minimum SDK: API 26 (Android 8.0 Oreo)
  - Target SDK: API 35 (Android 15)
- **Android NDK**: NDK `25.2.9519653`
- **Build System**: Gradle 9.4.1 (included via `./gradlew`)

### Local Setup & Compilation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Sednium-Technologies/Blade-Launcher.git
   cd Blade-Launcher
   ```

2. **Configure Android SDK**:
   The Gradle project root is `app/`, not the repository root — create `local.properties` there:
   ```bash
   echo "sdk.dir=/path/to/your/Android/Sdk" > app/local.properties
   ```

3. **Compile Kotlin sources**:
   ```bash
   cd app
   ./gradlew compileDebugKotlin
   ```

4. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The generated APK will be located under:
   `app/BladeLauncher/build/outputs/apk/debug/` — the exact filename follows the pattern `BladeLauncher-Debug-<version>.apk`.

5. **Install on Device via ADB**:
   ```bash
   adb install -r BladeLauncher/build/outputs/apk/debug/BladeLauncher-Debug-*.apk
   ```

---

## ⚙️ Configuration & Environment

| Property | Description | Location |
|---|---|---|
| `launcher_name` | Internal project identifier (`BladeLauncher`) | `app/BladeLauncher/gradle.properties` |
| `launcher_app_name` | Display application name (`Blade Launcher`) | `app/BladeLauncher/gradle.properties` |
| `oauth_client_id` | Optional Microsoft OAuth Client ID | `gradle.properties` / Env |
| `curseforge_api_key` | Optional CurseForge Developer API Key | `gradle.properties` / Env |
| `STORE_PASSWORD` / `KEY_PASSWORD` | **Release** signing credentials — read from environment variables first, falling back to gitignored `app/BladeLauncher/.store_password.txt` / `.key_password.txt`. Never committed. | Env / gitignored local file |

> The debug build's signing password is a fixed value in `gradle.properties` for local development convenience only — as with Android's own default debug keystore, this is not distribution-sensitive. Release builds always use the environment/gitignored-file path above.

---

## 🧪 Testing & Quality Assurance

Run unit tests and verification checks:

```bash
# Run unit tests across modules
./gradlew testDebugUnitTest

# Lint and check style
./gradlew lintDebug
```

---

## ❓ Troubleshooting & FAQ

### 1. Game crashes immediately upon launch
- **Verify Java Version**: Go to **Settings** → **Java Runtime Manager** and click **Download JDK**. Ensure Java 17 or Java 21 is installed for modern Minecraft versions (1.17+).
- **Check Renderer**: Switch to **Vulkan (Zink)** or **GL4ES 1.1.5** in **Settings** → **Renderer** depending on your GPU driver compatibility.

### 2. Microsoft login times out or fails
- Ensure network connectivity is stable. Use the embedded browser code flow or click "Get Device Code" to authenticate in an external browser.

### 3. Mods failing to load
- Ensure the selected Mod Loader (Fabric / Forge / NeoForge) version matches the installed Minecraft game version.

---

## 📜 License & Compliance Notice

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.  
See the [LICENSE](LICENSE) file for the full license text.

### GPLv3 Section 7 Notice
- **Blade Launcher** is an independent fork based on [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) and [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher).
- Pursuant to Section 7 of the GPLv3, this modified version uses the distinct name **Blade Launcher** and includes proper copyright and attribution notices to all upstream authors and open-source contributors.
- *Minecraft is a registered trademark of Mojang Synergies AB. Blade Launcher is not affiliated with Mojang or Microsoft.*

<div align="center">
  <p>Made with ❤ by the Sednium Team & Open Source Community.</p>
</div>
