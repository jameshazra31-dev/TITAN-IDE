plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.titan.core.common"
    compileSdk = 35

    defaultConfig {
        minSdk = 29
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("javax.inject:javax.inject:1")
    implementation("com.jakewharton.timber:timber:5.0.1")
}