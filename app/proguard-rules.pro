# Proguard rules for LetsSign
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
# PDFBox rules
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
