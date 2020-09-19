package com.unciv.app

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.unciv.UncivGame
import com.unciv.UncivGameParameters
import com.unciv.ui.utils.ORIGINAL_FONT_SIZE

class AndroidLauncher : AndroidApplication() {
    private val saveFolderHelper: SaveFolderHelperAndroid by lazy {
        SaveFolderHelperAndroid(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MultiplayerTurnCheckWorker.createNotificationChannels(applicationContext)

        val config = AndroidApplicationConfiguration().apply { useImmersiveMode = true }

        val androidParameters = UncivGameParameters(
                version = BuildConfig.VERSION_NAME,
                crashReportSender = CrashReportSenderAndroid(this),
                exitEvent = this::finish,
                fontImplementation = NativeFontAndroid(ORIGINAL_FONT_SIZE.toInt()),
                saveFolderHelper = saveFolderHelper
        )
        val game = UncivGame(androidParameters)
        initialize(game, config)
    }

    override fun onPause() {
        if (UncivGame.Companion.isCurrentInitialized()
                && UncivGame.Current.isGameInfoInitialized()
                && UncivGame.Current.settings.multiplayerTurnCheckerEnabled
                && UncivGame.Current.gameInfo.gameParameters.isOnlineMultiplayer) {
            MultiplayerTurnCheckWorker.startTurnChecker(applicationContext, UncivGame.Current.gameInfo, UncivGame.Current.settings)
        }
        super.onPause()
    }

    override fun onResume() {
        try { // Sometimes this fails for no apparent reason - the multiplayer checker failing to cancel should not be enough of a reason for the game to crash!
            WorkManager.getInstance(applicationContext).cancelAllWorkByTag(MultiplayerTurnCheckWorker.WORK_TAG)
            with(NotificationManagerCompat.from(this)) {
                cancel(MultiplayerTurnCheckWorker.NOTIFICATION_ID_INFO)
                cancel(MultiplayerTurnCheckWorker.NOTIFICATION_ID_SERVICE)
            }
        } catch (ex: Exception) {
        }
        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQ_PERMISSION_EXTERNAL_STORAGE && grantResults[0] == PERMISSION_GRANTED) {
            saveFolderHelper.chooseFolder()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_SAVE_FOLDER && resultCode == Activity.RESULT_OK) {
            Log.d("Unciv", "Intent data: ${intent?.data}")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}