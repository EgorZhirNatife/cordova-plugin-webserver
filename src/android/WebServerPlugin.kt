package webserverplugin

import android.content.Intent
import androidx.core.content.ContextCompat.startForegroundService
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONException

/**
 * This class echoes a string called from JavaScript.
 */
class WebServerPlugin : CordovaPlugin() {

    override fun pluginInitialize() {
        super.pluginInitialize()
        WebServer.getInstance(cordova.context).setPluginManager(webView.pluginManager)
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
            val intent = Intent(cordova.activity, WebServerService::class.java)
            intent.action = "start"
            startForegroundService(cordova.activity.applicationContext, intent)
            callbackContext.sendPluginResult(PluginResult(PluginResult.Status.OK))
        } catch (e: Exception) {
            callbackContext.sendPluginResult(PluginResult(PluginResult.Status.ERROR, e.message))
            return false
        }
        return true
    }

    private fun stopServer(callbackContext: CallbackContext): Boolean {
        try {
            val intent = Intent(cordova.activity, WebServerService::class.java)
            intent.action = "stop"
            startForegroundService(cordova.activity.applicationContext, intent)
            callbackContext.sendPluginResult(PluginResult(PluginResult.Status.OK))
        } catch (e: Exception) {
            callbackContext.sendPluginResult(PluginResult(PluginResult.Status.ERROR, e.message))
            return false
        }
        return true
    }
}
