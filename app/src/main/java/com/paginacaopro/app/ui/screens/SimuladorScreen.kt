package com.paginacaopro.app.ui.screens

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import java.io.File

/**
 * Simulador Paginação Pro completo dentro do app (100% das funções do simulador HTML):
 * paginação, amarração, compositor de vão, troca de medidas, mover em fiadas par/ímpar,
 * paginador inteligente que aprende (localStorage), legenda flutuante, cortes AutoCAD,
 * exportação SVG/DXF, 3D, quantitativos, etc.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SimuladorScreen(onExit: () -> Unit) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BackHandler {
        val wv = webViewRef
        if (wv != null && wv.canGoBack()) wv.goBack() else onExit()
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                WebView.setWebContentsDebuggingEnabled(true)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true // Persistência dos padrões aprendidos (localStorage)
                settings.databaseEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.setSupportZoom(true)
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.mediaPlaybackRequiresUserGesture = false
                webViewClient = WebViewClient()
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        android.util.Log.d("SIMULADOR_CONSOLE", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                        return true
                    }
                }
                addJavascriptInterface(SimuladorBridge(ctx, onExit), "AndroidBridge")
                loadUrl("file:///android_asset/simulador.html")
                webViewRef = this
            }
        }
    )
}

/** Recebe arquivos exportados pelo simulador (SVG/DXF), salva em Downloads e abre o compartilhamento. */
class SimuladorBridge(private val context: Context, private val onExit: () -> Unit) {
    @JavascriptInterface
    fun goBackHome() {
        val main = android.os.Handler(context.mainLooper)
        main.post { onExit() }
    }

    @JavascriptInterface
    fun saveFile(name: String, mime: String, base64: String) {
        val bytes = try { Base64.decode(base64, Base64.DEFAULT) } catch (e: Exception) { return }
        val cleanMime = mime.substringBefore(";").ifBlank { "application/octet-stream" }
        val main = android.os.Handler(context.mainLooper)
        try {
            // 1) Salva em Downloads/PaginacaoPro
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, name)
                    put(MediaStore.Downloads.MIME_TYPE, cleanMime)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PaginacaoPro")
                }
                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                    context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                }
            }
            // 2) Cópia em cache para compartilhar
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(dir, name).apply { writeBytes(bytes) }
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            main.post {
                Toast.makeText(context, "Salvo em Downloads/PaginacaoPro: $name", Toast.LENGTH_LONG).show()
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = cleanMime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(share, "Compartilhar $name").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        } catch (e: Exception) {
            main.post { Toast.makeText(context, "Erro ao exportar: ${e.message}", Toast.LENGTH_LONG).show() }
        }
    }
}
