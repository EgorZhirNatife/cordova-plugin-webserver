package webserverplugin

import android.content.Context
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
import org.apache.cordova.CallbackContext
import org.apache.cordova.PluginManager
import webserverplugin.util.RequestErrorMapper
import webserverplugin.util.respondRequestResult

class WebServer private constructor(private val applicationContext: Context) {

    private var pluginManager: PluginManager? = null
    private var job: CompletableJob? = null
    private var scope: CoroutineScope? = null
    private val server by lazy {
        createServer()
    }

    fun start() {
        job = SupervisorJob()
        scope = job?.let { CoroutineScope(Dispatchers.IO + it) }

        scope?.launch {
            server.start(wait = true)
        }
    }

    fun stop() {
        server.stop(1000, 2000)
        job?.cancelChildren()
    }

    private fun createServer(): ApplicationEngine {
        return embeddedServer(factory = Netty, environment = applicationEngineEnvironment {
            connector {
                port = PORT
                host = HOST
            }
            /*
            sslConnector(
                keyStore = SslCredentials.keyStore,
                keyAlias = SslCredentials.ALIAS,
                keyStorePassword = { SslCredentials.PASSWORD.toCharArray() },
                privateKeyPassword = { "".toCharArray() }) {
                port = 8181
                keyStorePath = SslCredentials.keyStoreFile.absoluteFile
            }
             */
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
                    get("/$STATIC_CONTENT/{...}") { // http://127.0.0.1:8080/static-content/www/index.html
                        serveStaticContent(this)
                    }
                    post("/$CORDOVA_REQUEST") { // http://127.0.0.1:8080/cordova-request
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
                val action = parameters[PARAM_ACTION]
                val args = parameters[PARAM_ARGS]
                val callbackId = "callbackId$service${(100000000..999999999).random()}"

                if (pluginManager == null) {
                    call.respondRequestResult(RequestErrorMapper.pluginManagerIsNull())
                    return@apply
                }

                val plugin = pluginManager?.getPlugin(service)
                if (plugin == null) {
                    call.respondRequestResult(RequestErrorMapper.serviceNotFound())
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
                    call.respondRequestResult(RequestErrorMapper.invalidExecutionAction())
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

        const val PARAM_ACTION = "action"
        const val PARAM_SERVICE = "service"
        const val PARAM_ARGS = "args"

        const val STATIC_CONTENT = "static-content"
        const val CORDOVA_REQUEST = "cordova-request"

        const val CORDOVA_REQUEST_TIMEOUT = 5000

        //        const val HOST = "127.0.0.1"
        const val HOST = "192.168.0.101"
        const val PORT = 8080

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
