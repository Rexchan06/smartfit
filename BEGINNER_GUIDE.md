# SmartFit Beginner's Guide
## For Teammates New to Kotlin Architecture

Hi! This guide is designed for you if you only know how to write basic Composable functions. We'll start from what you know and gradually build up to understanding the entire SmartFit architecture.

---

## 🎯 What You Already Know

You can write code like this in `MainActivity.kt`:

```kotlin
@Composable
fun MyScreen() {
    Text("Hello World")
    Button(onClick = { /* do something */ }) {
        Text("Click me")
    }
}
```

**Great!** That's the UI layer. But our app does way more than just display text. Let's understand the bigger picture.

---

## 🤔 The Problem: Why Is Our Project Complex?

Imagine you want to build a real fitness app. You need to:
1. **Display** activities on screen (what you know! ✅)
2. **Save** activities to a database (persist data)
3. **Load** activities from the database (retrieve data)
4. **Fetch** workout suggestions from the internet (API calls)
5. **Calculate** statistics (calories, duration)
6. **Survive** screen rotation without losing data
7. **Test** everything to ensure it works

If we put all this code in `MainActivity.kt`, it would be:
- 🔴 **Impossible to test** (can't test without running the whole app)
- 🔴 **Impossible to maintain** (one huge file with 5000+ lines)
- 🔴 **Impossible to collaborate** (everyone editing the same file = merge conflicts)

**Solution:** We split the code into **layers** (like floors in a building).

---

## 🏢 The Big Picture: Think of Layers Like a Restaurant

| Layer | Restaurant Analogy | SmartFit Example |
|-------|-------------------|------------------|
| **UI Layer** | The dining room where customers sit | Your Composable screens (HomeScreen, AddActivityScreen) |
| **ViewModel Layer** | The waiters who take orders and serve food | HomeViewModel, AddActivityViewModel |
| **Repository Layer** | The kitchen manager who coordinates everything | ActivityRepository, WorkoutRepository |
| **Data Layer** | The kitchen (cooking) and storage room | Room Database (local) + Retrofit API (internet) |

**Key Idea:** Each layer only talks to the layer directly below it. The customer (UI) doesn't go into the kitchen (database)!

---

## 📦 Understanding the Layers (Bottom-Up)

### Level 1: Data Layer (The Kitchen & Storage Room)

#### A. Room Database - Local Storage

**What is it?** Think of it as an Excel spreadsheet inside your phone that saves data permanently.

**Files:**
1. **ActivityEntity.kt** - Defines the spreadsheet structure (columns)
2. **ActivityDao.kt** - Defines what operations you can do (add row, delete row, read all rows)
3. **SmartFitDatabase.kt** - The actual database file

**Example:**

```kotlin
// ActivityEntity.kt - This is like defining columns in Excel
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Auto-incrementing ID (1, 2, 3...)
    val type: String,              // "Running", "Cycling", etc.
    val durationMinutes: Int,      // 30, 45, 60...
    val caloriesBurned: Int,       // 300, 450, etc.
    val timestamp: Long            // When it happened
)
```

```kotlin
// ActivityDao.kt - This defines operations on the spreadsheet
@Dao
interface ActivityDao {
    // Read all rows, sorted by newest first
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    // Add a new row
    @Insert
    suspend fun insert(activity: ActivityEntity): Long

    // Delete a row
    @Delete
    suspend fun delete(activity: ActivityEntity)
}
```

**Important Concepts:**

- **`Flow<List<ActivityEntity>>`** = A stream that automatically sends new data when the database changes
  - It's like subscribing to a YouTube channel - you get notified when new videos (data) arrive!

- **`suspend fun`** = Function that can pause and resume (runs in background without freezing UI)
  - Think of it like ordering food: you don't stand at the counter waiting, you sit down and get notified when it's ready

#### B. Retrofit API - Internet Data

**What is it?** Connects to wger.de to fetch workout suggestions from the internet.

```kotlin
interface FitnessApiService {
    @GET("exercise/")
    suspend fun getExercises(): ExerciseListResponse
}
```

Don't worry too much about this yet - just know it fetches data from the internet.

---

### Level 2: Repository Layer (The Kitchen Manager)

**What is it?** The middleman between ViewModels (waiters) and data sources (kitchen/storage).

**Why do we need it?**
- ViewModel shouldn't know if data comes from database, internet, or cache
- Makes testing easier (we can fake the repository)
- Single place to handle errors

**Example: ActivityRepository.kt**

```kotlin
class ActivityRepository(private val activityDao: ActivityDao) {

    // Get all activities (converts from Entity to Domain model)
    fun getAllActivities(): Flow<List<Activity>> {
        return activityDao.getAllActivities()
            .map { entities ->
                entities.map { it.toDomain() }  // Convert Entity → Activity
            }
    }

    // Add a new activity
    suspend fun insertActivity(activity: Activity): Long {
        val entity = ActivityEntity.fromDomain(activity)  // Convert Activity → Entity
        return activityDao.insert(entity)
    }
}
```

**Key Question:** Why convert between `ActivityEntity` and `Activity`?
- **ActivityEntity** = Database-specific (has annotations like `@Entity`, `@PrimaryKey`)
- **Activity** = Clean, framework-independent (no database stuff)
- This separation means we can change the database without changing the UI!

---

### Level 3: Domain Layer (Business Models)

**What is it?** Clean, simple data classes that represent our app's concepts.

**Example: Activity.kt**

```kotlin
data class Activity(
    val id: Int = 0,
    val type: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val timestamp: Long,
    val notes: String? = null
) {
    // Computed property - business logic
    val caloriesPerMinute: Double
        get() = if (durationMinutes > 0) {
            caloriesBurned.toDouble() / durationMinutes
        } else 0.0
}
```

**Why have this?**
- The UI works with `Activity` (clean and simple)
- The database works with `ActivityEntity` (has database annotations)
- They're kept separate so changes to one don't break the other

---

### Level 4: ViewModel Layer (The Waiter)

**What is it?** Holds the UI state and survives configuration changes (screen rotation).

**Why do we need it?**
- If you put state in a Composable, it gets lost when the screen rotates
- ViewModels survive rotation and keep the data

**Example: HomeViewModel.kt**

```kotlin
class HomeViewModel(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    // STATE: Recent activities (automatically updates when database changes!)
    val recentActivities: StateFlow<List<Activity>> =
        activityRepository.getAllActivities()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // STATE: Total calories
    private val _totalCalories = MutableStateFlow(0)
    val totalCalories: StateFlow<Int> = _totalCalories.asStateFlow()

    // ACTION: Add a sample activity
    fun addSampleActivity() {
        viewModelScope.launch {  // Run in background
            val activity = Activity(
                type = "Running",
                durationMinutes = 30,
                caloriesBurned = 300,
                timestamp = System.currentTimeMillis()
            )
            activityRepository.insertActivity(activity)
            // recentActivities automatically updates! (magic of Flow!)
        }
    }
}
```

**Important Concepts:**

- **StateFlow** = A special kind of Flow that:
  - Always has a current value
  - Survives screen rotation
  - Notifies the UI when the value changes

- **viewModelScope.launch { }** = Run code in the background without freezing the UI

---

### Level 5: UI Layer (The Dining Room - What You Know!)

**Example: HomeScreen.kt**

```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    // OBSERVE state from ViewModel
    val activities by viewModel.recentActivities.collectAsState()
    val totalCalories by viewModel.totalCalories.collectAsState()

    // DISPLAY data (what you already know!)
    Column {
        Text("Total Calories: $totalCalories", style = MaterialTheme.typography.headlineMedium)

        // Show list of activities
        LazyColumn {
            items(activities) { activity ->
                ActivityCard(activity)  // Reusable component
            }
        }

        // USER ACTION - call ViewModel method
        FloatingActionButton(onClick = { viewModel.addSampleActivity() }) {
            Icon(Icons.Default.Add, "Add activity")
        }
    }
}
```

**What's New Here?**

1. **`by viewModel.recentActivities.collectAsState()`**
   - "Subscribes" to the StateFlow
   - When StateFlow changes, the UI automatically recomposes (updates)
   - It's like following a news feed - you see new posts automatically!

2. **Automatic Updates**
   - You add an activity → Database changes → Flow emits → StateFlow updates → UI recomposes
   - **You don't need to manually update the UI!**

---

## 🔄 Complete Data Flow Example

Let's trace what happens when a user adds an activity:

```
USER CLICKS "Save Activity" BUTTON
          ↓
1. UI Layer (AddActivityScreen.kt)
   onClick = { viewModel.saveActivity(activity) }
          ↓
2. ViewModel Layer (AddActivityViewModel.kt)
   fun saveActivity(activity: Activity) {
       viewModelScope.launch {
           repository.insertActivity(activity)
       }
   }
          ↓
3. Repository Layer (ActivityRepository.kt)
   suspend fun insertActivity(activity: Activity) {
       val entity = ActivityEntity.fromDomain(activity)
       activityDao.insert(entity)
   }
          ↓
4. Data Layer (ActivityDao.kt)
   @Insert suspend fun insert(activity: ActivityEntity)
          ↓
5. Room Database
   Inserts row into SQLite database
   Detects change in "activities" table
          ↓
6. Flow Emission (ActivityDao.kt)
   getAllActivities(): Flow<List<ActivityEntity>>
   Emits new list with the added activity
          ↓
7. Repository Transforms
   .map { entities -> entities.map { it.toDomain() } }
   Converts List<ActivityEntity> → List<Activity>
          ↓
8. ViewModel StateFlow Updates
   recentActivities.value = new list
          ↓
9. UI Recomposes (HomeScreen.kt)
   LazyColumn automatically shows the new activity!
   ✅ User sees their activity appear in the list
```

**The Magic:** Steps 6-9 happen **automatically**! You don't write code to update the UI - Flow does it for you!

---

## 🔌 Dependency Injection (AppContainer)

**The Problem:** ViewModels need repositories. Repositories need DAOs. How do we create and pass them around?

**Old Way (Bad):**
```kotlin
val database = Room.databaseBuilder(...)
val dao = database.activityDao()
val repository = ActivityRepository(dao)
val viewModel = HomeViewModel(repository)
```
This is messy and hard to test.

**New Way (Good) - AppContainer:**

```kotlin
// AppContainer.kt - Creates everything once
class AppContainer(private val context: Context) {
    // Create database (only once)
    val database: SmartFitDatabase by lazy {
        SmartFitDatabase.getDatabase(context)
    }

    // Create repository (only once)
    val activityRepository: ActivityRepository by lazy {
        ActivityRepository(activityDao = database.activityDao())
    }
}

// SmartFitApplication.kt - App entry point
class SmartFitApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(applicationContext)
    }
}

// MainActivity.kt - Use it
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val appContainer = (application as SmartFitApplication).appContainer

        setContent {
            val viewModel = HomeViewModel(appContainer.activityRepository)
            HomeScreen(viewModel)
        }
    }
}
```

**Why?**
- Everything is created once and reused
- Easy to test (replace AppContainer with a fake one)
- Clear dependency graph

---

## 🧪 Testing Explained

### Why Do We Test?

Imagine you change the calorie calculation formula. Without tests:
- 🔴 You'd have to manually check every screen
- 🔴 You might miss bugs
- 🔴 You'd have to run the app on a phone/emulator (slow)

With tests:
- ✅ Run 100 tests in 5 seconds
- ✅ Automatically find bugs
- ✅ Confidence to refactor code

### Two Types of Tests

#### 1. Unit Tests (Fast - Run on Computer)

**What:** Test individual functions in isolation
**Where:** `app/src/test/java/`

**Example: CalorieCalculatorTest.kt**

```kotlin
class CalorieCalculatorTest {
    @Test
    fun `running for 30 minutes burns correct calories`() {
        // GIVEN - Setup
        val activityType = "Running"
        val durationMinutes = 30
        val weightKg = 70.0

        // WHEN - Execute
        val result = CalorieCalculator.calculateCalories(
            activityType = activityType,
            durationMinutes = durationMinutes,
            weightKg = weightKg
        )

        // THEN - Verify
        assertEquals(350, result)  // Expected: 350 calories
    }

    @Test
    fun `zero duration returns zero calories`() {
        val result = CalorieCalculator.calculateCalories(
            activityType = "Running",
            durationMinutes = 0,
            weightKg = 70.0
        )
        assertEquals(0, result)
    }
}
```

**Run:** Right-click on test → Run Test

#### 2. UI Tests (Slow - Run on Emulator)

**What:** Test user interactions (click button, verify text appears)
**Where:** `app/src/androidTest/java/`

**Example: AddActivityFlowTest.kt**

```kotlin
@RunWith(AndroidJUnit4::class)
class AddActivityFlowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun userCanAddActivity() {
        composeTestRule.setContent {
            SmartFitApp()  // Launch full app
        }

        // WHEN - User clicks "Add Activity" button
        composeTestRule
            .onNodeWithContentDescription("Add new activity")
            .performClick()

        // THEN - Should navigate to Add Activity screen
        composeTestRule
            .onNodeWithText("Add Activity")
            .assertExists()

        // WHEN - User fills form and saves
        composeTestRule
            .onNodeWithTag("type_field")
            .performTextInput("Running")

        composeTestRule
            .onNodeWithTag("duration_field")
            .performTextInput("30")

        composeTestRule
            .onNodeWithText("Save")
            .performClick()

        // THEN - Activity should appear in list
        composeTestRule
            .onNodeWithText("Running")
            .assertExists()

        composeTestRule
            .onNodeWithText("30 min")
            .assertExists()
    }
}
```

**Run:** Right-click on test → Run Test (launches emulator)

---

## 📝 Writing Your Report

Here's a structure you can use:

### 1. Introduction
"SmartFit is an Android fitness tracking app built with Kotlin and Jetpack Compose. It demonstrates modern Android architecture patterns including MVVM, Repository pattern, and Clean Architecture."

### 2. Architecture Overview
- Explain the layer diagram (UI → ViewModel → Repository → Data)
- Explain why we separate concerns
- Include a screenshot of the project structure

### 3. Technology Stack
- **UI:** Jetpack Compose (declarative UI)
- **Database:** Room (local persistence with SQLite)
- **API:** Retrofit (network calls to Wger API)
- **Architecture:** MVVM + Repository pattern
- **Dependency Injection:** Manual DI with AppContainer
- **Asynchronous:** Kotlin Coroutines + Flow

### 4. Key Features Implemented
- Activity logging (add, view, delete)
- Statistics calculation (total calories, duration)
- Workout suggestions from API
- Light/Dark theme support
- Data persistence with Room database

### 5. Testing Strategy

**Unit Tests (4 tests):**
1. `CalorieCalculatorTest` - Verify calorie calculation formulas
2. `DateFormatterTest` - Verify date formatting logic
3. `ActivityEntityTest` - Verify data transformations
4. `PreferencesManagerTest` - Verify settings storage

**UI Tests (4 tests):**
1. `NavigationTest` - Verify navigation between screens
2. `AddActivityFlowTest` - Verify adding activity flow
3. `ActivityLogDisplayTest` - Verify list displays correctly
4. `ThemeToggleTest` - Verify dark mode switching

**Test Results:**
- All 8 tests passed ✅
- Code coverage: XX%
- Screenshots of test results

### 6. Challenges & Solutions
- **Challenge:** Understanding Flow and StateFlow
  **Solution:** Visualized it as a subscription model (like YouTube notifications)

- **Challenge:** Managing dependencies
  **Solution:** Used AppContainer for manual dependency injection

### 7. Conclusion
"This project demonstrates production-ready Android development practices including Clean Architecture, reactive programming with Flow, and comprehensive testing."

---

## 🎓 Key Concepts Summary

| Concept | Simple Explanation | Why It Matters |
|---------|-------------------|----------------|
| **Room Database** | Excel spreadsheet inside your phone | Saves data permanently |
| **Flow** | YouTube subscription - get notified of new data | UI updates automatically |
| **StateFlow** | Flow that survives screen rotation | Don't lose data when rotating |
| **suspend fun** | Function that runs in background | Don't freeze the UI |
| **Repository** | Kitchen manager - coordinates data sources | Easy to test and swap data sources |
| **ViewModel** | Waiter - holds state and survives rotation | Don't lose data on screen rotation |
| **Dependency Injection** | Factory that creates objects once | Reuse objects, easy to test |
| **Clean Architecture** | Layers that don't know about each other | Easy to change and maintain |

---

## 🚀 Quick Start Checklist

To understand the project:
- [ ] Read this guide (you're here!)
- [ ] Open `MainActivity.kt` - see how AppContainer is used
- [ ] Open `HomeViewModel.kt` - see how StateFlow works
- [ ] Open `ActivityRepository.kt` - see how it transforms data
- [ ] Open `ActivityDao.kt` - see how database queries work
- [ ] Run the app - click "Add Sample Activity" and watch UI update
- [ ] Rotate the screen - notice data survives!
- [ ] Read `ARCHITECTURE.md` for deeper dive

To write your report:
- [ ] Understand the layer diagram
- [ ] Trace the data flow (user clicks button → database → UI updates)
- [ ] Run all tests and take screenshots
- [ ] Explain why we use each technology
- [ ] Include code snippets with explanations

---

## 💡 Common Questions

**Q: Why not put everything in MainActivity?**
A: It would be 5000+ lines of unmaintainable, untestable code.

**Q: What's the difference between Flow and StateFlow?**
A: StateFlow always has a current value and is perfect for UI state. Flow is more generic.

**Q: Why do we need both ActivityEntity and Activity?**
A: ActivityEntity is database-specific (has @Entity annotations). Activity is clean and UI-friendly. Separation of concerns!

**Q: How does the UI automatically update?**
A: Flow detects database changes → emits new data → StateFlow updates → Compose recomposes the UI.

**Q: What is viewModelScope.launch?**
A: Runs code in the background without freezing the UI. Automatically cancels if ViewModel is destroyed.

**Q: Why manual DI instead of Hilt?**
A: Learning! Manual DI helps you understand dependency injection before using a framework.

---

## 🎯 What You Should Be Able to Explain

After reading this guide, you should be able to:

✅ Draw the layer diagram and explain each layer's responsibility
✅ Trace the data flow when adding an activity
✅ Explain why we use ViewModel (survives rotation)
✅ Explain why we use Repository (single source of truth)
✅ Explain why we use Room (type-safe database)
✅ Explain why we use Flow (reactive updates)
✅ Explain the difference between unit and UI tests
✅ Explain what each test file tests

---

## 📚 Next Steps

1. **Read the code:** Start from `MainActivity.kt` and trace the code flow
2. **Run the app:** See it in action
3. **Rotate the screen:** Notice data survives
4. **Read tests:** See how we verify correctness
5. **Read ARCHITECTURE.md:** Deeper technical explanations
6. **Write your report:** Use the structure provided above

---

**Remember:** You don't need to understand every detail of Kotlin or Android to explain this project. Focus on understanding:
- The WHY (why we have layers)
- The WHAT (what each layer does)
- The HOW (how data flows through the layers)

Good luck with your report! 🚀
