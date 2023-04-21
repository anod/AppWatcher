
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes *Annotation*
-keep public class * extends java.lang.Exception

## Google Play Services 4.3.23 specific rules ##
## https://developer.android.com/google/play-services/setup.html#Proguard ##

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep class * extends info.anodsplace.framework.app.FragmentFactory

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn javax.annotation**

# Ignore legacy apache http lib
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**

# keyczar references gson and log4j
-dontwarn com.google.gson.**
#-keep class org.apache.commons.logging.**
-dontwarn org.apache.log4j.**

# kkep SerchView
-keep class android.support.v7.widget.SearchView { *; }

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# Fix OAuth Drive API failure for release builds
-keep class * extends com.google.api.client.json.GenericJson { *; }
-keep class com.google.api.services.drive.** { *; }
-keepclassmembers class * { @com.google.api.client.util.Key <fields>; }

# Protocol buffers
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
  <fields>;
}

-keep class finsky.protos.** { *; }