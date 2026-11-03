plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "ai.nova.browser"
    compileSdk = 35
    defaultConfig {
        applicationId = "ai.nova.browser"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
