# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep public class * extends androidx.*


-dontwarn javax.annotation.**

-dontwarn android.**
-dontwarn androidx.**
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }
-keep class androidx.** { *; }
-keep class android.support.v4.** { *; }
-keep class android.support.v7.** { *; }
-keep interface androidx.** { *; }

# Rename classes, package to single characters like as a, b, c ...
-repackageclasses ''

# Simplify the arithmetic that Dalvik 1.0 and 1.5 can not handle
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-assumenosideeffects class android.util.Log {
public static *** d(...);
public static *** v(...);
public static *** w(...);
public static *** e(...);
public static *** i(...);
}

-keepclasseswithmembernames class * {
 native <methods>;
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
 public static **[] values();
 public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
 public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
 public static <fields>;
}

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

-keep class com.x10.photo.maker.config.**{ *;}
-keepclassmembers class com.x10.photo.maker.data.** { <fields>; }

# Add the gson class
-keep public class com.google.gson.** { *; }
# Bouncy
-keep class org.bouncycastle.** { *; }
-keep class org.spongycastle.** { *; }

# Retrofit 2.X
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit.**
-dontwarn com.squareup.okhttp.**
-keep class retrofit.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions

-keep class org.apache.commons.logging.**               { *; }
-keep class org.codehaus.**                             { *; }

-keep class com.google.gson.stream.** { *; }

-keep class **$$Parcelable { *; }
-dontwarn java.lang.invoke.*

#Apache
-dontwarn org.apache.**
-keep class org.apache.http.** { *; }
-keep class org.apache.commons.logging.**               { *; }

### Kotlin
#https://stackoverflow.com/questions/33547643/how-to-use-kotlin-with-proguard
#https://medium.com/@AthorNZ/kotlin-metadata-jackson-and-proguard-f64f51e5ed32
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

### Kotlin Coroutine
# https://github.com/Kotlin/kotlinx.coroutines/blob/master/README.md
# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembernames class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
# https://github.com/Kotlin/kotlinx.atomicfu/issues/57
-dontwarn kotlinx.atomicfu.**

-dontwarn kotlinx.coroutines.flow.**


### Android Iconics
-keep class *.R
-keep class **.R$* {
    <fields>;
}

-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.* { *; }
-keep class javax.** {*;}
-keep class javax.swing.*
-keep class com.sun.** {*;}
-keep class myjava.** {*;}
-keep class org.*
-keep class java.beans.*
-dontwarn java.awt.**
-dontwarn javax.security.**

# https://github.com/dandar3/android-support-animated-vector-drawable/blob/master/proguard-project.txt
-keepclassmembers class android.support.graphics.drawable.VectorDrawableCompat$* {
   void set*(***);
   *** get*();
}

### Viewpager indicator
-dontwarn com.viewpagerindicator.**

-ignorewarnings

-keepattributes *Annotation*

###
#realm
-keepnames public class * extends io.realm.RealmObject
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.** { *; }
-dontwarn io.realm.**
