package webserverplugin

import android.content.Context
import com.example.appwebserver.R
import java.io.File
import java.security.KeyStore

object SslCertificate {

    fun getKeyStore(context: Context): KeyStore {
        val file = getKeyStoreFile(context)
        if (!file.exists()) {
            file.writeBytes(
                context.resources.openRawResource(R.raw.keystore).readBytes()
            )
        }
        val inputStream = file.inputStream()

        val keyStore = KeyStore.getInstance(TYPE)
        keyStore.load(inputStream, FILE_PASSWORD.toCharArray())
        return keyStore
    }

    fun getKeyStoreFile(context: Context) = File(context.filesDir.path + KEYSTORE_PATH)

    const val KEY_ALIAS = "myalias"
    const val KEY_STORE_PASSWORD = "password"
    const val PRIVATE_KEY_PASSWORD = "password"
    private const val FILE_PASSWORD = "password"
    private const val KEYSTORE_PATH = "/keystore.bks"
    private const val TYPE = "BKS"
}
