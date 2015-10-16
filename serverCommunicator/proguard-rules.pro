# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:/sdk/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepattributes Signature
-keep class com.doodeec.utils.network.* {
    public *;
}
-keep class com.doodeec.utils.network.listener.* {
    public *;
}

# GSON
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
-keep class com.google.gson.JsonSyntaxException
-keep class com.google.gson.Gson

# Gson specific classes
-keep class sun.misc.Unsafe { *; }

-dontwarn com.google.gson.**