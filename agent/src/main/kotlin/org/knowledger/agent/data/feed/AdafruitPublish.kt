package org.knowledger.agent.data.feed

import kotlinx.serialization.Serializable


@Serializable
data class AdafruitPublish(
    var value: String = "",
    var lat: String = "",
    var lon: String = "",
    var alt: String = "",
    var created_at: String = ""
)


