# Flix Pro — Native Android Rebuild

This project is a clean native Android rebuild based on the behavior and public API structure recovered from the supplied Hotflix APK. It is **not a WebView wrapper**.

## Native screens
- Home: native hero carousel + horizontal OTT rails
- Movies: native poster grid
- Series: native poster grid
- Search: native debounced search grid
- Movie / Show detail: native details UI
- Seasons / Episodes: native chips + episode list
- Playback: AndroidX Media3 / ExoPlayer
- Sports / Live TV items returned by Home/Search are routed through the native details/player flow when their backend fields are available

## Requested changes included
- Login / signup flow removed; app runs in Guest Mode
- App name changed to **Flix Pro**
- New Flix Pro launcher/app-bar icon
- Material 3 dark OTT design system
- Modern top app bar and bottom navigation
- Playback PIN: **9966**
- PIN is requested once per install/device; unlock is stored locally in SharedPreferences
- WhatsApp PIN button opens **+8801795071557**
- Legacy ad/payment/push/update SDK code is not included

## Backend
The recovered native API base is:

`https://hotflix.vip/api/v1/`

The app uses native HTTP requests with the legacy API's `data=<Base64(JSON)>` form format. No website page is loaded inside the app.

## Build on GitHub
A workflow is included at `.github/workflows/build-apk.yml`.

1. Upload this project's **contents** to your GitHub repository.
2. Open **Actions** → **Build Flix Pro Native APK**.
3. Click **Run workflow**.
4. After the green checkmark, open the run and download the artifact **Flix-Pro-Native-APK**.
5. Extract the artifact ZIP to get the APK.

## Build locally
Android Studio: open the project folder and let Gradle sync, then Build APK.

Command line on macOS/Linux:

```bash
./gradlew assembleDebug
```

Windows:

```bat
gradlew.bat assembleDebug
```

The bootstrap scripts use Gradle 8.9 if Gradle is not already installed.

## Notes
- Direct MP4/HLS/DASH style URLs are played natively with Media3. YouTube/Vimeo web URLs are opened in the provider app/browser instead of embedding a WebView.
- Backend API behavior can change independently of this source. If a particular endpoint/field differs on the live server, only the corresponding parser/request needs adjustment; the UI remains native.
- For production distribution, use a signed release APK/AAB and keep the same signing key for future updates.
