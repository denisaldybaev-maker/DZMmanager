plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.den31006.dzmmanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.den31006.dzmmanager"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

kotlin {
    jvmToolchain(17)
}
