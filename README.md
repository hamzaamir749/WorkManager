# Android WorkManager Demo App 🚀

An idiomatic Android application demonstrating background task scheduling using **Android Jetpack WorkManager**, **Dagger Hilt**, **Room Database**, **Retrofit**, and **Jetpack Compose** following **Clean Architecture** and **MVVM** principles.

---

## 📱 Features

- **One-Time Work Request & Chaining**: Trigger background network requests on-demand and chain notification workers sequentially.
- **Periodic Work Request**: Automatically schedule background sync every 15 minutes with network connectivity constraints.
- **Dependency Injection in Workers**: Utilize `@HiltWorker` and `@AssistedInject` to inject dependencies (`ApiService`, `QuoteDao`) into `CoroutineWorker` instances.
- **Custom WorkManager Initialization**: Disable default WorkManager initialization in `AndroidManifest.xml` and initialize manually with `HiltWorkerFactory`.
- **Local Persistence**: Offline-first quote caching using **Room Database**.
- **Remote API Fetching**: Fetch random quotes from [DummyJSON Quotes API](https://dummyjson.com/quotes/random) using **Retrofit**.
- **Modern Jetpack Compose UI**: Clean UI built with Material 3, displaying cached quotes, work type tags, formatted timestamps, and a manual refresh option.
- **System Notifications**: Display system notifications when new quotes are fetched in the background (handling Android 13+ `POST_NOTIFICATIONS` runtime permission).

---

## 🛠️ Architecture & Tech Stack

This project follows **Clean Architecture** with three core layers: **Presentation**, **Domain**, and **Data**.

```
    ┌────────────────────────────────────────────────────────┐
    │                    Presentation                        │
    │        (Jetpack Compose UI, MainViewModel, UiState)     │
    └───────────────────────────┬────────────────────────────┘
                                │
                                ▼
    ┌────────────────────────────────────────────────────────┐
    │                       Domain                           │
    │         (Use Cases, Quote Model, QuoteRepository)      │
    └───────────────────────────┬────────────────────────────┘
                                │
                                ▼
    ┌────────────────────────────────────────────────────────┐
    │                        Data                            │
    │  (QuoteRepoImpl, WorkManager, Room DB, Retrofit, Hilt) │
    └────────────────────────────────────────────────────────┘
```

### Libraries & Tools Used

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Background Processing**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) (`androidx.work:work-runtime-ktx`)
- **Dependency Injection**: [Dagger Hilt](https://dagger.dev/hilt/) & Hilt Work (`androidx.hilt:hilt-work`)
- **Database**: [Room](https://developer.android.com/training/data-storage/room) with KSP
- **Networking**: [Retrofit 2](https://square.github.io/retrofit/) + Gson Converter
- **Asynchronous / Streams**: Kotlin Coroutines & StateFlow
- **Annotation Processing**: KSP (Kotlin Symbol Processing)

---

## ⚙️ WorkManager Implementation Details

### 1. Disable Automatic WorkManager Initializer
In `AndroidManifest.xml`, default initialization via `androidx.startup.InitializationProvider` is disabled:

```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false">
    <meta-data
        android:name="androidx.work.WorkManagerInitializer"
        android:value="androidx.startup"
        tools:node="remove" />
</provider>
```

### 2. Manual Custom Initialization in Application
In `WorkManagerApplication.kt`, `WorkManager` is initialized with `HiltWorkerFactory`:

```kotlin
@HiltAndroidApp
class WorkManagerApplication : Application() {

    @Inject
    lateinit var hiltWorkerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(hiltWorkerFactory)
                .build()
        )
    }
}
```

### 3. Worker Injection with `@HiltWorker`
Each worker uses `@HiltWorker` and `@AssistedInject`:

```kotlin
@HiltWorker
class FetchWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val apiService: ApiService,
    private val quoteDao: QuoteDao
) : CoroutineWorker(context, workerParameters) { ... }
```

### 4. Work Requests & Chaining

#### One-Time Work Chaining (`FetchWorker` ➔ `NotificationWorker`)
```kotlin
val workRequest = OneTimeWorkRequestBuilder<FetchWorker>()
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()

val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
    .build()

workManager.beginWith(workRequest)
    .then(notificationWorkRequest)
    .enqueue()
```

#### Unique Periodic Work Request (`PeriodicWorker`)
```kotlin
val workRequest = PeriodicWorkRequest.Builder(PeriodicWorker::class.java, 15, TimeUnit.MINUTES)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()

workManager.enqueueUniquePeriodicWork(
    "UniqueWorkRequest",
    ExistingPeriodicWorkPolicy.UPDATE,
    workRequest
)
```

---

## 📂 Project Structure

```
com.coderpakistan.workmanager/
│
├── WorkManagerApplication.kt           # App entry point, Hilt & WorkManager setup, Notification Channel
├── MainActivity.kt                     # Jetpack Compose UI (MainScreen & TopAppBar)
│
├── common/
│   └── Utils.kt                        # Formatting helpers (e.g., date formatting)
│
├── data/
│   ├── di/
│   │   └── DataModule.kt               # Hilt Module (Retrofit, Room, WorkManager, Repository)
│   ├── local/
│   │   ├── QuoteDao.kt                 # Room DAO interface
│   │   └── QuoteDatabase.kt            # Room Database definition
│   ├── mappers/
│   │   └── Mappers.kt                  # DTO ➔ Domain mapping extension functions
│   ├── model/
│   │   └── QuoteDTO.kt                 # Data transfer object for API responses
│   ├── remote/
│   │   └── ApiService.kt               # Retrofit service interface
│   ├── repository/
│   │   └── QuoteRepoImpl.kt            # Repository implementation handling network & WorkManager
│   └── worker/
│       ├── FetchWorker.kt              # CoroutineWorker fetching remote quote to Room
│       ├── NotificationWorker.kt       # CoroutineWorker posting system notification
│       └── PeriodicWorker.kt           # CoroutineWorker running periodic background syncs
│
├── domain/
│   ├── model/
│   │   └── Quote.kt                    # Room Entity / Core Domain model
│   ├── repository/
│   │   └── QuoteRepository.kt          # Repository abstraction contract
│   └── use_cases/
│       ├── GetAllQuotesFromDbUseCase.kt # Observes cached quotes Flow
│       ├── GetQuotesUseCase.kt          # Triggers one-time work request
│       └── SetupPeriodicWorkRequestUseCase.kt # Configures 15-min periodic work
│
├── presentation/
│   └── MainViewModel.kt                # ViewModel managing state & use cases
│
└── ui/theme/                           # Compose Material 3 Theme setup
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer recommended
- **JDK**: Java 11 or higher
- **Android SDK**:
  - `compileSdk`: 37
  - `minSdk`: 24
  - `targetSdk`: 37

### Building & Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/coderpakistan/WorkManager.git
   cd WorkManager
   ```

2. **Open in Android Studio**:
   Open Android Studio and select **Open**, then navigate to the cloned project directory.

3. **Build the project**:
   Sync Gradle project files and execute:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on Emulator / Physical Device**:
   Select your connected device/emulator and press **Run** (`Shift + F10`).

---

## 🧪 Testing Background Execution

1. Launch the app.
2. Grant notification permission when prompted (Android 13+).
3. The app automatically enqueues a **Periodic Work Request** (runs every 15 minutes when connected to network).
4. Tap the **Refresh icon** in the top bar to trigger a **One-Time Work Request Chain**:
   - `FetchWorker` fetches a quote from DummyJSON API and inserts it into Room.
   - `NotificationWorker` receives output data and posts a system notification.
5. Inspect WorkManager logs or Background Task Inspector in Android Studio:
   - **View ➔ Tool Windows ➔ App Inspection ➔ Background Task Inspector**

---

## 👤 Author

**CoderPakistan**
- GitHub: [@coderpakistan](https://github.com/coderpakistan)

---

## 📄 License

```text
Copyright 2025 CoderPakistan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language governing
permissions and limitations under the License.
```
