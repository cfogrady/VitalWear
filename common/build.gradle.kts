import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.github.cfogrady.vitalwear.common"
    compileSdk = 34

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    buildFeatures {
        viewBinding = true
        compose = true
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // GIF
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // Compose
    implementation(libs.android.compose.ui)
    implementation(libs.android.compose.ui.preview)
    implementation(libs.android.compose.material)
    implementation(libs.android.compose.livedata)
    implementation(libs.android.compose.foundation)
    androidTestImplementation(libs.android.compose.ui.test)
    debugImplementation(libs.android.compose.ui.tooling)
    implementation(libs.android.activity.compose)

    // VB-Dim-Reader library
    implementation(libs.dim.reader)

    // Logging
    implementation(libs.timber)
    implementation(libs.tiny.log)

    // Needed
    implementation(libs.guava)

    // Rooms
    implementation(libs.room.runtime)
    // To use Kotlin Symbol Processing (KSP)
    ksp(libs.room.compile)
    // optional - Test helpers
    testImplementation(libs.room.testing)
}