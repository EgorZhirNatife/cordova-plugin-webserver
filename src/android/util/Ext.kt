package webserverplugin.util

import io.ktor.application.*
import io.ktor.response.*
import webserverplugin.RequestResult

fun String.wrapInSuccess(): String {
    return "{ \"success\": [$this] }"
}

fun String.wrapInError(): String {
    return "{ \"error\": [$this] }"
}

suspend fun ApplicationCall.respondRequestResult(requestResult: RequestResult) {
    this.respondText(
        contentType = requestResult.contentType,
        text = requestResult.text,
        status = requestResult.status
    )
}