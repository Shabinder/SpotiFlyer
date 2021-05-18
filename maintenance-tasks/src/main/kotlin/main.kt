import analytics_html_img.updateAnalyticsImage
import utils.debug

fun main(args: Array<String>) {
    debug("fun main: args -> ${args.joinToString(";")}")

    // TASK -> Update Analytics Image in Readme
    updateAnalyticsImage()
}
