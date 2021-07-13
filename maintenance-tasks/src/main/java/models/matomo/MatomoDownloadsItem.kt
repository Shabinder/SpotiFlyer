package models.matomo

import kotlinx.serialization.Serializable

@Serializable
data class MatomoDownloadsItem(
    val idsubdatatable: Int = 0,
    val label: String = "com.shabinder.spotiflyer",
    val nb_hits: Int = 0,
    val nb_visits: Int = 0,
    val sum_time_spent: Int = 0
)
