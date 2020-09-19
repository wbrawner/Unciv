package com.unciv.app.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.unciv.logic.SaveFolderHelperInternal
import com.unciv.models.metadata.GameSettings
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame

class SaveFolderHelperDesktop : SaveFolderHelperInternal() {
    override fun chooseFolder(): FileHandle? {
        val fileChooser = JFileChooser().apply fileChooser@{
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            currentDirectory = File(System.getProperty("user.home"))
        }

        JFrame().apply frame@{
            setLocationRelativeTo(null)
            isVisible = true
            toFront()
            fileChooser.showOpenDialog(this@frame)
            dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        }
        return fileChooser.selectedFile?.let { Gdx.files.absolute(it.absolutePath) }
    }

    override fun canChooseFolder(): Boolean = true

    override fun getSave(settings: GameSettings, GameName: String, multiplayer: Boolean): FileHandle {
        return settings.savesFolder?.let {
            Gdx.files.absolute("$it/${getSubfolder(multiplayer)}/$GameName")
        } ?: super.getSave(settings, GameName, multiplayer)

    }

    override fun getSaves(settings: GameSettings, multiplayer: Boolean): Sequence<FileHandle> {
        return settings.savesFolder?.let {
            val file = Gdx.files.absolute("$it/${getSubfolder(multiplayer)}")
            file.mkdirs()
            return file.list().asSequence()
        } ?: super.getSaves(settings, multiplayer)
    }
}