# Add project specific ProGuard rules here.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*