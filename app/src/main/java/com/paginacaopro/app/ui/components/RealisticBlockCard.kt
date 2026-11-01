package com.paginacaopro.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paginacaopro.app.model.BlockModel
import com.paginacaopro.app.ui.theme.PrimaryBlue
import com.paginacaopro.app.ui.theme.Slate900
import com.paginacaopro.app.util.BlockRealisticImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Componente compacto para exibição do bloco gerado com IA 3D.
 * Reduzido para dar prioridade visual máxima ao SVG da parede.
 */
@Composable
fun RealisticBlockCard(
    block: BlockModel,
    modifier: Modifier = Modifier,
    onRegenerateAi: ((BlockModel) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    // Carregar imagem gerada pela IA ou gerar proceduralmente se não existir
    LaunchedEffect(block.id, block.imageUri, block.lengthCm, block.heightCm, block.thicknessCm, block.material) {
        withContext(Dispatchers.IO) {
            val loaded = BlockRealisticImageGenerator.getOrGenerateBlockBitmap(context, block)
            bitmap = loaded
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Imagem Miniatura IA 3D do Bloco
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Bloco IA ${block.name}",
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Fit
                    )
                } else if (isGenerating) {
                    CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                }

                // Tag IA no topo
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp),
                    color = Color.Black.copy(alpha = 0.70f),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        "IA 3D",
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Informações do Bloco
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "BLOCO SELECIONADO (IA):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = when {
                            block.material.contains("Concreto", ignoreCase = true) -> Color(0xFFF1F5F9)
                            block.name.contains("Baiano", ignoreCase = true) -> Color(0xFFFFEDD5)
                            else -> Color(0xFFFEE2E2)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            block.material.uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = when {
                                block.material.contains("Concreto", ignoreCase = true) -> Color(0xFF334155)
                                block.name.contains("Baiano", ignoreCase = true) -> Color(0xFFC2410C)
                                else -> Color(0xFFB91C1C)
                            },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    block.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    "Dimensões 1:1: ${block.lengthCm} × ${block.heightCm} × ${block.thicknessCm} cm",
                    fontSize = 10.sp,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
            }

            // Botão compacto para regerar com IA
            IconButton(
                onClick = {
                    isGenerating = true
                    coroutineScope.launch {
                        val newUri = BlockRealisticImageGenerator.generateAndSaveBlockImage(context, block)
                        val updated = block.copy(imageUri = newUri)
                        bitmap = BlockRealisticImageGenerator.loadBlockBitmap(context, newUri)
                        isGenerating = false
                        onRegenerateAi?.invoke(updated)
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Regerar Bloco IA",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
