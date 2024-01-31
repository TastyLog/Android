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


#-keep interface com.adam.tastylog.* { *; }
#-keep class com.adam.tastylog.* { *; }

# ProGuard Rules for Kakao SDK
-keep class com.kakao.sdk.**.model.* { <fields>; }
-keep class * extends com.google.gson.TypeAdapter
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.**

# Retrofit and OkHttp
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.squareup.okhttp3.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okio.**

# Moshi
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Android YouTube Player
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }

# CircleImageView
-keep class de.hdodenhof.circleimageview.** { *; }

# Google Play Services (for Location)
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Lottie
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# Gson
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Glide
-keep class com.bumptech.glide.** { *; }
-keep class * extends com.bumptech.glide.module.AppGlideModule
-keep class * extends com.bumptech.glide.module.LibraryGlideModule
-keep public class * extends com.bumptech.glide.module.GlideModule
-dontwarn com.bumptech.glide.**

# Retrofit Gson Converter
-keep class retrofit2.converter.gson.** { *; }
-dontwarn retrofit2.converter.gson.**

# OkHttp Logging Interceptor
-keep class com.squareup.okhttp3.logging.** { *; }
-dontwarn com.squareup.okhttp3.logging.**

# General rules to avoid obfuscation of classes used in serialization and deserialization
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod