package org.knowledger.agent.data.feed

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class AdafruitPublish(
    var value: String = "",
    var lat: String = "",
    var lon: String = "",
    var alt: String = "",
    var created_at: String = ""
)


