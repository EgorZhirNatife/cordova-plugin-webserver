package localhostwebserver

import android.content.Context
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.network.tls.certificates.*
import io.ktor.network.tls.extensions.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import kotlin.text.toCharArray

// http://127.0.0.1:8080/static/www/index.html
class WebServer(private val applicationContext: Context) {

    private val server by lazy {
        createServer()
    }
    private val keyStoreFile by lazy {
        File("build/keystore.jks")
    }

    fun start() {
        CoroutineScope(Dispatchers.Default).launch { server.start(wait = true) }
    }

    fun stop() {
        server.stop(1000, 2000)
    }
    
    private fun createServer(): ApplicationEngine {
//        val keyStore = try {
//            createKeyStore()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }

//        val keystore = generateCertificate(
//            file = keyStoreFile,
//            keyAlias = ALIAS,
//            keyPassword = "",
//            jksPassword = PASSWORD
//        )

//        keyStore?.saveToFile(keyStoreFile, PASSWORD)
//        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
//        val sslContext: SSLContext = SSLContext.getInstance("TLS")
//        tmf.init(keyStore)
//        sslContext.init(null, tmf.trustManagers, null)
//        val x509TrustManager = tmf.trustManagers.first { it is X509TrustManager } as X509TrustManager
        return embeddedServer(factory = Netty, environment = applicationEngineEnvironment {
            connector {
                port = PORT
                host = HOST
            }
//            sslConnector(
//                keyStore = keyStore!!,
//                keyAlias = ALIAS_SHA256ECDSA,
//                keyStorePassword = { PASSWORD.toCharArray() },
//                privateKeyPassword = { PASSWORD.toCharArray() }) {
//                port = 8181
//                keyStorePath = keyStoreFile.absoluteFile
//            }
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
                    get("/$STATIC_CONTENT/{...}") {
                        serveStaticContent(this)
                    }
                    post("/$CORDOVA_EXECUTE/{...}") {
                        handleCordovaRequest(this)
                    }
                }
            }
        })
    }

    private fun handleCordovaRequest(context: PipelineContext<Unit, ApplicationCall>) {

    }

    private suspend fun serveStaticContent(context: PipelineContext<Unit, ApplicationCall>) {
        var status: HttpStatusCode
        var result: String
        var contentType: ContentType
        try {
            val route = context.call.request.path().trimMargin("/$STATIC_CONTENT/")
            contentType = ContentType.defaultForFilePath(route)
            result = applicationContext.assets.open(route).bufferedReader().use { it.readText() }
            status = HttpStatusCode.OK
        } catch (e: Exception) {
            result = HttpStatusCode.NotFound.toString()
            status = HttpStatusCode.NotFound
            contentType = ContentType.Any
        }
        context.call.respondText(
            text = result,
            contentType = contentType,
            status = status
        )
    }

    private fun createKeyStore() = buildKeyStore {
        certificate("sha384ecdsa") {
            hash = HashAlgorithm.SHA384
            sign = SignatureAlgorithm.ECDSA
            keySizeInBits = 384
            password = PASSWORD
        }
        certificate("sha256ecdsa") {
            hash = HashAlgorithm.SHA256
            sign = SignatureAlgorithm.ECDSA
            keySizeInBits = 256
            password = PASSWORD
        }
        certificate("sha384rsa") {
            hash = HashAlgorithm.SHA384
            sign = SignatureAlgorithm.RSA
            keySizeInBits = 1024
            password = PASSWORD
        }
        certificate("sha1rsa") {
            hash = HashAlgorithm.SHA1
            sign = SignatureAlgorithm.RSA
            keySizeInBits = 1024
            password = PASSWORD
        }
    }
    
    companion object {
        const val STATIC_CONTENT = "static"
        const val CORDOVA_EXECUTE = "exec"
        const val HOST = "127.0.0.1"
        const val PORT = 8080
        const val PASSWORD = "password"
        const val ALIAS_SHA384ECDSA = "sha384ecdsa"
        const val ALIAS_SHA256ECDSA = "sha256ecdsa"
        const val ALIAS_SHA384RSA = "sha384rsa"
        const val ALIAS_SHA1RSA = "sha1rsa"
    }
}
