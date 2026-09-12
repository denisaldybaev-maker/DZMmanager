plugins {
    id("com.android.application")
}

android {
    namespace = "com.den31006.dzmmanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.den31006.dzmmanager"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
}

kotlin {
    jvmToolchain(21)
}
