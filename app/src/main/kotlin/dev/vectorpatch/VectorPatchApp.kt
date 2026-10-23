package dev.vectorpatch

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.vectorpatch.updater.UpdateScheduler
import rikka.shizuku.Shizuku

@HiltAndroidApp
class VectorPatchApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initShizuku()
        UpdateScheduler.schedulePeriodicCheck(this)
    }

    private fun initShizuku() {
        Shizuku.addBinderReceivedListenerSticky {
            // Shizuku connected
        }
        Shizuku.addBinderDeadListener {
            // Shizuku disconnected
        }
    }
}
