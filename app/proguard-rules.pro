# Room Database keep rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * {
    @androidx.room.Insert <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
    @androidx.room.Query <methods>;
}

# Keep Model & Entity POJOs (used by Room, Firestore, and reflection)
-keep class com.example.vocabmaster.model.entity.** { *; }
-keep class com.example.vocabmaster.model.dao.** { *; }
-keep class com.example.vocabmaster.model.database.** { *; }

# Firebase Auth & Cloud Firestore
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# AndroidX Lifecycle & View
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
