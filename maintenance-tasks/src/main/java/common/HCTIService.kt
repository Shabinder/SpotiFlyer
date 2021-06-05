package common

import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.userAgent
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import utils.HCTI_URL_RESPONSE_ERROR

internal object HCTIService {

    private const val baseURL = "https://htmlcsstoimage.com/demo_run"

    /*
    * - When Using Either Viewport Width/Height(in Pixels) , Both are required
    * */
    suspend fun getImageURLFromHtml(
        html: String,
        css: String = "",
        delayInMilliSeconds: Int = 250,
        viewPortHeight: String = "",
        viewPortWidth: String = "",
        deviceScale: Int = 2
    ) = getImageURL(
        mode = "html",
        data = html,
        css = css,
        delayInMilliSeconds = delayInMilliSeconds,
        viewPortHeight = viewPortHeight,
        viewPortWidth = viewPortWidth,
        deviceScale = deviceScale
    )

    suspend fun getImageURLFromURL(
        url: String,
        delayInMilliSeconds: Int = 250,
        viewPortHeight: String = "",
        viewPortWidth: String = "",
        deviceScale: Int = 2
    ) = getImageURL(
        mode = "url",
        data = url,
        delayInMilliSeconds = delayInMilliSeconds,
        viewPortHeight = viewPortHeight,
        viewPortWidth = viewPortWidth,
        deviceScale = deviceScale
    )

    private suspend fun getImageURL(
        mode: String, // html/url
        data: String,
        css: String = "",
        viewPortHeight: String = "",
        viewPortWidth: String = "",
        delayInMilliSeconds: Int = 250,
        deviceScale: Int = 2,
    ): String {
        val resp = client.post<JsonObject>(baseURL) {
            body = buildJsonObject {
                put(mode, data)
                put("console_mode", "")
                put("css", css)
                put("selector", "")
                put("ms_delay", "$delayInMilliSeconds")
                put("render_when_ready", "")
                put("viewport_width", viewPortWidth)
                put("viewport_height", viewPortHeight)
                put("google_fonts", "")
                put("device_scale", "$deviceScale")
            }
            headers {
                contentType(ContentType.Application.Json)
                userAgent(Common.USER_AGENT)
                header("Referer", "https://htmlcsstoimage.com/demo")
                header("Origin", "https://htmlcsstoimage.com")
                header("Host", "htmlcsstoimage.com")
            }
        }
        val url = resp["url"] ?: throw HCTI_URL_RESPONSE_ERROR(response = resp.toString())
        // bubble-up exceptions
        return url.jsonPrimitive.toString().removeSurrounding("\"")
    }
}
