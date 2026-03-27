# 📋 MyWishList App

A modern Android wishlist manager built with **Jetpack Compose** and **Room Database**. Add, organize, and track everything you want — with priorities, categories, tags, and search.

---

## ✨ Features

- **Add / Edit / Delete** wishes with full detail support
- **Priority levels** — 🔥 High, ⚡ Medium, 🌱 Low
- **Categories & Tags** to keep things organized
- **Price tracking** for each wish item
- **Mark as completed** to track what you've got
- **Search screen** with real-time filtering by title, description, category, or tag
- **Dashboard** for a quick overview of your wishlist stats
- **Settings screen** for app preferences
- **Staggered animations** for smooth list entrance transitions

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose 2.8.7 (Type-Safe) |
| Database | Room 2.6.1 |
| Architecture | MVVM (ViewModel + Repository) |
| Async | Kotlin Coroutines + Flow |
| Language | Kotlin |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 |
| Compile SDK | 35 |

---

## 🗂️ Project Structure

```
app/src/main/java/com/example/mywishlistapp/
├── Data/
│   ├── Wish.kt              # Room entity + Priority enum + TypeConverters
│   ├── WishDao.kt           # Database access object
│   ├── WishDataBase.kt      # Room database class
│   └── Wishrepository.kt   # Repository layer
├── ui/
│   ├── screens/
│   │   ├── AddEditDetailView.kt   # Add & edit wish screen
│   │   ├── DashboardScreen.kt     # Dashboard overview
│   │   ├── SearchScreen.kt        # Search & filter screen
│   │   ├── SettingsScreen.kt      # Settings screen
│   │   └── WishListScreen.kt      # Main wishlist screen
│   ├── theme/               # Colors, typography, theme
│   ├── Animations.kt        # Shared spring/tween specs + StaggeredEntrance
│   ├── MainScreen.kt        # Bottom nav scaffold
│   ├── Navigation.kt        # Nav graph setup
│   ├── Screen.kt            # Type-safe route definitions
│   ├── WishViewModel.kt     # ViewModel with state + actions
│   └── Wishfilterutils.kt   # Filter & priority helper functions
├── MainActivity.kt
└── WishListApp.kt           # Application class (Room init)
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 26+
- Kotlin 2.0+

### Run Locally

1. Clone the repo:
   ```bash
   git clone https://github.com/kartikkh1607/MyWishListApp.git
   ```
2. Open in **Android Studio**
3. Let Gradle sync complete
4. Run on an emulator or physical device (API 26+)

---

## 📦 Key Dependencies

```kotlin
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Navigation (Type-Safe)
implementation("androidx.navigation:navigation-compose:2.8.7")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

// Compose BOM + Material 3
implementation(platform(libs.androidx.compose.bom))
implementation("androidx.compose.material3:material3:1.3.2")
implementation("androidx.compose.material:material-icons-extended")

// Lifecycle
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
```

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
