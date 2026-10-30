package com.slimenull.customsidebuttonfunctions

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/** Invisible launcher used when ColorOS blocks starting a background service directly. */
class RootCommandTrampolineActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val command = intent.getStringExtra(EXTRA_COMMAND)?.takeIf { it.isNotBlank() }
        if (command != null) {
            val serviceIntent = Intent(this, RootCommandService::class.java)
                .setAction(RootCommandService.ACTION)
                .putExtra(RootCommandService.EXTRA_COMMAND, command)
                .putExtra(RootCommandService.EXTRA_SHOW_TOAST, intent.getBooleanExtra(EXTRA_SHOW_TOAST, true))
                .putExtra(RootCommandService.EXTRA_PREFER_SU, intent.getBooleanExtra(EXTRA_PREFER_SU, false))
            startForegroundService(serviceIntent)
        }
        // This activity is launched in its own task so finishing it cannot expose MainActivity
        // from the app's existing task when the app is already running in the background.
        finishAndRemoveTask()
        // overridePendingTransition 自 API 34 起废弃，minSdk 37 下直接用新接口
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
    }

    companion object {
        const val ACTION = "com.slimenull.customsidebuttonfunctions.EXECUTE_ROOT"
        const val EXTRA_COMMAND = "command"
        const val EXTRA_SHOW_TOAST = "show_toast"
        const val EXTRA_PREFER_SU = "prefer_su"
    }
}
