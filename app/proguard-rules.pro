# kotlinx-serialization generates serializer classes that R8 must preserve.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class com.sysadmindoc.guitartuner.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.sysadmindoc.guitartuner.**$$serializer { *; }
-keepclassmembers class com.sysadmindoc.guitartuner.** {
    *** Companion;
}
