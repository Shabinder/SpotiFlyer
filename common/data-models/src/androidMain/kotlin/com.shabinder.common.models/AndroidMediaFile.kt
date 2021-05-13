package com.shabinder.common.models

import com.github.k1rakishou.fsaf.file.AbstractFile

// Use Storage Access Framework `SAF`
actual data class File(
    val documentFile: AbstractFile?
)