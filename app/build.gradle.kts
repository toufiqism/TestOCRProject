plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.testocrproject"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.testocrproject"
        minSdk = 29
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)



    // --- CameraX ---
    val cameraXVersion = "1.5.1"
    implementation("androidx.camera:camera-core:$cameraXVersion")
    implementation("androidx.camera:camera-camera2:$cameraXVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraXVersion")
    implementation("androidx.camera:camera-view:$cameraXVersion")

    // --- Tesseract (tess-two) ---
    implementation(libs.tess.two)

    // --- Kotlin + Coroutines ---
    implementation(libs.kotlinx.coroutines.android)

    //more icons
    implementation (libs.androidx.material.icons.extended)

    implementation(libs.androidx.concurrent.futures.ktx)
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

// To recognize Latin script
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")
// To recognize Chinese script
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-chinese:16.0.1")
// To recognize Devanagari script
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-devanagari:16.0.1")
// To recognize Japanese script
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-japanese:16.0.1")
// To recognize Korean script
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-korean:16.0.1")


    // Retrofit and GSON
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp (often used with Retrofit for advanced network configurations like logging) and Logging Interceptor
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //more icons
    implementation (libs.androidx.material.icons.extended)

    //coil
    implementation(libs.coil.compose)

}