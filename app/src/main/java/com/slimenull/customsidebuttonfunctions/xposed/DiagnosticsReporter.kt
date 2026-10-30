package com.slimenull.customsidebuttonfunctions.xposed

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.slimenull.customsidebuttonfunctions.model.DeviceDiagnostics

/**
 * 在 system_server 中记录按键拦截的运行状态，并通过显式广播回传给设置页。
 *
 * 用途：确认本机实际生效的 hook 路径，以及侧键的真实 keyCode / scanCode。
 * 广播用 setPackage 限定到本应用，避免外泄；按键上报做了节流。
 */
internal object DiagnosticsReporter {
    private const val TAG = "CustomSideButtonFunctions"
    private const val MIN_SEND_INTERVAL_MS = 400L
    private const val PACKAGE = "com.slimenull.customsidebuttonfunctions"

    @Volatile
    private var context: Context? = null

    @Volatile
    private var hookPath = ""

    @Volatile
    private var lastKeyCode: Int? = null

    @Volatile
    private var lastScanCode: Int? = null

    @Volatile
    private var keyEventCount = 0

    @Volatile
    private var lastSentAt = 0L

    private val lock = Any()

    fun bind(value: Context?) {
        context = value
    }

    fun reportHook(path: String) {
        hookPath = path
        send(force = true)
    }

    fun reportKeyEvent(event: KeyEvent) {
        lastKeyCode = event.keyCode
        lastScanCode = event.scanCode
        keyEventCount += 1
        send(force = false)
    }

    private fun send(force: Boolean) {
        val ctx = context
        if (ctx == null) {
            XposedBridge.log("$TAG: diagnostics context unavailable, skip report")
            return
        }
        val now = System.currentTimeMillis()
        synchronized(lock) {
            if (!force && now - lastSentAt < MIN_SEND_INTERVAL_MS) return
            lastSentAt = now
        }
        runCatching {
            val intent = Intent(DeviceDiagnostics.ACTION).apply {
                setPackage(PACKAGE)
                putExtra(DeviceDiagnostics.EXTRA_HOOK_PATH, hookPath)
                putExtra(DeviceDiagnostics.EXTRA_EVENT_COUNT, keyEventCount)
                lastKeyCode?.let { putExtra(DeviceDiagnostics.EXTRA_KEY_CODE, it) }
                lastScanCode?.let { putExtra(DeviceDiagnostics.EXTRA_SCAN_CODE, it) }
            }
            ctx.sendBroadcast(intent)
        }.onFailure { error ->
            XposedBridge.log("$TAG: send diagnostics broadcast failed: ${error.message}")
        }
    }
}
