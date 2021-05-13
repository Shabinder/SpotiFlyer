package com.shabinder.common.models

import android.net.Uri
import com.github.k1rakishou.fsaf.manager.base_directory.BaseDirectory
import java.io.File

class SpotiFlyerBaseDir(
    private val getDirType: ()-> ActiveBaseDirType,
    private val getJavaFile: ()-> File?,
    private val getSAFUri: ()-> Uri?
): BaseDirectory() {

    override fun currentActiveBaseDirType(): ActiveBaseDirType = getDirType()

    override fun getDirFile(): File? = getJavaFile()

    override fun getDirUri(): Uri? = getSAFUri()
}