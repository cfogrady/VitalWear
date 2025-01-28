// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    // TODO Remove
    extra.apply{
        set("compose_version", "1.5.3")
        set("room_version", "2.6.0")
        set("vb_dim_reader_version", "2.1.0.1-231156d")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.room) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
