# === ROOM ===
# Keep entities
-keep class com.example.focusflow.data.model.** { *; }

# Keep DAO interfaces (методы используются через интерфейсы)
-keep interface com.example.focusflow.data.**Dao { *; }
-keep interface com.example.focusflow.data.**Dao_* { *; }
-keepclassmembers interface * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Room internals
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# === DATA CLASSES (используются в Flow и reflection-подобных операциях) ===
-keep class com.example.focusflow.viewmodel.**State { *; }
-keep class com.example.focusflow.viewmodel.** { *; }
-keep class com.example.focusflow.utils.** { *; }

# === COROUTINES ===
-keepnames @kotlinx.coroutines.internal.CoroutineExceptionHandler class *
-keepclassmembers class kotlinx.** { *; }
-dontwarn kotlinx.coroutines.flow.**

# === ENUMS (SessionType, Categories) ===
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# === COMPOSE ===
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# === KOTLIN REFLECTION (для derivedStateOf и т.п.) ===
-keep class kotlin.Metadata { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# === KEEP GENERICS SIGNATURES (важно для Flow<T>) ===
-keepattributes Signature

# === DATASTORE ===
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# === DEBUG: показать что удалил R8 (раскомментируй для отладки) ===
# -printusage unused.txt
# -printmapping mapping.txt