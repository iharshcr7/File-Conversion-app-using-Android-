🏗️ Project Overview

Project Name: File Conversoin
Type: Android Studio Java App
Purpose: File and document converter (JPG/PNG/PDF/DOC), includes PDF operations (compress, merge, split) and user authentication via SQLite.

This app appears to be built for your Mobile Application Development (MAD) course.

📁 Root-Level Files
File	Purpose
build.gradle	Top-level Gradle build file managing dependencies for the whole project.
gradle.properties	Holds Gradle configuration properties like JVM args or parallel build settings.
gradlew & gradlew.bat	Gradle wrapper scripts for Linux/macOS and Windows. Lets you build without installing Gradle separately.
local.properties	Contains SDK path (auto-generated, not to be committed).
settings.gradle	Defines included modules (usually just app).
🧱 Folder: .gradle/ and .idea/

These are build cache and IDE configuration directories:

.gradle/ → Internal Gradle build data (no need to upload to GitHub).

.idea/ → Android Studio project settings, compiler preferences, etc.

They don’t affect app functionality.

📱 Folder: app/

This is the main Android module.
Contains your app’s Java source code, UI layouts, and manifest.

Key files:
File	Function
app/build.gradle	Module-specific Gradle file — lists app dependencies like PDFBox, Apache POI, etc.
proguard-rules.pro	Used for code shrinking/obfuscation when building release APKs.
⚙️ Folder: app/src/ (Expected, though not shown fully due to extraction limits)

This is where your main logic and UI code live.

It usually contains:

app/
 └── src/
      ├── main/
      │    ├── java/com/example/mad_project/
      │    │     ├── MainActivity.java
      │    │     ├── LoginActivity.java
      │    │     ├── SignupActivity.java
      │    │     ├── FileConvertActivity.java
      │    │     ├── PdfMergeActivity.java
      │    │     ├── PdfSplitActivity.java
      │    │     ├── PdfCompressActivity.java
      │    │     └── DatabaseHelper.java
      │    ├── res/
      │    │     ├── layout/*.xml (UI files)
      │    │     ├── drawable/*.xml (images/backgrounds)
      │    │     ├── values/*.xml (colors, strings)
      │    └── AndroidManifest.xml


Let’s break down these components next 👇

🧩 Major Functionalities (based on structure and dependencies)
🔐 1. Authentication System (Login/Signup)

Classes: LoginActivity, SignupActivity, DatabaseHelper

Purpose:

Allows user registration (email, username, password)

Stores user data locally in an SQLite database

Provides authentication for accessing conversion tools

Database file: Likely DatabaseHelper.java, handling CREATE TABLE, INSERT, SELECT operations.

🧾 2. File Conversion Module

Class: FileConvertActivity (or similar)

Dependencies: Apache POI (for DOC) and PDFBox (for PDF)

Features:

Converts between formats:

Image (JPG/PNG) ⇄ PDF

DOC ⇄ PDF

Lets users browse files from storage (Intent.ACTION_GET_CONTENT)

Saves converted files in local storage.

🧮 3. PDF Tools

These may include:

Merge PDFs: Combine multiple PDFs into one using PDFBox

Split PDFs: Extract pages into new files

Compress PDFs: Reduce file size

Each has a separate Activity (e.g., PdfMergeActivity.java)

PDFBox Library is visible in mergeDebugAssets, confirming PDF-related operations.

🗂️ 4. Recent Conversion History

Stores metadata (file name, date, type) into an SQLite table.

Likely displayed using RecyclerView in a “History” screen.

🎨 5. User Interface (XML Layouts)

All located in app/src/main/res/layout/

Contains:

activity_main.xml — main dashboard

activity_login.xml, activity_signup.xml — authentication screens

activity_pdf_merge.xml, etc. — feature-specific layouts

Uses LinearLayouts, Buttons, ImageViews, and EditTexts.

📦 6. Assets and Resources

In app/build/intermediates/assets/ you have:

Fonts and CMaps (com/tom_roush/fontbox/resources/cmap/...)

Used internally by PDFBox for text and encoding maps.

These assets allow accurate PDF rendering and manipulation.

⚡ 7. APK and Metadata

Found in:

app/build/intermediates/apk/debug/app-debug.apk
app/build/intermediates/apk/debug/output-metadata.json


These are the debug builds created by Android Studio.

You can install this APK directly to test your app.

🧭 Project Flow Summary
graph TD
A[Login Screen] -->|Login Successful| B[Main Dashboard]
B --> C[Convert Files (JPG, PNG, PDF, DOC)]
B --> D[Merge PDFs]
B --> E[Split PDFs]
B --> F[Compress PDFs]
C --> G[Save Result + Update History]
D --> G
E --> G
F --> G
G --> H[View Recent History]

🧰 Tools & Libraries Used
Library	Purpose
Apache POI	Handles Word document conversions.
PDFBox (com.tom_roush.pdfbox)	Manages PDF reading, writing, merging, splitting.
SQLite	Local database for authentication and history.
AndroidX	UI and lifecycle management.


Project Images


