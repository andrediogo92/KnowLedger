package pt.um.lei.masb.agent.data.feed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class AdafruitPublishJSON(
    var value: String = "",
    var lat: String = "",
    var lon: String = "",
    var alt: String = "",
    @SerialName("created_at")
    var createdAt: String = ""
)


