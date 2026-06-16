plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.neko.m002_api22_beep"
    compileSdk = 37         // 手動にて ⭐️

    defaultConfig {
        applicationId = "com.neko.m002_api22_beep"
        minSdk = 22         // 手動にて ⭐️
        targetSdk = 34      // 手動にて ⭐️
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
    // implementation(libs.androidx.activity.ktx)   // API22 未使用のため ⭐️
    // implementation("androidx.appcompat:appcompat:1.6.1") // Claude指摘 ⭐️
    // implementation(libs.androidx.constraintlayout)// API22 未使用のため ⭐️
    // implementation(libs.androidx.core.ktx)       // API22 未使用のため ⭐️
    // implementation(libs.material)                // API22 未使用のため ⭐️
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}