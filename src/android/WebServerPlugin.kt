package webserverplugin

import android.content.Intent
import androidx.core.content.ContextCompat.startForegroundService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONException

/**
 * This class echoes a string called from JavaScript.
 */
class WebServerPlugin : CordovaPlugin() {

    private val server by lazy {
        WebServer.getInstance(cordova.context)
    }
    private var job = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.IO + job)

    override fun pluginInitialize() {
        super.pluginInitialize()
        server.setPluginManager(webView.pluginManager)
    }

    @Throws(JSONException::class)
    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        return when (action) {
            "startServer" -> {
                startServer(callbackContext)
            }
            "stopServer" -> {
                stopServer(callbackContext)
            }
            else -> {
                false
            }
        }
    }

    private fun startServer(callbackContext: CallbackContext): Boolean {
        try {
            if (server.isRunning) {
                callbackContext.sendPluginResult(PluginResult(PluginResult.Status.OK, "already running"))
                return true
            }
            scope.launch {
                server.pluginResultForStart.collect { pluginResult ->
                    callbackContext.sendPluginResult(pluginResult)
                    cancel()
                }
            }
            val intent = Intent(cordova.activity, WebServerService::class.java)
            intent.action = "start"
            startForegroundService(cordova.activity.applicationContext, intent)
        } catch (e: Exception) {
            callbackContext.sendPluginResult(PluginResult(PluginResult.Status.ERROR, e.message))
            return false
        }
        return true
    }

    private fun stopServer(callbackContext: CallbackContext): Boolean {
        try {
            if (!server.isRunning) {
                callbackContext.sendPluginResult(PluginResult(PluginResult.Status.OK, "already stopped"))
                return true
            }
            scope.launch {
                server.pluginResultForStop.collect { pluginResult ->
                    callbackContext.sendPluginResult(pluginResult)
                    cancel()
                }
            }

            val intent = Intent(cordova.activity, WebServerService::class.java)
            intent.action = "stop"
            startForegroundService(cordova.activity.applicationContext, intent)
        } catch (e: Exception) {
            callbackContext.sendPluginResult(PluginResult(PluginResult.Status.ERROR, e.message))
            return false
        }
        return true
    }
}
