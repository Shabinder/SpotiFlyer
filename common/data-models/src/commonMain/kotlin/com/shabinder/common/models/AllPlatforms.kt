package com.shabinder.common.models

sealed class AllPlatforms{
    object Js:AllPlatforms()
    object Jvm:AllPlatforms()
    object Native:AllPlatforms()
}
