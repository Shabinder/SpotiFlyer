package com.shabinder.common.models

enum class AudioQuality(val kbps: String) {
    KBPS128("128"),
    KBPS160("160"),
    KBPS192("192"),
    KBPS224("224"),
    KBPS256("256"),
    KBPS320("320");

    companion object {
        fun getQuality(kbps: String): AudioQuality {
            return when (kbps) {
                "128" -> KBPS128
                "160" -> KBPS160
                "192" -> KBPS192
                "224" -> KBPS224
                "256" -> KBPS256
                "320" -> KBPS320
                else -> KBPS160 // Use 160 as baseline
            }
        }
    }
}
