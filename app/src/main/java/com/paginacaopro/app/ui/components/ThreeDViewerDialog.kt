package com.paginacaopro.app.ui.components

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.paginacaopro.app.model.FloorPlanProject
import com.paginacaopro.app.model.TilingResult
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ThreeDViewerDialog(
    tilingResult: TilingResult,
    floorPlanProject: FloorPlanProject? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0F172A)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val wallJson = remember(tilingResult) {
                    val root = JSONObject()
                    root.put("wallName", tilingResult.wall.wallName)
                    root.put("lengthCm", tilingResult.wall.lengthCm)
                    root.put("heightCm", tilingResult.wall.heightCm)
                    root.put("thicknessCm", tilingResult.wall.thicknessCm)

                    val coursesArr = JSONArray()
                    for (course in tilingResult.courses) {
                        val cArr = JSONArray()
                        for (b in course) {
                            val bObj = JSONObject()
                            bObj.put("xCm", b.xCm)
                            bObj.put("yCm", b.yCm)
                            bObj.put("lengthCm", b.lengthCm)
                            bObj.put("heightCm", b.heightCm)
                            bObj.put("thicknessCm", b.thicknessCm)
                            bObj.put("pieceType", b.pieceType.name)
                            bObj.put("notes", b.notes)
                            bObj.put("countsInQuantity", b.countsInQuantity)
                            cArr.put(bObj)
                        }
                        coursesArr.put(cArr)
                    }
                    root.put("courses", coursesArr)
                    root.toString()
                }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            settings.cacheMode = WebSettings.LOAD_NO_CACHE
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    view?.evaluateJavascript("window.loadWallData($wallJson);", null)
                                }
                            }
                            loadUrl("file:///android_asset/viewer3d.html")
                        }
                    },
                    update = { view ->
                        view.evaluateJavascript("window.loadWallData($wallJson);", null)
                    }
                )

                // Botão Fechar no Topo Direito (abaixo da barra de status)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 12.dp, top = 48.dp)
                        .background(Color(0xFF0F172A).copy(alpha = 0.85f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fechar Visualizador 3D",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
