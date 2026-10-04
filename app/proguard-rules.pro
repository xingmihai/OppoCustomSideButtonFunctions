# Xposed 通过 java_init.list 按全类名查找入口，入口及 hook 逻辑所在包必须原样保留
-keep class com.slimenull.customsidebuttonfunctions.xposed.** { *; }

# 设置读写与 libxposed service 回调位于 data 包，跨进程读取，不参与混淆
-keep class com.slimenull.customsidebuttonfunctions.data.** { *; }

# 模型对象在模块与应用间共享，字段按名访问
-keep class com.slimenull.customsidebuttonfunctions.model.** { *; }

# Manifest 中声明的组件（Application / Activity / Service / Receiver）由系统按名实例化
-keep class com.slimenull.customsidebuttonfunctions.CustomSideButtonApplication { *; }
-keep class com.slimenull.customsidebuttonfunctions.MainActivity { *; }
-keep class com.slimenull.customsidebuttonfunctions.RootCommandTrampolineActivity { *; }
-keep class com.slimenull.customsidebuttonfunctions.RootCommandService { *; }
-keep class com.slimenull.customsidebuttonfunctions.RootCommandReceiver { *; }

# libxposed service 在运行时被反射调用
-keep class io.github.libxposed.service.** { *; }
-keep class io.github.libxposed.api.** { *; }

# Kotlin 元数据：R8 处理 Kotlin 反射与内联需要
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault, Signature, InnerClasses, EnclosingMethod

# 反射实例化（Class.forName）的目标类不被移除
-keep class android.app.ActivityThread { *; }
-keep class com.android.internal.util.ScreenshotHelper { *; }

# 移除日志噪音，保留必要信息
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
