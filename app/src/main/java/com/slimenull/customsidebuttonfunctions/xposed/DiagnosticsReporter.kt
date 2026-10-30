package com.slimenull.customsidebuttonfunctions.xposed

import android.os.ParcelFileDescriptor
import android.view.KeyEvent
import com.slimenull.customsidebuttonfunctions.model.DeviceDiagnostics
import io.github.libxposed.api.XposedInterface
import java.nio.charset.StandardCharsets

/**
 * 在 system_server 中记录按键拦截的运行状态，并写入远程文件供应用读取。
 *
 * 用途：确认本机实际生效的 hook 路径，以及侧键的真实 keyCode / scanCode，
 * 避免只能靠默认值猜。这些信息只在按键或安装 hook 时更新，且做了节流。
 */
internal object DiagnosticsReporter {
    const val REMOTE_FILE_NAME = "diagnostics.json"

    private const val MIN_FLUSH_INTERVAL_MS = 700L
    private const val TAG = "CustomSideButtonFunctions"

    @Volatile
    private var api: XposedInterface? = null

    @Volatile
    private var hookPath = ""

    @Volatile
    private var hookInstalledAt = 0L

    @Volatile
    private var lastKeyCode: Int? = null

    @Volatile
    private var lastScanCode: Int? = null

    @Volatile
    private var lastKeyAt = 0L

    @Volatile
    private var keyEventCount = 0

    @Volatile
    private var lastFlushAt = 0L

    private val lock = Any()

    fun bind(value: XposedInterface) {
        api = value
    }

    fun reportHook(path: String) {
        hookPath = path
        hookInstalledAt = System.currentTimeMillis()
        flush(force = true)
    }

    fun reportKeyEvent(event: KeyEvent) {
        lastKeyCode = event.keyCode
        lastScanCode = event.scanCode
        lastKeyAt = System.currentTimeMillis()
        keyEventCount += 1
        flush(force = false)
    }

    internal fun snapshot(): DeviceDiagnostics = DeviceDiagnostics(
        hookPath = hookPath,
        lastKeyCode = lastKeyCode,
        lastScanCode = lastScanCode,
        lastKeyAt = lastKeyAt,
        keyEventCount = keyEventCount,
        hookInstalledAt = hookInstalledAt
    )

    private fun flush(force: Boolean) {
        val module = api ?: return
        val now = System.currentTimeMillis()
        synchronized(lock) {
            if (!force && now - lastFlushAt < MIN_FLUSH_INTERVAL_MS) return
            lastFlushAt = now
        }
        runCatching {
            val descriptor = module.openRemoteFile(REMOTE_FILE_NAME)
            ParcelFileDescriptor.AutoCloseOutputStream(descriptor).use { output ->
                output.channel.truncate(0)
                output.write(snapshot().toJson().toString().toByteArray(StandardCharsets.UTF_8))
                output.channel.force(true)
            }
        }.onFailure { error ->
            XposedBridge.log("$TAG: write diagnostics failed: ${error.message}")
        }
    }
}
