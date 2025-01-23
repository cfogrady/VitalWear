import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.room)
    alias(libs.plugins.ksp) // needed for room
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.github.cfogrady.vitalwear"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.github.cfogrady.vitalwear"
        minSdk = 28
        targetSdk = 34
        versionCode = 13
        versionName = "0.4.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDirs(files("$projectDir/schemas"))
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
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

    // Android Defaults
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(project(":common"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Compose
    implementation(libs.android.compose.ui)
    implementation(libs.android.compose.ui.preview)
    implementation(libs.android.compose.material)
    implementation(libs.android.compose.livedata)
    implementation(libs.android.compose.foundation)
    androidTestImplementation(libs.android.compose.ui.test)
    debugImplementation(libs.android.compose.ui.tooling)
    implementation(libs.android.activity.compose)

    //VB-DIM-Reader
    implementation(libs.dim.reader)

    //SLF4J and Logging
    // runtimeOnly 'org.slf4j:slf4j-android:1.7.26'
    runtimeOnly(libs.slf4j.timber)
    implementation(libs.timber)
    implementation(libs.tiny.log)
    implementation(libs.tiny.log.impl)

    //Channel Client (data transfer)
    implementation(libs.play.services.wearable)

    // Task.await() extension
    implementation(libs.kotlinx.coroutines.play.services)

    // Rooms
    implementation(libs.room.runtime)
    // To use Kotlin Symbol Processing (KSP)
    ksp(libs.room.compile)
    // To Test Room migrations
    androidTestImplementation(libs.room.testing)
}