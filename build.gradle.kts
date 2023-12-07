// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.4.1" apply false
    id("com.android.library") version "7.4.1" apply false
    id("org.jetbrains.dokka") version "1.8.10"
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("org.jetbrains.kotlinx.kover") version "0.6.1"
    id("com.jfrog.artifactory") version "4.32.0"
    id("maven-publish")
}

buildscript {
    val kotlinVersion by extra("1.8.10")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        repositories {
            maven("https://cboost.jfrog.io/artifactory/private-chartboost-core/") {
                credentials {
                    username = System.getenv("JFROG_USER")
                    password = System.getenv("JFROG_PASS")
                }
            }
            maven("https://cboost.jfrog.io/artifactory/chartboost-core/")
        }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("com.vanniktech:gradle-android-apk-size-plugin:0.4.0")
        classpath("com.getkeepsafe.dexcount:dexcount-gradle-plugin:4.0.0")
        classpath("com.google.gms:google-services:4.3.15")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.8")
        classpath("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
        classpath("com.google.firebase:firebase-appdistribution-gradle:4.0.0")
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4.32.0")
        classpath("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        classpath("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

        classpath("org.jetbrains.dokka:dokka-base:$kotlinVersion") {
            exclude(group = "com.fasterxml.jackson.core", module = "jackson-databind")
        }
        classpath("com.fasterxml.jackson.core:jackson-databind:2.14.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()

        maven("https://cboost.jfrog.io/artifactory/chartboost-core/") {
            name = "Chartboost Core's Production Repo"
        }
        maven("https://cboost.jfrog.io/artifactory/private-chartboost-core/") {
            name = "Chartboost Core's Private Repo"
            credentials {
                System.getenv("JFROG_USER")?.let {
                    username = it
                }
                System.getenv("JFROG_PASS")?.let {
                    password = it
                }
            }
        }
    }
}

val chartboostCoreVersion: String by extra {
    System.getenv("CHARTBOOST_CORE_VERSION")?.takeIf { it.isNotBlank() }  ?: "0.4.0"
}

project(":ChartboostCore") {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.getkeepsafe.dexcount")
    apply(plugin = "com.jfrog.artifactory")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    ////
    // -- Publishing
    ////
    val groupProjectID = "com.chartboost"
    val artifactProjectID = "chartboost-core-sdk"

    artifactory {
        clientConfig.isIncludeEnvVars = true
        setContextUrl("https://cboost.jfrog.io/artifactory")

        publish {
            repository {
                // If this is a release build, push to the public "chartboost-core" artifactory.
                // Otherwise, push to the "private-chartboost-core" artifactory.
                val isReleaseBuild = "true" == System.getenv("CHARTBOOST_CORE_IS_RELEASE")
                if (isReleaseBuild) {
                    setRepoKey("chartboost-core")
                } else {
                    setRepoKey("private-chartboost-core")
                }
                // Set the environment variables for these to be able to push to artifactory.
                setUsername(System.getenv("JFROG_USER"))
                setPassword(System.getenv("JFROG_PASS"))
            }

            defaults {
                publications("ChartboostCoreRelease")
                setPublishArtifacts(true)
                setPublishPom(true)
            }
        }
    }

    afterEvaluate {
        publishing {
            publications {
                register<MavenPublication>("ChartboostCoreRelease") {
                    artifact("${project.buildDir}/outputs/aar/${project.name}-release.aar")

                    groupId = groupProjectID
                    artifactId = artifactProjectID
                    version = if (project.hasProperty("snapshot")) {
                        chartboostCoreVersion + rootProject.ext["SNAPSHOT"]
                    } else {
                        chartboostCoreVersion
                    }

                    pom {
                        name.set("Chartboost Core SDK")
                        description.set("Chartboost Core SDK")
                        url.set("https://www.chartboost.com/")

                        licenses {
                            license {
                                name.set("https://answers.chartboost.com/en-us/articles/200780239")
                            }
                        }

                        developers {
                            developer {
                                id.set("chartboostmobile")
                                name.set("chartboost mobile")
                                email.set("support@chartboost.com")
                            }
                        }

                        scm {
                            val gitUrl = "https://github.com/ChartBoost/chartboost-core-android-sdk/"
                            url.set(gitUrl)
                            connection.set(gitUrl)
                            developerConnection.set(gitUrl)
                        }
                    }
                }
            }
        }

        tasks.named<org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask>("artifactoryPublish") {
            publications(publishing.publications.getByName("ChartboostCoreRelease"))
        }
    }
}

task("ci") {
    // first, try things that fail quickly, like test and lint.
    // If those succeed, then try things that take longer, like assemble
    dependsOn(":ChartboostCore:clean")
    dependsOn(":CoreCanary:clean")
    dependsOn(":ChartboostCore:lint")
    dependsOn(":ChartboostCore:build")
    dependsOn(":CoreCanary:assembleLocal")
}
