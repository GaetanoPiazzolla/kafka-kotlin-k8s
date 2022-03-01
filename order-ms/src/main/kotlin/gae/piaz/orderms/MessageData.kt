package gae.piaz.orderms

import com.fasterxml.jackson.annotation.JsonProperty

// https://kotlinlang.org/docs/data-classes.html

data class MessageData(
    @JsonProperty("message") val message: String,
    @JsonProperty("identifier") val identifier: Int
)