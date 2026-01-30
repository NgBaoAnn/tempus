plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

import java.util.Properties
import java.io.FileInputStream


android {
    namespace = "com.projectapp.tempus"
    compileSdk = 36


    defaultConfig {
        applicationId = "com.projectapp.tempus"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Helper to read .env file
        val envFile = rootProject.file(".env")
        val envProperties = Properties()
        if (envFile.exists()) {
            envProperties.load(FileInputStream(envFile))
        }

        // Supabase service
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${project.findProperty("SUPABASE_URL")}\""
        )

        buildConfigField(
            "String",
            "SUPABASE_KEY",
            "\"${project.findProperty("SUPABASE_KEY")}\""
        )

        // Gemini AI service - Fallback to first key if not set
        val geminiKey1 = envProperties.getProperty("GEMINI_API_KEY_1") ?: ""
        
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"$geminiKey1\""
        )
        
        // Gemini API Keys Pool (from .env)
        buildConfigField("String", "GEMINI_API_KEY_1", "\"${envProperties.getProperty("GEMINI_API_KEY_1") ?: ""}\"")
        buildConfigField("String", "GEMINI_API_KEY_2", "\"${envProperties.getProperty("GEMINI_API_KEY_2") ?: ""}\"")
        buildConfigField("String", "GEMINI_API_KEY_3", "\"${envProperties.getProperty("GEMINI_API_KEY_3") ?: ""}\"")
        buildConfigField("String", "GEMINI_API_KEY_4", "\"${envProperties.getProperty("GEMINI_API_KEY_4") ?: ""}\"")
        buildConfigField("String", "GEMINI_API_KEY_5", "\"${envProperties.getProperty("GEMINI_API_KEY_5") ?: ""}\"")
        buildConfigField("String", "GEMINI_API_KEY_6", "\"${envProperties.getProperty("GEMINI_API_KEY_6") ?: ""}\"")
        buildConfigField("String", "GEMINI_API_KEY_7", "\"${envProperties.getProperty("GEMINI_API_KEY_7") ?: ""}\"")
        buildConfigField("String", "GEMINI_API_KEY_8", "\"${envProperties.getProperty("GEMINI_API_KEY_8") ?: ""}\"")
    }


    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }

    signingConfigs {
        create("shared") {
            storeFile = file("keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "AndroidDebugKey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName("shared")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Supabase
    implementation("io.github.jan-tennert.supabase:supabase-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.1.3")
    implementation("io.ktor:ktor-client-okhttp:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Lottie Animations
    implementation("com.airbnb.android:lottie:6.3.0")

    // Biometric for secure delete
    implementation("androidx.biometric:biometric:1.1.0")

    // ============ JETPACK COMPOSE ============
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.lottie.compose)
    
    // Material Icons Extended - for more icons
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    
    // Compose Debug
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Image Loading (Coil)
    implementation("io.coil-kt:coil:2.7.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Media Notification
    implementation("androidx.media:media:1.7.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Google Sign-In with Credential Manager (Modern approach)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
}
