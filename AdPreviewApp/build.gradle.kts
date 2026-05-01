/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

import com.android.build.api.dsl.DefaultConfig
import com.android.build.api.dsl.SigningConfig
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20"
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
    id("com.google.firebase.crashlytics")
}

// region Property Loading
val secrets = Properties()
val secretsFile = project.file("secrets.properties")
if (secretsFile.exists()) {
    secretsFile.inputStream().use { secrets.load(it) }
} else {
    println("Warning: secrets.properties file not found.")
}

val config = Properties()
val configFile = rootProject.file("config.properties")
if (configFile.exists()) {
    configFile.inputStream().use { config.load(it) }
} else {
    println("Warning: config.properties file not found in AdPreviewApp module.")
}
// endregion

// SDK versions used in dependencies
val mediationSdkVersion = "5.3.0"
val monetizationSdkVersion = "9.9.3"

android {
    namespace = "com.chartboost.adpreviewapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.chartboost.adpreviewapp"
        minSdk = 21
        targetSdk = 34
        versionCode = 2
        versionName = AdPreviewAppInfo.adPreviewAppVersion

        buildConfigField("String", "MONETIZATION_SDK_VERSION", "\"$monetizationSdkVersion\"")
        configureAuth0(secrets, config)
    }

    signingConfigs {
        create("release") {
            // Check if we're in CI environment
            if (isCICDEnvironment()) {
                // CI environment - use environment variables TODO: [HB-9892]
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
                storeFile = rootProject.file("release-keystore.jks")
                storePassword = System.getenv("KEYSTORE_PASSWORD")
            } else {
                // Local development - use properties file
                configureKeystoreLocally()
            }
        }
    }

    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"https://fusion-api.chartboost.com/\"")
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        disable.add("NotificationPermission")
    }
}

/**
 * Reads Auth0 properties, validates them, and configures build fields and manifest placeholders.
 * This is an extension function on DefaultConfig, so it can access its methods directly.
 */
fun DefaultConfig.configureAuth0(
    secrets: Properties,
    config: Properties,
) {
    val auth0ClientId: String =
        if (isCICDEnvironment()) {
            System.getenv("AUTH0_CLIENT_ID")
        } else {
            secrets.getProperty("AUTH0_CLIENT_ID", "")
        }
//    check(auth0ClientId.isNotBlank()) {
//        "AUTH0_CLIENT_ID is missing. Add it to your project secrets.properties file."
//    }
    buildConfigField("String", "AUTH0_CLIENT_ID", "\"$auth0ClientId\"")

    val auth0Domain: String = config.getProperty("auth0.domain", "")
    check(auth0Domain.isNotBlank()) {
        "auth0.domain is missing. Add it to your project config.properties file."
    }
    buildConfigField("String", "AUTH0_DOMAIN", "\"$auth0Domain\"")

    val auth0Scheme: String = config.getProperty("auth0.scheme", "")
    check(auth0Scheme.isNotBlank()) {
        "auth0.scheme is missing. Add it to your project config.properties file."
    }
    buildConfigField("String", "AUTH0_SCHEME", "\"$auth0Scheme\"")

    // put auth0 required placeholders to manifest
    manifestPlaceholders +=
        mapOf(
            "auth0Domain" to auth0Domain,
            "auth0Scheme" to auth0Scheme,
        )
}

fun SigningConfig.configureKeystoreLocally() {
    val keystoreProperties = Properties()
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use {
            keystoreProperties.load(it)
        }
        keyAlias =
            keystoreProperties.getProperty("keyAlias", "").also {
                check(it.isNotBlank()) {
                    "keyAlias cannot be null or blank"
                }
            }

        keyPassword =
            keystoreProperties.getProperty("keyPassword", "").also {
                check(it.isNotBlank()) {
                    "keyPassword cannot be null or blank"
                }
            }
        storePassword =
            keystoreProperties.getProperty("storePassword", "").also {
                check(it.isNotBlank()) {
                    "storePassword cannot be null or blank"
                }
            }
        val storeFilePath = keystoreProperties.getProperty("storeFile", "")
        check(storeFilePath.isNotBlank()) {
            "storeFile cannot be null or blank"
        }
        storeFile = rootProject.file(storeFilePath)
    } else {
        "keystore.properties file not found at ${keystorePropertiesFile.absolutePath}!"
    }
}

fun isCICDEnvironment() = System.getenv("AD_PREVIEW_CI_CD") == "true"

dependencies {

    // Chartboost
    implementation("com.chartboost:chartboost-mediation-sdk:$mediationSdkVersion")
    implementation("com.chartboost:chartboost-mediation-adapter-chartboost:5.9.9.3.0")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.6.6")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.6")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.8.9")

    // Lifecycle + ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    implementation("androidx.compose.foundation:foundation-layout-android:1.6.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.compose.foundation:foundation-layout:1.6.7")
    kapt("com.google.dagger:hilt-compiler:2.51")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Compose tooling (preview + test)
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.6")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.6")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.6")
    implementation("androidx.compose.material:material-icons-extended:1.6.7")
    implementation("io.coil-kt:coil-compose:2.3.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:31.2.2"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Google Play services
    implementation("com.google.android.gms:play-services-base:18.1.0")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
    implementation("com.google.android.gms:play-services-appset:16.0.2")

    // JUnit & Espresso
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Auth0
    implementation("com.auth0.android:auth0:2.9.2")

    // Credentials
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
}
