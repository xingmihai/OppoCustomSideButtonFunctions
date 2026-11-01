package com.slimenull.customsidebuttonfunctions.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDark
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLight
import com.slimenull.customsidebuttonfunctions.appColorScheme
import com.slimenull.customsidebuttonfunctions.model.ThemeMode

/**
 * Miuix 与 Material 3 的取色桥接。
 *
 * MiuixTheme 只提供自己的 LocalColors / LocalTextStyles，不提供 MaterialTheme，
 * 而项目里的语义色（AppText / AppCardSurface 等）读的是 [androidx.compose.material3.MaterialTheme]。
 * 因此这里做两件事：
 * 1. 用与 Material 分支相同的基准色构造 Miuix 的 [Colors]，使动态取色对两种风格同时生效；
 * 2. 再由 Miuix 的 [Colors] 反推一份 Material [ColorScheme]，让语义色与 Miuix 组件保持一致。
 *
 * 主题嵌套顺序为 MaterialTheme → MiuixTheme，这样 Miuix 的 LocalContentColor 生效，
 * 语义色也能正确解析。
 */

@Composable
internal fun miuixColors(mode: ThemeMode, dynamicColor: Boolean, dark: Boolean): Colors {
    // 复用 Material 分支的基准色板：动态取色时用系统壁纸色，否则用品牌蓝。
    // 这样「使用壁纸主色」开关对 Miuix 同样生效。
    val base: ColorScheme = appColorScheme(mode, dynamicColor)
    return if (dark) {
        miuixDark(
            primary = base.primary,
            primaryVariant = base.primary,
            primaryContainer = base.primary,
            onPrimaryContainer = base.onPrimary,
            tertiaryContainer = base.primaryContainer,
            onTertiaryContainer = base.primary
        )
    } else {
        miuixLight(
            primary = base.primary,
            primaryVariant = base.primary,
            primaryContainer = base.primary,
            onPrimaryContainer = base.onPrimary,
            tertiaryContainer = base.primaryContainer,
            onTertiaryContainer = base.primary
        )
    }
}

/** 由 Miuix 配色反推 Material 配色，供项目内既有的语义色使用。 */
internal fun miuixToMaterial(c: Colors, dark: Boolean): ColorScheme {
    val fallback = if (dark) darkColorScheme() else lightColorScheme()
    return fallback.copy(
        primary = c.primary,
        onPrimary = c.onPrimary,
        primaryContainer = c.tertiaryContainer,
        onPrimaryContainer = c.onTertiaryContainer,
        secondary = c.secondary,
        onSecondary = c.onSecondary,
        secondaryContainer = c.secondaryContainer,
        onSecondaryContainer = c.onSecondaryContainer,
        background = c.background,
        onBackground = c.onBackground,
        surface = c.surface,
        onSurface = c.onSurface,
        surfaceVariant = c.surfaceVariant,
        onSurfaceVariant = c.onBackgroundVariant,
        outline = c.outline,
        outlineVariant = c.dividerLine,
        surfaceContainerLowest = c.surfaceVariant,
        surfaceContainerLow = c.surfaceContainer,
        surfaceContainer = c.surfaceContainer,
        surfaceContainerHigh = c.surfaceContainerHigh,
        surfaceContainerHighest = c.surfaceContainerHighest,
        error = c.error,
        onError = c.onError,
        errorContainer = c.errorContainer,
        onErrorContainer = c.onErrorContainer
    )
}
