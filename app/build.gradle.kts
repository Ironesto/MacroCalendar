// Archivo app/build.gradle.kts

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.gabriel.cal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gabriel.cal"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Jetpack Compose
    implementation(platform(libs.compose.bom.v20240300))
    implementation(libs.ui)
    implementation(libs.material3)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.activity.compose.v190)

    // Android básico
    implementation(libs.core.ktx.v1130)
    implementation(libs.appcompat.v161)
    implementation(libs.material)
    implementation(libs.constraintlayout.v214)

    // Firebase
    implementation(platform(libs.google.firebase.bom.v3280))
    implementation(libs.com.google.firebase.firebase.auth.ktx)
    implementation(libs.com.google.firebase.firebase.firestore.ktx)

    // Debug para Compose
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.espresso.core.v351)
    androidTestImplementation(platform(libs.androidx.compose.compose.bom.v20240300))
    androidTestImplementation(libs.ui.test.junit4)

    implementation(libs.navigation.fragment.ktx.v277)
    implementation(libs.navigation.ui.ktx.v277)

    // MaterialCalendarView
    implementation(libs.material.calendarview)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material.calendarview)
}
