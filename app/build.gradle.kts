plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.room)
    alias(libs.plugins.ksp) // needed for room
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
        versionCode = libs.versions.projectVersionCode.get().toInt()
        versionName = libs.versions.projectVersion.get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDirs(files("$projectDir/schemas"))
    }

    // For Kotlin projects
    kotlinOptions {
        jvmTarget = "11"
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.preview)
    implementation(libs.androidx.compose.livedata)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.compose.material3)
    androidTestImplementation(libs.androidx.compose.ui.test)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.watchface.complications.data.source.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.concurrent.futures.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(project(":common"))
    implementation(project(":protos"))
    implementation(project(":transfer"))
    implementation(libs.protobuf.javalite)
    implementation(libs.connections)
    implementation(libs.connections.wear.ui)

    // Work Manager
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.wear.compose.foundation)
    // For Wear Material Design UX guidelines and specifications
    implementation(libs.androidx.wear.compose.material)
    // For integration between Wear Compose and Androidx Navigation libraries
    implementation(libs.androidx.wear.compose.navigation)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.guava)
    implementation(libs.androidx.percentlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.wear)
    implementation(libs.androidx.wear.tiles)
    debugImplementation(libs.androidx.wear.tiles.renderer)

    //Channel Client (data transfer)
    implementation(libs.play.services.wearable)

    //SLF4J and Logging
    // runtimeOnly 'org.slf4j:slf4j-android:1.7.26'
    runtimeOnly(libs.slf4j.timber)
    implementation(libs.timber)
    implementation(libs.tiny.log)
    runtimeOnly(libs.tiny.log.impl)

    // Rooms
    implementation(libs.room.runtime)
    // To use Kotlin Symbol Processing (KSP)
    ksp(libs.room.compile)
    // To Test Room migrations
    androidTestImplementation(libs.room.testing)

    //VB-Dim-Reader library
    implementation(libs.dim.reader)
}