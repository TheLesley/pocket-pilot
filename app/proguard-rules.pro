# PocketPilot R8 / ProGuard rules
#
# The `proguard-android-optimize.txt` base file already covers the Android
# framework and standard Kotlin runtime. Everything below is scoped to the
# specific reflection-based libraries this app ships with.

# Preserve stack traces for crash reporting.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- kotlinx.serialization -------------------------------------------------
# @Serializable classes are constructed reflectively via their generated
# companion `Companion` / `$serializer` objects, so R8 must not rename them.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.example.pocketpilot.**$$serializer { *; }
-keepclassmembers class com.example.pocketpilot.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.pocketpilot.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Retrofit / OkHttp -----------------------------------------------------
# Retrofit uses runtime annotations and reflection over interface signatures.
-keepattributes Signature, Exceptions, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# OkHttp / Okio ship their own consumer rules but this suppresses noisy notes.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Room ------------------------------------------------------------------
# Room ships consumer ProGuard rules; keep DAO interfaces to be safe.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# --- Compose ---------------------------------------------------------------
# Compose ships its own rules; keep Composer-generated helpers just in case.
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# --- App entry points ------------------------------------------------------
# The Application class is referenced from the manifest and must not be renamed.
-keep class com.example.pocketpilot.PocketPilotApplication { *; }

# Baseline profile follow-up
# ---------------------------
# A `:baselineprofile` benchmark module (com.android.baselineprofile plugin +
# macrobenchmark tests) is the recommended next step to squeeze cold-start and
# scroll performance further. Not enabled here because it requires a separate
# module and a hardware device / managed AVD to generate the profile.
