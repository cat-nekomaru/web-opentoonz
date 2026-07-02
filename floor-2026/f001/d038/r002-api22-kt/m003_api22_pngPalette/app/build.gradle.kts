import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.neko.m003_api22_pngPalette"
    // AGP 8.6.0 is tested up to compileSdk 35.
    compileSdk = 35

    defaultConfig {
        applicationId = "com.neko.m003_api22_pngPalette"
        minSdk = 22
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Kotlin 2.0+ : use the compilerOptions DSL instead of the deprecated
// kotlinOptions { jvmTarget = "17" } block. Must match compileOptions (17).
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
}
