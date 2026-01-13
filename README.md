# FoodLink 🍲

**FoodLink** is a modern Android application designed to bridge the gap between food donors and receivers, aiming to reduce food waste and help those in need. Built with **Kotlin** and **Jetpack Compose**, it offers a seamless and intuitive user experience for Donors, Receivers, and Admins.

## 📱 Features

The application supports three distinct user roles, each with tailored features:

### 🧑‍🍳 Donor
- **Dashboard**: View and manage your active current food donations.
- **Add Donation**: Easily post new food items with details.
- **Edit Donation**: Update details of existing posts.
- **Chat**: Communicate directly with verified receivers to coordinate pickup.
- **History**: Track past donations.

### 🤲 Receiver (NGOs/Volunteers)
- **Dashboard**: Browse available food donations in the network.
- **Connect**: Initiate chats with donors to claim food.
- **Profile**: Manage your receiver profile.

### 🛡️ Admin
- **Dashboard**: Oversee platform activity and users.
- **Management**: Ensure safety and authenticity of posts.

## 🛠️ Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/) - First-class support for Android.
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern, declarative UI framework.
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles.
- **Local Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room) - Robust SQLite abstraction.
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/guide/navigation) - Single-activity navigation.
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/) - Fast, lightweight image loading for Compose.
- **Animations**: [Lottie](https://airbnb.io/lottie/#/) - High-quality animations.
- **Async**: Coroutines & Flow.

## 📦 Dependencies

Dependencies are managed via Gradle Version Catalogs. Key libraries include:
- `androidx.compose.material3`: Material Design 3 components.
- `androidx.room`: Local database storage.
- `io.coil-kt:coil-compose`: Asynchronous image loading.
- `com.airbnb.android:lottie-compose`: Vector animations.

*Note: For a full list of dependencies, see `app/build.gradle.kts`.*

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Koala or later recommended)
- JDK 11 or higher

### Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/FoodLink.git
    ```
2.  **Open in Android Studio**:
    - Launch Android Studio.
    - Select **Open** and navigate to the cloned directory.
3.  **Sync Gradle**:
    - Allow Android Studio to download dependencies and sync the project.
4.  **Run the App**:
    - Connect an Android device or start an Emulator (Min SDK 24).
    - Click the **Run** button (▶️).

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a pull request for any improvements or bug fixes.

## 📄 License

This project is licensed under the MIT License.
