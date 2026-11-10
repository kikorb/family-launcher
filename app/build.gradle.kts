plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.familyboard.tv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.familyboard.tv"
        minSdk = 28
        targetSdk = 34
        ndk { abiFilters += listOf("armeabi-v7a", "arm64-v8a") }
        resourceConfigurations += listOf("es", "en")
        versionCode = 3
        versionName = "1.2.0"

        val notionToken = System.getenv("PERSONAL_NOTION").orEmpty()
        buildConfigField("String", "PERSONAL_NOTION_TOKEN", "\"$notionToken\"")
        buildConfigField("String", "TMDB_API_KEY", "\"${System.getenv("TMDB_API_KEY").orEmpty()}\"")
    }

    signingConfigs {
        create("local") {
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("local")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Jetpack Compose for Android TV
    implementation("androidx.tv:tv-foundation:1.0.0-alpha11")
    implementation("androidx.tv:tv-material:1.0.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Coil for Compose (Image/Wallpaper loading)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Coroutines & OkHttp (for Notion API & Open-Meteo Weather)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")
}

tasks.named("preBuild") {
    doFirst {
        if (System.getenv("PERSONAL_NOTION").isNullOrBlank()) {
            throw GradleException(
                "PERSONAL_NOTION is not set. Export your Notion integration token before building, " +
                    "for example: export PERSONAL_NOTION=ntn_xxx (see README)."
            )
        }
        if (System.getenv("TMDB_API_KEY").isNullOrBlank()) {
            throw GradleException(
                "TMDB_API_KEY is not set. Create a free key at themoviedb.org/settings/api and " +
                    "export it before building: export TMDB_API_KEY=xxx (see README)."
            )
        }
    }
}
