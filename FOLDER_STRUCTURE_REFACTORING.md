# Folder Structure Refactoring Plan

## Current Structure Analysis

The current project structure has some organization but doesn't follow clean architecture principles. Here's the current state and recommended improvements:

## Current Structure Issues

1. **Mixed responsibilities**: UI components, ViewModels, and data classes are all in the main package
2. **No clear separation**: Domain, data, and presentation layers are mixed
3. **Inconsistent naming**: Some folders use lowercase, others use PascalCase
4. **No modularization**: Everything is in a single module

## Recommended Clean Architecture Structure

```
app/src/main/java/com/example/mywishlistapp/
│
├── data/                           # Data Layer
│   ├── local/                      # Local data sources
│   │   ├── database/
│   │   │   ├── WishDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── WishDao.kt
│   │   │   │   ├── UserProfileDao.kt
│   │   │   │   └── MilestoneDao.kt
│   │   │   └── entities/
│   │   │       ├── WishEntity.kt
│   │   │       ├── UserProfileEntity.kt
│   │   │       └── MilestoneEntity.kt
│   │   ├── preferences/
│   │   │   └── PreferencesManager.kt
│   │   └── notifications/
│   │       ├── ReminderSystem.kt
│   │       ├── ReminderReceiver.kt
│   │       └── ReminderActionReceiver.kt
│   ├── remote/                     # Remote data sources (future)
│   │   ├── api/
│   │   └── dto/
│   ├── repository/                 # Repository implementations
│   │   ├── WishRepositoryImpl.kt
│   │   ├── UserProfileRepositoryImpl.kt
│   │   └── MilestoneRepositoryImpl.kt
│   └── mappers/                    # Data mappers
│       ├── WishMapper.kt
│       ├── UserProfileMapper.kt
│       └── MilestoneMapper.kt
│
├── domain/                         # Domain Layer (Business Logic)
│   ├── model/                      # Domain models
│   │   ├── Wish.kt
│   │   ├── UserProfile.kt
│   │   ├── Milestone.kt
│   │   ├── Achievement.kt
│   │   ├── Priority.kt
│   │   └── NotificationModels.kt
│   ├── repository/                 # Repository interfaces
│   │   ├── WishRepository.kt
│   │   ├── UserProfileRepository.kt
│   │   └── MilestoneRepository.kt
│   ├── usecase/                    # Use cases (business logic)
│   │   ├── wish/
│   │   │   ├── GetWishesUseCase.kt
│   │   │   ├── AddWishUseCase.kt
│   │   │   ├── UpdateWishUseCase.kt
│   │   │   ├── DeleteWishUseCase.kt
│   │   │   └── SearchWishesUseCase.kt
│   │   ├── user/
│   │   │   ├── GetUserProfileUseCase.kt
│   │   │   ├── UpdateUserProfileUseCase.kt
│   │   │   └── GetUserStatsUseCase.kt
│   │   └── milestone/
│   │       ├── GetMilestonesUseCase.kt
│   │       ├── AddMilestoneUseCase.kt
│   │       └── CompleteMilestoneUseCase.kt
│   └── util/                       # Domain utilities
│       ├── DateUtils.kt
│       ├── ValidationUtils.kt
│       └── Constants.kt
│
├── presentation/                   # Presentation Layer (UI)
│   ├── ui/
│   │   ├── screens/                # Screen Composables
│   │   │   ├── home/
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   ├── HomeViewModel.kt
│   │   │   │   └── components/
│   │   │   │       ├── WelcomeCard.kt
│   │   │   │       ├── StatsCards.kt
│   │   │   │       └── RecentWishesList.kt
│   │   │   ├── wishlist/
│   │   │   │   ├── WishListScreen.kt
│   │   │   │   ├── WishListViewModel.kt
│   │   │   │   └── components/
│   │   │   │       ├── WishItem.kt
│   │   │   │       ├── EmptyWishList.kt
│   │   │   │       └── WishFilters.kt
│   │   │   ├── addedit/
│   │   │   │   ├── AddEditScreen.kt
│   │   │   │   ├── AddEditViewModel.kt
│   │   │   │   └── components/
│   │   │   │       ├── WishForm.kt
│   │   │   │       └── ImagePicker.kt
│   │   │   ├── search/
│   │   │   │   ├── SearchScreen.kt
│   │   │   │   ├── SearchViewModel.kt
│   │   │   │   └── components/
│   │   │   │       ├── SearchBar.kt
│   │   │   │       └── SearchFilters.kt
│   │   │   ├── dashboard/
│   │   │   │   ├── DashboardScreen.kt
│   │   │   │   ├── DashboardViewModel.kt
│   │   │   │   └── components/
│   │   │   │       ├── AnalyticsCards.kt
│   │   │   │       └── GoalProgress.kt
│   │   │   ├── profile/
│   │   │   │   ├── ProfileScreen.kt
│   │   │   │   ├── ProfileViewModel.kt
│   │   │   │   └── components/
│   │   │   │       ├── ProfileHeader.kt
│   │   │   │       └── AchievementsList.kt
│   │   │   ├── settings/
│   │   │   │   ├── SettingsScreen.kt
│   │   │   │   ├── SettingsViewModel.kt
│   │   │   │   └── components/
│   │   │   │       ├── ThemeSettings.kt
│   │   │   │       └── DataManagement.kt
│   │   │   ├── calendar/
│   │   │   │   ├── CalendarScreen.kt
│   │   │   │   ├── CalendarViewModel.kt
│   │   │   │   └── components/
│   │   │   │       └── CalendarView.kt
│   │   │   ├── notifications/
│   │   │   │   ├── NotificationsScreen.kt
│   │   │   │   ├── NotificationsViewModel.kt
│   │   │   │   └── components/
│   │   │   │       └── NotificationItem.kt
│   │   │   └── onboarding/
│   │   │       ├── OnboardingScreen.kt
│   │   │       ├── OnboardingViewModel.kt
│   │   │       └── components/
│   │   │           └── OnboardingPages.kt
│   │   ├── components/             # Shared UI Components
│   │   │   ├── common/
│   │   │   │   ├── LoadingComponents.kt
│   │   │   │   ├── ErrorComponents.kt
│   │   │   │   ├── EmptyStates.kt
│   │   │   │   └── CommonButtons.kt
│   │   │   ├── animations/
│   │   │   │   ├── EnhancedAnimations.kt
│   │   │   │   └── SpringAnimations.kt
│   │   │   ├── forms/
│   │   │   │   ├── FormValidation.kt
│   │   │   │   └── InputFields.kt
│   │   │   └── navigation/
│   │   │       ├── BottomNavBar.kt
│   │   │       └── TopAppBar.kt
│   │   ├── theme/                  # Theme and styling
│   │   │   ├── Theme.kt
│   │   │   ├── Color.kt
│   │   │   ├── Typography.kt
│   │   │   ├── Shapes.kt
│   │   │   └── Spacing.kt
│   │   └── state/                  # UI State management
│   │       ├── UiState.kt
│   │       └── ViewModelState.kt
│   ├── navigation/                 # Navigation logic
│   │   ├── NavigationGraph.kt
│   │   ├── Screen.kt
│   │   └── NavigationExt.kt
│   └── util/                       # Presentation utilities
│       ├── UiUtils.kt
│       ├── PermissionUtils.kt
│       └── ImageUtils.kt
│
├── di/                             # Dependency Injection
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   ├── UseCaseModule.kt
│   └── ViewModelModule.kt
│
├── util/                           # App-wide utilities
│   ├── CrashReporter.kt
│   ├── Logger.kt
│   └── Extensions.kt
│
└── MyWishListApplication.kt        # Application class
```

## Migration Steps

### Phase 1: Create New Structure
1. Create the new directory structure
2. Create empty files in appropriate locations

### Phase 2: Move Data Layer
1. Move `Data/` folder contents to `data/local/database/entities/`
2. Move DAOs to `data/local/database/dao/`
3. Move repositories to `data/repository/`

### Phase 3: Create Domain Layer
1. Move domain models to `domain/model/`
2. Create repository interfaces in `domain/repository/`
3. Create use cases in `domain/usecase/`

### Phase 4: Restructure Presentation Layer
1. Move screen files to `presentation/ui/screens/[screen_name]/`
2. Move ViewModels to respective screen folders
3. Move shared components to `presentation/ui/components/`
4. Move theme files to `presentation/ui/theme/`

### Phase 5: Create Navigation Module
1. Move navigation files to `presentation/navigation/`
2. Clean up navigation logic

### Phase 6: Add Dependency Injection
1. Create DI modules in `di/` folder
2. Set up proper dependency injection

## Benefits of New Structure

1. **Separation of Concerns**: Clear boundaries between layers
2. **Testability**: Easy to test individual components
3. **Maintainability**: Easy to find and modify code
4. **Scalability**: Structure supports growth
5. **Team Collaboration**: Clear ownership of different parts
6. **SOLID Principles**: Follows dependency inversion and single responsibility

## File Naming Conventions

- **Screens**: `[ScreenName]Screen.kt`
- **ViewModels**: `[ScreenName]ViewModel.kt`
- **Components**: `[ComponentName].kt`
- **Use Cases**: `[Action][Entity]UseCase.kt`
- **Repositories**: `[Entity]Repository.kt` (interface), `[Entity]RepositoryImpl.kt` (implementation)
- **DAOs**: `[Entity]Dao.kt`
- **Entities**: `[Entity]Entity.kt` (for Room), `[Entity].kt` (for domain models)

## Implementation Priority

1. **High Priority**: Create UI state management, move ViewModels
2. **Medium Priority**: Reorganize screens and components
3. **Low Priority**: Create use cases and clean architecture layers

This structure will make the codebase more maintainable, testable, and scalable while following Android development best practices.