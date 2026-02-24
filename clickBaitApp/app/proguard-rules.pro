# Add project specific ProGuard rules here.
-keep class com.magicteamdev0.viralclicker.** { *; }
-keepclassmembers class com.magicteamdev0.viralclicker.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Hilt
-keep class dagger.hilt.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }

# Play Billing
-keep class com.android.billingclient.** { *; }
