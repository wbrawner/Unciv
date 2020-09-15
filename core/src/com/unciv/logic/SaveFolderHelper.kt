package com.unciv.logic

import com.badlogic.gdx.files.FileHandle
import com.unciv.models.metadata.GameSettings

interface SaveFolderHelper {
    fun canChooseFolder(): Boolean
    fun chooseFolder(): FileHandle?
    fun getSave(settings: GameSettings, GameName: String, multiplayer: Boolean = false): FileHandle
    fun getSaves(settings: GameSettings, multiplayer: Boolean = false): Sequence<FileHandle>
}