package localhostwebserver

import android.content.Intent
import androidx.core.content.ContextCompat.startForegroundService
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CallbackContext
import org.json.JSONArray
import org.json.JSONException

/**
 * This class echoes a string called from JavaScript.
 */
class LocalhostWebServer : CordovaPlugin() {

//    override fun pluginInitialize() {
//        super.pluginInitialize()
//        // Start server
//        val intent = Intent(cordova.activity, WebServerService::class.java)
//        startForegroundService(cordova.activity.applicationContext, intent)
//    }


    @Throws(JSONException::class)
    override fun execute(action: String, args: JSONArray, callbackContext: CallbackContext): Boolean {
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
        val intent = Intent(cordova.activity, WebServerService::class.java)
        intent.action = "start"
        startForegroundService(cordova.activity.applicationContext, intent)
        return true
    }

    private fun stopServer(callbackContext: CallbackContext): Boolean {
        val intent = Intent(cordova.activity, WebServerService::class.java)
        intent.action = "stop"
        startForegroundService(cordova.activity.applicationContext, intent)
        return true
    }
}
