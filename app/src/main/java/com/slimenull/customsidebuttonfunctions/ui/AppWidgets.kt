package com.slimenull.customsidebuttonfunctions.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.slimenull.customsidebuttonfunctions.AppBlueDark
import com.slimenull.customsidebuttonfunctions.IconBadge
import com.slimenull.customsidebuttonfunctions.AppBlueSoft
import com.slimenull.customsidebuttonfunctions.AppCardSurface
import com.slimenull.customsidebuttonfunctions.AppMuted
import com.slimenull.customsidebuttonfunctions.AppText
import com.slimenull.customsidebuttonfunctions.clickSound
import com.slimenull.customsidebuttonfunctions.isGlassStyle
import io.github.neoruaa.glasmio.icons.SfIcon
import io.github.neoruaa.glasmio.preference.GlasmioPreferenceGroupTitle
import io.github.neoruaa.glasmio.preference.GlasmioSwitchPreference
import io.github.neoruaa.glasmio.ui.GlasmioCard
import io.github.neoruaa.glasmio.ui.GlasmioListRow
import io.github.neoruaa.glasmio.ui.GlasmioSegmentedControl
import io.github.neoruaa.glasmio.ui.GlasmioSlider
import io.github.neoruaa.glasmio.ui.GlasmioSwitch
import io.github.neoruaa.glasmio.ui.GlasmioTopBar

/**
 * 按当前界面风格分流的通用组件。
 *
 * Material 3 风格走 Android 原生组件，iOS 液态玻璃风格走 Glasmio 组件。
 * 页面只调用这里的 App* 版本，切换风格时无需改动各页面。
 *
 * 注意：Glasmio 组件只能在 GlasmioTheme 内调用（依赖 LocalGlassColors 等），
 * 而 Material 分支不会执行到它们，因此两套分支互不干扰。
 */

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (isGlassStyle()) {
        GlasmioCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
            Column(Modifier.fillMaxWidth()) { content() }
        }
    } else {
        val shape = RoundedCornerShape(14.dp)
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = AppCardSurface),
            modifier = modifier.fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        ) {
            Column(Modifier.fillMaxWidth()) { content() }
        }
    }
}

/** 分组标题。Glasmio 下用 iOS 的 group title 语义样式。 */
@Composable
fun AppSectionLabel(text: String) {
    if (isGlassStyle()) {
        GlasmioPreferenceGroupTitle(title = text)
    } else {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
        )
    }
}

/** 导航用的设置行：标题 + 副标题 + 右箭头。 */
@Composable
fun AppSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector?,
    onClick: () -> Unit
) {
    val view = LocalView.current
    if (isGlassStyle()) {
        GlasmioListRow(
            title = title,
            subtitle = subtitle,
            detail = null,
            leading = if (icon != null) {
                { Icon(icon, contentDescription = null, modifier = Modifier.size(21.dp)) }
            } else null,
            onClick = { clickSound(view); onClick() },
            showTopSeparator = false
        )
    } else {
        val shape = RoundedCornerShape(14.dp)
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = AppCardSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    IconBadge(icon)
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AppMuted)
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = AppMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/** 带开关的设置行。 */
@Composable
fun AppSwitchRow(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val view = LocalView.current
    if (isGlassStyle()) {
        GlasmioSwitchPreference(
            title = { Text(title) },
            description = description,
            checked = checked,
            onCheckedChange = { clickSound(view); onCheckedChange(it) },
            showTopSeparator = false
        )
    } else {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title)
                if (!description.isNullOrBlank()) {
                    Text(description, style = MaterialTheme.typography.bodySmall, color = AppMuted)
                }
            }
            Switch(checked, { clickSound(view); onCheckedChange(it) })
        }
    }
}

/** 分段选择器。items 为「值 + 显示名」对。 */
@Composable
fun <T> AppSegmented(
    items: List<Pair<T, String>>,
    selected: T?,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    if (isGlassStyle()) {
        GlasmioSegmentedControl(
            items = items,
            selected = selected,
            onSelected = { clickSound(view); onSelected(it) },
            modifier = modifier.fillMaxWidth(),
            height = 36.dp
        )
    } else {
        androidx.compose.material3.SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
            items.forEachIndexed { index, (value, label) ->
                androidx.compose.material3.SegmentedButton(
                    selected = selected == value,
                    onClick = { clickSound(view); onSelected(value) },
                    shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(
                        index, items.size, baseShape = RoundedCornerShape(8.dp)
                    ),
                    colors = androidx.compose.material3.SegmentedButtonDefaults.colors(
                        activeContainerColor = AppBlueSoft,
                        activeContentColor = AppBlueDark,
                        activeBorderColor = MaterialTheme.colorScheme.primary,
                        inactiveContainerColor = AppCardSurface,
                        inactiveContentColor = AppText,
                        inactiveBorderColor = MaterialTheme.colorScheme.outline
                    )
                ) { Text(label) }
            }
        }
    }
}

@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (isGlassStyle()) {
        GlasmioSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished,
            modifier = modifier
        )
    } else {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished,
            modifier = modifier
        )
    }
}

@Composable
fun AppSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val view = LocalView.current
    if (isGlassStyle()) {
        GlasmioSwitch(checked = checked, onCheckedChange = { clickSound(view); onCheckedChange(it) })
    } else {
        Switch(checked, { clickSound(view); onCheckedChange(it) })
    }
}

/** 详情页顶部的返回标题栏。 */
@Composable
fun AppBackTitle(title: String, back: () -> Unit) {
    val view = LocalView.current
    if (isGlassStyle()) {
        GlasmioTopBar(
            title = title,
            navigation = {
                GlasmioNavigationIcon(onClick = { clickSound(view); back() })
            }
        )
    } else {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { clickSound(view); back() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = AppText
                )
            }
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GlasmioNavigationIcon(onClick: () -> Unit) {
    androidx.compose.material3.IconButton(onClick = onClick) {
        SfIcon("chevron.left", contentDescription = null, size = 20.dp)
    }
}

/** 次级/描边按钮。Glasmio 下用玻璃按钮，Material 下用 OutlinedButton。 */
@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    if (isGlassStyle()) {
        GlasmioButton(onClick = onClick, modifier = modifier) { content() }
    } else {
        androidx.compose.material3.OutlinedButton(onClick = onClick, modifier = modifier, content = content)
    }
}
