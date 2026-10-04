package com.slimenull.customsidebuttonfunctions.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * SharedPreferences 在 system_server 侧可能停留在开机时的快照，
 * 因此额外维护一份 JSON 快照：模块 App 写入远程文件，system_server 每次重新打开文件描述符读取，
 * 从而绕过偏好缓存拿到最新配置。
 *
 * 这里的键名与 SettingsStore 中的偏好键保持一致，便于对照排查。
 */
internal const val SETTINGS_SNAPSHOT_FILE = "settings_snapshot.json"

private const val KEY_ENABLED = "enabled"
private const val KEY_KEY_CODE = "key_code"
private const val KEY_INPUT_PATH = "input_device_path"
private const val KEY_LONG_PRESS_MS = "long_press_ms"
private const val KEY_DOUBLE_WINDOW_MS = "double_click_window_ms"
private const val KEY_SINGLE_ACTION = "single_action"
private const val KEY_DOUBLE_ACTION = "double_action"
private const val KEY_LONG_ACTION = "long_action"
private const val KEY_OPERATION_MODE = "operation_mode"
private const val KEY_MORSE_LONG_PRESS_MS = "morse_long_press_ms"
private const val KEY_MORSE_COMMAND_WINDOW_MS = "morse_command_window_ms"
private const val KEY_MORSE_PRESS_VIBRATION = "morse_press_vibration"
private const val KEY_MORSE_LONG_VIBRATION = "morse_long_vibration"
private const val KEY_MORSE_IMMEDIATE_EXECUTION = "morse_immediate_execution"
private const val KEY_MORSE_BINDINGS = "morse_bindings"
private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
private const val KEY_TOAST_ENABLED = "toast_enabled"
private const val KEY_TOAST_TEXT = "toast_text"
private const val KEY_UNKNOWN_MORSE_FEEDBACK = "unknown_morse_feedback"
private const val KEY_UNKNOWN_MORSE_TOAST_TEXT = "unknown_morse_toast_text"
private const val KEY_WAKE_SCREEN = "wake_screen_when_off"
private const val KEY_CURSOR_CONTROL_MODE = "cursor_control_mode"
private const val KEY_CURSOR_LONG_PRESS_ACTION = "cursor_long_press_action"
private const val KEY_CURSOR_LONG_PRESS_MS = "cursor_long_press_ms"
private const val KEY_CURSOR_REPEAT_INTERVAL_MS = "cursor_repeat_interval_ms"

private inline fun <reified T : Enum<T>> JSONObject.enumOrNull(key: String): T? =
    runCatching { enumValueOf<T>(optString(key)) }.getOrNull()

private fun CustomActionSettings.toJson(): JSONObject = JSONObject().apply {
    put("common_action", commonAction.name)
    put("activity_package", activityPackage)
    put("activity_class", activityClass)
    put("activity_action", activityAction)
    put("url_scheme", urlScheme)
    put("xiaobu_shortcut_id", xiaobuShortcutId)
    put("shell_command", shellCommand)
    put("shell_toast_enabled", shellToastEnabled)
}

private fun JSONObject.toCustomAction(): CustomActionSettings = CustomActionSettings(
    commonAction = enumOrNull<CommonAction>("common_action") ?: CommonAction.WECHAT_PAY,
    activityPackage = optString("activity_package", ""),
    activityClass = optString("activity_class", ""),
    activityAction = optString("activity_action", ""),
    urlScheme = optString("url_scheme", ""),
    xiaobuShortcutId = optString("xiaobu_shortcut_id", ""),
    shellCommand = optString("shell_command", ""),
    shellToastEnabled = optBoolean("shell_toast_enabled", true)
)

private fun MorseBinding.toJson(): JSONObject = JSONObject().apply {
    put("sequence", sequence)
    put("action", action.name)
    put("custom", custom.toJson())
}

private fun JSONObject.toMorseBinding(): MorseBinding? {
    val sequence = optString("sequence")
    if (sequence.isEmpty() || sequence.any { it != '0' && it != '1' }) return null
    val action = enumOrNull<ActionType>("action")?.takeIf { it != ActionType.NONE } ?: return null
    val custom = optJSONObject("custom")?.toCustomAction() ?: CustomActionSettings()
    return MorseBinding(sequence = sequence, action = action, custom = custom)
}

fun AppSettings.toSnapshotJson(): JSONObject = JSONObject().apply {
    put(KEY_ENABLED, enabled)
    put(KEY_KEY_CODE, keyCode)
    put(KEY_INPUT_PATH, inputDevicePath)
    put(KEY_LONG_PRESS_MS, longPressMs)
    put(KEY_DOUBLE_WINDOW_MS, doubleClickWindowMs)
    put(KEY_SINGLE_ACTION, singleAction.name)
    put(KEY_DOUBLE_ACTION, doubleAction.name)
    put(KEY_LONG_ACTION, longAction.name)
    put(KEY_OPERATION_MODE, operationMode.name)
    put(KEY_MORSE_LONG_PRESS_MS, morseLongPressMs)
    put(KEY_MORSE_COMMAND_WINDOW_MS, morseCommandWindowMs)
    put(KEY_MORSE_PRESS_VIBRATION, morsePressVibrationEnabled)
    put(KEY_MORSE_LONG_VIBRATION, morseLongVibrationEnabled)
    put(KEY_MORSE_IMMEDIATE_EXECUTION, morseImmediateExecutionEnabled)
    put(KEY_MORSE_BINDINGS, JSONArray().apply { morseBindings.forEach { put(it.toJson()) } })
    put("single_custom", singleCustom.toJson())
    put("double_custom", doubleCustom.toJson())
    put("long_custom", longCustom.toJson())
    put(KEY_VIBRATION_ENABLED, vibrationEnabled)
    put(KEY_TOAST_ENABLED, toastEnabled)
    put(KEY_TOAST_TEXT, toastText)
    put(KEY_UNKNOWN_MORSE_FEEDBACK, unknownMorseFeedbackEnabled)
    put(KEY_UNKNOWN_MORSE_TOAST_TEXT, unknownMorseToastText)
    put(KEY_WAKE_SCREEN, wakeScreenWhenOff)
    put(KEY_CURSOR_CONTROL_MODE, cursorControlMode.name)
    put(KEY_CURSOR_LONG_PRESS_ACTION, cursorLongPressAction.name)
    put(KEY_CURSOR_LONG_PRESS_MS, cursorLongPressMs)
    put(KEY_CURSOR_REPEAT_INTERVAL_MS, cursorRepeatIntervalMs)
}

fun JSONObject.toAppSettings(): AppSettings {
    val bindings = optJSONArray(KEY_MORSE_BINDINGS)
    val morseBindings = if (bindings == null) {
        emptyList()
    } else {
        (0 until bindings.length()).mapNotNull { index ->
            bindings.optJSONObject(index)?.toMorseBinding()
        }.distinctBy { it.sequence }
    }
    return AppSettings(
        enabled = optBoolean(KEY_ENABLED, true),
        keyCode = optInt(KEY_KEY_CODE, 735),
        inputDevicePath = optString(KEY_INPUT_PATH, "/dev/input/event0"),
        longPressMs = optLong(KEY_LONG_PRESS_MS, 300L).coerceIn(100L, 800L),
        doubleClickWindowMs = optLong(KEY_DOUBLE_WINDOW_MS, 300L).coerceIn(100L, 800L),
        singleAction = enumOrNull<ActionType>(KEY_SINGLE_ACTION) ?: ActionType.CYCLE_RINGER,
        doubleAction = enumOrNull<ActionType>(KEY_DOUBLE_ACTION) ?: ActionType.NONE,
        longAction = enumOrNull<ActionType>(KEY_LONG_ACTION) ?: ActionType.SCREENSHOT,
        operationMode = enumOrNull<OperationMode>(KEY_OPERATION_MODE) ?: OperationMode.SIMPLE,
        morseLongPressMs = optLong(KEY_MORSE_LONG_PRESS_MS, 300L).coerceIn(100L, 800L),
        morseCommandWindowMs = optLong(KEY_MORSE_COMMAND_WINDOW_MS, 300L).coerceIn(100L, 800L),
        morsePressVibrationEnabled = optBoolean(KEY_MORSE_PRESS_VIBRATION, false),
        morseLongVibrationEnabled = optBoolean(KEY_MORSE_LONG_VIBRATION, true),
        morseImmediateExecutionEnabled = optBoolean(KEY_MORSE_IMMEDIATE_EXECUTION, true),
        morseBindings = morseBindings,
        singleCustom = optJSONObject("single_custom")?.toCustomAction() ?: CustomActionSettings(),
        doubleCustom = optJSONObject("double_custom")?.toCustomAction() ?: CustomActionSettings(),
        longCustom = optJSONObject("long_custom")?.toCustomAction() ?: CustomActionSettings(),
        vibrationEnabled = optBoolean(KEY_VIBRATION_ENABLED, true),
        toastEnabled = optBoolean(KEY_TOAST_ENABLED, false),
        toastText = optString(KEY_TOAST_TEXT, "侧键操作已执行"),
        unknownMorseFeedbackEnabled = optBoolean(KEY_UNKNOWN_MORSE_FEEDBACK, false),
        unknownMorseToastText = optString(KEY_UNKNOWN_MORSE_TOAST_TEXT, DEFAULT_UNKNOWN_MORSE_TOAST),
        wakeScreenWhenOff = optBoolean(KEY_WAKE_SCREEN, false),
        cursorControlMode = enumOrNull<CursorControlMode>(KEY_CURSOR_CONTROL_MODE) ?: CursorControlMode.DISABLED,
        cursorLongPressAction = enumOrNull<CursorLongPressAction>(KEY_CURSOR_LONG_PRESS_ACTION)
            ?: CursorLongPressAction.NONE,
        cursorLongPressMs = optLong(KEY_CURSOR_LONG_PRESS_MS, DEFAULT_CURSOR_LONG_PRESS_MS)
            .coerceIn(MIN_CURSOR_LONG_PRESS_MS, MAX_CURSOR_LONG_PRESS_MS),
        cursorRepeatIntervalMs = optLong(KEY_CURSOR_REPEAT_INTERVAL_MS, DEFAULT_CURSOR_REPEAT_INTERVAL_MS)
            .coerceIn(MIN_CURSOR_REPEAT_INTERVAL_MS, MAX_CURSOR_REPEAT_INTERVAL_MS)
    )
}
