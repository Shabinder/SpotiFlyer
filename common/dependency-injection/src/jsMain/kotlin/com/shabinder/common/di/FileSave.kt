@file:JsModule("file-saver")
@file:JsNonModule

package com.shabinder.common.di

import org.w3c.files.Blob

external interface FileSaverOptions {
    var autoBom: Boolean
}

external fun saveAs(data: Blob, filename: String = definedExternally, options: FileSaverOptions = definedExternally)

external fun saveAs(data: Blob)

external fun saveAs(data: Blob, filename: String = definedExternally)

external fun saveAs(data: String, filename: String = definedExternally, options: FileSaverOptions = definedExternally)

external fun saveAs(data: String)

external fun saveAs(data: String, filename: String = definedExternally)

external fun saveAs(data: Blob, filename: String = definedExternally, disableAutoBOM: Boolean = definedExternally)

external fun saveAs(data: String, filename: String = definedExternally, disableAutoBOM: Boolean = definedExternally)