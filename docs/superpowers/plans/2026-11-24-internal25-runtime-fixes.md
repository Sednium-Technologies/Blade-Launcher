# Implementation Plan: Fix Internal-25 Missing Runtime, JdkDownloadManager Local Asset Routing, and Download Button Text Wrapping

## Goal
Resolve three issues in Blade Launcher:
1. `Internal-25` missing from the main **Settings → Java** runtime list on fresh/updated installs: fix asset detection in `UnpackJreTask`, add auto-unpacking and registration of bundled runtimes in `RuntimesManager` and `SplashActivity`/`MainActivity`.
2. Route `JDK Java Runtime Manager` downloads for Java 8, 17, 21, and 25 to local bundled assets via `UnpackJreTask` instead of remote HTTP requests that 404.
3. Fix vertical `"Download"` text wrapping in portrait mode in `JavaManageScreen.kt` by setting `maxLines = 1, softWrap = false` and proper button width.
4. Bump version to **v2.5.2** (`versionCode: 200034`), verify unit tests, build release APKs for all three architectures (`arm64-v8a`, `armeabi-v7a`, `x86_64`), and publish to GitHub.

---

## Proposed Changes

### 1. `UnpackJreTask.kt`
- Replace `assetManager.list(jre.jrePath)` with direct stream validation:
  ```kotlin
  val archPack = "${jre.jrePath}/${getRuntimeByArch()}"
  val universalPack = "${jre.jrePath}/universal.tar.xz"
  assetManager.open(archPack).close()
  assetManager.open(universalPack).close()
  ```
  This eliminates failure on Android ROMs where `AssetManager.list()` returns empty for assets without explicit zip directory entries.
- Make version loading resilient: fallback to `"1"` if `version` file is missing or unreadable.

### 2. `RuntimesManager.kt` & Startup (`SplashActivity.kt`, `MainActivity.kt`)
- In `RuntimesManager.kt`:
  - Add `suspend fun ensureBundledRuntimes(context: Context) = withContext(Dispatchers.IO)`: checks each `Jre.entries` against assets; if available in APK assets and not complete on disk, extracts it with `UnpackJreTask`.
  - In `loadRuntime()`: Ensure Java 25 is accurately detected and parsed (`javaVersion == 25`, `arch == "aarch64"`).
- In `MainActivity.kt`:
  - In `onCreate()`, invoke `lifecycleScope.launch(Dispatchers.IO) { RuntimesManager.ensureBundledRuntimes(this@MainActivity) }` to self-heal any missing bundled runtimes even if splash was bypassed.
- In `SplashActivity.kt`:
  - With `UnpackJreTask` fixed, `Internal-25` will be added to `unpackItems` on startup and unpacked automatically.

### 3. `JdkDownloadManager.kt`
- In `downloadAndInstallJdk()`:
  - If `internalJre != null` (matching Java 8, 17, 21, or 25):
    - Unpack directly via `UnpackJreTask(context, internalJre).run()`.
    - Update progress (`_downloadingVersions.value`, `onProgress(1f)`).
    - If complete, toast success and trigger `onSuccess()`.
    - Never fall back to remote HTTP GET for bundled runtimes.

### 4. `JavaManageScreen.kt` & `Buttons.kt`
- In `Buttons.kt`:
  - Add `maxLines: Int = 1, softWrap: Boolean = false` to `BaseIconTextButton` and `IconTextButton`.
- In `JavaManageScreen.kt`:
  - In `JdkDownloadDialog`:
    - Constrain the title/badge Row with `Modifier.weight(1f, fill = false)`.
    - Add `Spacer(Modifier.width(8.dp))`.
    - Add `defaultMinSize(minWidth = 100.dp)` and `maxLines = 1, softWrap = false` to the Download button so text never wraps vertically.
  - In `JavaManageScreen`:
    - Add `LaunchedEffect(Unit)` to ensure bundled runtimes are extracted and update `runtimes` state.

### 5. Version Bump & Release
- Bump `launcher_version_code` to `200034` and `launcher_version_name` to `2.5.2` in `app/BladeLauncher/gradle.properties`.
- Run `./gradlew testDebugUnitTest`.
- Build release APKs for `arm64`, `arm`, and `x86_64`.
- Push commit, tag `v2.5.2`, and publish release on GitHub with all three APKs attached.

---

## Verification Plan
1. Unit tests pass: `./gradlew testDebugUnitTest`.
2. Inspect APK contents with `unzip -l`: verify `jre-25/bin-arm64.tar.xz`, `jre-25/universal.tar.xz`, and `version` are packaged.
3. Test `UnpackJreTask` asset opening logic.
4. Verify all 3 architectures compile and assemble release APKs with R8.
