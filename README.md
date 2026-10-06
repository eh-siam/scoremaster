# 🏏 ScoreMaster - Android Cricket Scoring & Tournament App

[![Android Studio](https://img.shields.io/badge/Android%20Studio-2026.1-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-orange.svg)](https://developer.android.com/topic/architecture)

**ScoreMaster** is a modern, high-performance Android application built with **Jetpack Compose**, **Material 3**, and **Room Database**. It features an official **ICC DLS (Duckworth-Lewis-Stern) Engine**, ball-by-ball live scoring with 3D particle celebration overlays, zero-lag audio effects, real-time analytics leaderboards, and tournament league management.

---

## 🌟 Key Features

### 🌧️ Official ICC Standard DLS Calculator Engine
- **ICC Standard Formula**: $Z(u, w) = Z_0(w) \times (1 - e^{-b(w) u})$ with exact ICC constants.
- **Format-Aware Average Scores ($G$)**: $G = 150$ for T20 and $G = 245$ for ODI matches.
- **Premature 1st Innings & Match Overs Revision**: Computes revised targets, par scores, and resource loss percentages ($R_1$ vs $R_2$).
- **Minimum Overs Safeguard**: Enforces official minimum overs rules (e.g. 5 overs for T20) before applying revised DLS targets.

### ⚡ Ball-by-Ball Live Scoring & Audio Effects
- **Live Scoring Grid**: Ball-by-ball recording with runs, extras (Wide, No Ball, Bye, Leg Bye), wickets, and strike swaps.
- **3D Celebration Overlays**: Hardware-accelerated 360-degree particle explosions and 3D bouncing badges for **FOUR!**, **SIX!**, **WICKET!**, and **50/100 Milestones**.
- **Zero-Lag Audio Feedback**: Crisp sound effects for boundaries and wickets with a top app bar **Sound ON/OFF (`🔊 / 🔇`)** toggle switch.
- **Cricket Rule Enforcement**: Auto-prompts for bowler selection at the end of each over and prevents consecutive overs by the same bowler.

### 📊 Real-Time Analytics & Leaderboards
- **Orange Cap (Most Runs)**: Top batters leaderboard with total runs, balls faced, 4s, 6s, and strike rates.
- **Purple Cap (Most Wickets)**: Top bowlers leaderboard with total wickets, overs bowled, runs conceded, and economy rates.
- **Match Highlights**: Highest individual batting score and best bowling spell figures across all matches.

### 🏆 Tournament & League Management
- **3-in-1 Match Formats**: Head-to-Head 1v1, Tri-Nations Series, and Multi-Team Tournament Leagues.
- **Live Points Table & NRR**: Automated team standings with Played, Won, Lost, Tied, Points, and Net Run Rate (NRR) calculations.
- **Tournament Dashboards**: View match fixtures, standings, and tournament statistics.

### 🎨 Ultra-Premium Broadcast UI & UX
- **Theme Palette**: Deep Navy (`#071A2B`) primary brand, Fresh Electric Green (`#19C37D`) accent, and Crimson Alert Red (`#EF4444`).
- **3D Animated Splash Screen**: Radial gradient, bouncy scale physics, and 3D spinning cricket emblem.
- **Keyboard Handling**: Soft keyboard auto-scroll padding (`imePadding()`) and `Next/Done` action navigation for squad input.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose + Material 3 Design System
- **Architecture**: MVVM + Clean Architecture + Kotlin StateFlow / Flow
- **Local Database**: Room Database + KSP (Kotlin Symbol Processing)
- **Navigation**: Jetpack Navigation Compose
- **Graphics & Animation**: Compose Canvas, `graphicsLayer`, `Animatable`, `spring` physics

---

## 📂 Project Structure

```text
com.example.scoremaster
├── data
│   ├── database             # Room Database, Entities & DAOs
│   └── repository           # Match & Tournament Repositories
├── domain
│   ├── dls                  # Official ICC DLS Calculator Engine & Formulas
│   ├── engine               # Cricket Scoring Engine & Points Table Calculator
│   └── model                # Domain Data Models (Match, Innings, BallEvent, Player)
├── navigation               # NavHost, Routes & Bottom Navigation Bar
└── ui
    ├── analytics            # Analytics & Leaderboards Screen
    ├── components           # Reusable Tables, Cards, Overlays & Buttons
    ├── details              # Match Details Screen
    ├── home                 # Dashboard Screen with 3D Hero Card & Live Matches
    ├── result               # Match Summary & Result Screen
    ├── scoring              # Live Match Scoring & Sound Effects
    ├── setup                # Match & Tournament Setup Form
    ├── splash               # 3D Animated Splash Screen
    ├── theme                # Color Palette, Typography & Material 3 Theme
    └── tournament           # Tournaments List & Tournament Dashboard
```

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 11 / JDK 17
- Android SDK 35/36 (Minimum SDK: 24)

### Build Commands
```bash
# Clone the repository
git clone https://github.com/your-username/ScoreMaster.git
cd ScoreMaster

# Build Debug APK
./gradlew :app:assembleDebug
```

---

## 📄 License
Licensed under the [MIT License](LICENSE).
