# Xposed discovers the entry point by name from res/values/strings.xml.
-keep class com.slimenull.customsidebuttonfunctions.xposed.** { *; }

# Shizuku 用 ComponentName 在独立进程里按类名实例化 UserService，不能被改名
-keep class com.slimenull.customsidebuttonfunctions.ShizukuShellUserService { *; }

# AIDL：跨进程按接口描述符调用，Stub/Proxy 的 onTransact 必须保留
-keep class com.slimenull.customsidebuttonfunctions.IShizukuShellService { *; }
-keep class com.slimenull.customsidebuttonfunctions.IShizukuShellService$Stub { *; }
-keep class com.slimenull.customsidebuttonfunctions.IShizukuShellService$Default { *; }
-keep class com.slimenull.customsidebuttonfunctions.IShizukuShellCallback { *; }
-keep class com.slimenull.customsidebuttonfunctions.IShizukuShellCallback$Stub { *; }
-keep class com.slimenull.customsidebuttonfunctions.IShizukuShellCallback$Default { *; }

# 设置以 JSON 存取，枚举按名字读写，保留 values() / valueOf()
-keepclassmembers enum com.slimenull.customsidebuttonfunctions.model.** { *; }

# libxposed API 由宿主（LSPosed）在运行时提供，构建期是 compileOnly
-dontwarn io.github.libxposed.**
