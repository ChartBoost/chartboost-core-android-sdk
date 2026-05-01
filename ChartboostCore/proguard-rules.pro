# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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

-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient { *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info { *; }

-keep class kotlin.Metadata { *; }
-keepattributes Signature,Exceptions,InnerClasses,Deprecated,SourceFile,LineNumberTable,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault,KotlinMetadata
-keepattributes *Annotation*
-keep interface com.chartboost.core.network.ChartboostCoreApi { *; }
-keep class com.chartboost.core.network.ChartboostCoreNetworking { *; }
-keep class com.chartboost.core.network.** { *; }
-keep class com.chartboost.core.network.model.** { *; }
-keep class com.jakewharton.retrofit2.converter.kotlinx.serialization.** { *; }

# Keep all OkHttp3
-keep class okhttp3.** { *; }
# Keep necessary Retrofit2 classes
-keep class retrofit2.** { *; }
-keep class retrofit2.converter.** { *; }

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
 -keep,allowobfuscation,allowshrinking interface retrofit2.Call
 -keep,allowobfuscation,allowshrinking class retrofit2.Response

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-keep class com.chartboost.core.network.ChartboostCoreNetworking {
    private <fields>;
}
