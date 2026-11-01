package com.slimenull.customsidebuttonfunctions.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import io.github.neoruaa.glasmio.ui.GlasmioPopUpButton
import com.slimenull.customsidebuttonfunctions.AppBlue
import com.slimenull.customsidebuttonfunctions.AppSubtleSurface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import io.github.neoruaa.glasmio.ui.GlasmioButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import android.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import io.github.neoruaa.glasmio.core.GlasmioTheme
import io.github.neoruaa.glasmio.ui.GlasmioAlertButtonRole
import io.github.neoruaa.glasmio.ui.GlasmioAlertButtonSpec
import io.github.neoruaa.glasmio.ui.GlasmioAlertDialog
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import io.github.neoruaa.glasmio.ui.GlasmioTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
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
/** [modifier] 在与按钮同排时必须给 weight(1f)：玻璃分组标题会 fillMaxWidth，否则会把同排控件挤成零宽。 */
fun AppSectionLabel(text: String, modifier: Modifier = Modifier) {
    if (isGlassStyle()) {
        GlasmioPreferenceGroupTitle(title = text, modifier = modifier)
    } else {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = modifier.padding(top = 6.dp, bottom = 2.dp)
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
                .clip(shape)
                .clickable { clickSound(view); onClick() }
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
        SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
            items.forEachIndexed { index, (value, label) ->
                SegmentedButton(
                    selected = selected == value,
                    onClick = { clickSound(view); onSelected(value) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index, items.size, baseShape = RoundedCornerShape(8.dp)
                    ),
                    colors = SegmentedButtonDefaults.colors(
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

/**
 * 下拉选择。Glasmio 风格下用 iOS 原生的 Pop-up Button，
 * Material 风格下沿用 ExposedDropdownMenuBox。
 *
 * [title] 为可选的上方标签，[leadingIcon] 仅在 Material 分支显示。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AppDropdown(
    selected: T,
    items: List<T>,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    leadingIcon: ImageVector? = null
) {
    val view = LocalView.current
    if (isGlassStyle()) {
        Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (title != null) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            // GlasmioMenu 的内容是普通 Column、不支持滚动，项数多时会顶出屏幕。
            // 超过阈值改用带 LazyColumn 的 iOS 弹层（最高半屏，可滚动）。
            if (items.size > GlassPopupItemLimit) {
                GlassListPicker(selected, items, label, onSelected)
            } else {
                GlasmioPopUpButton(
                    selected = selected,
                    items = items,
                    onSelected = { clickSound(view); onSelected(it) },
                    label = label,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { clickSound(view); expanded = it },
            modifier = modifier.fillMaxWidth()
        ) {
            Box(
                Modifier.fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .background(AppSubtleSurface, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (leadingIcon != null) {
                        IconBadge(leadingIcon, size = 34.dp, iconSize = 19.dp)
                        Spacer(Modifier.width(12.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        if (title != null) {
                            Text(title, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            label(selected),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppMuted,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = AppMuted
                    )
                }
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(14.dp)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(label(item)) },
                        trailingIcon = if (item == selected) {
                            {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AppBlue
                                )
                            }
                        } else null,
                        onClick = {
                            clickSound(view)
                            expanded = false
                            onSelected(item)
                        }
                    )
                }
            }
        }
    }
}

/** 超过该数量的选项，玻璃风格改用可滚动弹层而非 iOS 弹出按钮。 */
private const val GlassPopupItemLimit = 6

/**
 * 长列表的 iOS 风格选择器：外观沿用弹出按钮的胶囊行，展开后用带列表的弹层，
 * 列表最高半屏且可滚动，避免项数多时顶出屏幕。
 */
@Composable
private fun <T> GlassListPicker(
    selected: T,
    items: List<T>,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit
) {
    val view = LocalView.current
    var expanded by remember { mutableStateOf(false) }
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp / 2
    Row(
        Modifier.fillMaxWidth().height(44.dp)
            .clickable(role = androidx.compose.ui.semantics.Role.Button) { clickSound(view); expanded = true },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label(selected),
            style = MaterialTheme.typography.bodyLarge,
            color = GlasmioTheme.colors.accent,
            modifier = Modifier.weight(1f)
        )
        SfIcon("chevron.up.chevron.down", contentDescription = null, size = 13.dp)
    }
    GlasmioAlertDialog(
        visible = expanded,
        onDismissRequest = { expanded = false },
        title = "",
        buttons = listOf(
            GlasmioAlertButtonSpec(
                stringResource(android.R.string.cancel),
                { expanded = false },
                GlasmioAlertButtonRole.Cancel
            )
        )
    ) {
        LazyColumn(Modifier.heightIn(max = maxHeight)) {
            items(items.size) { index ->
                val item = items[index]
                GlasmioListRow(
                    title = label(item),
                    onClick = { clickSound(view); expanded = false; onSelected(item) },
                    trailing = if (item == selected) {
                        { SfIcon("checkmark", contentDescription = null, size = 16.dp) }
                    } else null,
                    showTopSeparator = false
                )
            }
        }
    }
}

/** 主按钮。Glasmio 下为玻璃按钮，Material 下为 Filled Button。点击音效内部处理。 */
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val view = LocalView.current
    if (isGlassStyle()) {
        GlasmioButton(
            onClick = { clickSound(view); onClick() },
            modifier = modifier,
            enabled = enabled,
            content = content
        )
    } else {
        androidx.compose.material3.Button(
            onClick = { clickSound(view); onClick() },
            modifier = modifier,
            enabled = enabled,
            content = content
        )
    }
}

/**
 * 文本输入框。Glasmio 风格下用 iOS 的 [GlasmioTextField]，Material 下用 [OutlinedTextField]。
 *
 * 两点差异需要注意：
 * 1. GlasmioTextField 只有占位符（placeholder）没有浮动标签，因此 [label] 在其分支作为占位符。
 * 2. GlasmioTextField 不支持多行，[multiLine] 为 true 时玻璃分支回退到 Material 输入框，
 *    以免削弱多行输入（如 Shell 指令）——外观仍跟随 iOS 语义配色。
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    multiLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null
) {
    if (isGlassStyle() && !multiLine) {
        Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            GlasmioTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = label,
                enabled = enabled,
                trailing = trailingIcon,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions
            )
            if (isError && errorText != null) {
                Text(errorText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    } else {
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            label = { Text(label) },
            singleLine = !multiLine,
            minLines = if (multiLine) 3 else 1,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            trailingIcon = trailingIcon,
            isError = isError,
            supportingText = if (isError && errorText != null) { { Text(errorText) } } else null
        )
    }
}
