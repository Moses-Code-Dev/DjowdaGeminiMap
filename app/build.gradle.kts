/*
 * Created by the Djowda Project Team
 * Copyright (c) 2017-2025 Djowda. All rights reserved.
 *
 * This file is part of the Djowda Project.
 *
 * Licensed under the Djowda Non-Commercial, Non-Profit License v1.0
 *
 * Permissions:
 * - You may use, modify, and share this file for non-commercial and non-profit purposes only.
 * - Commercial use of this file, in any form, requires prior written permission
 *   from the Djowda Project maintainers.
 *
 * Notes:
 * - This project is community-driven and continuously evolving.
 * - The Djowda Project reserves the right to relicense future versions.
 *
 * Last Modified: 2025-09-10 19:31
 */

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
    alias(libs.plugins.google.gms.google.services) // Use your Kotlin version
}

android {
    namespace = "com.djowda.djowdageminimap"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.djowda.djowdageminimap"
        minSdk = 27
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.glide)

    implementation(libs.core.splashscreen)
    implementation(libs.room.runtime)


    annotationProcessor(libs.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.libphonenumber)

    implementation(libs.play.services.location)

    implementation(libs.firebase.ai)

    implementation(libs.guava)

    implementation(libs.reactive.streams)


    // Remove the Gson dependency if you are fully switching to Kotlin Serialization
    // implementation("com.google.code.gson:gson:2.13.2")

    // Add the Kotlinx Serialization JSON dependency
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0") // Use the latest stable version




    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}