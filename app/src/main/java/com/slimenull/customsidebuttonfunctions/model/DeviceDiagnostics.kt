package com.slimenull.customsidebuttonfunctions.model

import android.content.Intent

/**
 * 模块在 system_server 中采集到的运行信息，通过显式广播回传给应用。
 *
 * 注意：LibXposed 的远程文件是「应用创建、模块只读」。模块侧调用 openRemoteFile
 * 去写一个应用从未创建过的文件会拿到无效 fd（EBADF），所以这里改用广播。
 */
data class DeviceDiagnostics(
    val hookPath: String = "",
    val lastKeyCode: Int? = null,
    val lastScanCode: Int? = null,
    val keyEventCount: Int = 0
) {
    val hasKeySample: Boolean
        get() = lastKeyCode != null || lastScanCode != null

    companion object {
        const val ACTION = "com.slimenull.customsidebuttonfunctions.DIAGNOSTICS"
        const val EXTRA_HOOK_PATH = "hookPath"
        const val EXTRA_KEY_CODE = "keyCode"
        const val EXTRA_SCAN_CODE = "scanCode"
        const val EXTRA_EVENT_COUNT = "eventCount"

        fun fromIntent(intent: Intent?): DeviceDiagnostics {
            if (intent == null) return DeviceDiagnostics()
            return DeviceDiagnostics(
                hookPath = intent.getStringExtra(EXTRA_HOOK_PATH).orEmpty(),
                lastKeyCode = intent.getIntExtra(EXTRA_KEY_CODE, -1).takeIf { it >= 0 },
                lastScanCode = intent.getIntExtra(EXTRA_SCAN_CODE, -1).takeIf { it >= 0 },
                keyEventCount = intent.getIntExtra(EXTRA_EVENT_COUNT, 0)
            )
        }
    }
}
