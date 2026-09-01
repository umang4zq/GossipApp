# 💬 GossipApp — Real-Time Chat & Social Android App

> Chat. Connect. Share. All in real-time. 🔥

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

**GossipApp** is a modern, real-time Android chat and collaboration application built with Java and Firebase. It features seamless one-on-one and group messaging, friend management, rich media sharing, notes and document viewing, interactive message reactions, and smooth animations.

---

## ✨ Key Features

| Feature | Description |
|---|---|
| 🔐 **Authentication** | Email & Password registration/login + Google Sign-In via Google Play Services |
| 💬 **Private Chat** | Instant 1-on-1 messaging powered by Firebase Firestore & Realtime Database |
| 👥 **Group Chat** | Create and manage custom chat groups with multi-user selection |
| 🤝 **Friends Management** | Add and connect with other users seamlessly |
| 📝 **Notes & PDF Viewer** | Share personal/study notes and view paginated PDF documents directly inside the app |
| 🖼️ **Media & Image Viewer** | Full-screen image preview with pinch-to-zoom capabilities (powered by PhotoView & Glide) |
| 😄 **Message Reactions** | React to individual chat messages with interactive emojis |
| 🔔 **Push Notifications** | FCM-powered real-time notifications for incoming messages and alerts |
| 🎭 **Animated UI** | Fluid Lottie splash animations and typewriter text effects |
| 👤 **User Profiles** | Custom avatar selection and profile customization |
| 📢 **Master Controls** | Broadcast announcements and access control tools |

---

## 🎯 Use Cases

- 👨‍👩‍👧 **Friends & Family:** Staying connected in private and group chats.
- 🎓 **Students & Study Groups:** Sharing study notes, PDF documents, and project discussions.
- 💼 **Small Teams & Clubs:** Communicating instantly with broadcast announcements and group threads.
- 🗣️ **Personal Spaces:** Dedicated real-time communication platform with customizable profiles.

---

## 🛠️ Tech Stack & Architecture

| Layer | Technology / Library |
|---|---|
| 📱 **Language & Platform** | Java (Android SDK 34 / Min SDK 24) |
| 🗄️ **Database & Backend** | Firebase Firestore & Firebase Realtime Database |
| 🔐 **Authentication** | Firebase Authentication & Google Play Services Auth |
| 🔔 **Notifications** | Firebase Cloud Messaging (FCM) |
| 🖼️ **Image & Media** | Glide, PhotoView |
| 🎨 **UI & Dimensions** | Material Design 3, Lottie, SDP & SSP (responsive screen scaling) |
| 🌐 **Networking** | Android Volley |

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
- A [Firebase](https://console.firebase.google.com/) project

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
   - Enable **Authentication** (Email/Password and Google Sign-In Provider).
   - Enable **Cloud Firestore** and **Realtime Database** with appropriate read/write security rules.
   - Enable **Cloud Messaging (FCM)** for notifications.

3. **Build and Run:**
   - Open the project in Android Studio.
   - Sync Gradle dependencies.
   - Run on an emulator or physical Android device (`Shift + F10`).

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 👨‍💻 Author

Developed with ❤️ by **Umang Markana**
- GitHub: [@umang4zq](https://github.com/umang4zq)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
