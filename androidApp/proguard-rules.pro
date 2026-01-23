# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Koin
-keepclassmembers class * extends org.koin.core.scope.Scope { *; }
-keep class org.koin.** { *; }

# Keep SQLDelight
-keep class app.cash.sqldelight.** { *; }
-keep class com.bikemanager.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes Signature

# Keep data classes
-keepclassmembers class * {
    @kotlin.Metadata *;
}

# Keep Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Keep Napier logging
-keep class io.github.aakira.napier.** { *; }
