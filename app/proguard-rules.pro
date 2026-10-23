# Shizuku rules
-keep class rikka.shizuku.** { *; }
-keep interface rikka.shizuku.** { *; }

# VectorPatch AIDL & services
-keep class dev.vectorpatch.service.** { *; }
-keep interface dev.vectorpatch.service.** { *; }

# Serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Compose
-keep class androidx.compose.material.icons.** { *; }
