package utils

import org.piwik.java.tracking.PiwikRequest
import org.piwik.java.tracking.PiwikTracker
import java.net.URL


fun PiwikTracker.trackAsync(
    baseURL:String = "https://com.shabinder.spotiflyer/",
    requestBuilder: PiwikRequest.() -> Unit = {}
) {
    val req = PiwikRequest(
        1,
        URL(baseURL)
    ).apply { requestBuilder() }
    // Send Request
    sendRequestAsync(req)
}

fun PiwikTracker.trackScreenAsync(
    screenAddress:String,
    requestBuilder: PiwikRequest.() -> Unit = {}
) {
    val req = PiwikRequest(
        1,
        URL("https://com.shabinder.spotiflyer/" + screenAddress.removeSurrounding("/"))
    ).apply { requestBuilder() }
    // Send Request
    sendRequestAsync(req)
}