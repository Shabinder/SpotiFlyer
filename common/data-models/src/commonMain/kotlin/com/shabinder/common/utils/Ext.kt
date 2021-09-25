package com.shabinder.common.utils

import com.shabinder.common.models.TrackDetails
import com.shabinder.common.models.dispatcherIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <T : Any?> T?.requireNotNull(): T = requireNotNull(this)

@OptIn(ExperimentalContracts::class)
inline fun buildString(track: TrackDetails, builderAction: StringBuilder.() -> Unit): String {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return StringBuilder().run {
        appendLine("Find Link for ${track.title} ${if (!track.videoID.isNullOrBlank()) "-> VideoID:" + track.videoID else ""}")
        apply(builderAction)
    }.toString()
}

fun StringBuilder.appendPadded(data: Any?) {
    appendLine().append(data).appendLine()
}

fun StringBuilder.appendPadded(header: Any?, data: Any?) {
    appendLine().append(header).appendLine(data).appendLine()
}

suspend fun <T> runOnMain(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Main, block)

suspend fun <T> runOnIO(block: suspend CoroutineScope.() -> T): T =
    withContext(dispatcherIO, block)

suspend fun <T> runOnDefault(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Default, block)
