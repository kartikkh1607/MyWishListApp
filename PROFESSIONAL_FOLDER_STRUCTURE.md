# Professional Android Folder Structure Refactoring

## Current vs Recommended Structure

### Current State Analysis
The project currently has files scattered across the main package. Here's the recommended professional structure:

## Recommended Folder Structure

```
app/src/main/java/com/example/mywishlistapp/
├── data/                           # Data layer
│   ├── local/                      # Local data sources
│   │   ├── database/               
│   │   │   ├── WishDatabase.kt     # ← Move from Data/WishDataBase.kt
│   │   │   ├── dao/
│   │   │   │   ├── WishDao.kt      # ← Move from Data/WishDao.kt  
│   │   │   │   ├── UserProfileDao.kt # ← Move from Data/UserProfileDao.kt
│   │   │   │   └── MilestoneDao.kt  # ← Move from Data/MilestoneDao.kt
│   │   │   └── entities/
│   │   │       ├── Wish.kt         # ← Move from Data/Wish.kt
│   │   │       ├── Milestone.kt    # ← Move from Data/Milestone.kt
│   │   │       └── UserProfile.kt  # ← Add missing entity
│   │   └── preferences/            # SharedPreferences, DataStore
│   ├── remote/                     # API clients (future)
│   ├── repository/                 # Repository implementations
│   │   ├── WishRepository.kt       # ← Move from Data/Wishrepository.kt
│   │   ├── UserProfileRepository.kt # ← Move from Data/UserProfileRepository.kt
│   │   └── MilestoneRepository.kt  # ← Move from Data/MilestoneRepository.kt
│   └── models/                     # Data models
│       ├── GamificationModels.kt   # ← Move from Data/GamificationModels.kt
│       └── NotificationModels.kt   # ← Move from models/NotificationModels.kt
│
├── domain/                         # Business logic layer
│   ├── model/                      # Domain models
│   ├── repository/                 # Repository interfaces
│   └── usecase/                    # Use cases (business logic)
│       ├── wish/
│       ├── user/
│       └── notification/
│
├── presentation/                   # UI layer
│   ├── navigation/                 # ✓ Already exists
│   │   └── NavigationGraph.kt      # ✓ Already properly placed
│   ├── state/                      # ✓ Already exists  
│   │   └── UiState.kt              # ✓ Already properly placed
│   ├── components/                 # ✓ Already exists
│   │   └── ModernComponents.kt     # ✓ Already properly placed
│   ├── screens/                    # Main app screens
│   │   ├── dashboard/
│   │   │   ├── DashboardScreen.kt  # ← Move from root
│   │   │   └── DashboardViewModel.kt # ← Extract from WishViewModel.kt
│   │   ├── wishlist/
│   │   │   ├── WishListScreen.kt   # ← Move from root
│   │   │   └── WishListViewModel.kt
│   │   ├── search/
│   │   │   ├── SearchScreen.kt     # ← Move from root
│   │   │   └── SearchViewModel.kt
│   │   ├── calendar/
│   │   │   ├── CalendarScreen.kt   # ← Move from root
│   │   │   └── CalendarViewModel.kt
│   │   ├── profile/
│   │   │   ├── ProfileScreen.kt    # ← Move from root
│   │   │   └── ProfileViewModel.kt
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt   # ← Move from root
│   │   │   └── SettingsViewModel.kt
│   │   ├── notifications/
│   │   │   ├── NotificationsScreen.kt # ← Move from root
│   │   │   └── NotificationsViewModel.kt
│   │   ├── onboarding/
│   │   │   ├── OnboardingScreen.kt # ← Move from root
│   │   │   └── OnboardingViewModel.kt
│   │   ├── addedit/
│   │   │   ├── AddEditDetailView.kt # ← Move from root
│   │   │   └── AddEditViewModel.kt
│   │   └── home/
│   │       ├── HomeView.kt         # ← Move from root
│   │       └── HomeViewModel.kt
│   ├── theme/                      # ✓ Already in ui/theme/
│   │   ├── Color.kt                # ✓ Already properly placed
│   │   ├── Theme.kt                # ✓ Already properly placed
│   │   ├── Type.kt                 # ✓ Already properly placed
│   │   └── Spacing.kt              # ✓ Already properly placed
│   └── MainActivity.kt             # ← Move from root
│
├── di/                             # Dependency injection
│   ├── DatabaseModule.kt           # Hilt/Koin modules
│   ├── RepositoryModule.kt
│   └── ViewModelModule.kt
│
├── utils/                          # ✓ Already exists
│   ├── CrashReporter.kt            # ✓ Already properly placed
│   ├── extensions/                 # Extension functions
│   └── Constants.kt                # App constants
│
├── notifications/                  # ✓ Already exists and well-organized
│   ├── ReminderSystem.kt           # ✓ Already properly placed
│   ├── ReminderReceiver.kt         # ✓ Already properly placed
│   └── ReminderActionReceiver.kt   # ✓ Already properly placed
│
└── WishListApp.kt                  # ← Keep in root (Application class)
```

## Migration Steps

### Phase 1: Create New Directory Structure
```bash
# Create main directories
mkdir -p app/src/main/java/com/example/mywishlistapp/data/local/database/dao
mkdir -p app/src/main/java/com/example/mywishlistapp/data/local/database/entities
mkdir -p app/src/main/java/com/example/mywishlistapp/data/repository
mkdir -p app/src/main/java/com/example/mywishlistapp/data/models
mkdir -p app/src/main/java/com/example/mywishlistapp/domain/model
mkdir -p app/src/main/java/com/example/mywishlistapp/domain/repository
mkdir -p app/src/main/java/com/example/mywishlistapp/domain/usecase/wish
mkdir -p app/src/main/java/com/example/mywishlistapp/domain/usecase/user
mkdir -p app/src/main/java/com/example/mywishlistapp/domain/usecase/notification
mkdir -p app/src/main/java/com/example/mywishlistapp/presentation/screens/dashboard
mkdir -p app/src/main/java/com/example/mywishlistapp/presentation/screens/wishlist
mkdir -p app/src/main/java/com/example/mywishlistapp/presentation/screens/search
mkdir -p app/src/main/java/com/example/mywishlistapp/presentation/screens/calendar
mkdir -p app/src/main/java/com/example/mywishlistapp/presentation/screens/profile
mkdir -p app/src/main/java/com/example/mywishlistapp/presentation/screens/settings
mkdir -p app/src/main/java/com/example/mywishlistapp/presentation/screens/notifications
mkdir -p app/src/main/java/com/example/mywishlistapp/presentation/screens/onboarding
mkdir -p app/src/main/java/com/example/mywishlistapp/presentation/screens/addedit
mkdir -p app/src/main/java/com/example/mywishlistapp/presentation/screens/home
mkdir -p app/src/main/java/com/example/mywishlistapp/di
mkdir -p app/src/main/java/com/example/mywishlistapp/utils/extensions
```

### Phase 2: Move Data Layer Files
```bash
# Move database files
git mv app/src/main/java/com/example/mywishlistapp/Data/WishDataBase.kt app/src/main/java/com/example/mywishlistapp/data/local/database/WishDatabase.kt
git mv app/src/main/java/com/example/mywishlistapp/Data/WishDao.kt app/src/main/java/com/example/mywishlistapp/data/local/database/dao/WishDao.kt
git mv app/src/main/java/com/example/mywishlistapp/Data/UserProfileDao.kt app/src/main/java/com/example/mywishlistapp/data/local/database/dao/UserProfileDao.kt
git mv app/src/main/java/com/example/mywishlistapp/Data/MilestoneDao.kt app/src/main/java/com/example/mywishlistapp/data/local/database/dao/MilestoneDao.kt

# Move entities
git mv app/src/main/java/com/example/mywishlistapp/Data/Wish.kt app/src/main/java/com/example/mywishlistapp/data/local/database/entities/Wish.kt
git mv app/src/main/java/com/example/mywishlistapp/Data/Milestone.kt app/src/main/java/com/example/mywishlistapp/data/local/database/entities/Milestone.kt

# Move repositories
git mv app/src/main/java/com/example/mywishlistapp/Data/Wishrepository.kt app/src/main/java/com/example/mywishlistapp/data/repository/WishRepository.kt
git mv app/src/main/java/com/example/mywishlistapp/Data/UserProfileRepository.kt app/src/main/java/com/example/mywishlistapp/data/repository/UserProfileRepository.kt
git mv app/src/main/java/com/example/mywishlistapp/Data/MilestoneRepository.kt app/src/main/java/com/example/mywishlistapp/data/repository/MilestoneRepository.kt

# Move models
git mv app/src/main/java/com/example/mywishlistapp/Data/GamificationModels.kt app/src/main/java/com/example/mywishlistapp/data/models/GamificationModels.kt
git mv app/src/main/java/com/example/mywishlistapp/models/NotificationModels.kt app/src/main/java/com/example/mywishlistapp/data/models/NotificationModels.kt
```

### Phase 3: Move Presentation Layer Files
```bash
# Move screen files to their respective directories
git mv app/src/main/java/com/example/mywishlistapp/MainActivity.kt app/src/main/java/com/example/mywishlistapp/presentation/MainActivity.kt
git mv app/src/main/java/com/example/mywishlistapp/DashboardScreen.kt app/src/main/java/com/example/mywishlistapp/presentation/screens/dashboard/DashboardScreen.kt
git mv app/src/main/java/com/example/mywishlistapp/WishListScreen.kt app/src/main/java/com/example/mywishlistapp/presentation/screens/wishlist/WishListScreen.kt
git mv app/src/main/java/com/example/mywishlistapp/SearchScreen.kt app/src/main/java/com/example/mywishlistapp/presentation/screens/search/SearchScreen.kt
git mv app/src/main/java/com/example/mywishlistapp/CalendarScreen.kt app/src/main/java/com/example/mywishlistapp/presentation/screens/calendar/CalendarScreen.kt
git mv app/src/main/java/com/example/mywishlistapp/ProfileScreen.kt app/src/main/java/com/example/mywishlistapp/presentation/screens/profile/ProfileScreen.kt
git mv app/src/main/java/com/example/mywishlistapp/SettingsScreen.kt app/src/main/java/com/example/mywishlistapp/presentation/screens/settings/SettingsScreen.kt
git mv app/src/main/java/com/example/mywishlistapp/NotificationsScreen.kt app/src/main/java/com/example/mywishlistapp/presentation/screens/notifications/NotificationsScreen.kt
git mv app/src/main/java/com/example/mywishlistapp/OnboardingScreen.kt app/src/main/java/com/example/mywishlistapp/presentation/screens/onboarding/OnboardingScreen.kt
git mv app/src/main/java/com/example/mywishlistapp/AddEditDetailView.kt app/src/main/java/com/example/mywishlistapp/presentation/screens/addedit/AddEditDetailView.kt
git mv app/src/main/java/com/example/mywishlistapp/HomeView.kt app/src/main/java/com/example/mywishlistapp/presentation/screens/home/HomeView.kt
```

### Phase 4: Update All Import Statements
After moving files, you'll need to update import statements in all files. This can be done with IDE refactoring tools or manually.

### Phase 5: Clean Up Unused Files
```bash
# Remove duplicate/test files that are no longer needed
git rm app/src/main/java/com/example/mywishlistapp/MinimalDashboardScreen.kt
git rm app/src/main/java/com/example/mywishlistapp/MinimalMainScreen.kt
git rm app/src/main/java/com/example/mywishlistapp/MinimalWishViewModel.kt
git rm app/src/main/java/com/example/mywishlistapp/MinimalWishViewModelFactory.kt
git rm app/src/main/java/com/example/mywishlistapp/SimpleMainScreen.kt
git rm app/src/main/java/com/example/mywishlistapp/TestDashboardMainScreen.kt
git rm app/src/main/java/com/example/mywishlistapp/TestMainScreen.kt
git rm app/src/main/java/com/example/mywishlistapp/UltraMinimalDashboard.kt
```

## Benefits of This Structure

1. **Separation of Concerns**: Clear separation between data, domain, and presentation layers
2. **Scalability**: Easy to add new features and screens
3. **Maintainability**: Related code is grouped together
4. **Testability**: Each layer can be tested independently
5. **Team Collaboration**: Multiple developers can work on different features without conflicts
6. **Clean Architecture**: Follows Android architecture guidelines
7. **Navigation**: Centralized navigation with type-safe routing

## Important Notes

1. **Gradual Migration**: This refactoring should be done incrementally to avoid breaking the build
2. **Import Updates**: All import statements will need to be updated after moving files
3. **Build Configuration**: Update any build scripts that reference moved files
4. **Testing**: Run tests after each phase to ensure nothing is broken
5. **Version Control**: Use `git mv` to preserve file history

## Commit Strategy

```bash
# Commit each phase separately
git commit -m "Phase 1: Create new folder structure"
git commit -m "Phase 2: Move data layer files to new structure"
git commit -m "Phase 3: Move presentation layer files to new structure"
git commit -m "Phase 4: Update all import statements"
git commit -m "Phase 5: Clean up unused files and complete refactoring"
```

This structure follows modern Android development best practices and will make your codebase more maintainable and scalable.