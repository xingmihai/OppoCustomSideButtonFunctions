package com.slimenull.customsidebuttonfunctions

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.slimenull.customsidebuttonfunctions.data.SettingsStore
import com.slimenull.customsidebuttonfunctions.model.ActionType
import com.slimenull.customsidebuttonfunctions.model.AppSettings
import com.slimenull.customsidebuttonfunctions.model.CommonAction
import com.slimenull.customsidebuttonfunctions.model.CursorControlMode
import com.slimenull.customsidebuttonfunctions.model.CursorLongPressAction
import com.slimenull.customsidebuttonfunctions.model.CustomActionSettings
import com.slimenull.customsidebuttonfunctions.model.MorseBinding
import com.slimenull.customsidebuttonfunctions.model.OperationMode
import com.slimenull.customsidebuttonfunctions.ui.GestureKind
import com.slimenull.customsidebuttonfunctions.ui.MainPagerState
import com.slimenull.customsidebuttonfunctions.ui.Navigator
import com.slimenull.customsidebuttonfunctions.ui.Route
import com.slimenull.customsidebuttonfunctions.ui.rememberMainPagerState
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.preference.SliderPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import top.yukonga.miuix.kmp.utils.overScrollVertical

private val AppKeyColor = Color(0xFF347FE8)

private const val ABOUT_URL = "https://github.com/SlimeNull/OppoCustomSideButtonFunctions"
private const val LICENSE_URL = "https://www.gnu.org/licenses/lgpl-3.0.html"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { false },
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { false }
        )
        // Xiaomi moment, this code must be here
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            CustomSideButtonApp()
        }
    }
}

@Composable
fun CustomSideButtonApp() {
    val context = LocalContext.current
    val themeController = remember {
        ThemeController(ColorSchemeMode.MonetSystem, keyColor = AppKeyColor)
    }
    var settings by remember { mutableStateOf(SettingsStore.load(context)) }

    fun persist(next: AppSettings) {
        settings = next
        SettingsStore.save(next)
    }

    MiuixTheme(controller = themeController) {
        val backStack = rememberNavBackStack<Route>(Route.Main)
        val navigator = remember(backStack) { Navigator(backStack) }

        NavDisplay(
            backStack = backStack,
            onBack = { navigator.pop() },
            transition = NavTransitions.MiuixDefault
        ) {
            entry<Route.Main> {
                MainPage(
                    settings = settings,
                    persist = ::persist,
                    navigator = navigator
                )
            }
            entry<Route.Morse> {
                MorsePage(
                    settings = settings,
                    persist = ::persist,
                    navigator = navigator
                )
            }
            entry<Route.Feedback> {
                FeedbackPage(
                    settings = settings,
                    persist = ::persist,
                    navigator = navigator
                )
            }
            entry<Route.Advanced> {
                AdvancedPage(
                    settings = settings,
                    persist = ::persist,
                    navigator = navigator
                )
            }
            entry<Route.Gesture> { route ->
                GesturePage(
                    kind = route.kind,
                    settings = settings,
                    persist = ::persist,
                    navigator = navigator
                )
            }
        }
    }
}

// ---------------------------------------------------------------- 主页面（底部导航 + 分页）

private const val MAIN_PAGE_COUNT = 3
private const val PAGE_HOME = 0
private const val PAGE_SETTINGS = 1
private const val PAGE_ABOUT = 2

@Composable
private fun MainPage(
    settings: AppSettings,
    persist: (AppSettings) -> Unit,
    navigator: Navigator
) {
    val pagerState = rememberPagerState(pageCount = { MAIN_PAGE_COUNT })
    val mainPagerState = rememberMainPagerState(pagerState)

    LaunchedEffect(pagerState.currentPage) {
        mainPagerState.syncPage()
    }

    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = PagerNavigationSpringSpec,
    )
    val pageNestedScrollConnection =
        PagerDefaults.pageNestedScrollConnection(pagerState, Orientation.Horizontal)

    Scaffold(
        bottomBar = {
            AppNavigationBar(page = mainPagerState.selectedPage, mainPagerState = mainPagerState)
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            flingBehavior = flingBehavior,
            pageNestedScrollConnection = pageNestedScrollConnection,
            verticalAlignment = Alignment.Top,
            pageContent = { page ->
                when (page) {
                    PAGE_SETTINGS -> SettingsContent(
                        padding = padding,
                        settings = settings,
                        persist = persist,
                        navigator = navigator
                    )

                    PAGE_ABOUT -> AboutContent(padding = padding)

                    else -> HomeContent(
                        padding = padding,
                        settings = settings,
                        persist = persist,
                        navigator = navigator
                    )
                }
            }
        )
    }
}

@Composable
private fun HomeContent(
    padding: PaddingValues,
    settings: AppSettings,
    persist: (AppSettings) -> Unit,
    navigator: Navigator
) {
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = "侧键功能",
                largeTitle = "侧键功能",
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            )
        ) {
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    SwitchPreference(
                        title = "启用自定义侧键功能",
                        summary = if (settings.enabled) "当前状态：已启用" else "当前状态：已停用",
                        checked = settings.enabled,
                        onCheckedChange = { persist(settings.copy(enabled = it)) }
                    )
                }
            }
            item { SmallTitle(text = "操作模式") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    OverlaySpinnerPreference(
                        title = "操作模式",
                        items = OperationMode.entries.map { DropdownItem(text = it.title) },
                        selectedIndex = OperationMode.entries.indexOf(settings.operationMode),
                        onSelectedIndexChange = { index ->
                            val mode = OperationMode.entries.getOrNull(index) ?: return@OverlaySpinnerPreference
                            persist(settings.copy(operationMode = mode))
                        }
                    )
                }
            }
            if (settings.operationMode == OperationMode.SIMPLE) {
                item { SmallTitle(text = "手势动作") }
                item {
                    Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        GestureKind.entries.forEach { kind ->
                            ArrowPreference(
                                title = kind.title,
                                summary = gestureSummary(settings, kind),
                                onClick = { navigator.push(Route.Gesture(kind)) }
                            )
                        }
                    }
                }
            }
            if (settings.operationMode == OperationMode.MORSE) {
                item { SmallTitle(text = "摩斯电码") }
                item {
                    Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        ArrowPreference(
                            title = "摩斯电码设置",
                            summary = "${settings.morseBindings.size} 条指令",
                            onClick = { navigator.push(Route.Morse) }
                        )
                    }
                }
            }
            item {
                Spacer(
                    Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    }
}

@Composable
private fun AppNavigationBar(page: Int, mainPagerState: MainPagerState) {
    NavigationBar {
        NavigationBarItem(
            selected = page == PAGE_HOME,
            onClick = { mainPagerState.animateToPage(PAGE_HOME) },
            icon = MiuixIcons.Home,
            label = "首页"
        )
        NavigationBarItem(
            selected = page == PAGE_SETTINGS,
            onClick = { mainPagerState.animateToPage(PAGE_SETTINGS) },
            icon = MiuixIcons.Settings,
            label = "设置"
        )
        NavigationBarItem(
            selected = page == PAGE_ABOUT,
            onClick = { mainPagerState.animateToPage(PAGE_ABOUT) },
            icon = MiuixIcons.Info,
            label = "关于"
        )
    }
}

private fun gestureSummary(settings: AppSettings, kind: GestureKind): String = when (kind) {
    GestureKind.SINGLE -> actionTitle(settings.singleAction, settings.singleCustom)
    GestureKind.DOUBLE -> actionTitle(settings.doubleAction, settings.doubleCustom)
    GestureKind.LONG -> actionTitle(settings.longAction, settings.longCustom)
}

private fun actionTitle(action: ActionType, custom: CustomActionSettings): String = when {
    action == ActionType.NONE -> "未配置"
    action == ActionType.COMMON_FUNCTION -> custom.commonAction.title
    else -> action.title
}

// ---------------------------------------------------------------- 手势配置

@Composable
private fun GesturePage(
    kind: GestureKind,
    settings: AppSettings,
    persist: (AppSettings) -> Unit,
    navigator: Navigator
) {
    val scrollBehavior = MiuixScrollBehavior()
    val currentAction = when (kind) {
        GestureKind.SINGLE -> settings.singleAction
        GestureKind.DOUBLE -> settings.doubleAction
        GestureKind.LONG -> settings.longAction
    }
    val currentCustom = when (kind) {
        GestureKind.SINGLE -> settings.singleCustom
        GestureKind.DOUBLE -> settings.doubleCustom
        GestureKind.LONG -> settings.longCustom
    }

    fun updateAction(action: ActionType) {
        persist(
            when (kind) {
                GestureKind.SINGLE -> settings.copy(singleAction = action)
                GestureKind.DOUBLE -> settings.copy(doubleAction = action)
                GestureKind.LONG -> settings.copy(longAction = action)
            }
        )
    }

    fun updateCustom(custom: CustomActionSettings) {
        persist(
            when (kind) {
                GestureKind.SINGLE -> settings.copy(singleCustom = custom)
                GestureKind.DOUBLE -> settings.copy(doubleCustom = custom)
                GestureKind.LONG -> settings.copy(longCustom = custom)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = kind.title,
                largeTitle = kind.title,
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            )
        ) {
            item { SmallTitle(text = "说明") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(
                        text = kind.description,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            if (kind != GestureKind.SINGLE) {
                item { SmallTitle(text = "判定时间") }
                item {
                    Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        when (kind) {
                            GestureKind.DOUBLE -> {
                                SliderPreference(
                                    value = settings.doubleClickWindowMs.toFloat(),
                                    onValueChange = {
                                        persist(settings.copy(doubleClickWindowMs = it.toLong()))
                                    },
                                    title = "等待时间",
                                    summary = "${settings.doubleClickWindowMs} ms",
                                    valueRange = 100f..800f
                                )
                            }
                            GestureKind.LONG -> {
                                SliderPreference(
                                    value = settings.longPressMs.toFloat(),
                                    onValueChange = {
                                        persist(settings.copy(longPressMs = it.toLong()))
                                    },
                                    title = "按下时长",
                                    summary = "${settings.longPressMs} ms",
                                    valueRange = 100f..800f
                                )
                            }
                            else -> Unit
                        }
                    }
                }
            }
            item { SmallTitle(text = "选择要执行的功能") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    OverlaySpinnerPreference(
                        title = "执行的功能",
                        items = actionChoices.map { DropdownItem(text = it.title) },
                        selectedIndex = actionChoices.indexOfFirst { choice ->
                            choice.action == currentAction &&
                                choice.common == (if (currentAction == ActionType.COMMON_FUNCTION) currentCustom.commonAction else null)
                        }.coerceAtLeast(0),
                        onSelectedIndexChange = { index ->
                            val choice = actionChoices.getOrNull(index) ?: return@OverlaySpinnerPreference
                            updateAction(choice.action)
                            if (choice.action == ActionType.COMMON_FUNCTION && choice.common != null) {
                                updateCustom(currentCustom.copy(commonAction = choice.common))
                            }
                        }
                    )
                }
            }
            if (currentAction in setOf(
                    ActionType.XIAOBU_SHORTCUT,
                    ActionType.CUSTOM_ACTIVITY,
                    ActionType.CUSTOM_URL,
                    ActionType.SHELL_COMMAND
                )
            ) {
                item { SmallTitle(text = "功能参数") }
                item {
                    Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        CustomActionFields(
                            action = currentAction,
                            custom = currentCustom,
                            onChange = ::updateCustom
                        )
                    }
                }
            }
            item { SmallTitle(text = "提示") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(
                        text = when {
                            kind == GestureKind.DOUBLE && settings.doubleAction == ActionType.NONE ->
                                "双击未配置，单击松开后会立即触发。"
                            kind == GestureKind.LONG -> "长按触发后不会再触发单击或双击。"
                            else -> "修改后会立即保存并由 Xposed 模块热加载。"
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item {
                Spacer(
                    Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    }
}

@Composable
private fun CustomActionFields(
    action: ActionType,
    custom: CustomActionSettings,
    onChange: (CustomActionSettings) -> Unit
) {
    when (action) {
        ActionType.XIAOBU_SHORTCUT -> {
            TextField(
                value = custom.xiaobuShortcutId,
                onValueChange = { onChange(custom.copy(xiaobuShortcutId = it)) },
                label = "小布快捷指令 ID"
            )
        }
        ActionType.CUSTOM_ACTIVITY -> {
            TextField(
                value = custom.activityPackage,
                onValueChange = { onChange(custom.copy(activityPackage = it)) },
                label = "应用包名"
            )
            TextField(
                value = custom.activityClass,
                onValueChange = { onChange(custom.copy(activityClass = it)) },
                label = "Activity 类名"
            )
            TextField(
                value = custom.activityAction,
                onValueChange = { onChange(custom.copy(activityAction = it)) },
                label = "Intent Action"
            )
        }
        ActionType.CUSTOM_URL -> {
            TextField(
                value = custom.urlScheme,
                onValueChange = { onChange(custom.copy(urlScheme = it)) },
                label = "Url Scheme"
            )
        }
        ActionType.SHELL_COMMAND -> {
            TextField(
                value = custom.shellCommand,
                onValueChange = { onChange(custom.copy(shellCommand = it)) },
                label = "Shell 指令"
            )
            SwitchPreference(
                title = "执行后显示提示",
                checked = custom.shellToastEnabled,
                onCheckedChange = { onChange(custom.copy(shellToastEnabled = it)) }
            )
        }
        else -> Unit
    }
}

// ---------------------------------------------------------------- 摩斯电码

@Composable
private fun MorsePage(
    settings: AppSettings,
    persist: (AppSettings) -> Unit,
    navigator: Navigator
) {
    val scrollBehavior = MiuixScrollBehavior()
    var editing by remember { mutableStateOf<MorseBinding?>(null) }
    var editorVisible by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<MorseBinding?>(null) }
    var deleteVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "摩斯电码设置",
                largeTitle = "摩斯电码设置",
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editing = MorseBinding("", ActionType.FLASHLIGHT)
                        editorVisible = true
                    }) {
                        Icon(MiuixIcons.Add, contentDescription = "添加指令")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            )
        ) {
            item { SmallTitle(text = "指令映射") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    if (settings.morseBindings.isEmpty()) {
                        Text(
                            text = "还没有指令，点击右上角添加。",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        settings.morseBindings.forEach { binding ->
                            ArrowPreference(
                                title = morseSequenceTitle(binding.sequence),
                                summary = actionTitle(binding.action, binding.custom),
                                onClick = {
                                    editing = binding
                                    editorVisible = true
                                }
                            )
                        }
                    }
                }
            }
            item { SmallTitle(text = "按键设置") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    SwitchPreference(
                        title = "按下时振动",
                        summary = "每次按下侧键时给出振动反馈",
                        checked = settings.morsePressVibrationEnabled,
                        onCheckedChange = { persist(settings.copy(morsePressVibrationEnabled = it)) }
                    )
                    SwitchPreference(
                        title = "长按时振动",
                        summary = "达到长按判定后再次振动",
                        checked = settings.morseLongVibrationEnabled,
                        onCheckedChange = { persist(settings.copy(morseLongVibrationEnabled = it)) }
                    )
                    SwitchPreference(
                        title = "立即执行",
                        summary = "序列匹配成功后立即执行，不等待判定窗口结束",
                        checked = settings.morseImmediateExecutionEnabled,
                        onCheckedChange = { persist(settings.copy(morseImmediateExecutionEnabled = it)) }
                    )
                }
            }
            item { SmallTitle(text = "判定时间") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    SliderPreference(
                        value = settings.morseLongPressMs.toFloat(),
                        onValueChange = { persist(settings.copy(morseLongPressMs = it.toLong())) },
                        title = "长按判定",
                        summary = "${settings.morseLongPressMs} ms",
                        valueRange = 100f..800f
                    )
                    SliderPreference(
                        value = settings.morseCommandWindowMs.toFloat(),
                        onValueChange = { persist(settings.copy(morseCommandWindowMs = it.toLong())) },
                        title = "指令间隔",
                        summary = "${settings.morseCommandWindowMs} ms",
                        valueRange = 100f..800f
                    )
                }
            }
            item { SmallTitle(text = "提示") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(
                        text = "短按记为 0，长按记为 1。序列由 0 和 1 组成，例如 01 表示短按后长按。",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item {
                Spacer(
                    Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }

        val target = editing
        if (target != null) {
            MorseBindingSheet(
                show = editorVisible,
                binding = target,
                existing = settings.morseBindings.filter { it.sequence != target.sequence },
                onDismissRequest = { editorVisible = false },
                onDismissFinished = { editing = null },
                onDelete = if (target.sequence.isEmpty()) null else {
                    {
                        editorVisible = false
                        pendingDelete = target
                        deleteVisible = true
                    }
                },
                onConfirm = { next ->
                    val updated = settings.morseBindings
                        .filter { it.sequence != target.sequence }
                        .plus(next)
                        .distinctBy { it.sequence }
                    persist(settings.copy(morseBindings = updated))
                    editorVisible = false
                }
            )
        }

        val deleting = pendingDelete
        if (deleting != null) {
            OverlayDialog(
                title = "删除指令",
                summary = "确定删除序列 ${morseSequenceTitle(deleting.sequence)} 吗？",
                show = deleteVisible,
                onDismissRequest = { deleteVisible = false },
                onDismissFinished = { pendingDelete = null }
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        text = "取消",
                        onClick = { deleteVisible = false },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(20.dp))
                    TextButton(
                        text = "删除",
                        onClick = {
                            persist(
                                settings.copy(
                                    morseBindings = settings.morseBindings
                                        .filter { it.sequence != deleting.sequence }
                                )
                            )
                            deleteVisible = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }
            }
        }
    }
}

@Composable
private fun MorseBindingSheet(
    show: Boolean,
    binding: MorseBinding,
    existing: List<MorseBinding>,
    onDismissRequest: () -> Unit,
    onDismissFinished: () -> Unit,
    onDelete: (() -> Unit)?,
    onConfirm: (MorseBinding) -> Unit
) {
    var sequence by remember { mutableStateOf(binding.sequence) }
    var actionIndex by remember {
        mutableStateOf(
            morseActionChoices.indexOfFirst { it.action == binding.action }.coerceAtLeast(0)
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    fun confirm() {
        when {
            sequence.isEmpty() -> error = "序列不能为空"
            existing.any { it.sequence == sequence } -> error = "该序列已存在"
            else -> {
                val action = morseActionChoices.getOrNull(actionIndex)?.action
                    ?: ActionType.FLASHLIGHT
                onConfirm(
                    MorseBinding(
                        sequence = sequence,
                        action = action,
                        custom = binding.custom
                    )
                )
            }
        }
    }

    OverlayBottomSheet(
        show = show,
        title = if (binding.sequence.isEmpty()) "添加指令" else "编辑指令",
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        startAction = if (onDelete == null) null else {
            {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = MiuixIcons.Delete,
                        contentDescription = "删除指令",
                        tint = MiuixTheme.colorScheme.onBackground
                    )
                }
            }
        },
        endAction = {
            IconButton(onClick = ::confirm) {
                Icon(
                    imageVector = MiuixIcons.Ok,
                    contentDescription = "保存",
                    tint = MiuixTheme.colorScheme.onBackground
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .overScrollVertical()
        ) {
            item {
                SmallTitle(text = "序列")
                Card(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = morseSequenceTitle(sequence),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item {
                SmallTitle(text = "输入")
                Card(modifier = Modifier.padding(bottom = 12.dp)) {
                    TextButton(
                        text = "短按（0）",
                        onClick = { sequence += "0" },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        text = "长按（1）",
                        onClick = { sequence += "1" },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        text = "退格",
                        onClick = { if (sequence.isNotEmpty()) sequence = sequence.dropLast(1) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                SmallTitle(text = "动作")
                Card(modifier = Modifier.padding(bottom = 12.dp)) {
                    OverlaySpinnerPreference(
                        title = "执行的功能",
                        items = morseActionChoices.map { DropdownItem(text = it.title) },
                        selectedIndex = actionIndex,
                        onSelectedIndexChange = { actionIndex = it }
                    )
                }
            }
            if (error != null) {
                item {
                    Text(
                        text = error!!,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            item {
                Spacer(
                    Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    }
}

private fun morseSequenceTitle(sequence: String): String =
    if (sequence.isEmpty()) "未设置" else sequence.map { if (it == '0') "·" else "—" }.joinToString(" ")

// ---------------------------------------------------------------- 振动与提示

@Composable
private fun FeedbackPage(
    settings: AppSettings,
    persist: (AppSettings) -> Unit,
    navigator: Navigator
) {
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = "振动与提示",
                largeTitle = "振动与提示",
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            )
        ) {
            item { SmallTitle(text = "振动") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    SwitchPreference(
                        title = "触发时振动",
                        summary = "执行动作时给出振动反馈",
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { persist(settings.copy(vibrationEnabled = it)) }
                    )
                }
            }
            item { SmallTitle(text = "Toast 提示") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    SwitchPreference(
                        title = "显示 Toast",
                        checked = settings.toastEnabled,
                        onCheckedChange = { persist(settings.copy(toastEnabled = it)) }
                    )
                    TextField(
                        value = settings.toastText,
                        onValueChange = { persist(settings.copy(toastText = it)) },
                        label = "提示文字",
                        enabled = settings.toastEnabled
                    )
                }
            }
            item { SmallTitle(text = "未知序列提示") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    SwitchPreference(
                        title = "提示未知序列",
                        summary = "摩斯电码序列没有匹配指令时提示",
                        checked = settings.unknownMorseFeedbackEnabled,
                        onCheckedChange = { persist(settings.copy(unknownMorseFeedbackEnabled = it)) }
                    )
                    TextField(
                        value = settings.unknownMorseToastText,
                        onValueChange = { persist(settings.copy(unknownMorseToastText = it)) },
                        label = "提示文字",
                        enabled = settings.unknownMorseFeedbackEnabled
                    )
                }
            }
            item {
                Spacer(
                    Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    }
}

// ---------------------------------------------------------------- 设置

@Composable
private fun SettingsContent(
    padding: PaddingValues,
    settings: AppSettings,
    persist: (AppSettings) -> Unit,
    navigator: Navigator
) {
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = "设置",
                largeTitle = "设置",
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            )
        ) {
            item { SmallTitle(text = "功能设置") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    ArrowPreference(
                        title = "振动与提示",
                        summary = "配置触发振动和 Toast 提示",
                        onClick = { navigator.push(Route.Feedback) }
                    )
                    ArrowPreference(
                        title = "高级设置",
                        summary = "设备输入与息屏行为",
                        onClick = { navigator.push(Route.Advanced) }
                    )
                }
            }
            item { SmallTitle(text = "输入法光标") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    OverlaySpinnerPreference(
                        title = "音量键控制光标",
                        items = CursorControlMode.entries.map { DropdownItem(text = it.title) },
                        selectedIndex = CursorControlMode.entries.indexOf(settings.cursorControlMode),
                        onSelectedIndexChange = { index ->
                            val mode = CursorControlMode.entries.getOrNull(index) ?: return@OverlaySpinnerPreference
                            persist(settings.copy(cursorControlMode = mode))
                        }
                    )
                }
            }
            item { SmallTitle(text = "长按操作") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    OverlaySpinnerPreference(
                        title = "长按行为",
                        items = CursorLongPressAction.entries.map { DropdownItem(text = it.title) },
                        selectedIndex = CursorLongPressAction.entries.indexOf(settings.cursorLongPressAction),
                        onSelectedIndexChange = { index ->
                            val action = CursorLongPressAction.entries.getOrNull(index)
                                ?: return@OverlaySpinnerPreference
                            persist(settings.copy(cursorLongPressAction = action))
                        }
                    )
                }
            }
            item { SmallTitle(text = "时间设置") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    SliderPreference(
                        value = settings.cursorLongPressMs.toFloat(),
                        onValueChange = { persist(settings.copy(cursorLongPressMs = it.toLong())) },
                        title = "长按判定",
                        summary = "${settings.cursorLongPressMs} ms",
                        valueRange = 100f..1000f
                    )
                    if (settings.cursorLongPressAction == CursorLongPressAction.REPEAT) {
                        SliderPreference(
                            value = settings.cursorRepeatIntervalMs.toFloat(),
                            onValueChange = { persist(settings.copy(cursorRepeatIntervalMs = it.toLong())) },
                            title = "连续移动间隔",
                            summary = "${settings.cursorRepeatIntervalMs} ms",
                            valueRange = 16f..200f
                        )
                    }
                }
            }
            item { SmallTitle(text = "提示") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(
                        text = "该功能受首页“启用自定义侧键功能”总控开关控制。设备亮屏、未锁屏、未通话且输入法窗口可见时，音量键才会转换为方向键。",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item {
                Spacer(
                    Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    }
}

// ---------------------------------------------------------------- 高级设置

@Composable
private fun AdvancedPage(
    settings: AppSettings,
    persist: (AppSettings) -> Unit,
    navigator: Navigator
) {
    val scrollBehavior = MiuixScrollBehavior()
    var keyCodeText by remember(settings.keyCode) { mutableStateOf(settings.keyCode.toString()) }
    var devicePathText by remember(settings.inputDevicePath) { mutableStateOf(settings.inputDevicePath) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = "高级设置",
                largeTitle = "高级设置",
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            )
        ) {
            item { SmallTitle(text = "息屏行为") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    SwitchPreference(
                        title = "息屏时唤醒屏幕",
                        summary = "按下侧键时若屏幕处于熄灭状态，先唤醒屏幕",
                        checked = settings.wakeScreenWhenOff,
                        onCheckedChange = { persist(settings.copy(wakeScreenWhenOff = it)) }
                    )
                }
            }
            item { SmallTitle(text = "输入设备") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    TextField(
                        value = devicePathText,
                        onValueChange = {
                            devicePathText = it
                            persist(settings.copy(inputDevicePath = it))
                        },
                        label = "设备路径"
                    )
                    TextField(
                        value = keyCodeText,
                        onValueChange = { text ->
                            keyCodeText = text
                            text.toIntOrNull()?.let { persist(settings.copy(keyCode = it)) }
                        },
                        label = "按键码"
                    )
                }
            }
            item { SmallTitle(text = "提示") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(
                        text = "按键码默认是 735（BTN_TRIGGER_HAPPY）。只有在框架 hook 不可用、回退到原始输入读取时才需要改动设备路径。",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item {
                Spacer(
                    Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    }
}

// ---------------------------------------------------------------- 关于

@Composable
private fun AboutContent(padding: PaddingValues) {
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = "关于",
                largeTitle = "关于",
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            )
        ) {
            item { SmallTitle(text = "应用") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    ArrowPreference(
                        title = "一加侧键自定义功能",
                        summary = "版本 1.0.0"
                    )
                    ArrowPreference(
                        title = "开发者",
                        summary = "SlimeNull Issac"
                    )
                }
            }
            item { SmallTitle(text = "开源") }
            item {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    ArrowPreference(
                        title = "许可协议",
                        summary = "LGPL-3.0",
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, LICENSE_URL.toUri())
                            )
                        }
                    )
                    ArrowPreference(
                        title = "项目仓库",
                        summary = ABOUT_URL,
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, ABOUT_URL.toUri())
                            )
                        }
                    )
                }
            }
            item {
                Spacer(
                    Modifier.padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() +
                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    }
}

// ---------------------------------------------------------------- 动作选项

private data class ActionChoice(val action: ActionType, val common: CommonAction?)

private val actionChoices: List<ActionChoice> = ActionType.entries.flatMap { action ->
    if (action == ActionType.COMMON_FUNCTION) {
        CommonAction.entries.map { ActionChoice(action, it) }
    } else {
        listOf(ActionChoice(action, null))
    }
}

private val morseActionChoices: List<ActionChoice> =
    actionChoices.filter { it.action != ActionType.NONE }

private val ActionChoice.title: String
    get() = common?.title ?: action.title
