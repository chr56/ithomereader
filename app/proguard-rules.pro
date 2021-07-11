-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keepattributes RuntimeVisibleAnnotations,
                RuntimeVisibleParameterAnnotations,
                RuntimeVisibleTypeAnnotations,
                AnnotationDefault

-keep class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking @interface *

-keep,allowobfuscation,allowoptimization class me.ikirby.ithomereader.clientapi.** { *; }
-keep,allowobfuscation,allowoptimization class me.ikirby.ithomereader.entity.** { *; }
