package com.slimenull.customsidebuttonfunctions.ui

import android.content.Context

/**
 * 仅影响 App 自身界面的外观设置。
 *
 * 与 [com.slimenull.customsidebuttonfunctions.model.AppSettings] 严格分开：后者会被序列化成
 * 快照文件供 system_server 中的 Xposed 模块读取，界面外观对它毫无意义，混进去只会让模块
 * 读到一堆用不上的字段。
 */
data class UiSettings(
    /**
     * 启用模糊效果（需要设备支持 RuntimeShader）。
     *
     * 默认关闭：模糊依赖 RuntimeShader 采集背景图层，不同机型/系统版本的 GPU 路径差异较大。
     * 默认关闭可保证冷启动一定成功，用户可以自行开启；一旦开启后反复闪退，
     * 在「设置 → 应用管理」中清除本应用数据即可回到关闭状态。
     */
    val enableBlur: Boolean = false,
    /** 顶部应用栏模糊样式：0 = Gaussian，1 = Progressive。 */
    val blurStyle: Int = 0,
    /** 使用悬浮导航栏替代贴底导航栏。 */
    val useFloatingNavigationBar: Boolean = false,
    /** 悬浮导航栏样式：0 = Default，1 = Glass。 */
    val floatingNavigationBarStyle: Int = 0,
    /** 悬浮导航栏位置：0 = Center，1 = Start，2 = End。 */
    val floatingNavigationBarPosition: Int = 0
)

/** 悬浮导航栏水平对齐。与 miuix 官方示例的 FloatingNavigationBarAlignment 一致。 */
enum class FloatingBarAlignment(val value: Int) {
    Center(0),
    Start(1),
    End(2),
    ;

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: Center
    }
}

/** 界面设置的本地持久化，走独立的 SharedPreferences。 */
object UiSettingsStore {
    private const val PREFS_NAME = "ui_settings"
    private const val KEY_ENABLE_BLUR = "enable_blur"
    private const val KEY_BLUR_STYLE = "blur_style"
    private const val KEY_USE_FLOATING_NAV = "use_floating_navigation_bar"
    private const val KEY_FLOATING_NAV_STYLE = "floating_navigation_bar_style"
    private const val KEY_FLOATING_NAV_POSITION = "floating_navigation_bar_position"

    fun load(context: Context): UiSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return UiSettings(
            enableBlur = prefs.getBoolean(KEY_ENABLE_BLUR, false),
            blurStyle = prefs.getInt(KEY_BLUR_STYLE, 0),
            useFloatingNavigationBar = prefs.getBoolean(KEY_USE_FLOATING_NAV, false),
            floatingNavigationBarStyle = prefs.getInt(KEY_FLOATING_NAV_STYLE, 0),
            floatingNavigationBarPosition = prefs.getInt(KEY_FLOATING_NAV_POSITION, 0)
        )
    }

    fun save(context: Context, settings: UiSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLE_BLUR, settings.enableBlur)
            .putInt(KEY_BLUR_STYLE, settings.blurStyle)
            .putBoolean(KEY_USE_FLOATING_NAV, settings.useFloatingNavigationBar)
            .putInt(KEY_FLOATING_NAV_STYLE, settings.floatingNavigationBarStyle)
            .putInt(KEY_FLOATING_NAV_POSITION, settings.floatingNavigationBarPosition)
            .apply()
    }
}
