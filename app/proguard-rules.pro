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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile


-keep class androidx.appcompat.widget.** { *; }
-dontwarn com.videotrimmer.library**
-keep class com.videotrimmer.library** { *; }
-keep interface com.videotrimmer.library** { *; }
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl { *; }


#-injars      bin/classes
#-injars      libs
-outjars     bin/classes-processed.jar
-dontpreverify
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic
-keepattributes *Annotation*
-dontskipnonpubliclibraryclasses
-optimizationpasses 5
-printmapping map.txt
-flattenpackagehierarchy

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
#-keep public class * extends android.app.MapActivity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

#-libraryjars  libs/commons-io-2.2.jar
#-libraryjars  libs/ftp4j-1.7.1.jar
#-libraryjars  libs/gson-2.2.2.jar

-keep public class org.apache.commons.io.**
-keep public class it.sauronsoftware.ftp4j.**
-keep public class com.google.gson.**

-keep public class com.mypackagename.ActivityMonitor$*

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}


-keepattributes Signature
# POJOs used with GSON
# The variable names are JSON key values and should not be obfuscated
-keepclassmembers class com.meetsportal.meets.networking.registration.* { <fields>; }
# You can apply the rule to all the affected classes also
# -keepclassmembers class com.example.apps.android.model.** { <fields>; }

-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

#crouserView
-keep class com.synnapps.carouselview.** { *; }

-keep class com.google.maps.** { *; }

#-keep class com.meetsportal.meets.networking.meetup.ChatDM
#-keep class com.meetsportal.meets.models.MeetFireData

