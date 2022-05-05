package webserverplugin

import android.content.Context
import android.util.Log
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import org.apache.cordova.CallbackContext
import org.apache.cordova.PluginManager
import org.apache.cordova.PluginResult
import webserverplugin.util.RequestErrorMapper
import webserverplugin.util.respondRequestResult

class WebServer private constructor(private val applicationContext: Context) {
    val pluginResultForStart = MutableSharedFlow<PluginResult>()
    val pluginResultForStop = MutableSharedFlow<PluginResult>()
    var isRunning = false
    private var pluginManager: PluginManager? = null
    private var job: CompletableJob? = null
    private var scope: CoroutineScope? = null
    private var server: ApplicationEngine? = null

    fun start() {
        if (isRunning) {
            CoroutineScope(Dispatchers.Default).launch {
                pluginResultForStart.emit(PluginResult(PluginResult.Status.OK, "already running"))
            }
            return
        }
        try {
            job = SupervisorJob()
            scope = job?.let { CoroutineScope(Dispatchers.IO + it) }
            server = createServer()
            server?.start(wait = false)
            isRunning = true
            CoroutineScope(Dispatchers.Default).launch {
                pluginResultForStart.emit(
                    PluginResult(
                        PluginResult.Status.OK,
                        "successfully started"
                    )
                )
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Default).launch {
                pluginResultForStart.emit(PluginResult(PluginResult.Status.ERROR, e.toString()))
            }
        }
    }

    fun stop() {
        if (!isRunning) {
            CoroutineScope(Dispatchers.Default).launch {
                pluginResultForStop.emit(PluginResult(PluginResult.Status.OK, "already stopped"))
            }
            return
        }
        try {
            server?.stop(0, 0)
            server = null
            job?.cancelChildren()
            isRunning = false
            CoroutineScope(Dispatchers.Default).launch {
                pluginResultForStop.emit(
                    PluginResult(
                        PluginResult.Status.OK,
                        "successfully stopped"
                    )
                )
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Default).launch {
                pluginResultForStop.emit(PluginResult(PluginResult.Status.ERROR, e.toString()))
            }
        }
    }

    private fun createServer(): ApplicationEngine {
        return embeddedServer(factory = Netty, environment = applicationEngineEnvironment {

            connector {
                host = HOST
                port = HTTP_PORT
            }

            module {
                install(WebSockets)
                install(CallLogging)
                install(ContentNegotiation) {
                    gson {
                        setPrettyPrinting()
                        disableHtmlEscaping()
                    }
                }
                install(CORS) {
                    method(HttpMethod.Get)
                    method(HttpMethod.Post)
                    anyHost()
                }
                install(Compression) {
                    gzip()
                }
                routing {
                    get("/$STATIC_CONTENT/{...}") { // http://localhost:3005/static-content/www/index.html
                        serveStaticContent(this)
                    }
                    post("/$CORDOVA_REQUEST") { // http://localhost:3005/cordova-request
                        handleCordovaRequest(this)
                    }
                }
            }
        })
    }

    private suspend fun handleCordovaRequest(context: PipelineContext<Unit, ApplicationCall>) {
        context.apply {
            try {
                val startRequestTime = System.currentTimeMillis()
                var isRequestFinished = false
                val parameters = call.receiveParameters()
                val service = parameters[PARAM_SERVICE]
                val action = parameters[PARAM_ACTION] ?: ""
                val args = parameters[PARAM_ARGS] ?: "[]"
                val callbackId = "callbackId$service${(100000000..999999999).random()}"
                Log.d("tag", "cordova-request:\nservice: $service\naction: $action\nargs: $args")
                if (pluginManager == null) {
                    call.respondRequestResult(RequestErrorMapper.pluginManagerIsNull())
                    return@apply
                }

                val plugin = pluginManager?.getPlugin(service)
                if (plugin == null) {
                    call.respondRequestResult(RequestErrorMapper.serviceNotFound(service))
                    return@apply
                }

                val fakeWebView = FakeCordovaWebViewImpl { pluginResponse, responseCallbackId ->
                    scope?.launch {
                        call.respondRequestResult(
                            RequestErrorMapper.handlePluginResult(
                                pluginResponse = pluginResponse,
                                responseCallbackId = responseCallbackId,
                                callbackId = callbackId
                            )
                        )
                        isRequestFinished = true
                    }
                }

                val callbackContext = CallbackContext(callbackId, fakeWebView)
                val wasValidAction = plugin.execute(action, args, callbackContext)
                if (!wasValidAction) {
                    call.respondRequestResult(RequestErrorMapper.invalidExecutionAction(action))
                    return@apply
                }

                while (!isRequestFinished) {
                    if (System.currentTimeMillis() - startRequestTime > CORDOVA_REQUEST_TIMEOUT) {
                        call.respondRequestResult(RequestErrorMapper.gatewayTimeout())
                        isRequestFinished = true
                    }
                    delay(25)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondRequestResult(RequestErrorMapper.internalServerError(e))
            }
        }
    }

    private suspend fun serveStaticContent(context: PipelineContext<Unit, ApplicationCall>) {
        context.apply {
            try {
                val path = call.request.path().trimMargin("/$STATIC_CONTENT/")
                call.respondBytes(
                    contentType = ContentType.defaultForFilePath(path),
                    bytes = applicationContext.assets.open(path).readBytes(),
                    status = HttpStatusCode.OK
                )
            } catch (e: Exception) {
                call.respondRequestResult(RequestErrorMapper.notFoundStaticContent())
            }
        }
    }

    fun setPluginManager(pluginManager: PluginManager) {
        this.pluginManager = pluginManager
    }

    companion object {

        const val PARAM_SERVICE = "service"
        const val PARAM_ACTION = "action"
        const val PARAM_ARGS = "args"

        const val STATIC_CONTENT = "static-content"
        const val CORDOVA_REQUEST = "cordova-request"

        const val CORDOVA_REQUEST_TIMEOUT = 5000

        const val HOST = "127.0.0.1"
        const val HTTP_PORT = 3005

        private var INSTANCE: WebServer? = null

        @Synchronized
        fun getInstance(applicationContext: Context): WebServer {
            if (INSTANCE == null) {
                INSTANCE = WebServer(applicationContext)
            }
            return INSTANCE!!
        }
    }
}
