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
-keep class com.doodeec.scom.BaseServerRequest {
    public *;
}
-keep class com.doodeec.scom.RequestType {
    public *;
}
-keep class com.doodeec.scom.CancellableServerRequest {
    public *;
}
-keep class com.doodeec.scom.ErrorType {
    public *;
}
-keep class com.doodeec.scom.ImageServerRequest {
    public *;
}
-keep class com.doodeec.scom.RequestError {
    public *;
}
-keep class com.doodeec.scom.ServerRequest {
    public *;
}
-keep class com.doodeec.scom.listener.BaseRequestListener {
    public *;
}
-keep class com.doodeec.scom.listener.JSONRequestListener {
    public *;
}
-keep class com.doodeec.scom.listener.ResponseListener {
    public *;
}
-keep class com.doodeec.scom.listener.SimpleResponseListener {
    public *;
}