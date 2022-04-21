package localhostwebserver

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.cordova.CallbackContext
import org.apache.cordova.PluginManager

class WebServer private constructor(private val applicationContext: Context) {

    private var pluginManager: PluginManager? = null
    private val server by lazy {
        createServer()
    }

    fun start() {
        CoroutineScope(Dispatchers.Default).launch { server.start(wait = true) }
    }

    fun stop() {
        server.stop(1000, 2000)
    }

    private fun createServer(): ApplicationEngine {
        return embeddedServer(factory = Netty, environment = applicationEngineEnvironment {
            connector {
                port = PORT
                host = HOST
                Log.d("tag", host)
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
                    get("/$STATIC_CONTENT/{...}") { // http://127.0.0.1:8080/static/www/index.html
                        serveStaticContent(this)
                    }
                    post("/$CORDOVA_REQUEST/{...}") { // http://127.0.0.1:8080/cordova-request
                        handleCordovaRequest(this)
                    }
                }
            }
        })
    }

    private suspend fun handleCordovaRequest(context: PipelineContext<Unit, ApplicationCall>) {
        var isRequestFinished = false
        context.apply {
            val parameters = call.receiveParameters()
            val service = parameters[PARAM_SERVICE]
            val action = parameters[PARAM_ACTION]
            val args = parameters[PARAM_ARGS]
            val callbackId = "callbackId$service${(100000000..999999999).random()}"
            if (pluginManager == null) {
                call.respondText(contentType = ContentType.Text.Plain, text = "pluginManager is null", status = HttpStatusCode.InternalServerError)
                return@apply
            }

            val plugin = pluginManager?.getPlugin(service)
            if (plugin == null) {
                call.respondText(contentType = ContentType.Text.Plain, text = "service not found", status = HttpStatusCode.NotFound)
                return@apply
            }

            val fakeWebView = FakeCordovaWebViewImpl { pluginResult, callbackId ->
                pluginResult?.let { result ->
                    CoroutineScope(Dispatchers.IO).launch {
                        call.respondText(contentType = ContentType.Text.Plain, text = result.message, status = HttpStatusCode.OK)
                        isRequestFinished = true
                    }
                }
            }
            val callbackContext = CallbackContext(callbackId, fakeWebView)
            val wasValidAction = plugin.execute(action, args, callbackContext)
        }

        while (!isRequestFinished) { // TODO rework
//            delay(50)
        }
    }

    private suspend fun serveStaticContent(context: PipelineContext<Unit, ApplicationCall>) {
        context.apply {
            var status: HttpStatusCode
            var result: String
            var contentType: ContentType
            try {
                val route = call.request.path().trimMargin("/$STATIC_CONTENT/")
                contentType = ContentType.defaultForFilePath(route)
                result = applicationContext.assets.open(route).bufferedReader().use { it.readText() }
                status = HttpStatusCode.OK
            } catch (e: Exception) {
                result = HttpStatusCode.NotFound.toString()
                status = HttpStatusCode.NotFound
                contentType = ContentType.Any
            }
            call.respondText(
                text = result,
                contentType = contentType,
                status = status
            )
        }
    }

    fun setPluginManager(pluginManager: PluginManager) {
        this.pluginManager = pluginManager
    }

    companion object {

        const val PARAM_ACTION = "action"
        const val PARAM_SERVICE = "service"
        const val PARAM_ARGS = "args"
        const val STATIC_CONTENT = "static"
        const val CORDOVA_REQUEST = "cordova-request"
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
