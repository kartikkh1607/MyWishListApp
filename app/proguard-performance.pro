# Performance Optimization Rules for MyWishListApp
# These rules improve app performance by aggressive optimization

# Enable optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove debug information for smaller APK
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimize collections
-optimizations !code/simplification/advanced

# Keep Room database optimization
-keep class com.example.mywishlistapp.Data.** { *; }
-keep class androidx.room.** { *; }

# Keep Jetpack Compose performance annotations
-keep class androidx.compose.runtime.** { *; }
-keep @androidx.compose.runtime.Stable class *
-keep @androidx.compose.runtime.Immutable class *

# Optimize Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Memory and code optimization is handled by build.gradle
# -shrinkresources and -dontshrink are not valid in ProGuard files

# Performance: Remove reflection calls where possible
-assumenosideeffects class java.lang.reflect.** {
    public static *** *(...);
}
