# 💬 GossipApp

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**GossipApp** is a modern, real-time Android chat and collaboration application built with Java and Firebase. It features seamless one-on-one and group messaging, rich media sharing, notes and document viewing, interactive message reactions, and smooth animations.

---

## ✨ Features

- 🔐 **Authentication & Security**
  - Email & Password registration/login.
  - One-tap Google Sign-In integration via Google Play Services.
  - Persistent login sessions and secure user profile management.

- 💬 **Real-time 1-on-1 Messaging**
  - Instant direct messages powered by Firebase Firestore & Realtime Database.
  - Live message delivery timestamps and typing status.
  - Smooth recycler view rendering with FirebaseUI Firestore paging.

- 👥 **Group Chats**
  - Create and manage custom chat groups.
  - Interactive multi-user selection dialogs.
  - Broadcast conversations to multiple friends simultaneously.

- 📝 **Notes & PDF Document Viewer**
  - Share and view shared notes and study materials.
  - Built-in paginated PDF viewer for reading documents without external apps.

- 🖼️ **Media & Photo Viewer**
  - Full-screen image preview with pinch-to-zoom capabilities (powered by PhotoView).
  - Fast image caching and loading with Glide.

- ❤️ **Message Reactions & Emojis**
  - Quick emoji reactions on individual chat bubbles.

- 🎨 **Modern & Responsive UI**
  - Fluid Lottie animations and typewriter text effects on onboarding/splash.
  - Scalable DP/SP units (`sdp` & `ssp`) for pixel-perfect responsiveness across screen sizes.
  - Clean Material Design 3 theme with intuitive navigation.

- 📢 **Announcements & Master Controls**
  - Broadcast announcements and access control tools.

---

## 🛠️ Tech Stack & Libraries

- **Language:** [Java (1.8 / 17 compatible)](https://www.java.com/)
- **Target SDK:** Android 34 (UpsideDownCake) | **Min SDK:** Android 24 (Nougat)
- **Backend & Cloud Services:**
  - [Firebase Auth](https://firebase.google.com/docs/auth) – Authentication
  - [Cloud Firestore](https://firebase.google.com/docs/firestore) & [Realtime Database](https://firebase.google.com/docs/database) – Real-time sync & data storage
  - [Firebase Cloud Messaging (FCM)](https://firebase.google.com/docs/cloud-messaging) – Push notifications
  - [FirebaseUI Firestore](https://github.com/firebase/FirebaseUI-Android) – UI binding
- **UI & Media:**
  - [Material Components for Android](https://material.io/develop/android)
  - [Glide](https://github.com/bumptech/glide) – Image loading & caching
  - [Lottie for Android](https://github.com/airbnb/lottie-android) – Animations
  - [PhotoView](https://github.com/Baseflow/PhotoView) – Zoomable image view
  - [SDP & SSP](https://github.com/intuit/sdp) – Scalable dimension & text sizing
- **Networking:** [Volley](https://github.com/google/volley)

---

## 📁 Project Structure

```text
GossipApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/gossipapp/
│   │   │   ├── Activity/             # Core activities (Login, SignIn, Splash, Chat, Main, etc.)
│   │   │   ├── Adapter/              # Custom adapters (ChatAdapter, NotesAdapter, GroupsAdapter, etc.)
│   │   │   ├── Model/                # Data models (UserModel, ChatModel, GroupModel, NoteModel, etc.)
│   │   │   ├── Fragment/             # ChatFragment, FriendsFragment, NotesFragment, ProfileFragment, etc.)
│   │   │   └── Utility/              # FirebaseUtil, AndroidUtil helper classes
│   │   ├── res/                      # Layouts, drawables, values, anims, and raw resources
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── functions/                        # Firebase Cloud Functions (Node.js backend triggers)
├── build.gradle
├── settings.gradle
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- [Android Studio Iguana | 2023.2.1](https://developer.android.com/studio) or newer
- JDK 17 / JDK 8 configured in Android Studio
- Android SDK (API Level 24+)
- A [Firebase](https://console.firebase.google.com/) account

### Installation & Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/umang4zq/GossipApp.git
   cd GossipApp
   ```

2. **Set Up Firebase:**
   - Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
   - Add an Android app with the package name `com.example.gossipapp`.
   - Download the `google-services.json` file and place it inside the `app/` directory (`app/google-services.json`).
   - Enable **Authentication** (Email/Password and Google Provider).
   - Enable **Cloud Firestore** and **Realtime Database** with appropriate read/write security rules.

3. **Build the Project:**
   - Open the project in Android Studio.
   - Let Gradle sync and download all dependencies.
   - Run on an emulator or physical Android device using `Shift + F10` or the **Run** button.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 👤 Author

- **Umang** - [@umang4zq](https://github.com/umang4zq)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
