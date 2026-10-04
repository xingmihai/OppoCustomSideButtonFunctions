package com.slimenull.customsidebuttonfunctions.xposed

import com.slimenull.customsidebuttonfunctions.data.SettingsStore
import com.slimenull.customsidebuttonfunctions.model.AppSettings
import com.slimenull.customsidebuttonfunctions.onXposedFailure
import android.view.KeyEvent
import io.github.libxposed.api.XposedInterface

internal object SettingsReader {
    private var api: XposedInterface? = null

    /** 仅用于诊断：记录上一次读到的设置快照，变化时打日志，避免每次按键刷屏。 */
    @Volatile
    private var lastSnapshot: String? = null

    fun bind(value: XposedInterface) {
        api = value
    }

    fun load(): AppSettings {
        val prefs = runCatching {
            api?.getRemotePreferences(SettingsStore.PREFS_NAME)
        }.onXposedFailure("load shared preferences").getOrNull()
        if (prefs == null) {
            XposedBridge.log("CustomSideButtonFunctions: [diag] load: remote preferences unavailable, using defaults")
            return AppSettings()
        }
        val settings = SettingsStore.fromPreferences(prefs)
        val snapshot = "enabled=${settings.enabled} mode=${settings.operationMode} " +
            "single=${settings.singleAction} double=${settings.doubleAction} long=${settings.longAction} " +
            "singleCustom=${settings.singleCustom.commonAction} keyCode=${settings.keyCode}"
        if (snapshot != lastSnapshot) {
            lastSnapshot = snapshot
            XposedBridge.log("CustomSideButtonFunctions: [diag] load: settings changed -> $snapshot")
        }
        return settings
    }

    fun peekKeyCode(): Int = load().keyCode

    /** Vendor buttons often expose the Linux BTN_TRIGGER_HAPPY value as scanCode, not keyCode. */
    fun matches(event: KeyEvent): Boolean {
        return matches(event, load())
    }

    fun matches(event: KeyEvent, settings: AppSettings): Boolean {
        val configuredCode = settings.keyCode
        return event.keyCode == configuredCode || event.scanCode == configuredCode
    }
}
