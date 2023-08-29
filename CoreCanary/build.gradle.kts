plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val kotlinVersion: String by rootProject.extra
val chartboostCoreVersion: String by rootProject.extra

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.chartboost.core.canary"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = chartboostCoreVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "location"
    productFlavors {
        create("local")
        create("remote")
        create("candidate")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.2"
    }

    packagingOptions {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    "localImplementation"(project(":ChartboostCore"))
    "localImplementation"(project(":UsercentricsAdapter"))

    "remoteImplementation"("com.chartboost:chartboost-core-sdk:0.+")
    "remoteImplementation"("com.chartboost:chartboost-core-consent-adapter-usercentrics:0.+")

    "candidateImplementation"("com.chartboost:chartboost-core-sdk:$chartboostCoreVersion")
    "candidateImplementation"("com.chartboost:chartboost-core-consent-adapter-usercentrics:0.+")

    val composeBom = platform("androidx.compose:compose-bom:2023.04.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:$kotlinVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.0-alpha02")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
