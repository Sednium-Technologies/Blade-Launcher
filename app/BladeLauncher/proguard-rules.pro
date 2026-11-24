-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn com.github.luben.zstd.**
-dontwarn java.lang.management.**
-dontwarn io.ktor.util.debug.**

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# SDL
-keep class org.libsdl.app.** { *; }

# Launcher
-keep class org.lwjgl.glfw.CallbackBridge {
    *;
}
-keep class com.oracle.dalvik.VMLauncher {
    *;
}

#
## Hilt
#-keep class dagger.hilt.** { *; }
#-keep class javax.inject.** { *; }
#-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
#-keepclasseswithmembers class * {
#    @dagger.hilt.* <methods>;
#}

# Prevent R8 from over-optimizing constructors (causes StackOverflow with Hilt + proguard-android-optimize.txt)
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.internal.GeneratedEntryPoint <init>(...);
}
-keep,allowobfuscation @dagger.hilt.android.AndroidEntryPoint class *


-keep class com.sednium.bladelauncher.bridge.** { *; }
-keep class com.sednium.bladelauncher.utils.device.VulkanChecker {
    *;
}
-keep class com.sednium.bladelauncher.utils.device.VulkanCapabilities {
    *;
}
-keep interface com.sednium.bladelauncher.utils.device.VulkanLogCallback {
    *;
}
-keep class com.sednium.bladelauncher.game.input.CriticalNativeTest {
    *;
}

# Libraries
-keep class com.github.steveice10.opennbt.** { *; }

# Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep classes and members annotated with @Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# MultiRT / Runtime models and managers
-keep class com.sednium.bladelauncher.game.multirt.** { *; }
-keepclassmembers class com.sednium.bladelauncher.game.multirt.** { *; }
-keep class com.sednium.bladelauncher.components.jre.** { *; }
-keepclassmembers class com.sednium.bladelauncher.components.jre.** { *; }

# Version, manifest, and download JSON models
-keep class com.sednium.bladelauncher.game.versioninfo.models.** { *; }
-keepclassmembers class com.sednium.bladelauncher.game.versioninfo.models.** { *; }
-keep class com.sednium.bladelauncher.game.version.installed.** { *; }
-keepclassmembers class com.sednium.bladelauncher.game.version.installed.** { *; }
-keep class com.sednium.bladelauncher.game.download.** { *; }
-keepclassmembers class com.sednium.bladelauncher.game.download.** { *; }

# Parcelable preservation
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
-keep class com.sednium.bladelauncher.ui.activities.JvmCrash { *; }