# SmartFit Architecture Documentation

## 📐 Architecture Overview

SmartFit follows Clean Architecture principles with MVVM (Model-View-ViewModel) pattern.

```
┌─────────────────────────────────────────────────────┐
│           UI LAYER (Jetpack Compose)                │
│  Screens, Composables, Navigation                  │
│  • HomeScreen, ActivityLogScreen, AddActivityScreen│
│  • Stateless, declarative UI                       │
└─────────────────┬───────────────────────────────────┘
                  │ observes StateFlow/Flow
┌─────────────────▼───────────────────────────────────┐
│              VIEWMODEL LAYER                        │
│  UI State Management, Business Logic               │
│  • Holds UI state (StateFlow)                      │
│  • Survives configuration changes                  │
│  • Calls repository methods                        │
└─────────────────┬───────────────────────────────────┘
                  │ calls repository methods
┌─────────────────▼───────────────────────────────────┐
│            REPOSITORY LAYER                         │
│  Single Source of Truth, Data Abstraction          │
│  • ActivityRepository (local database)             │
│  • WorkoutRepository (API)                         │
│  • Combines multiple data sources                  │
└─────────────┬───────────────────┬───────────────────┘
              │                   │
┌─────────────▼─────────┐  ┌──────▼──────────────────┐
│   LOCAL DATA SOURCE   │  │  REMOTE DATA SOURCE     │
│  Room Database        │  │  Retrofit API           │
│  • ActivityDao        │  │  • FitnessApiService    │
│  • Entity → Domain    │  │  • DTO → Domain         │
└───────────────────────┘  └─────────────────────────┘
```

---

## 🗂️ Package Structure

```
com.example.smartfit/
├── SmartFitApplication.kt      # App entry point, creates AppContainer
│
├── di/                          # DEPENDENCY INJECTION
│   └── AppContainer.kt          # Manual DI container (creates singletons)
│
├── data/                        # DATA LAYER
│   ├── local/                   # Local persistence
│   │   ├── entity/              # Room entities (database tables)
│   │   │   └── ActivityEntity.kt
│   │   ├── dao/                 # Data Access Objects (queries)
│   │   │   └── ActivityDao.kt
│   │   └── database/            # Database instance
│   │       └── SmartFitDatabase.kt
│   │
│   ├── remote/                  # Network layer
│   │   ├── api/                 # Retrofit service interfaces
│   │   │   └── FitnessApiService.kt
│   │   └── dto/                 # API response models
│   │       └── WorkoutDto.kt
│   │
│   └── repository/              # Repository pattern (single source of truth)
│       ├── ActivityRepository.kt
│       └── WorkoutRepository.kt
│
├── domain/                      # BUSINESS LOGIC LAYER
│   └── model/                   # Domain models (UI-friendly, no framework dependencies)
│       ├── Activity.kt
│       └── Workout.kt
│
├── ui/                          # UI LAYER (Jetpack Compose)
│   ├── theme/                   # Material Design 3 theme
│   │   ├── Color.kt             # Color palette
│   │   ├── Theme.kt             # Theme setup
│   │   └── Type.kt              # Typography
│   │
│   ├── screens/                 # Screen-level composables + ViewModels
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── activitylog/
│   │   │   ├── ActivityLogScreen.kt
│   │   │   └── ActivityLogViewModel.kt
│   │   ├── addactivity/
│   │   │   ├── AddActivityScreen.kt
│   │   │   └── AddActivityViewModel.kt
│   │   └── profile/
│   │       ├── ProfileScreen.kt
│   │       └── ProfileViewModel.kt
│   │
│   ├── components/              # Reusable UI components
│   │   ├── ActivityCard.kt
│   │   └── StatCard.kt
│   │
│   └── navigation/              # Navigation setup
│       └── SmartFitNavigation.kt
│
├── util/                        # UTILITIES
│   ├── Logger.kt                # Centralized logging
│   ├── CalorieCalculator.kt     # Business logic (testable)
│   └── DateFormatter.kt         # Date formatting
│
└── MainActivity.kt              # App entry point (Activity)
```

---

## 🔄 Data Flow

### Reading Data (Database → UI)

```kotlin
// 1. DAO emits Flow when database changes
@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>
}

// 2. Repository transforms to domain models
class ActivityRepository(private val dao: ActivityDao) {
    fun getAllActivities(): Flow<List<Activity>> {
        return dao.getAllActivities()
            .map { entities -> entities.map { it.toDomain() } }
    }
}

// 3. ViewModel collects as StateFlow
class ActivityViewModel(private val repository: ActivityRepository) : ViewModel() {
    val activities: StateFlow<List<Activity>> = repository
        .getAllActivities()
        .stateIn(viewModelScope, WhileSubscribed(5000), emptyList())
}

// 4. UI observes and recomposes
@Composable
fun ActivityListScreen(viewModel: ActivityViewModel) {
    val activities by viewModel.activities.collectAsState()

    LazyColumn {
        items(activities) { activity ->
            ActivityCard(activity)
        }
    }
}
```

**Key Points:**
- Flow makes data **reactive** - UI updates automatically when database changes
- No manual refresh needed
- StateFlow survives configuration changes
- viewModelScope manages coroutine lifecycle

### Writing Data (UI → Database)

```kotlin
// 1. UI triggers action
Button(onClick = { viewModel.addActivity(activity) }) {
    Text("Save")
}

// 2. ViewModel calls repository (in coroutine)
fun addActivity(activity: Activity) {
    viewModelScope.launch {
        repository.insertActivity(activity)
    }
}

// 3. Repository converts and inserts
suspend fun insertActivity(activity: Activity): Long {
    val entity = ActivityEntity.fromDomain(activity)
    return dao.insert(entity)
}

// 4. DAO inserts into database
@Insert
suspend fun insert(activity: ActivityEntity): Long

// 5. Room detects change, emits new Flow value
// 6. Steps 1-4 from "Reading Data" repeat automatically!
```

---

## 🧩 Key Architectural Components

### 1. Room Database (Local Persistence)

**What:** SQLite database with type-safe, compile-time verified queries

**Components:**
- **Entity:** Defines table structure (`@Entity`, `@PrimaryKey`)
- **DAO:** Defines queries (`@Query`, `@Insert`, `@Update`, `@Delete`)
- **Database:** Singleton instance that connects everything

**Why Use Room:**
- ✓ Compile-time SQL verification (catches errors before runtime)
- ✓ Type-safe queries
- ✓ Automatic object mapping
- ✓ Observable queries with Flow
- ✓ Migration support

**Example:**
```kotlin
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val timestamp: Long
)
```

### 2. Repository Pattern

**What:** Single source of truth that abstracts data sources

**Why Use Repository:**
- ✓ UI doesn't know if data is from database, API, or cache
- ✓ Easy to switch data sources
- ✓ Centralized business logic
- ✓ Testable (easy to mock)
- ✓ Offline-first architecture support

**Responsibilities:**
- Fetch data from DAO/API
- Transform entities/DTOs to domain models
- Handle errors
- Decide caching strategy
- Coordinate multiple data sources

### 3. ViewModel + StateFlow

**What:** UI state holder that survives configuration changes

**Why Use ViewModel:**
- ✓ Survives screen rotation
- ✓ Manages coroutines with viewModelScope
- ✓ Separates UI logic from UI presentation
- ✓ Single source of truth for UI state

**StateFlow vs Flow:**
- **StateFlow:** Hot flow, always has current value, perfect for UI state
- **Flow:** Cold flow, emits when collected, perfect for one-time operations

**Example:**
```kotlin
class HomeViewModel(private val repository: ActivityRepository) : ViewModel() {
    // Automatically updates when database changes
    val activities: StateFlow<List<Activity>> = repository
        .getAllActivities()
        .stateIn(viewModelScope, WhileSubscribed(5000), emptyList())

    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            repository.insertActivity(activity)
            // activities StateFlow updates automatically!
        }
    }
}
```

### 4. Manual Dependency Injection

**What:** AppContainer creates and manages dependencies

**Why Manual DI (not Hilt/Dagger):**
- ✓ Simple to understand - see exactly how objects are created
- ✓ No annotation processing complexity
- ✓ Full control over object lifecycle
- ✓ Educational - learn DI principles first
- ✓ Sufficient for small/medium apps

**Flow:**
```kotlin
// 1. Application creates AppContainer
class SmartFitApplication : Application() {
    lateinit var appContainer: AppContainer
    override fun onCreate() {
        appContainer = AppContainer(this)
    }
}

// 2. AppContainer creates singletons
class AppContainer(context: Context) {
    val database = SmartFitDatabase.getDatabase(context)
    val activityRepository = ActivityRepository(database.activityDao())
}

// 3. ViewModels get dependencies from AppContainer
val appContainer = (application as SmartFitApplication).appContainer
val viewModel = HomeViewModel(appContainer.activityRepository)
```

### 5. Jetpack Compose UI

**What:** Modern declarative UI framework

**Why Compose:**
- ✓ Less boilerplate than XML
- ✓ Reactive - UI updates automatically when state changes
- ✓ Kotlin-first, type-safe
- ✓ Easy animations and theming
- ✓ Composable functions are reusable

**Key Concepts:**
```kotlin
@Composable
fun ActivityCard(activity: Activity) {
    // Recomposes when activity changes
    Card {
        Text(activity.type)
        Text("${activity.caloriesBurned} cal")
    }
}
```

---

## 🌐 API Integration (Retrofit)

**API Used:** Wger Workout Manager API (wger.de/api/v2/)
- ✅ Free, no API key required
- ✅ 800+ exercises with images
- ✅ Nutrition data
- ✅ Well-documented REST API

**How Retrofit Works:**
```kotlin
// 1. Define API interface
interface FitnessApiService {
    @GET("exercise/")
    suspend fun getExercises(): ExerciseListResponse
}

// 2. Retrofit generates implementation
val retrofit = Retrofit.Builder()
    .baseUrl("https://wger.de/api/v2/")
    .addConverterFactory(KotlinSerializationConverterFactory)
    .build()

val api = retrofit.create(FitnessApiService::class.java)

// 3. Call from repository
suspend fun getWorkouts(): Result<List<Workout>> {
    return try {
        val response = api.getExercises()
        Result.Success(response.results.map { it.toDomain() })
    } catch (e: IOException) {
        Result.Error("No internet connection")
    }
}
```

---

## 🧪 Testing Strategy

### Unit Tests (Fast, runs on JVM)
**What to Test:**
- ✓ CalorieCalculator business logic
- ✓ Repository data transformations
- ✓ ViewModel state management
- ✓ DateFormatter utilities

**Example:**
```kotlin
@Test
fun `calculateCalories for 30min running returns 350cal`() {
    val result = CalorieCalculator.calculateCalories(
        activityType = "running",
        durationMinutes = 30,
        weightKg = 70.0
    )
    assertEquals(350, result)
}
```

### UI Tests (Runs on emulator)
**What to Test:**
- ✓ Navigation flows
- ✓ Button clicks save data
- ✓ Lists display data correctly
- ✓ Forms validation

**Example:**
```kotlin
@Test
fun clickingAddActivitySavesToDatabase() {
    composeTestRule.onNodeWithText("Add Activity").performClick()
    composeTestRule.onNodeWithTag("type_field").performTextInput("Running")
    composeTestRule.onNodeWithText("Save").performClick()

    // Verify activity appears in list
    composeTestRule.onNodeWithText("Running").assertExists()
}
```

---

## 📝 Best Practices Followed

1. **Single Responsibility:** Each class has one job
2. **Separation of Concerns:** UI, business logic, data are separate
3. **Dependency Inversion:** High-level code depends on abstractions
4. **Reactive Architecture:** UI automatically updates when data changes
5. **Offline First:** Local database is source of truth
6. **Testability:** Pure functions, dependency injection, mockable repositories
7. **Clean Code:** Extensive comments, meaningful names, consistent style

---

## 🚀 Getting Started

### Running the App
1. Open in Android Studio
2. Sync Gradle
3. Run on emulator or device (API 24+)

### Adding a New Feature
1. Define domain model in `domain/model/`
2. Create entity in `data/local/entity/`
3. Add DAO methods in `data/local/dao/`
4. Create repository in `data/repository/`
5. Create ViewModel in `ui/screens/[feature]/`
6. Create Composable screen
7. Add to navigation

### Common Tasks
- **Add sample data:** Call `viewModel.addSampleActivity()`
- **View logs:** Logcat filter `SmartFit`
- **Test database:** Device File Explorer → `/data/data/com.example.smartfit/databases/`
- **Change theme:** Toggle system dark mode

---

## 📚 Learning Resources

- **Jetpack Compose:** developer.android.com/jetpack/compose
- **Room Database:** developer.android.com/training/data-storage/room
- **Kotlin Coroutines:** kotlinlang.org/docs/coroutines-overview.html
- **Material Design 3:** m3.material.io
- **MVVM Pattern:** developer.android.com/topic/architecture
- **Wger API Docs:** wger.de/api/v2/

---

## 🎓 University Project Notes

**What makes this architecture suitable for academic evaluation:**
- ✅ Industry-standard patterns (MVVM, Repository)
- ✅ Clean Architecture principles
- ✅ Extensive documentation explaining WHY, not just HOW
- ✅ Testable design
- ✅ Modern Android development (Compose, Coroutines, Flow)
- ✅ Clear data flow that can be explained in presentation
- ✅ Scalable structure for team collaboration

**Demonstrating Understanding:**
When presenting, explain:
1. How data flows from database through layers to UI
2. Why we separate concerns (ViewModel, Repository, DAO)
3. How Flow makes UI reactive
4. Benefits of each architectural decision
5. How to test each layer independently

---

## 🤝 Team Collaboration

**Suggested Division:**
- **Person A:** Screens 1-2, Tests 1-2, Feature X
- **Person B:** Screens 3-4, Tests 3-4, Feature Y

**Git Workflow:**
- Main branch: stable code
- Feature branches: new features
- Pull requests: code review before merge
- Commit messages: Clear and descriptive

---

This architecture provides a solid foundation that you can build upon, understand deeply, and explain confidently in your presentation!
