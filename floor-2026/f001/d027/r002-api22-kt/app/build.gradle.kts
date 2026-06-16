plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.neko.m002_api22_beep"
    compileSdk = 37         // ⭐️

    defaultConfig {
        applicationId = "com.neko.m002_api22_beep"
        minSdk = 22         // ⭐️
        targetSdk = 34      // ⭐️
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    // implementation(libs.androidx.activity.ktx)   // API22 未対応のため ⭐️
    // implementation("androidx.appcompat:appcompat:1.6.1") // Claude指摘 ⭐️
    // implementation(libs.androidx.constraintlayout)// API22 未対応のため ⭐️
    // implementation(libs.androidx.core.ktx)       // API22 未対応のため ⭐️
    // implementation(libs.material)                // API22 未対応のため ⭐️
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}