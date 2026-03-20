# 📰 NewsFeed — Android News Application

> A mobile application that delivers real-time news updates with offline reading support, built as part of a university semester project.

---

## 📌 Project Overview

**NewsFeed** is an Android application developed in Android Studio. It fetches live news data from an external REST API and enables users to browse, filter, and save articles for offline access. The app is designed with a focus on real-time updates and persistent local storage.

---

## 🚀 Features

### ⏱️ Real-Time Updates
- The news feed automatically refreshes every **3 seconds** using API polling, ensuring users always see the latest headlines.
- Seamless integration with a live news service API for continuous data retrieval.

### 🗄️ Local Storage & Persistence
- Articles are stored in a **local database** (SQLite/Room), enabling offline access and improved performance.
- Logic implemented to distinguish and visually prioritize **Featured** articles over standard ones.

### 🔍 Content Management
- **Category Filtering** — users can filter news by topic or category.
- **Sorting** — articles can be sorted by publication date or filtered to exclude content containing unwanted keywords.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Platform | Android |
| Language | Kotlin |
| IDE | Android Studio |
| Networking | REST API (HTTP) |
| Local Storage | SQLite / Room Database |
| Real-time Logic | Handlers / Schedulers (3s polling) |

---

## ⚙️ Setup & Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/rubina-rekic/Newsfeed-Android-App.git
   ```

2. **Open in Android Studio**
   - File → Open → select the project folder

3. **Configure API Key**
   - Obtain an API key from your news provider
   - Add it to `local.properties` or directly in the constants file:
     ```
     NEWS_API_KEY=your_api_key_here
     ```

4. **Run the app**
   - Connect a device or start an emulator
   - Click **Run ▶** in Android Studio

---

## 📚 Course Info

This project was developed as part of a semester assignment. The goal was to practice Android development concepts including API integration, local database management, and real-time data handling.

---

## 👤 Author

**Rubina Rekić**

---

## 📄 License

This project is for educational purposes only.
