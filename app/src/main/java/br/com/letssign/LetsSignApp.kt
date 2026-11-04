package br.com.letssign

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

@HiltAndroidApp
class LetsSignApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()

                Log.e("LetsSignApp", "FATAL CRASH on thread ${thread.name}: $stackTrace")

                // Gravar log interno
                val internalLog = File(filesDir, "crash_log.txt")
                internalLog.writeText("CRASH on thread ${thread.name}:\n$stackTrace")

                // Gravar na pasta compartilhada do BlueStacks se existir
                val bstShared = File("/sdcard/windows/BstSharedFolder")
                if (bstShared.exists() && bstShared.canWrite()) {
                    File(bstShared, "crash_log.txt").writeText("CRASH on thread ${thread.name}:\n$stackTrace")
                }
            } catch (e: Exception) {
                Log.e("LetsSignApp", "Falha ao gravar arquivo de crash log", e)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}

