# SmartFit - Fitness Tracking Android App

**University Project | Built with Kotlin & Jetpack Compose**

A comprehensive fitness tracking application demonstrating modern Android development with Clean Architecture, MVVM pattern, and best practices.

---

## 🎯 Project Overview

SmartFit is a production-ready skeleton for a fitness tracking app that demonstrates:
- ✅ **Jetpack Compose UI** with Material Design 3
- ✅ **Room Database** for local persistence
- ✅ **Retrofit API integration** with Wger API
- ✅ **MVVM architecture** with Repository pattern
- ✅ **Manual Dependency Injection** (AppContainer)
- ✅ **Reactive programming** with Flow/StateFlow
- ✅ **Comprehensive documentation** explaining every concept

---

## 🏗️ What Has Been Built

### ✅ Complete Data Layer
- **Room Database** (local persistence)
  - `ActivityEntity` - Table structure with detailed annotations explained
  - `ActivityDao` - Database operations (queries, inserts, updates, deletes)
  - `SmartFitDatabase` - Database instance with singleton pattern

- **Retrofit API Integration** (network layer)
  - `FitnessApiService` - API endpoints for Wger Workout API
  - `WorkoutDto` - Data Transfer Objects for API responses
  - Error handling with Result sealed class

- **Repositories** (single source of truth)
  - `ActivityRepository` - Manages local activity data
  - `WorkoutRepository` - Fetches workout suggestions from API
  - Transforms between entities/DTOs and domain models

### ✅ Domain Layer
- `Activity` - Domain model for fitness activities with computed properties
- `Workout` - Domain model for exercise suggestions
- Clean separation from database/network implementation details

### ✅ Dependency Injection
- `AppContainer` - Manual DI container with extensive documentation
- `SmartFitApplication` - Application class that creates AppContainer
- Demonstrates DI principles without Hilt/Dagger complexity

### ✅ UI Layer
- **Material Design 3 Theme**
  - Light and dark mode support
  - Dynamic color support (Android 12+)
  - Complete color palette for fitness app
  - `SmartFitTheme` composable with system bar styling

- **HomeViewModel** - Complete example showing:
  - StateFlow for UI state management
  - Flow collection from repository
  - Coroutines with viewModelScope
  - Data flow from Database → UI

- **MainActivity** - Jetpack Compose setup with:
  - Working placeholder screen
  - Demonstrates complete data flow
  - Add sample activities button
  - Statistics display

### ✅ Utilities
- `Logger` - Centralized logging for debugging
- `CalorieCalculator` - Business logic for fitness calculations (testable)
- `DateFormatter` - Consistent date formatting

### ✅ Documentation
- **ARCHITECTURE.md** - Complete architecture guide explaining:
  - Data flow diagrams
  - Layer responsibilities
  - Why we use each pattern
  - Code examples for every component

- **Extensive inline comments** in every file explaining:
  - What each component does
  - Why it exists
  - How to use it
  - Common patterns and best practices

### ✅ Build Configuration
- All dependencies configured with explanations
- Compose, Room, Retrofit, Coil, Navigation, DataStore
- KSP for annotation processing
- Testing libraries (JUnit, MockK, Turbine, Compose Test)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version)
- Android SDK API 24+ (Android 7.0+)
- Emulator or physical device

### Running the App

1. **Open Project in Android Studio**
   ```
   File → Open → Select SmartFit folder
   ```

2. **Sync Gradle**
   ```
   Wait for Gradle sync to complete
   If errors occur, File → Invalidate Caches → Restart
   ```

3. **Run the App**
   ```
   Click Run button or Shift + F10
   Select emulator or device
   Wait for build and installation
   ```

4. **Test the App**
   - App opens with placeholder home screen
   - Click "Add Sample Activity" to insert test data into database
   - Watch as the UI automatically updates (reactive Flow!)
   - Statistics update in real-time
   - Recent activities list populates

### Viewing Logs
```
Android Studio → Logcat
Filter by: SmartFit
You'll see:
- App initialization logs
- Database operations
- UI events
```

### Inspecting Database
```
Device File Explorer → data → data → com.example.smartfit → databases
Right-click smartfit_database → Save As
Open with DB Browser for SQLite
```

---

## 📝 What to Build Next

The skeleton is complete! Here's your implementation roadmap:

### Phase 1: Complete UI Screens (Priority)
1. **Create Navigation** (`ui/navigation/SmartFitNavigation.kt`)
   - Set up NavHost with routes
   - Define screen destinations
   - Pass AppContainer for ViewModel creation

2. **Build HomeScreen** (full-featured)
   - Stats cards (calories, distance, duration)
   - Recent activities list
   - Navigation to other screens
   - Workout suggestions from API

3. **Build ActivityLogScreen**
   - List all activities with filtering
   - Delete activities
   - Navigate to detail view
   - Date range selection

4. **Build AddActivityScreen**
   - Form to input activity data
   - Activity type dropdown
   - Duration and calorie inputs
   - Save to database via repository

5. **Build ProfileScreen**
   - User settings (weight, height, age)
   - Theme toggle (light/dark/system)
   - Statistics overview
   - Clear data option

### Phase 2: Reusable Components
- `ActivityCard` - Display activity info with intensity badge
- `StatCard` - Show statistics with icons
- `WorkoutCard` - Display exercise suggestions with images (Coil)
- `ChartCard` - Progress visualization

### Phase 3: Features & Polish
- DataStore for user preferences
- Animations (FAB transitions, list item animations)
- Accessibility (content descriptions, semantic roles)
- Search functionality
- Filtering and sorting
- Date-based statistics (week, month, year)

### Phase 4: Testing
- **Unit Tests** (4 required):
  - `CalorieCalculatorTest` - Test business logic
  - `ActivityRepositoryTest` - Test data operations
  - `StepGoalProgressTest` - Test calculations
  - `DateFormatterTest` - Test formatting

- **UI Tests** (4 required):
  - `NavigationTest` - Test screen navigation
  - `AddActivityTest` - Test activity creation flow
  - `ActivityLogDisplayTest` - Test data display
  - `ThemeToggleTest` - Test dark mode switching

---

## 💡 Understanding the Architecture

### Data Flow Example

**How adding an activity works:**

```
1. UI (AddActivityScreen)
   ↓ User clicks "Save"

2. ViewModel
   ↓ addActivity(activity)
   ↓ viewModelScope.launch { }

3. Repository
   ↓ insertActivity(activity)
   ↓ converts Activity → ActivityEntity

4. DAO
   ↓ @Insert suspend fun insert(entity)

5. Room Database
   ↓ Inserts into SQLite
   ↓ Detects data change

6. DAO emits new Flow value
   ↓ Flow<List<ActivityEntity>>

7. Repository transforms
   ↓ map { it.toDomain() }
   ↓ Flow<List<Activity>>

8. ViewModel StateFlow updates
   ↓ recentActivities.value = new list

9. UI automatically recomposes
   ✓ Activity appears in list
   ✓ Statistics update
   ✓ No manual refresh needed!
```

### Why This Architecture?

**Room Database:**
- ✓ Type-safe queries verified at compile time
- ✓ Automatic object mapping
- ✓ Observable queries with Flow (UI updates automatically)
- ✓ Migration support for schema changes

**Repository Pattern:**
- ✓ UI doesn't know if data is from DB, API, or cache
- ✓ Single source of truth
- ✓ Easy to test (mock repository)
- ✓ Offline-first architecture

**ViewModel + StateFlow:**
- ✓ Survives configuration changes (screen rotation)
- ✓ Manages coroutines automatically
- ✓ Reactive UI (updates when data changes)
- ✓ Separation of concerns

**Manual DI:**
- ✓ Simple and understandable
- ✓ No annotation processing complexity
- ✓ Full control over object lifecycle
- ✓ Learn DI principles before using Hilt

---

## 🧪 Testing Your Code

### Running Unit Tests
```bash
Right-click on test/ folder → Run Tests
Or: ./gradlew test
```

### Running UI Tests
```bash
Right-click on androidTest/ folder → Run Tests
Or: ./gradlew connectedAndroidTest
```

### Writing a Unit Test Example
```kotlin
class CalorieCalculatorTest {
    @Test
    fun `calculateCalories for running returns correct value`() {
        val result = CalorieCalculator.calculateCalories(
            activityType = "running",
            durationMinutes = 30,
            weightKg = 70.0
        )
        assertEquals(350, result)
    }
}
```

### Writing a UI Test Example
```kotlin
@Test
fun addActivitySavesToDatabase() {
    composeTestRule.onNodeWithText("Add Activity").performClick()
    composeTestRule.onNodeWithTag("type_input").performTextInput("Running")
    composeTestRule.onNodeWithText("Save").performClick()
    composeTestRule.onNodeWithText("Running").assertExists()
}
```

---

## 📚 Learning Resources

### Jetpack Compose
- [Official Documentation](https://developer.android.com/jetpack/compose)
- [Compose Pathway](https://developer.android.com/courses/pathways/compose)
- [Material Design 3](https://m3.material.io/)

### Room Database
- [Room Guide](https://developer.android.com/training/data-storage/room)
- [7 Pro-tips for Room](https://medium.com/androiddevelopers/7-pro-tips-for-room-fbadea4bfbd1)

### Architecture
- [Guide to App Architecture](https://developer.android.com/topic/architecture)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Repository Pattern](https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern)

### Kotlin & Coroutines
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Flow Guide](https://kotlinlang.org/docs/flow.html)
- [StateFlow and SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)

### API Used
- [Wger API Documentation](https://wger.de/api/v2/)
- Free workout and nutrition database
- No API key required

---

## 🤝 Team Collaboration

### Git Workflow
```bash
# Clone repository
git clone <your-repo-url>
cd SmartFit

# Create feature branch
git checkout -b feature/add-activity-screen

# Make changes, commit frequently
git add .
git commit -m "feat: add activity input form"

# Push to remote
git push origin feature/add-activity-screen

# Create pull request on GitHub for review
```

### Division of Work
**Person A:**
- HomeScreen + ViewModel
- ActivityLogScreen + ViewModel
- Unit tests: CalorieCalculator, ActivityRepository
- UI tests: Navigation, AddActivity

**Person B:**
- AddActivityScreen + ViewModel
- ProfileScreen + ViewModel
- Unit tests: StepGoalProgress, DateFormatter
- UI tests: ActivityLogDisplay, ThemeToggle

---

## 🎓 For Your Presentation

### Key Points to Explain

1. **Architecture Overview**
   - Show the layer diagram (UI → ViewModel → Repository → Data Sources)
   - Explain why we separate concerns
   - Demonstrate data flow with a live example

2. **Room Database**
   - Show Entity, DAO, Database classes
   - Explain compile-time SQL verification
   - Demonstrate reactive queries with Flow

3. **Repository Pattern**
   - Explain "single source of truth"
   - Show how it abstracts data sources
   - Demonstrate offline-first capability

4. **MVVM with StateFlow**
   - Show how ViewModel survives rotation
   - Explain reactive UI updates
   - Demonstrate with screen rotation

5. **Dependency Injection**
   - Show AppContainer and how objects are created
   - Explain benefits vs manual instantiation
   - Discuss when to use Hilt vs manual DI

6. **API Integration**
   - Show Retrofit interface
   - Explain how DTOs transform to domain models
   - Demonstrate error handling

### Demo Flow
1. Open app (show home screen with stats)
2. Add an activity (show form, save button)
3. Navigate back (show activity in list, stats updated)
4. Rotate screen (show data survives)
5. Toggle dark mode (show theme switching)
6. Open Logcat (show logging in action)
7. Inspect database (show SQLite data)

---

## 📁 Project Structure Summary

```
SmartFit/
├── ARCHITECTURE.md          ← Read this first!
├── README.md                ← You are here
├── build.gradle.kts         ← All dependencies configured
├── app/src/main/java/com/example/smartfit/
│   ├── MainActivity.kt      ← Working Compose UI
│   ├── SmartFitApplication.kt
│   ├── di/AppContainer.kt   ← Dependency injection
│   ├── data/                ← Database, API, Repositories
│   ├── domain/              ← Business models
│   ├── ui/                  ← Compose UI, ViewModels, Theme
│   └── util/                ← Utilities
└── app/src/test/            ← Unit tests (add yours here)
    └── androidTest/         ← UI tests (add yours here)
```

---

## 🎯 Project Status

### ✅ Completed (Ready to Use)
- ✅ Complete data layer (Room + Retrofit)
- ✅ Repository pattern
- ✅ Dependency injection
- ✅ Domain models
- ✅ Material3 theme with dark mode
- ✅ HomeViewModel example
- ✅ Working MainActivity with Compose
- ✅ Utilities (Logger, CalorieCalculator, DateFormatter)
- ✅ Comprehensive documentation

### 🚧 To Be Implemented (Your Work)
- 🚧 Navigation setup
- 🚧 Individual screen UIs
- 🚧 Reusable UI components
- 🚧 DataStore preferences
- 🚧 Unit tests (4 required)
- 🚧 UI tests (4 required)
- 🚧 Animations
- 🚧 Advanced features

---

## 💬 Questions & Support

### Common Issues

**Build errors after sync?**
- File → Invalidate Caches → Restart
- Check internet connection for dependencies
- Update Android Studio to latest version

**App crashes on launch?**
- Check Logcat for stack trace
- Verify AndroidManifest has correct Application name
- Ensure emulator/device meets minSdk 24

**Database not saving data?**
- Check Logcat for SQLite errors
- Verify entity/DAO annotations
- Inspect database file with Device File Explorer

### Getting Help
- Read ARCHITECTURE.md for detailed explanations
- Check inline comments in source files
- Review official Android documentation
- Ask your partner or instructor

---

## 🏆 Success Criteria

Your app demonstrates understanding when it:
- ✅ Follows Clean Architecture principles
- ✅ Uses MVVM pattern correctly
- ✅ Implements reactive UI with Flow/StateFlow
- ✅ Properly separates concerns across layers
- ✅ Has comprehensive tests
- ✅ Handles errors gracefully
- ✅ Follows Material Design guidelines
- ✅ Works on different screen sizes and orientations
- ✅ Demonstrates accessibility features
- ✅ Is well-documented and maintainable

---

## 📄 License

This is an educational project created for university coursework.

---

**Built with ❤️ for Android Development Education**

Good luck with your project! Remember: Understanding the "why" is more important than the "how". This architecture teaches you production-ready Android development patterns that you'll use in your career.

For detailed explanations of every component, read **ARCHITECTURE.md**!
