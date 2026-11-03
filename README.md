# NOVA Browser — Android source starter

This is a native Android Kotlin project using Android System WebView (Chromium-based).

## Features in this starter
- YouTube and YouTube Music quick launch
- URL/search bar, back/forward/reload
- Basic tab list and tab switching
- Local preference for a basic host-based request guard
- Android WebView with JavaScript + DOM storage for web media sites

## Important limitations
- Android WebView is Chromium-based, but it is **not the full Chrome/Chromium browser engine package**.
- The host list is a small demo filter, not AdGuard's full filter engine.
- It cannot reliably remove YouTube ads because YouTube may serve ads and media from shared domains. No guarantee of ad-free playback.
- YouTube Music website compatibility/background playback may differ from the official app. Sign-in, DRM, downloads, casting and notifications are not equivalent to the official YouTube Music app.
- Tabs here are basic URL slots, not isolated browser processes; this is a starter implementation, not production-grade multi-tab Chromium.
- Build/signing has not been performed in this environment, so this ZIP is source code, **not an APK**.

## Build an APK
1. Install Android Studio (stable) and Android SDK Platform 35.
2. Open this folder as a project.
3. Let Gradle sync (JDK 17 recommended).
4. Build > Build Bundle(s) / APK(s) > Build APK(s).
5. For a signed release: Build > Generate Signed Bundle / APK and create a keystore.

Use the official YouTube/YouTube Music websites and comply with their terms. Do not use this wrapper to bypass paid features or access controls.
