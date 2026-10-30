package com.slimenull.customsidebuttonfunctions.model

import org.json.JSONObject

/**
 * 模块在 system_server 中采集到的运行信息，供设置页的诊断页展示。
 * 通过 LibXposed 的远程文件回传给应用进程。
 */
data class DeviceDiagnostics(
    val hookPath: String = "",
    val lastKeyCode: Int? = null,
    val lastScanCode: Int? = null,
    val lastKeyAt: Long = 0L,
    val keyEventCount: Int = 0,
    val hookInstalledAt: Long = 0L
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("hookPath", hookPath)
        put("lastKeyCode", lastKeyCode ?: -1)
        put("lastScanCode", lastScanCode ?: -1)
        put("lastKeyAt", lastKeyAt)
        put("keyEventCount", keyEventCount)
        put("hookInstalledAt", hookInstalledAt)
    }

    val hasKeySample: Boolean
        get() = lastKeyCode != null || lastScanCode != null

    companion object {
        fun fromJson(json: JSONObject): DeviceDiagnostics = DeviceDiagnostics(
            hookPath = json.optString("hookPath", ""),
            lastKeyCode = json.optInt("lastKeyCode", -1).takeIf { it >= 0 },
            lastScanCode = json.optInt("lastScanCode", -1).takeIf { it >= 0 },
            lastKeyAt = json.optLong("lastKeyAt", 0L),
            keyEventCount = json.optInt("keyEventCount", 0),
            hookInstalledAt = json.optLong("hookInstalledAt", 0L)
        )
    }
}
