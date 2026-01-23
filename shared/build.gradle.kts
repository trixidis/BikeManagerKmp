plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kover)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
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
}

sqldelight {
    databases {
        create("BikeManagerDatabase") {
            packageName.set("com.bikemanager.data.local")
        }
    }
}

koverReport {
    filters {
        excludes {
            // Exclude generated code and DI
            packages(
                "com.bikemanager.data.local.shared",
                "com.bikemanager.di"
            )
            classes(
                // SQLDelight generated
                "*Database*Impl*",
                "*Queries*",
                // Koin modules
                "*Module*"
            )
        }
    }
    defaults {
        html {
            onCheck = false
        }
        xml {
            onCheck = false
        }
    }
}
