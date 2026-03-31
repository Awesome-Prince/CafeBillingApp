# Add project specific ProGuard rules here.

# Keep Room entity classes
-keep class com.cafe.billing.data.models.** { *; }

# Keep Gson model classes (needed for JSON serialization of CartItem)
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Hilt
-dontwarn dagger.hilt.**
