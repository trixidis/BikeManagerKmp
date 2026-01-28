import java.util.Properties
import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.google.services)
}

/**
 * Extracts the Web Client ID (OAuth client_type: 3) from a google-services.json file.
 */
fun getWebClientId(buildType: String): String {
    val googleServicesFile = file("src/$buildType/google-services.json")
    if (!googleServicesFile.exists()) {
        return "MISSING_GOOGLE_SERVICES_JSON"
    }

    val json = JsonSlurper().parseText(googleServicesFile.readText()) as Map<*, *>
    val clients = json["client"] as? List<*> ?: return "NO_CLIENT_FOUND"

    for (client in clients) {
        val clientMap = client as? Map<*, *> ?: continue
        val oauthClients = clientMap["oauth_client"] as? List<*> ?: continue

        for (oauth in oauthClients) {
            val oauthMap = oauth as? Map<*, *> ?: continue
            if (oauthMap["client_type"] == 3) {
                return oauthMap["client_id"] as? String ?: continue
            }
        }
    }
    return "WEB_CLIENT_ID_NOT_FOUND"
}

android {
    namespace = "com.bikemanager.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bikemanager.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 7
        versionName = "1.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(keystorePropertiesFile.inputStream())

                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            // Web Client ID extracted from src/debug/google-services.json
            buildConfigField("String", "WEB_CLIENT_ID", "\"${getWebClientId("debug")}\"")
        }
        release {
            // Web Client ID extracted from src/release/google-services.json
            buildConfigField("String", "WEB_CLIENT_ID", "\"${getWebClientId("release")}\"")

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigningConfig = signingConfigs.findByName("release")
            if (releaseSigningConfig?.storeFile != null) {
                signingConfig = releaseSigningConfig
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":shared"))

    // AndroidX Core
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    debugImplementation(libs.compose.ui.tooling)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Coil
    implementation(libs.coil.compose)

    // Firebase - Commented out as they are already in shared module via GitLive
    // implementation(platform(libs.firebase.bom))
    // implementation(libs.firebase.auth)
    // implementation(libs.firebase.database)
    implementation(libs.play.services.auth)

    // Coroutines
    implementation(libs.coroutines.android)

    // Napier
    implementation(libs.napier)

    // KMPAuth
    implementation(libs.kmpauth.google)
    implementation(libs.kmpauth.firebase)
    implementation(libs.kmpauth.uihelper)

    // Testing
    testImplementation(libs.junit)
}
