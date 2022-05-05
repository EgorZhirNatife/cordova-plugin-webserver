package webserverplugin.util

import io.ktor.http.*
import org.apache.cordova.PluginResult
import webserverplugin.RequestResult

object RequestErrorMapper {

    private const val ACTION_EXECUTION_IS_INVALID = "invalid action execution: "
    private const val SERVICE_NOT_FOUND = "service not found: "
    private const val PLUGIN_MANAGER_IS_NULL = "pluginManager is null"
    private const val GATEWAY_TIMEOUT = "gateway timeout"
    private const val DIFFERENT_CALLBACK_ID = "different callbackId"
    private const val PLUGIN_RESPONSE_IS_NULL = "plugin response is null"

    fun handlePluginResult(
        pluginResponse: PluginResult?,
        responseCallbackId: String?,
        callbackId: String
    ): RequestResult {
        if (responseCallbackId != callbackId) {
            return differentCallbackId()
        } else if (pluginResponse == null) {
            return pluginResponseIsNull()
        }

        return when (pluginResponse.status) {
            PluginResult.Status.OK.ordinal -> {
                RequestResult(
                    contentType = ContentType.Text.Plain,
                    text = pluginResponse.message.wrapInSuccess(),
                    status = HttpStatusCode.OK
                )
            }
            PluginResult.Status.NO_RESULT.ordinal -> {
                RequestResult(
                    contentType = ContentType.Text.Plain,
                    text = pluginResponse.message.wrapInSuccess(),
                    status = HttpStatusCode.NotFound
                )
            }
            PluginResult.Status.CLASS_NOT_FOUND_EXCEPTION.ordinal -> {
                RequestResult(
                    contentType = ContentType.Text.Plain,
                    text = pluginResponse.message.wrapInSuccess(),
                    status = HttpStatusCode.NotFound
                )
            }
            PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION.ordinal -> {
                RequestResult(
                    contentType = ContentType.Text.Plain,
                    text = pluginResponse.message.wrapInSuccess(),
                    status = HttpStatusCode.NotFound
                )
            }
            PluginResult.Status.INSTANTIATION_EXCEPTION.ordinal -> {
                RequestResult(
                    contentType = ContentType.Text.Plain,
                    text = pluginResponse.message.wrapInSuccess(),
                    status = HttpStatusCode.NotFound
                )
            }
            PluginResult.Status.MALFORMED_URL_EXCEPTION.ordinal -> {
                RequestResult(
                    contentType = ContentType.Text.Plain,
                    text = pluginResponse.message.wrapInSuccess(),
                    status = HttpStatusCode.NotFound
                )
            }
            PluginResult.Status.IO_EXCEPTION.ordinal -> {
                RequestResult(
                    contentType = ContentType.Text.Plain,
                    text = pluginResponse.message.wrapInSuccess(),
                    status = HttpStatusCode.NotFound
                )
            }
            PluginResult.Status.INVALID_ACTION.ordinal -> {
                RequestResult(
                    contentType = ContentType.Text.Plain,
                    text = pluginResponse.message.wrapInSuccess(),
                    status = HttpStatusCode.NotFound
                )
            }
            PluginResult.Status.JSON_EXCEPTION.ordinal -> {
                RequestResult(
                    contentType = ContentType.Text.Plain,
                    text = pluginResponse.message.wrapInSuccess(),
                    status = HttpStatusCode.NotFound
                )
            }
            PluginResult.Status.ERROR.ordinal -> {
                RequestResult(
                    contentType = ContentType.Text.Plain,
                    text = pluginResponse.message.wrapInSuccess(),
                    status = HttpStatusCode.NotFound
                )
            }
            else -> {
                RequestResult(
                    contentType = ContentType.Text.Plain,
                    text = pluginResponse.message.wrapInSuccess(),
                    status = HttpStatusCode.NotFound
                )
            }
        }
    }

    fun gatewayTimeout() = RequestResult(
        contentType = ContentType.Text.Plain,
        text = GATEWAY_TIMEOUT.wrapInError(),
        status = HttpStatusCode.GatewayTimeout
    )

    fun invalidExecutionAction(action: String?) = RequestResult(
        contentType = ContentType.Text.Plain,
        text = ACTION_EXECUTION_IS_INVALID.plus(action).wrapInError(),
        status = HttpStatusCode.NotFound
    )

    fun serviceNotFound(service: String?) = RequestResult(
        contentType = ContentType.Text.Plain,
        text = SERVICE_NOT_FOUND.plus(service).wrapInError(),
        status = HttpStatusCode.NotFound
    )

    fun pluginManagerIsNull() = RequestResult(
        contentType = ContentType.Text.Plain,
        text = PLUGIN_MANAGER_IS_NULL.wrapInError(),
        status = HttpStatusCode.InternalServerError
    )

    fun differentCallbackId() = RequestResult(
        contentType = ContentType.Text.Plain,
        text = DIFFERENT_CALLBACK_ID.wrapInError(),
        status = HttpStatusCode.NotFound
    )

    fun pluginResponseIsNull() = RequestResult(
        contentType = ContentType.Text.Plain,
        text = PLUGIN_RESPONSE_IS_NULL.wrapInError(),
        status = HttpStatusCode.NotFound
    )

    fun internalServerError(e: Exception) = RequestResult(
        contentType = ContentType.Text.Plain,
        text = e.toString().wrapInError(),
        status = HttpStatusCode.InternalServerError
    )

    fun notFoundStaticContent() = RequestResult(
        contentType = ContentType.Any,
        text = HttpStatusCode.NotFound.toString(),
        status = HttpStatusCode.NotFound
    )
}
