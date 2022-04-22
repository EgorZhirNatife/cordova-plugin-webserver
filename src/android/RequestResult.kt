package webserverplugin

import io.ktor.http.*

data class RequestResult(
    val contentType: ContentType,
    val text: String,
    val status: HttpStatusCode
)
