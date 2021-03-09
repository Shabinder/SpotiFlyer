package com.shabinder.common.models.wynk

data class SingerWynk(
    val id: String,
    val isCurated: Boolean,
    val packageId: String,
    val smallImage: String,
    val title: String,
    val type: String
)