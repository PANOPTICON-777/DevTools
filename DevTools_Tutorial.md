# Building DevTools: A Privacy-First Android Utility App

This tutorial walks through the complete process of how we researched, built, tested, and packaged **DevTools**, a native Android app designed specifically for F-Droid.

## 1. Researching the F-Droid Gap

F-Droid is a repository of FOSS (Free and Open Source Software) apps for Android. Users value privacy, zero telemetry, and offline capabilities. 

I researched what users were asking for and found a specific gap: **There were zero dedicated text encoding/decoding and hashing utilities on F-Droid.** If a developer or student wanted to quickly decode Base64, generate a SHA-256 hash, or format JSON offline, they had to rely on web tools or bloated Google Play apps.

We decided to build **DevTools**: a 100% offline, zero-permission utility app.

## 2. Project Setup & Architecture

The app was built using modern Android development standards:
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Build System:** Gradle (Kotlin DSL)
- **Minimum SDK:** Android 8.0 (API 26)
- **Target SDK:** Android 14 (API 34)

### The Magic of Zero Permissions
The most important file in our app is `AndroidManifest.xml`. Unlike most apps, ours is completely empty of `<uses-permission>` tags.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- No permissions required - fully offline app -->
    <application
        android:icon="@drawable/ic_launcher"
        android:label="DevTools"
        android:theme="@style/Theme.DevTools">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```
Because we don't request `INTERNET` or `READ_EXTERNAL_STORAGE`, the Android OS mathematically guarantees this app cannot track you or steal your data.

## 3. The Core Logic: `ToolEngine.kt`

All the mathematical and text transformations happen in a single Kotlin object called `ToolEngine`. By isolating the logic from the UI, it becomes incredibly easy to test.

Here are a few examples of how we implemented the tools using native Java/Kotlin APIs without any third-party libraries:

**Base64 Encoding:**
```kotlin
fun base64Encode(input: String): String {
    return Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
}
```

**SHA-256 Hashing:**
```kotlin
fun sha256(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
    return hashBytes.joinToString("") { "%02x".format(it) }
}
```

**JSON Formatting:**
```kotlin
fun formatJson(input: String): String {
    val trimmed = input.trim()
    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
        val obj = org.json.JSONTokener(trimmed).nextValue()
        return when (obj) {
            is org.json.JSONObject -> obj.toString(2)
            is org.json.JSONArray -> obj.toString(2)
            else -> "Error"
        }
    }
    return "Error"
}
```

## 4. The User Interface: Jetpack Compose

We used **Jetpack Compose**, Android's modern declarative UI toolkit. Instead of messy XML layouts, UI is written in Kotlin functions.

The app uses a `Scaffold` to provide the top app bar, and a `LazyColumn` (similar to a RecyclerView) to display the list of tools.

```kotlin
@Composable
fun DevToolsApp() {
    var selectedTool by remember { mutableStateOf<ToolItem?>(null) }
    
    // If no tool is selected, show the list
    if (selectedTool == null) {
        LazyColumn {
            items(tools) { tool ->
                ToolCard(tool = tool, onClick = { selectedTool = tool })
            }
        }
    } else {
        // Otherwise, show the specific tool screen
        ToolScreen(
            tool = selectedTool!!,
            onBack = { selectedTool = null }
        )
    }
}
```

Every tool uses a shared `OutputCard` composable that automatically includes a "Copy to Clipboard" button, making the app highly efficient to use.

## 5. Testing and Verification

Before compiling the final APK, I wrote a comprehensive Python test suite (`test_tools.py`) that replicated every single algorithm in `ToolEngine.kt`. 

I tested edge cases like:
- Emojis in Base64 strings
- Invalid JSON inputs
- Correct byte counting (handling multi-byte UTF-8 characters)
- JWT parsing

After the Python tests passed, I ran a static analysis script against the Kotlin source code to verify that no network libraries (like `HttpURLConnection` or `OkHttp`) were accidentally imported.

## 6. Compiling and Signing the APK

Finally, we used the Android SDK command-line tools to build the app.

1. **Compile:** `gradle assembleRelease`
2. **Generate Keystore:** `keytool -genkey -keystore devtools-release.jks ...`
3. **Sign APK:** `apksigner sign --ks devtools-release.jks app-release-unsigned.apk`

The result is two files:
- `DevTools-v1.0.0.apk` (1.4 MB) - The highly optimized, minified release build.
- `DevTools-v1.0.0-debug.apk` (14 MB) - The debug build (useful if you want to connect Android Studio and step through the code).

## 7. How to Submit to F-Droid

If you want to publish this to the official F-Droid repository, you don't submit the APK. F-Droid builds everything from source to guarantee safety.

Here is what you need to do:
1. Upload this source code to a public Git repository (GitHub, GitLab, Codeberg).
2. Fork the `fdroiddata` repository on GitLab.
3. Add a metadata file for `org.devtools.app` pointing to your Git repo.
4. Submit a Merge Request to F-Droid.

Their build servers will pull your code, verify it has no proprietary dependencies, compile it, and publish it to their store!
