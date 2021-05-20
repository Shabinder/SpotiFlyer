@file:Suppress("ClassName")

package utils

data class ENV_KEY_MISSING(
    val keyName: String,
    override val message: String? = "$keyName was not found, please check your ENV variables"
) : Exception(message)

data class HCTI_URL_RESPONSE_ERROR(
    val response: String,
    override val message: String? = "Server Error, We Recieved this Resp: $response"
) : Exception(message)

data class RETRY_LIMIT_EXHAUSTED(
    override val message: String? = "RETRY LIMIT EXHAUSTED!"
) : Exception(message)
