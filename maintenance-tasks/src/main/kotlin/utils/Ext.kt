package utils

val String.byProperty: String get() = System.getenv(this)
    ?: throw (ENV_KEY_MISSING(this))

val String.byOptionalProperty: String? get() = System.getenv(this)

fun debug(message: String) = println("::debug::$message")
