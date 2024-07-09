// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.service) apply false
    kotlin("kapt") version "2.0.0"
    id("com.google.firebase.crashlytics") version "2.9.9" apply false

}