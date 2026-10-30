package com.slimenull.customsidebuttonfunctions.data

import android.os.ParcelFileDescriptor
import com.slimenull.customsidebuttonfunctions.model.DeviceDiagnostics
import com.slimenull.customsidebuttonfunctions.xposed.DiagnosticsReporter
import org.json.JSONObject
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets

/** Reads the diagnostics snapshot produced by the module inside system_server. */
object DiagnosticsStore {
    fun load(): DeviceDiagnostics? {
        val service = XposedServiceManager.xposedService ?: return null
        return try {
            val descriptor = service.openRemoteFile(DiagnosticsReporter.REMOTE_FILE_NAME)
            if (descriptor.statSize <= 0L) {
                descriptor.close()
                null
            } else {
                ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input ->
                    DeviceDiagnostics.fromJson(
                        JSONObject(input.readBytes().toString(StandardCharsets.UTF_8))
                    )
                }
            }
        } catch (_: FileNotFoundException) {
            null
        } catch (_: Throwable) {
            null
        }
    }
}
