plugins {
    alias(libs.plugins.android.application)
    // Kotlin 2.0に対応しつつ、古いComposeランタイムをサポートするプラグイン
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
}

android {
    // 💡 正しいパッケージ名に修正
    namespace = "com.neko.api22_tester"

    // API 22との互換ギャップを減らすため、安定した34に指定
    compileSdk = 34

    defaultConfig {
        // 💡 正しいパッケージ名に修正
        applicationId = "com.neko.api22_tester"
        minSdk = 22
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // 🔥 API 22で膨大なメソッド数を扱えるようにマルチデックスを強制有効化
        multiDexEnabled = true

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
        isCoreLibraryDesugaringEnabled = true
        // API 22端末の負荷を下げるため、Java 8（VERSION_1_8）を指定
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // ------------------------------------------------------
    // 🛠️ Jetpack Compose 用ライブラリ（API 22向けに限界まで下げた安定版）
    // ------------------------------------------------------
    // Activity-Composeを古い端末で安定する 1.7系 に指定
    implementation("androidx.activity:activity-compose:1.7.2")

    // Composeのバージョンを、API 22でもクラッシュ報告の少ない 1.5系 に指定
    val composeVersion = "1.5.4"
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-graphics:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")

    // 🔥 Material 3 はAPI 22では絶対クラッシュするため、Material 2（レガシー）を使用
    implementation("androidx.compose.material:material:$composeVersion")

    // ------------------------------------------------------
    // 📦 古い端末用の安定版ライブラリ
    // ------------------------------------------------------
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.core:core:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // マルチデックス用のライブラリを明示的に追加
    implementation("androidx.multidex:multidex:2.0.1")

    // API 22で Java 8以降の挙動をエミュレートするシステム
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // ------------------------------------------------------
    // 🧪 テスト用
    // ------------------------------------------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
