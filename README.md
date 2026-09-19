# 🌿 DETTO — Digital Detox & Mindfulness

> **Focus deeply. Cultivate presence. Grow your Zen Tree.**

DETTO is a minimalist, aesthetic digital detox and focus ecosystem engineered to break the mindless scrolling cycle through proactive friction, gamified mindfulness, and real-time distraction tracking.

---

## 📱 Phase 1: Android Application (`ANDROID_APP/`)

The native Android app is built from the ground up with **Jetpack Compose**, **Kotlin Coroutines / Flow**, **Room Database**, **DataStore**, and **Android Accessibility Services**.

### ✨ Key Features

1. **🌳 Dynamic Zen Tree Visualizer**
   - A live, swaying minimalist Japanese Bonsai tree that evolves organically based on your daily distraction-free hours.
   - **6 Time-Synced Stages**:
     - `● Seed`: *0m – 15m* (Nested in a zen soil bed with root tendrils and emerging cotyledon shoot)
     - `▲ Sprout`: *15m – 45m* (Tender shoot with developing leaves)
     - `⬡ Plant`: *45m – 2h* (Young bonsai structure and initial cloud canopies)
     - `◈ Tree`: *2h – 4h* (Majestic full bonsai with multilayered cloud foliage and glowing zen spores)
     - `✦ Garden`: *4h – 6h* (Flourishing tree flanked by companion plants)
     - `❖ Forest`: *6h+* (Ancient sanctuary of master discipline)
   - Real-time responsive canvas rendering designed for dark OLED screens.

2. **🛑 Intelligent Intervention Overlay**
   - Detects when distraction-heavy apps (Instagram, TikTok, YouTube, Reddit, X, etc.) are launched.
   - Intercepts opening with a mindful intervention before unlocking, breaking the unconscious dopamine loop.

3. **🎮 Mindfulness Detox Mini-Games**
   - **Number Sequence (1–9)**: Calming cognitive reaction test.
   - **Breathing Circle (4-7-8)**: Guided visual breathing pacer for mindfulness.
   - **Pattern Memory**: Visual memory recall sequence.

4. **⏱ Granular App Management & Per-App Timers**
   - Toggle monitoring for any installed app on your device.
   - Enabled tracked apps automatically pin to the top of the list for quick access.
   - Set individual daily time limits per app or configure a global daily distraction budget.

5. **📊 Evening Analytics & Live Permission Engine**
   - 11:00 PM evening breakdown notifications.
   - Detailed distraction vs. focus analytics and wellness recommendations.
   - Live status detection for Usage Access, Overlay, Accessibility Service, and Battery Optimization Exemption.

---

## 📂 Project Structure

```
DETTO/
├── ANDROID_APP/                  # Phase 1: Native Jetpack Compose Android Client
│   ├── app/                      # App module (Kotlin, Compose UI, Services, DataStore, Room)
│   ├── gradle/                   # Gradle wrapper
│   ├── build.gradle.kts          # Root build configuration
│   ├── settings.gradle.kts       # Module settings
│   └── gradlew / gradlew.bat     # Build scripts
├── README.md                     # Documentation & Roadmap
└── .gitignore                    # Project gitignore
```

---

## 🚀 Building & Running Locally

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17+
- Android SDK 35 (Android 15)

### Build Debug APK
```bash
cd ANDROID_APP
./gradlew assembleDebug
```

### Install directly to connected device
```bash
cd ANDROID_APP
./gradlew installDebug
```

---

## 🗺️ Roadmap & Upcoming Phases

- [x] **Phase 1**: Native Android App (Intervention engine, Zen tree visualizer, detox games, per-app limits).
- [ ] **Phase 2**: DETTO Web App & Cross-Platform Dashboard (Cloud sync, focus statistics, community sanctuary).
- [ ] **Phase 3**: Browser Extension & Desktop Focus Client.
