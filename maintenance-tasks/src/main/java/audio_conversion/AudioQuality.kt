package audio_conversion

@Suppress("EnumEntryName")
enum class AudioQuality(val kbps: String) {
    `128KBPS`("128"),
    `160KBPS`("160"),
    `192KBPS`("192"),
    `224KBPS`("224"),
    `256KBPS`("256"),
    `320KBPS`("320");

    companion object {
        fun getQuality(kbps: String): AudioQuality {
            return when (kbps) {
                "128" -> `128KBPS`
                "160" -> `160KBPS`
                "192" -> `192KBPS`
                "224" -> `224KBPS`
                "256" -> `256KBPS`
                "320" -> `320KBPS`
                else -> `160KBPS`
            }
        }
    }
}
