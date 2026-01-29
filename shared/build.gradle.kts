plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kover)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.coroutines.core)
                implementation(libs.napier)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)

                // Lifecycle ViewModel for KMP
                implementation(libs.androidx.lifecycle.viewmodel)

                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // Navigation Compose (Jetpack)
                implementation(libs.compose.navigation)

                // Koin Compose Multiplatform
                implementation(libs.koin.compose.multiplatform)

                // GitLive Firebase KMP SDK
                implementation(libs.gitlive.firebase.auth)
                implementation(libs.gitlive.firebase.database)

                // KMPAuth for Google Sign-In
                implementation(libs.kmpauth.google)
                implementation(libs.kmpauth.firebase)
                implementation(libs.kmpauth.uihelper)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.coroutines.test)
                implementation(libs.turbine)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.android)
                implementation(libs.koin.android)
                implementation(libs.coroutines.android)
                // Android-only: Lifecycle ViewModel Compose
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                // Android-only: UI Tooling Preview for @Preview annotation
                implementation(libs.compose.ui.tooling.preview)
                // WorkManager for notifications
                implementation("androidx.work:work-runtime-ktx:2.9.0")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.sqldelight.native)
                implementation(libs.stately.common)
            }
        }
    }
}

android {
    namespace = "com.bikemanager.shared"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

sqldelight {
    databases {
        create("BikeManagerDatabase") {
            packageName.set("com.bikemanager.data.local")
        }
    }
}

// Kover configuration commented out - needs update for new Kover version
// koverReport {
//     filters {
//         excludes {
//             // Exclude generated code and DI
//             packages(
//                 "com.bikemanager.data.local.shared",
//                 "com.bikemanager.di"
//             )
//             classes(
//                 // SQLDelight generated
//                 "*Database*Impl*",
//                 "*Queries*",
//                 // Koin modules
//                 "*Module*"
//             )
//         }
//     }
//     defaults {
//         html {
//             onCheck = false
//         }
//         xml {
//             onCheck = false
//         }
//     }
// }
