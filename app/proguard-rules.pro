# ============================================================================
# ProGuard/R8 rules for QuranEngine Android
# ============================================================================

# ---- Standard Android / Kotlin ----

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# Keep classes annotated with @Keep
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# ---- Kotlin ----

# Kotlin Metadata (needed for reflection-based libraries)
-keep class kotlin.Metadata { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ---- Hilt / Dagger ----

# Hilt-generated components and entry points
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.* <*>;
}
-keepclasseswithmembers class * {
    @dagger.* <*>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <*>;
}

# Keep @HiltAndroidApp, @AndroidEntryPoint, @HiltViewModel annotated classes
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep @dagger.hilt.android.lifecycle.HiltViewModel class *

# ---- Room (annotation-persistence & batch-downloader) ----

# Room entities and DAOs
-keep class com.quranengine.data.annotation.entity.** { *; }
-keep class com.quranengine.data.annotation.dao.** { *; }
-keep class com.quranengine.data.batchdownloader.db.** { *; }

# Room-generated _Impl classes
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    abstract <methods>;
}
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ---- Ktor + OkHttp ----

# Ktor engine internals accessed via reflection
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# OkHttp
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---- Kotlin Serialization ----

# Keep @Serializable classes and their generated serializers
-keepattributes RuntimeVisibleAnnotations

-if @kotlinx.serialization.Serializable class **
-keep class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static ** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

-keepnames class <1>$$serializer {
    static <1>$$serializer INSTANCE;
}

# ---- Model enums ----

-keepclassmembers enum com.quranengine.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
}

# ---- Media3 / ExoPlayer ----

# Media3 uses ServiceLoader; keep service implementations
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }
-dontwarn androidx.media3.**

# ---- Timber (strip logs in release) ----

-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}

-assumenosideeffects class timber.log.Timber$Tree {
    public *** d(...);
    public *** v(...);
    public *** i(...);
    public *** w(...);
    public *** e(...);
    public *** wtf(...);
}

# ---- Jetpack Compose ----

# Compose compiler handles most needs; keep Composable metadata for tooling
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# ---- General safety nets ----

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Serializable classes (Java)
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
