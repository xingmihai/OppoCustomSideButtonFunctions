package com.slimenull.customsidebuttonfunctions.xposed

import android.os.ParcelFileDescriptor
import com.slimenull.customsidebuttonfunctions.data.SettingsStore
import com.slimenull.customsidebuttonfunctions.model.AppSettings
import com.slimenull.customsidebuttonfunctions.model.DeviceConfig
import android.view.KeyEvent
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import io.github.libxposed.api.XposedInterface
import org.json.JSONObject

internal object SettingsReader {
    @Volatile
    private var api: XposedInterface? = null
    @Volatile
    private var latestSettings = AppSettings()

    fun bind(value: XposedInterface) {
        api = value
    }

    fun load(): AppSettings {
        readRemote()?.let { latestSettings = it }
        return latestSettings
    }

    private fun readRemote(): AppSettings? {
        val module = api ?: return null
        return try {
            val descriptor = module.openRemoteFile(SettingsStore.REMOTE_FILE_NAME)
            if (descriptor.statSize <= 0L) {
                descriptor.close()
                null
            } else {
                ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input ->
                    SettingsStore.fromJson(JSONObject(input.readBytes().toString(StandardCharsets.UTF_8)))
                }
            }
        } catch (_: FileNotFoundException) {
            null
        } catch (error: Throwable) {
            XposedBridge.log("CustomSideButtonFunctions: load remote settings file failed: ${error.message}")
            XposedBridge.log(error)
            null
        }
    }

    /** 回退读取路径用的是内核码，对应 KeyEvent.scanCode。 */
    fun peekKeyCode(): Int = DeviceConfig.SIDE_KEY_SCANCODE

    /**
     * 侧键事件有两层编号：框架层 keyCode 与内核层 scanCode。
     * 本机实测两者不同（780 / 735），任一命中即认为匹配。
     */
    fun matches(event: KeyEvent): Boolean {
        return matches(event, load())
    }

    fun matches(event: KeyEvent, settings: AppSettings): Boolean {
        // 按键码已按本机实测固化，settings 保留参数仅为兼容既有调用
        return event.keyCode == DeviceConfig.SIDE_KEY_KEYCODE ||
            event.scanCode == DeviceConfig.SIDE_KEY_SCANCODE
    }
}
