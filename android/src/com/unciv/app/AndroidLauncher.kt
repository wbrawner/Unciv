package com.unciv.app

import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.unciv.UncivGame
import com.unciv.UncivGameParameters
import com.unciv.logic.GameSaver
import com.unciv.ui.utils.ORIGINAL_FONT_SIZE
import java.io.File

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MultiplayerTurnCheckWorker.createNotificationChannels(applicationContext)

        val config = AndroidApplicationConfiguration().apply { useImmersiveMode = true }

        val androidParameters = UncivGameParameters(
                version = BuildConfig.VERSION_NAME,
                crashReportSender = CrashReportSenderAndroid(this),
                exitEvent = this::finish,
                fontImplementation = NativeFontAndroid(ORIGINAL_FONT_SIZE.toInt()),
                saveFolderHelper = SaveFolderHelperAndroid(this)
        )
        val game = UncivGame ( androidParameters )
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
        }
        catch (ex:Exception){}
        super.onResume()
    }
}