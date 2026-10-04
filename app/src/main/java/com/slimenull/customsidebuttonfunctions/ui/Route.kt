package com.slimenull.customsidebuttonfunctions.ui

import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavKey

/** 页面键：每个目的地都是一个 NavKey，可被 miuix-nav 的返回栈保存与恢复。 */
@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Home : Route

    @Serializable
    data object Other : Route

    @Serializable
    data object About : Route

    @Serializable
    data object Morse : Route

    @Serializable
    data object Feedback : Route

    @Serializable
    data object Advanced : Route

    @Serializable
    data class Gesture(val kind: GestureKind) : Route
}

/** 侧键手势类型。 */
@Serializable
enum class GestureKind(val title: String, val description: String) {
    SINGLE("单击", "按下并松开后立即触发。"),
    DOUBLE("双击", "连续两次按下并松开，两次之间的间隔不能超过设定的等待时间。"),
    LONG("长按", "按住并达到指定的按下时长后触发，触发后不会再触发单击或双击。")
}
