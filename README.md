# VectorPatch

<p align="center">
  <b>Modern Non-Root Xposed Framework</b><br>
  Merging <b>LSPatch</b> patching architecture with <b>Vector</b> modern Jetpack Compose UI & engine.
</p>

---

## ⚡ Features

- **Non-Root Xposed Runtime:** Run modern Xposed modules in user-space applications without unlocking your bootloader or needing root access.
- **Modern Vector UI:** Redesigned interface built entirely from scratch with **Jetpack Compose** and **Material 3**, featuring dynamic theming (Material You), smooth animations, and a sleek dark/light palette.
- **Shizuku Integration:** Execute privileged package manager operations (`pm install`, uninstall, queries) silently and reliably without root.
- **Integrated & Manager Modes:**
  - *Integrated Mode:* Embeds selected modules directly into target APKs for portable, standalone distribution.
  - *Manager Mode:* Injects a lightweight loader and manages modules dynamically via VectorPatch.
- **In-App Auto-Updater:** Seamless update checks against GitHub Releases with background download notifications.
- **Automated GitHub CI/CD:** Complete release workflow that builds, signs, and publishes release APKs automatically upon pushing version tags (`v*`).

---

## 🏗️ Architecture

```text
VectorPatch/
├── app/                  # Main Android Manager app (Compose UI, ViewModels, Hilt)
│   ├── ui/               # Screens: Home, Modules, Apps, Patch, Settings
│   ├── service/          # ShizukuManager & lifecycle listeners
│   └── updater/          # In-app GitHub update checker & scheduler
├── patch-core/           # Non-root patching engine (ported from LSPatch)
│   ├── apk/              # APK decoder, DEX injector, repacker
│   ├── patcher/          # Main patch pipeline & options
│   └── signing/          # APK v1/v2/v3 signer
├── shizuku-service/      # Privileged AIDL service running under shell UID
│   └── aidl/             # IVectorPatchService definitions
├── updater/              # Standalone GitHub release checker & background worker
└── .github/workflows/    # CI/CD: release.yml, nightly.yml, generate-keystore.yml
```

---

## 🚀 Getting Started

### Prerequisites

- Android 9.0+ (API 28+)
- [Shizuku](https://shizuku.rikka.app/) (strongly recommended for non-root automated installs)
- JDK 17+ & Android SDK Platform 35 (for building from source)

### Building from Source

```bash
# Clone the repository
git clone https://github.com/realheckerrr-bit/VectorPatch.git
cd VectorPatch

# Build debug APK
./gradlew :app:assembleDebug

# Build release APK
./gradlew :app:assembleRelease
```

---

## 📦 Releases & CI/CD Setup

VectorPatch includes a ready-to-use GitHub Actions workflow (`.github/workflows/release.yml`).

### Setting up Keystore Signing Secrets

To produce signed release APKs in GitHub Actions:
1. Generate a keystore or run the **Generate Release Keystore** workflow (`.github/workflows/generate-keystore.yml`) via `workflow_dispatch`.
2. Add the following repository secrets under **Settings > Secrets and variables > Actions**:
   - `KEYSTORE_BASE64`: Base64 string of your `.jks` file.
   - `KEY_ALIAS`: Keystore alias name.
   - `STORE_PASSWORD`: Keystore password.
   - `KEY_PASSWORD`: Key password.
3. Push a tag (e.g. `git tag v1.0.0 && git push origin v1.0.0`) to trigger automated release creation and APK upload.

---

## 📄 Credits & Acknowledgments

- **[LSPatch](https://github.com/LSPosed/LSPatch)** by the LSPosed Developers — Original non-root Xposed implementation and patching concepts.
- **[Vector](https://github.com/JingMatrix/Vector)** by JingMatrix — Modern ART hooking engine and UI design.
- **[Shizuku](https://github.com/RikkaApps/Shizuku)** by Rikka — Binder IPC bridge for privileged shell execution on non-rooted Android.
- **[LSPlant](https://github.com/LSPosed/LSPlant)** — High-performance native ART hooking library.

---

## ⚖️ License

VectorPatch is distributed under the GNU General Public License v3.0 (GPL-3.0) consistent with upstream dependencies.
