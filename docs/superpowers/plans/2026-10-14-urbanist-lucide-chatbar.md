# Urbanist Font, Lucide Icons, and In-Game Chat Bar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate the Urbanist typography across Blade Launcher, refresh core navigation/toolbar icons with Lucide-style vector drawables, and implement a floating Compose in-game chat bar docked above the virtual keyboard in landscape mode.

**Architecture:**
- **Typography:** Download static Urbanist font files (Regular, Medium, SemiBold, Bold) into `app/BladeLauncher/src/main/res/font/` and configure `AppTypography` in `ui/theme/Type.kt` using `FontFamily`.
- **Lucide Icons:** Replace core navigation and action vector drawables (`ic_home`, `ic_settings`, `ic_tune`, `ic_download`, `ic_extension`, `ic_play_arrow`) with crisp 24dp Lucide-style vectors.
- **In-Game Chat Bar:** Implement `InGameChatBar` in `TextInput.kt` using `Modifier.imePadding()` to float immediately above the on-screen keyboard, featuring a live text field, quick `/` command toggle, Tab completion button, Send button, and Close button. Hook into `VMActivity.kt` when `vmViewModel.textInputMode == TextInputMode.ENABLE`.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Android IME Insets, Vector Drawables, Gradle.

**Spec:** User request: "Bro use lucide icons, urbanist font. And the game running, but the world isn't rendering. And when writing anything on the chat, because of keyboard the chat area is obstacles. Fix that."

## Global Constraints
- Target package: `com.sednium.bladelauncher.v2`. Do not change package name or launcher application icon.
- Preserve upstream attribution to MovTery; maintain repository and release references for `Sednium-Technologies/Blade-Launcher`.
- Respect dark/light themes and design tokens in `Theme.kt`.
- All tests in `:BladeLauncher:testDebugUnitTest` must pass.

---

### Task 1: Urbanist Font Integration

**Files:**
- Create: `app/BladeLauncher/src/main/res/font/urbanist_regular.ttf`
- Create: `app/BladeLauncher/src/main/res/font/urbanist_medium.ttf`
- Create: `app/BladeLauncher/src/main/res/font/urbanist_semibold.ttf`
- Create: `app/BladeLauncher/src/main/res/font/urbanist_bold.ttf`
- Modify: `app/BladeLauncher/src/main/java/com/sednium/bladelauncher/ui/theme/Type.kt`

**Interfaces:**
- Produces: `urbanistFontFamily: FontFamily` used in `AppTypography: Typography`

- [ ] **Step 1: Download Urbanist TTF font files**
Download static TTFs from `https://raw.githubusercontent.com/coreyhu/Urbanist/main/fonts/ttf/` into `app/BladeLauncher/src/main/res/font/`.
- [ ] **Step 2: Update Type.kt**
Define `urbanistFontFamily` and configure all `Typography` styles (display, headline, title, body, label) to use `fontFamily = urbanistFontFamily`.
- [ ] **Step 3: Verify build and resource compilation**
Run `./gradlew :BladeLauncher:compileDebugKotlin` to verify font resources and typography compile cleanly.
- [ ] **Step 4: Commit**
`git add app/BladeLauncher/src/main/res/font/ app/BladeLauncher/src/main/java/com/sednium/bladelauncher/ui/theme/Type.kt && git commit -m "feat(ui): integrate Urbanist font family across typography"`

---

### Task 2: Core Lucide Vector Icons

**Files:**
- Modify/Create:
  - `app/BladeLauncher/src/main/res/drawable/ic_home.xml`
  - `app/BladeLauncher/src/main/res/drawable/ic_settings.xml`
  - `app/BladeLauncher/src/main/res/drawable/ic_tune.xml`
  - `app/BladeLauncher/src/main/res/drawable/ic_download.xml`
  - `app/BladeLauncher/src/main/res/drawable/ic_extension.xml`
  - `app/BladeLauncher/src/main/res/drawable/ic_play_arrow.xml`

**Interfaces:**
- Produces: Vector XML drawables matching Lucide icon geometry and stroke styling with standard `?attr/colorControlNormal` / `@android:color/white` tinting.

- [ ] **Step 1: Check existing drawables and usage**
Inspect existing icon references in navigation drawer, bottom bar, and launch screen.
- [ ] **Step 2: Add Lucide vector drawables**
Add crisp Lucide SVG/Vector XML equivalents for Home, Settings, Tune (sliders), Download, Extension (puzzle piece), and Play (play triangle).
- [ ] **Step 3: Verify vector drawable rendering and build**
Run `./gradlew :BladeLauncher:mergeDebugResources` to verify vector validity.
- [ ] **Step 4: Commit**
`git add app/BladeLauncher/src/main/res/drawable/ && git commit -m "feat(ui): update core navigation and toolbar icons to Lucide style"`

---

### Task 3: In-Game Chat Input Bar

**Files:**
- Modify: `app/BladeLauncher/src/main/java/com/sednium/bladelauncher/ui/control/input/TextInput.kt`
- Modify: `app/BladeLauncher/src/main/java/com/sednium/bladelauncher/ui/activities/VMActivity.kt`

**Interfaces:**
- Consumes: `vmViewModel.sendInputText(text: String)`, `vmViewModel.sendEnder()`, `vmViewModel.sendTab()`, `vmViewModel.textInputMode`
- Produces: `InGameChatBar` Compose composable docked via `Modifier.imePadding()`

- [ ] **Step 1: Create InGameChatBar in TextInput.kt**
Construct a lightweight floating toolbar containing:
- Rounded background with elevation / blur / dark translucent container
- TextField / BasicTextField with auto-focus when visible
- `/` quick button (inserts `/` at beginning if not already present)
- `Tab` completion button (sends Tab keypress to LWJGL)
- `Send` button (sends current text to game via `vmViewModel.sendInputText(text)` + `vmViewModel.sendEnder()`, then clears text)
- `Close` button (closes keyboard and hides chat bar)
- [ ] **Step 2: Connect InGameChatBar to VMActivity.kt**
In `VMActivity.kt`, host `InGameChatBar` inside the fullscreen Compose overlay whenever `vmViewModel.textInputMode == TextInputMode.ENABLE`.
- [ ] **Step 3: Verify compilation and tests**
Run `./gradlew :BladeLauncher:compileDebugKotlin` and `./gradlew :BladeLauncher:testDebugUnitTest`.
- [ ] **Step 4: Commit**
`git commit -m "feat(game): add docked in-game chat bar above soft keyboard"`

---

### Task 4: Release APK Build & Device Delivery

**Files:**
- `app/BladeLauncher/build/outputs/apk/release/BladeLauncher-release.apk`

- [ ] **Step 1: Build release APK**
Run `./gradlew :BladeLauncher:assembleRelease`.
- [ ] **Step 2: Verify APK creation and size**
Ensure release APK exists and is properly signed.
- [ ] **Step 3: Send APK to Infinix device via KDE Connect**
Run `kdeconnect-cli --device d13cd17d9cb54283bb0bc4cf62cd32b8 --share <apk_path>`.
- [ ] **Step 4: Provide in-game chunk rendering settings guidance**
Document the exact Sodium/Iris chunk renderer toggle to solve the black world rendering on Mali-G57 Zink.
