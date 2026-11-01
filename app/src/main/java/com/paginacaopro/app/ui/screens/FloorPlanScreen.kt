package com.paginacaopro.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.paginacaopro.app.model.ConsolidatedQuantitativo
import com.paginacaopro.app.model.FloorPlanProject
import com.paginacaopro.app.model.FloorPlanWall
import com.paginacaopro.app.model.OpeningModel
import com.paginacaopro.app.model.OpeningType
import com.paginacaopro.app.ui.theme.PrimaryBlue
import com.paginacaopro.app.ui.theme.SecondaryOrange
import com.paginacaopro.app.ui.theme.Slate800
import com.paginacaopro.app.ui.theme.Slate900
import com.paginacaopro.app.ui.viewmodel.PaginacaoViewModel
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorPlanScreen(
    viewModel: PaginacaoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToViewer: () -> Unit
) {
    val project by viewModel.floorPlanProject.collectAsState()
    val selectedWallId by viewModel.selectedFloorPlanWallId.collectAsState()
    val context = LocalContext.current

    var showAddWallDialog by remember { mutableStateOf(false) }
    var showAddOpeningDialog by remember { mutableStateOf(false) }
    var showConsolidatedReport by remember { mutableStateOf(false) }

    // Zoom, Pan, Redimensionamento e Visibilidade na Planta Baixa
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var isPlanVisible by remember { mutableStateOf(true) }
    var planHeightDp by remember { mutableFloatStateOf(240f) }

    // Estados da Elevação SVG / Corte AutoCAD da Parede Selecionada
    var isElevationAutoCad by remember { mutableStateOf(true) }
    var elevScale by remember { mutableFloatStateOf(1.0f) }
    var elevPanX by remember { mutableFloatStateOf(0f) }
    var elevPanY by remember { mutableFloatStateOf(0f) }

    val selectedWall = project.walls.firstOrNull { it.id == selectedWallId } ?: project.walls.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Planta Baixa da Obra", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "${project.name} • ${project.walls.size} paredes",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    // Botão Ocultar / Mostrar Planta Baixa
                    Button(
                        onClick = { isPlanVisible = !isPlanVisible },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPlanVisible) Color(0xFF1E293B) else Color(0xFFF59E0B)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(if (isPlanVisible) "Ocultar Planta" else "Mostrar Planta", fontSize = 10.sp, color = if (isPlanVisible) Color(0xFF38BDF8) else Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    // Modelo da Planta da Imagem (Jardim das Araucárias)
                    Button(
                        onClick = { viewModel.loadFloorPlanTemplateAraucarias() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Araucárias", fontSize = 10.sp, color = Color(0xFF38BDF8))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showAddWallDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Parede", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showConsolidatedReport = true },
                        modifier = Modifier.weight(1.6f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CALCULAR GERAL & SVGS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Seletor rápido horizontal das paredes (A, B, C, D, E, F, G, H)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Paredes:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                for (w in project.walls) {
                    val isSelected = w.id == selectedWall?.id
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clickable {
                                viewModel.selectFloorPlanWall(w.id)
                                elevScale = 1.0f
                                elevPanX = 0f
                                elevPanY = 0f
                            },
                        color = if (isSelected) PrimaryBlue else Color(0xFF1E293B),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155))
                    ) {
                        Text(
                            text = "Parede ${w.label} (${(w.lengthCm / 100.0)}m)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Barra de Seleção Global da Família/Espessura de Toda a Planta (9, 11.5, 14, 19 cm)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Família Toda a Planta:", color = Color(0xFFFDE047), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                listOf(9.0, 11.5, 14.0, 19.0).forEach { th ->
                    val isPlanThick = kotlin.math.abs(project.defaultThicknessCm - th) < 0.2
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clickable { viewModel.changeAllFloorPlanWallsThickness(th) },
                        color = if (isPlanThick) Color(0xFF0284C7) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPlanThick) Color(0xFF38BDF8) else Color(0xFF475569))
                    ) {
                        Text(
                            text = "${com.paginacaopro.app.engine.FreePlanEngine.formatClean(th)} cm",
                            color = if (isPlanThick) Color.White else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = if (isPlanThick) FontWeight.Black else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Canvas Gráfico da Planta Baixa (Movível, Redimensionável, Ocultável e Clicável em todas as paredes A..H)
            if (isPlanVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(planHeightDp.dp)
                        .background(Color(0xFF0A0F1D))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.4f, 6.0f)
                                panX += pan.x
                                panY += pan.y
                            }
                        }
                        .pointerInput(project, scale, panX, panY) {
                            detectTapGestures(
                                onTap = { tapOffset ->
                                    val meterPx = 28f * scale
                                    val canvasW = size.width.toFloat()
                                    val canvasH = size.height.toFloat()
                                    val offsetX = canvasW / 2f - (5f * meterPx) + panX
                                    val offsetY = canvasH / 2f - (4f * meterPx) + panY

                                    var closestWall: FloorPlanWall? = null
                                    var minDistance = Float.MAX_VALUE

                                    for (wall in project.walls) {
                                        val p1 = Offset(offsetX + wall.startX * meterPx, offsetY + wall.startY * meterPx)
                                        val p2 = Offset(offsetX + wall.endX * meterPx, offsetY + wall.endY * meterPx)
                                        val midX = (p1.x + p2.x) / 2f
                                        val midY = (p1.y + p2.y) / 2f

                                        val distCenter = sqrt((tapOffset.x - midX) * (tapOffset.x - midX) + (tapOffset.y - midY) * (tapOffset.y - midY))

                                        val dx = p2.x - p1.x
                                        val dy = p2.y - p1.y
                                        val lenSq = dx * dx + dy * dy
                                        val distSeg = if (lenSq > 0.001f) {
                                            val t = (((tapOffset.x - p1.x) * dx + (tapOffset.y - p1.y) * dy) / lenSq).coerceIn(0f, 1f)
                                            val projX = p1.x + t * dx
                                            val projY = p1.y + t * dy
                                            sqrt((tapOffset.x - projX) * (tapOffset.x - projX) + (tapOffset.y - projY) * (tapOffset.y - projY))
                                        } else distCenter

                                        val effDist = min(distCenter, distSeg)
                                        if (effDist < minDistance) {
                                            minDistance = effDist
                                            closestWall = wall
                                        }
                                    }

                                    // Se o toque foi perto de uma parede (raio de tolerância)
                                    if (closestWall != null && minDistance < 55f * scale.coerceIn(0.8f, 2.5f)) {
                                        viewModel.selectFloorPlanWall(closestWall.id)
                                        elevScale = 1.0f
                                        elevPanX = 0f
                                        elevPanY = 0f
                                    }
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawFloorPlanCanvas(
                            project = project,
                            selectedWallId = selectedWall?.id,
                            scale = scale,
                            panX = panX,
                            panY = panY
                        )
                    }

                    // Controles de Redimensionar / Centralizar / Ocultar Planta
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = {
                                planHeightDp = (planHeightDp - 40f).coerceAtLeast(140f)
                                scale = (scale * 0.88f).coerceAtLeast(0.4f)
                            },
                            color = Slate900.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Tam -", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Surface(
                            onClick = {
                                planHeightDp = (planHeightDp + 40f).coerceAtMost(400f)
                                scale = (scale * 1.15f).coerceAtMost(6.0f)
                            },
                            color = Slate900.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Tam +", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Surface(
                            onClick = {
                                scale = 1.0f
                                panX = 0f
                                panY = 0f
                            },
                            color = Slate900.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Centro", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    // Legenda de identificação flutuante
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        color = Slate900.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "👆 Toque na parede (A..H) para ver seu SVG • Pinça p/ zoom",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // =========================================================================
            // VISUALIZADOR SVG & CORTE AUTOCAD EM TEMPO REAL DA PAREDE SELECIONADA
            // Sempre à mostra, atualiza instantaneamente ao tocar nas paredes A..H
            // =========================================================================
            selectedWall?.let { wall ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val tilingResult by viewModel.tilingResult.collectAsState()
                    val currentResult = tilingResult

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Linha de Cabeçalho da Elevação SVG
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(PrimaryBlue, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(wall.label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "ELEVAÇÃO SVG • CORTE AUTOCAD",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            "Parede ${wall.label} (${(wall.lengthCm / 100.0)}m × ${(wall.heightCm / 100.0)}m • ${com.paginacaopro.app.engine.FreePlanEngine.formatClean(wall.thicknessCm)}cm)",
                                            fontSize = 11.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(
                                        onClick = { isElevationAutoCad = !isElevationAutoCad },
                                        color = if (isElevationAutoCad) Color(0xFF0284C7) else Color(0xFF334155),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            if (isElevationAutoCad) "📐 CAD" else "🎨 Cor",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        onClick = {
                                            val svg = viewModel.generateSvgOutput(
                                                isSimplified = false,
                                                cadStyle = if (isElevationAutoCad) com.paginacaopro.app.svg.CadStyle.AUTOCAD_DARK else com.paginacaopro.app.svg.CadStyle.CONSTRUCTION
                                            )
                                            com.paginacaopro.app.util.SvgExportUtil.shareSvgFile(context, svg, "parede_${wall.label}_corte_autocad")
                                        },
                                        color = Color(0xFF1E293B),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("SVG", color = Color(0xFFFDE047), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                                    }

                                    Surface(
                                        onClick = {
                                            viewModel.selectWallForIndividualTiling(wall)
                                            onNavigateToViewer()
                                        },
                                        color = Color(0xFF0284C7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Expandir", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // CANVAS GRÁFICO DA ELEVAÇÃO / CORTE AUTOCAD
                            if (currentResult != null && currentResult.isValid) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                        .background(if (isElevationAutoCad) Color(0xFF0D1117) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        .pointerInput(Unit) {
                                            detectTransformGestures { _, pan, zoom, _ ->
                                                elevScale = (elevScale * zoom).coerceIn(0.5f, 15.0f)
                                                elevPanX += pan.x
                                                elevPanY += pan.y
                                            }
                                        }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawTilingElevation(
                                            result = currentResult,
                                            selectedCourse = 0,
                                            isCotadaView = true,
                                            isAutoCadMode = isElevationAutoCad,
                                            scale = elevScale,
                                            panX = elevPanX,
                                            panY = elevPanY
                                        )
                                    }

                                    // Botão Reset Zoom da Elevação
                                    Surface(
                                        onClick = {
                                            elevScale = 1.0f
                                            elevPanX = 0f
                                            elevPanY = 0f
                                        },
                                        color = Slate900.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                    ) {
                                        Text("Reset", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Discriminação Rápida dos Blocos e Argamassa da Parede
                                val sizeGroups = remember(currentResult) { buildBlockSizeLegendItems(currentResult) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    sizeGroups.forEachIndexed { idx, item ->
                                        if (idx > 0) Text("•", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            "${item.shortLabel}: ${item.count} un",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                    Text("•", fontSize = 10.sp, color = Color.Gray)
                                    Text("Argamassa: ${currentResult.billOfMaterials.mortarJointVolumeM3Estimated} m³", fontSize = 11.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Calculando paginação da Parede ${wall.label}...", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Card de Características e Configuração de Espessura
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Dimensões da Parede ${wall.label}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                IconButton(onClick = { viewModel.removeWallFromFloorPlan(wall.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir Parede", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                InfoPill("Comprimento", "${wall.lengthCm} cm (${wall.lengthCm / 100.0} m)")
                                InfoPill("Altura", "${wall.heightCm} cm")
                                InfoPill("Espessura", "${com.paginacaopro.app.engine.FreePlanEngine.formatClean(wall.thicknessCm)} cm")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Seletor de Espessura Exclusivo para esta Parede
                            Text("Alterar espessura da Parede ${wall.label}:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(9.0, 11.5, 14.0, 19.0).forEach { th ->
                                    val isCurrent = kotlin.math.abs(wall.thicknessCm - th) < 0.2
                                    FilterChip(
                                        selected = isCurrent,
                                        onClick = { viewModel.changeFloorPlanWallThickness(wall.id, th) },
                                        label = {
                                            Text(
                                                "${com.paginacaopro.app.engine.FreePlanEngine.formatClean(th)} cm",
                                                fontSize = 11.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vãos da Parede Selecionada
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vãos de Portas e Janelas (${wall.openings.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        OutlinedButton(
                            onClick = { showAddOpeningDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Adicionar Vão", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (wall.openings.isEmpty()) {
                        Text(
                            "Nenhum vão nesta parede (parede cega).",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        wall.openings.forEach { op ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (op.type == OpeningType.DOOR) Icons.Default.DoorFront else Icons.Default.Window,
                                        contentDescription = null,
                                        tint = SecondaryOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(op.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(
                                            "${op.widthCm}×${op.heightCm} cm • Posição X: ${op.xCm} cm",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeOpeningFromFloorPlanWall(wall.id, op.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover Vão", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Diálogo para Adicionar Nova Parede à Planta
        if (showAddWallDialog) {
            AddWallToFloorPlanDialog(
                defaultHeight = project.defaultHeightCm,
                defaultThickness = project.defaultThicknessCm,
                onDismiss = { showAddWallDialog = false },
                onAdd = { name, len, h, th ->
                    viewModel.addWallToFloorPlan(name, len, h, th)
                    showAddWallDialog = false
                }
            )
        }

        // Diálogo para Adicionar Vão na Parede Selecionada
        if (showAddOpeningDialog && selectedWall != null) {
            AddOpeningDialog(
                onDismiss = { showAddOpeningDialog = false },
                onAddOpening = { op ->
                    viewModel.addOpeningToFloorPlanWall(selectedWall.id, op)
                    showAddOpeningDialog = false
                }
            )
        }

        // DIÁLOGO / MODAL DE QUANTITATIVO GERAL CONSOLIDADO DA OBRA E EXPORTAÇÃO DE SVGS
        if (showConsolidatedReport) {
            val quantitativo = viewModel.calculateConsolidatedQuantitativo()
            ConsolidatedReportDialog(
                quantitativo = quantitativo,
                context = context,
                onDismiss = { showConsolidatedReport = false },
                onOpenWallElevation = { wallResult ->
                    viewModel.selectWallForIndividualTiling(
                        project.walls.first { it.label == wallResult.wall.wallName.substringAfter("Parede ").substringBefore(" —") }
                    )
                    showConsolidatedReport = false
                    onNavigateToViewer()
                }
            )
        }
    }
}

@Composable
private fun InfoPill(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
    }
}

/**
 * Renderiza a Planta Baixa Arquitetônica 2D sem móveis (apenas as linhas de alvenaria e letras A, B, C...).
 */
private fun DrawScope.drawFloorPlanCanvas(
    project: FloorPlanProject,
    selectedWallId: String?,
    scale: Float,
    panX: Float,
    panY: Float
) {
    val canvasW = size.width
    val canvasH = size.height

    // Grid de referência
    val gridSpacing = 40f * scale
    var x = panX % gridSpacing
    while (x < canvasW) {
        drawLine(Color(0xFF1E293B).copy(alpha = 0.4f), Offset(x, 0f), Offset(x, canvasH), 1f)
        x += gridSpacing
    }
    var y = panY % gridSpacing
    while (y < canvasH) {
        drawLine(Color(0xFF1E293B).copy(alpha = 0.4f), Offset(0f, y), Offset(canvasW, y), 1f)
        y += gridSpacing
    }

    val meterPx = 28f * scale
    val offsetX = canvasW / 2f - (5f * meterPx) + panX
    val offsetY = canvasH / 2f - (4f * meterPx) + panY

    // Desenhar cada parede da planta baixa
    for (wall in project.walls) {
        val p1 = Offset(offsetX + wall.startX * meterPx, offsetY + wall.startY * meterPx)
        val p2 = Offset(offsetX + wall.endX * meterPx, offsetY + wall.endY * meterPx)
        val isSelected = wall.id == selectedWallId

        val wallColor = if (isSelected) Color(0xFF38BDF8) else Color(0xFFF1F5F9)
        val wallThicknessPx = max(4f, (wall.thicknessCm.toFloat() / 100f) * meterPx)

        // Traço da parede
        drawLine(
            color = wallColor,
            start = p1,
            end = p2,
            strokeWidth = wallThicknessPx
        )

        // Linha externa de contorno
        drawLine(
            color = if (isSelected) Color(0xFF0284C7) else Color(0xFF475569),
            start = p1,
            end = p2,
            strokeWidth = wallThicknessPx + 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 0f))
        )

        // Identificador da Parede [ A ], [ B ] no centro da parede
        val midX = (p1.x + p2.x) / 2f
        val midY = (p1.y + p2.y) / 2f

        drawCircle(
            color = if (isSelected) Color(0xFF0284C7) else Slate900,
            radius = 14f * scale.coerceIn(0.8f, 2.0f),
            center = Offset(midX, midY)
        )
        drawCircle(
            color = if (isSelected) Color(0xFF38BDF8) else Color.White,
            radius = 14f * scale.coerceIn(0.8f, 2.0f),
            center = Offset(midX, midY),
            style = Stroke(width = 1.5f)
        )

        // Letra da parede (usando canvas nativo do Android para desenhar texto de alta nitidez)
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 14f * scale.coerceIn(0.8f, 2.0f)
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            drawText(wall.label, midX, midY + (5f * scale.coerceIn(0.8f, 2.0f)), paint)
        }
    }
}

@Composable
private fun AddWallToFloorPlanDialog(
    defaultHeight: Double,
    defaultThickness: Double,
    onDismiss: () -> Unit,
    onAdd: (String, Double, Double, Double) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var lengthText by remember { mutableStateOf("400.0") }
    var heightText by remember { mutableStateOf(defaultHeight.toString()) }
    var thicknessText by remember { mutableStateOf(defaultThickness.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Parede à Planta", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nome da Parede / Ambiente") },
                    placeholder = { Text("Ex: Divisória Sala / Cozinha") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lengthText,
                    onValueChange = { lengthText = it },
                    label = { Text("Comprimento (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Ex: 400.0 para 4 metros") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Altura (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = thicknessText,
                        onValueChange = { thicknessText = it },
                        label = { Text("Espessura (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val l = lengthText.toDoubleOrNull() ?: 400.0
                    val h = heightText.toDoubleOrNull() ?: defaultHeight
                    val th = thicknessText.toDoubleOrNull() ?: defaultThickness
                    val modL = com.paginacaopro.app.engine.TilingEngine.snapToModularWallLength(l)
                    val modH = com.paginacaopro.app.engine.TilingEngine.snapToModularWallHeight(h)
                    onAdd(nameText.ifBlank { "Parede sem nome" }, modL, modH, th)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Diálogo com o Quantitativo Geral Consolidado da Obra e lista de paredes com seus SVGs.
 */
@Composable
private fun ConsolidatedReportDialog(
    quantitativo: ConsolidatedQuantitativo,
    context: Context,
    onDismiss: () -> Unit,
    onOpenWallElevation: (com.paginacaopro.app.model.TilingResult) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                // Topo do relatório
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "QUANTITATIVO GERAL DA OBRA",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            "${quantitativo.project.name} • ${quantitativo.project.walls.size} Paredes",
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    // Card com resumo consolidado de compra
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("TOTAIS CONSOLIDADOS DE COMPRA", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("${quantitativo.totalRecommendedPurchaseBlocks} un", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    Text("Blocos Inteiros (Total da Obra)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${quantitativo.totalRecommendedPurchaseHalfBlocks} un", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFFBAE6FD))
                                    Text("Meios Blocos Comerciais", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Argamassa Total: ${quantitativo.totalMortarVolumeM3} m³", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Área Líquida: ${quantitativo.totalNetAreaM2} m²", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // DISCRIMINAÇÃO GERAL POR TAMANHO DE BLOCO (CASA INTEIRA)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Quantidade Geral por Tamanho de Bloco", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                            Spacer(modifier = Modifier.height(10.dp))
                            quantitativo.globalBreakdown.forEach { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(p.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Slate900)
                                        Text("${p.lengthCm} × ${p.heightCm} × ${p.thicknessCm} cm • ${p.percentageOfWall}% da obra", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${p.count} un", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                        Text("Comprar: ${p.recommendedPurchase} un", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // LISTA DE PAREDES COM DETALHES E BOTÃO PARA VER SVG DE CADA UMA
                    Text("Paredes da Obra e SVGs Individuais", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    quantitativo.project.walls.forEach { wall ->
                        val result = quantitativo.wallResults[wall.label]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(PrimaryBlue, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(wall.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Parede ${wall.label} — ${wall.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                            Text(
                                                "${wall.lengthCm} cm × ${wall.heightCm} cm • ${wall.openings.size} vãos",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    if (result != null) {
                                        Button(
                                            onClick = { onOpenWallElevation(result) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("VER SVG", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (result != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Peças: ${result.billOfMaterials.wholeBlockCount} inteiros, ${result.billOfMaterials.halfBlockCount} meios, ${result.billOfMaterials.cutPieceCount} cortes • Argamassa: ${result.billOfMaterials.mortarJointVolumeM3Estimated} m³",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
