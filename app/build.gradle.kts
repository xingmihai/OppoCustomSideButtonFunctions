import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val signingProperties = Properties().apply {
    val propertiesFile = rootProject.file("keystore/keystore.properties")
    if (propertiesFile.isFile) propertiesFile.inputStream().use(::load)
}

// 版本号由 git 推导，无需每次发版手动修改：
//   versionCode = 提交总数（单调递增）
//   versionName = 最近一个 v* 标签，落后则追加 +N，无标签则回退 0.1.<提交数>
// 注意：浅克隆会算错提交数，CI 里 checkout 必须带 fetch-depth: 0。
val providerFactory = providers
fun gitOutput(vararg args: String): String? = runCatching {
    val result = providerFactory.exec {
        commandLine("git", *args)
        isIgnoreExitValue = true
    }
    if (result.result.get().exitValue == 0) {
        result.standardOutput.asText.get().trim().takeIf { it.isNotBlank() }
    } else {
        null
    }
}.getOrNull()

// 取不到 git 信息时（例如直接下载源码压缩包构建）的兜底值
val fallbackVersionCode = 11

val commitCount = gitOutput("rev-list", "--count", "HEAD")?.toIntOrNull()
    ?: fallbackVersionCode
// 不带 --dirty：CI 中恢复签名文件会让工作区变“脏”，版本号不该因此带上脏标记
val describe = gitOutput("describe", "--tags", "--match", "v*", "--long", "--always")

val resolvedVersionName = run {
    if (describe == null) return@run "0.1.$commitCount"
    val tagged = Regex("""^v?(\d+(?:\.\d+)*)-(\d+)-g[0-9a-f]+$""").matchEntire(describe)
    when {
        tagged != null -> tagged.groupValues[1] +
            if (tagged.groupValues[2] != "0") "+${tagged.groupValues[2]}" else ""
        describe.matches(Regex("""^v?\d+(?:\.\d+)*$""")) -> describe.removePrefix("v")
        else -> "0.1.$commitCount"
    }
}

android {
    namespace = "com.slimenull.customsidebuttonfunctions"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.slimenull.customsidebuttonfunctions"
        minSdk = 26
        targetSdk = 35
        versionCode = commitCount
        versionName = resolvedVersionName
    }

    signingConfigs {
        create("release") {
            val storePath = signingProperties.getProperty("storeFile")
            if (!storePath.isNullOrBlank()) {
                storeFile = rootProject.file(storePath)
                storePassword = signingProperties.getProperty("storePassword")
                keyAlias = signingProperties.getProperty("keyAlias")
                keyPassword = signingProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
        aidl = true
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.runtime:runtime:1.8.0")
    implementation("androidx.compose.ui:ui:1.8.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.8.0")
    implementation("androidx.compose.foundation:foundation:1.8.0")
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.8.0")

    // Xposed/LSPosed supplies these classes at runtime; they must not be packaged in the APK.
    compileOnly("io.github.libxposed:api:102.0.0")
    implementation("io.github.libxposed:service:102.0.0")

    // Shizuku is used from the application process before falling back to the existing su path.
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
}

// 供 CI 读取真实版本号，避免在工作流里重复推演一遍
tasks.register("printVersionInfo") {
    doLast {
        println("VERSION_NAME=$resolvedVersionName")
        println("VERSION_CODE=$commitCount")
    }
}
