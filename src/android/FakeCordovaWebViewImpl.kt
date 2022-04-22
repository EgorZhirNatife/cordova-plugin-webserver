package webserverplugin

import android.content.Context
import android.content.Intent
import android.view.View
import android.webkit.WebChromeClient
import org.apache.cordova.*

class FakeCordovaWebViewImpl(private val callback: (PluginResult?, String?) -> Unit) :
    CordovaWebView {

    override fun sendPluginResult(cr: PluginResult?, callbackId: String?) {
        callback(cr, callbackId)
    }

    override fun init(
        cordova: CordovaInterface?,
        pluginEntries: MutableList<PluginEntry>?,
        preferences: CordovaPreferences?
    ) {
        TODO("Not yet implemented")
    }

    override fun isInitialized(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getView(): View {
        TODO("Not yet implemented")
    }

    override fun loadUrlIntoView(url: String?, recreatePlugins: Boolean) {
        TODO("Not yet implemented")
    }

    override fun stopLoading() {
        TODO("Not yet implemented")
    }

    override fun canGoBack(): Boolean {
        TODO("Not yet implemented")
    }

    override fun clearCache() {
        TODO("Not yet implemented")
    }

    override fun clearCache(b: Boolean) {
        TODO("Not yet implemented")
    }

    override fun clearHistory() {
        TODO("Not yet implemented")
    }

    override fun backHistory(): Boolean {
        TODO("Not yet implemented")
    }

    override fun handlePause(keepRunning: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onNewIntent(intent: Intent?) {
        TODO("Not yet implemented")
    }

    override fun handleResume(keepRunning: Boolean) {
        TODO("Not yet implemented")
    }

    override fun handleStart() {
        TODO("Not yet implemented")
    }

    override fun handleStop() {
        TODO("Not yet implemented")
    }

    override fun handleDestroy() {
        TODO("Not yet implemented")
    }

    override fun sendJavascript(statememt: String?) {
        TODO("Not yet implemented")
    }

    override fun showWebPage(
        url: String?,
        openExternal: Boolean,
        clearHistory: Boolean,
        params: MutableMap<String, Any>?
    ) {
        TODO("Not yet implemented")
    }

    override fun isCustomViewShowing(): Boolean {
        TODO("Not yet implemented")
    }

    override fun showCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        TODO("Not yet implemented")
    }

    override fun hideCustomView() {
        TODO("Not yet implemented")
    }

    override fun getResourceApi(): CordovaResourceApi {
        TODO("Not yet implemented")
    }

    override fun setButtonPlumbedToJs(keyCode: Int, override: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isButtonPlumbedToJs(keyCode: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getPluginManager(): PluginManager {
        TODO("Not yet implemented")
    }

    override fun getEngine(): CordovaWebViewEngine {
        TODO("Not yet implemented")
    }

    override fun getPreferences(): CordovaPreferences {
        TODO("Not yet implemented")
    }

    override fun getCookieManager(): ICordovaCookieManager {
        TODO("Not yet implemented")
    }

    override fun getUrl(): String {
        TODO("Not yet implemented")
    }

    override fun getContext(): Context {
        TODO("Not yet implemented")
    }

    override fun loadUrl(url: String?) {
        TODO("Not yet implemented")
    }

    override fun postMessage(id: String?, data: Any?): Any {
        TODO("Not yet implemented")
    }
}