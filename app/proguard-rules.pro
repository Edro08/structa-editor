# ProGuard / R8 rules for Structa
# Keep enum names and members used for persistence in SharedPreferences and Draft headers
-keepnames enum com.edro08.structa.**
-keepclassmembers enum com.edro08.structa.** {
    *;
}
