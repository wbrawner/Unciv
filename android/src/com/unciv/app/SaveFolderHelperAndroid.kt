package com.unciv.app

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT_TREE
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.unciv.logic.SaveFolderHelperInternal
import com.unciv.models.metadata.GameSettings
import java.io.File

const val REQ_SAVE_FOLDER = 1
const val REQ_PERMISSION_EXTERNAL_STORAGE = 2

class SaveFolderHelperAndroid(private val activity: Activity) : SaveFolderHelperInternal() {
    /** When set, we know we're on Android and can save to the app's personal external file directory
     * Only allow mods on KK+, to avoid READ_EXTERNAL_STORAGE permission earlier versions need
     * See https://developer.android.com/training/data-storage/app-specific#external-access-files */
    private val externalFilesDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        copyMods(activity)
        activity.getExternalFilesDir(null)?.path ?: ""
    } else ""

    override fun canChooseFolder(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    override fun getSave(settings: GameSettings, GameName: String, multiplayer: Boolean): FileHandle {
        val localfile = super.getSave(settings, GameName, multiplayer)
        if (externalFilesDir == "" || !Gdx.files.isExternalStorageAvailable) return localfile
        val externalFile = Gdx.files.absolute(externalFilesDir + "/${getSubfolder(multiplayer)}/$GameName")
        if (localfile.exists() && !externalFile.exists()) return localfile
        return externalFile
    }

    override fun getSaves(settings: GameSettings, multiplayer: Boolean): Sequence<FileHandle> {
        val localSaves = Gdx.files.local(getSubfolder(multiplayer)).list().asSequence()
        if (externalFilesDir == "" || !Gdx.files.isExternalStorageAvailable) return localSaves
        return localSaves + Gdx.files.absolute(externalFilesDir + "/${getSubfolder(multiplayer)}").list().asSequence()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun chooseFolder(): FileHandle? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && PermissionChecker.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            activity.requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), REQ_PERMISSION_EXTERNAL_STORAGE)
            return null
        }
        activity.startActivityForResult(Intent(ACTION_OPEN_DOCUMENT_TREE), REQ_SAVE_FOLDER)
        return null
    }

    /**
     * Copies mods from external data directory (where users can access) to the private one (where
     * libGDX reads from). Note: deletes all files currently in the private mod directory and
     * replaces them with the ones in the external folder!)
     */
    private fun copyMods(activity: Activity) {
        with(activity) {
            // Mod directory in the internal app data (where Gdx.files.local looks)
            val internalModsDir = File("${filesDir.path}/mods")

            // Mod directory in the shared app data (where the user can see and modify)
            val externalModsDir = File("${getExternalFilesDir(null)?.path}/mods")

            // Empty out the mods directory so it can be replaced by the external one
            // Done to ensure it only contains mods in the external dir (so users can delete some)
            if (internalModsDir.exists()) internalModsDir.deleteRecursively()

            // Copy external mod directory (with data user put in it) to internal (where it can be read)
            if (!externalModsDir.exists()) externalModsDir.mkdirs() // this can fail sometimes, which is why we check if it exists again in the next line
            if (externalModsDir.exists()) externalModsDir.copyRecursively(internalModsDir)
        }
    }
}