package com.unciv.logic

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.unciv.logic.GameSaver.multiplayerFilesFolder
import com.unciv.logic.GameSaver.saveFilesFolder
import com.unciv.models.metadata.GameSettings

interface SaveFolderHelper {
    fun canChooseFolder(): Boolean
    fun chooseFolder(): FileHandle?
    fun getSave(settings: GameSettings, GameName: String, multiplayer: Boolean = false): FileHandle
    fun getSaves(settings: GameSettings, multiplayer: Boolean = false): Sequence<FileHandle>
}

open class SaveFolderHelperInternal : SaveFolderHelper {
    override fun canChooseFolder(): Boolean = false

    override fun chooseFolder(): FileHandle? = null

    override fun getSave(settings: GameSettings, GameName: String, multiplayer: Boolean): FileHandle = Gdx.files.local("${getSubfolder(multiplayer)}/$GameName")

    override fun getSaves(settings: GameSettings, multiplayer: Boolean): Sequence<FileHandle> = Gdx.files.local(getSubfolder(multiplayer)).list().asSequence()

    fun getSubfolder(multiplayer: Boolean = false) = if (multiplayer) multiplayerFilesFolder else saveFilesFolder
}