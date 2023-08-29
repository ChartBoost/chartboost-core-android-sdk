/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

rootProject.name = "ChartboostCore"
include(
    ":CoreCanary",
    ":ChartboostCore",
    ":UsercentricsAdapter",
)

val commonNamedRepoPrefix = "./chartboost-core-android-consent-adapter-"

project(":UsercentricsAdapter").projectDir = File(
    "${commonNamedRepoPrefix}usercentrics/UsercentricsAdapter"
)
