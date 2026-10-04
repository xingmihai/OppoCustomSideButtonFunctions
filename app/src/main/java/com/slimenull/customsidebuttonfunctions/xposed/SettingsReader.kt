package com.slimenull.customsidebuttonfunctions.xposed

import com.slimenull.customsidebuttonfunctions.data.SettingsStore
import com.slimenull.customsidebuttonfunctions.model.AppSettings
import com.slimenull.customsidebuttonfunctions.model.SETTINGS_SNAPSHOT_FILE
import com.slimenull.customsidebuttonfunctions.model.toAppSettings
import com.slimenull.customsidebuttonfunctions.onXposedFailure
import android.view.KeyEvent
import io.github.libxposed.api.XposedInterface
import org.json.JSONObject

internal object SettingsReader {
    private var api: XposedInterface? = null

    /** 最近一次成功读到的文件快照；文件偶发读取失败时复用，避免退回过期的偏好快照导致行为跳变。 */
    @Volatile
    private var lastFromFile: AppSettings? = null

    /** 仅在设置内容真正变化时打一次日志，避免每次按键刷屏。 */
    @Volatile
    private var lastSnapshot: String? = null

    fun bind(value: XposedInterface) {
        api = value
    }

    fun load(): AppSettings {
        // system_server 侧的 RemotePreferences 会停留在开机时的快照，
        // 因此优先读取每次重新打开的远程文件。
        val fromFile = readSnapshotFile()
        if (fromFile != null) {
            lastFromFile = fromFile
            logIfChanged("file", snapshotOf(fromFile))
            return fromFile
        }

        // 文件不可用：优先复用上一次成功读到的文件值
        lastFromFile?.let { cached ->
            XposedBridge.log("CustomSideButtonFunctions: snapshot file unreadable, reusing last known settings")
            return cached
        }

        val prefs = runCatching {
            api?.getRemotePreferences(SettingsStore.PREFS_NAME)
        }.onXposedFailure("load shared preferences").getOrNull()
        if (prefs == null) {
            XposedBridge.log("CustomSideButtonFunctions: remote preferences unavailable, using defaults")
            return AppSettings()
        }
        val settings = SettingsStore.fromPreferences(prefs)
        logIfChanged("prefs", snapshotOf(settings))
        return settings
    }

    /** 每次调用都重新打开文件描述符，因此不会命中 RemotePreferences 的过期快照。 */
    private fun readSnapshotFile(): AppSettings? {
        val currentApi = api ?: return null
        return try {
            val text = currentApi.openRemoteFile(SETTINGS_SNAPSHOT_FILE)?.use { descriptor ->
                java.io.FileInputStream(descriptor.fileDescriptor).use { stream ->
                    stream.readBytes().toString(Charsets.UTF_8)
                }
            } ?: return null
            JSONObject(text).toAppSettings()
        } catch (error: Throwable) {
            XposedBridge.log(
                "CustomSideButtonFunctions: snapshot file read failed: ${error.javaClass.simpleName}: ${error.message}"
            )
            null
        }
    }

    private fun snapshotOf(settings: AppSettings): String =
        "enabled=${settings.enabled} mode=${settings.operationMode} " +
            "single=${settings.singleAction} double=${settings.doubleAction} long=${settings.longAction} " +
            "singleCustom=${settings.singleCustom.commonAction} keyCode=${settings.keyCode}"

    private fun logIfChanged(source: String, snapshot: String) {
        if (snapshot == lastSnapshot) return
        lastSnapshot = snapshot
        XposedBridge.log("CustomSideButtonFunctions: settings updated ($source): $snapshot")
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
