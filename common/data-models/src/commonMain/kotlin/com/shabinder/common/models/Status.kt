package com.shabinder.common.models

import kotlin.jvm.JvmStatic

/**
 * Enumeration which contains the different states a download
 * could go through.
 *
 * From Fetch
 * */
enum class Status constructor(val value: Int) {

    /** Indicates when a download is newly created and not yet queued.*/
    NONE(0),

    /** Indicates when a newly created download is queued.*/
    QUEUED(1),

    /** Indicates when a download is currently being downloaded.*/
    DOWNLOADING(2),

    /** Indicates when a download is paused.*/
    PAUSED(3),

    /** Indicates when a download is completed.*/
    COMPLETED(4),

    /** Indicates when a download is cancelled.*/
    CANCELLED(5),

    /** Indicates when a download has failed.*/
    FAILED(6),

    /** Indicates when a download has been removed and is no longer managed by Fetch.*/
    REMOVED(7),

    /** Indicates when a download has been deleted and is no longer managed by Fetch.*/
    DELETED(8),

    /** Indicates when a download has been Added to Fetch for management.*/
    ADDED(9);

    companion object {

        @JvmStatic
        fun valueOf(value: Int): Status {
            return when (value) {
                0 -> NONE
                1 -> QUEUED
                2 -> DOWNLOADING
                3 -> PAUSED
                4 -> COMPLETED
                5 -> CANCELLED
                6 -> FAILED
                7 -> REMOVED
                8 -> DELETED
                9 -> ADDED
                else -> NONE
            }
        }
    }
}
