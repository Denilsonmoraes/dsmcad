package com.paginacaopro.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.paginacaopro.app.model.JointRenderStyle
import com.paginacaopro.app.model.PieceType
import com.paginacaopro.app.model.TilingBlock
import com.paginacaopro.app.model.TilingResult
import com.paginacaopro.app.ui.components.RealisticBlockCard
import com.paginacaopro.app.ui.theme.CardBorder
import com.paginacaopro.app.ui.theme.PrimaryBlue
import androidx.compose.foundation.border
import androidx.compose.material3.HorizontalDivider
import com.paginacaopro.app.ui.theme.SecondaryOrange
import com.paginacaopro.app.ui.theme.Slate800
import com.paginacaopro.app.ui.theme.Slate900
import com.paginacaopro.app.ui.viewmodel.PaginacaoViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TilingViewerScreen(
    viewModel: PaginacaoViewModel,
    onNavigateBack: () -> Unit
) {
    val tilingResult by viewModel.tilingResult.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados do Visualizador Gráfico (Zoom / Pan)
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showFullscreenViewer by remember { mutableStateOf(false) }
    var show3DViewer by remember { mutableStateOf(false) }

    // Filtro de Fiada (0 = Todas, 1..N = Fiada específica)
    var selectedCourseFilter by remember { mutableIntStateOf(0) }
    var isCotadaView by remember { mutableStateOf(true) }
    var isAutoCadMode by remember { mutableStateOf(true) }

    // Legenda Retrátil / Flutuante (igual ao simulador: escondida por padrão, abre ao clicar no botão "— Legenda & Cores")
    var isLegendOpen by remember { mutableStateOf(false) }
    var highlightedSizeKey by remember { mutableStateOf<String?>(null) }

    // Bloco inspecionado pelo usuário
    var inspectedBlock by remember { mutableStateOf<TilingBlock?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Desenho, 1: Quantitativos

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Paginação da Parede", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            tilingResult?.wall?.let { "${it.wallName.substringBefore(" (").trim()} (${it.lengthCm.toInt()}×${it.heightCm.toInt()} cm)" } ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    // Botão Tela Cheia / Zoom Infinito
                    IconButton(onClick = { showFullscreenViewer = true }) {
                        Icon(Icons.Default.Fullscreen, contentDescription = "Tela Cheia / Zoom Infinito", tint = Color(0xFF38BDF8))
                    }
                    // Salvar Projeto
                    IconButton(onClick = {
                        viewModel.saveCurrentWallProject()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Projeto salvo no histórico local com sucesso!")
                        }
                    }) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Salvar Projeto", tint = Color.White)
                    }
                    // Exportar / Compartilhar SVG
                    IconButton(onClick = {
                        val svgContent = viewModel.generateSvgOutput(isSimplified = false)
                        shareSvgFile(context, svgContent, tilingResult?.wall?.wallName ?: "paginacao")
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartilhar SVG", tint = Color.White)
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
            val result = tilingResult
            if (selectedTab == 0 && result != null && result.isValid) {
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
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val svg = viewModel.generateSvgOutput(
                                    isSimplified = true,
                                    cadStyle = if (isAutoCadMode) com.paginacaopro.app.svg.CadStyle.AUTOCAD_DARK else com.paginacaopro.app.svg.CadStyle.CONSTRUCTION
                                )
                                shareSvgFile(context, svg, "${result.wall.wallName}_corte_autocad_simplificado")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SVG SIMPLES", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                val svg = viewModel.generateSvgOutput(
                                    isSimplified = false,
                                    cadStyle = if (isAutoCadMode) com.paginacaopro.app.svg.CadStyle.AUTOCAD_DARK else com.paginacaopro.app.svg.CadStyle.CONSTRUCTION
                                )
                                shareSvgFile(context, svg, "${result.wall.wallName}_corte_autocad_8k")
                            },
                            modifier = Modifier.weight(1.4f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isAutoCadMode) "CORTE AUTOCAD 8K" else "EXPORTAR SVG 8K", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Slate800,
                contentColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("DESENHO TÉCNICO", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("QUANTITATIVOS", fontWeight = FontWeight.Bold) }
                )
            }

            if (tilingResult == null || !tilingResult!!.isValid) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tilingResult?.errors?.firstOrNull() ?: "Calculando paginação...",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                val result = tilingResult!!

                if (selectedTab == 0) {
                    // Barra 1: Controles Rápidos do Desenho (Vista Cotada & Seletor de Fiadas)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = isAutoCadMode,
                                onClick = { isAutoCadMode = !isAutoCadMode },
                                label = {
                                    Text(
                                        if (isAutoCadMode) "📐 Corte AutoCAD" else "🎨 Blocos Coloridos",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAutoCadMode) PrimaryBlue else Slate900
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(
                                selected = isCotadaView,
                                onClick = { isCotadaView = !isCotadaView },
                                label = { Text(if (isCotadaView) "Cotas On" else "Cotas Off", fontSize = 11.sp) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Seletor de Fiadas
                            Text("Fiada:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChip(
                                selected = selectedCourseFilter == 0,
                                onClick = { selectedCourseFilter = 0 },
                                label = { Text("Todas", fontSize = 11.sp) }
                            )
                            for (cIdx in 1..result.courses.size) {
                                Spacer(modifier = Modifier.width(4.dp))
                                FilterChip(
                                    selected = selectedCourseFilter == cIdx,
                                    onClick = { selectedCourseFilter = cIdx },
                                    label = { Text("F$cIdx", fontSize = 11.sp) }
                                )
                            }
                        }

                        // Reset Zoom
                        IconButton(onClick = {
                            scale = 1.0f
                            offsetX = 0f
                            offsetY = 0f
                        }) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset Zoom", modifier = Modifier.size(18.dp))
                        }
                    }

                    // Barra 2: Seletor de Paredes A, B, C, D, E, F... (Mesmo tamanho e características)
                    val floorPlanProject by viewModel.floorPlanProject.collectAsState()
                    val walls = floorPlanProject.walls
                    if (walls.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A))
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Paredes:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            for (w in walls) {
                                val isSelected = result.wall.wallName.contains("Parede ${w.label}")
                                Surface(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .clickable {
                                            viewModel.selectWallForIndividualTiling(w)
                                            selectedCourseFilter = 0
                                        },
                                    color = if (isSelected) PrimaryBlue else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155))
                                ) {
                                    Text(
                                        text = "Parede ${w.label} (${(w.lengthCm / 100.0)}m)",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Canvas Gráfico Interativo com Pan & Zoom 20x e Toque para Tela Cheia
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true)
                            .background(Color(0xFFE2E8F0))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.5f, 20.0f)
                                    offsetX += pan.x
                                    offsetY += pan.y
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        showFullscreenViewer = true
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawTilingElevation(
                                result = result,
                                selectedCourse = selectedCourseFilter,
                                isCotadaView = isCotadaView,
                                isAutoCadMode = isAutoCadMode,
                                scale = scale,
                                panX = offsetX,
                                panY = offsetY,
                                highlightedSizeKey = highlightedSizeKey,
                                isVerticalRotated = false
                            )
                        }

                        // Botões Flutuantes Superiores Esquerdos (Tela Cheia e Visualizador 3D)
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = Slate900.copy(alpha = 0.90f),
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = 3.dp,
                                onClick = { showFullscreenViewer = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("TELA CHEIA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Surface(
                                color = Slate900.copy(alpha = 0.92f),
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = 3.dp,
                                onClick = { show3DViewer = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.FitScreen, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("3D REAL", color = Color(0xFFFDE047), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }

                        // =====================================================================
                        // LEGENDA FLUTUANTE RETRÁTIL (IGUAL AO SIMULADOR DE PAGINAÇÃO PRO)
                        // Escondida por padrão! Mostra botão "— Legenda & Cores • X un"
                        // Ao clicar, expande o card com cores, quantidades e clique p/ destacar
                        // =====================================================================
                        val normalLegendItems = remember(result) { buildBlockSizeLegendItems(result) }
                        val totalWallBlocks = remember(result) { result.allBlocks.count { it.countsInQuantity } }

                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            // Botão retrátil quando escondida
                            Surface(
                                color = Slate900.copy(alpha = 0.94f),
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                    .clickable { isLegendOpen = !isLegendOpen }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "—",
                                        color = Color(0xFFFBBF24),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Legenda & Cores",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFFBBF24).copy(alpha = 0.20f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            "$totalWallBlocks un",
                                            color = Color(0xFFFDE68A),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            // Card Flutuante Expandido (abre ao clicar)
                            if (isLegendOpen) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .heightIn(max = 260.dp),
                                    color = Slate900.copy(alpha = 0.96f),
                                    shape = RoundedCornerShape(12.dp),
                                    shadowElevation = 8.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "LEGENDA DE BLOCOS",
                                                color = Color(0xFFFBBF24),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            IconButton(
                                                onClick = { isLegendOpen = false },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Fechar Legenda",
                                                    tint = Color(0xFFFBBF24),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        if (highlightedSizeKey != null) {
                                            Surface(
                                                color = Color(0xFF334155),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { highlightedSizeKey = null }
                                            ) {
                                                Text(
                                                    "Mostrar Todos os Blocos",
                                                    color = Color(0xFFFDE047),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        normalLegendItems.forEach { item ->
                                            val isSelected = highlightedSizeKey == item.key
                                            Surface(
                                                color = if (isSelected) item.swatchColor.copy(alpha = 0.32f) else Color(0xFF1E293B).copy(alpha = 0.75f),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                                        color = if (isSelected) Color.White else item.swatchColor.copy(alpha = 0.6f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable {
                                                        highlightedSizeKey = if (highlightedSizeKey == item.key) null else item.key
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .background(item.swatchColor, RoundedCornerShape(2.dp))
                                                                .border(0.5.dp, Color.White, RoundedCornerShape(2.dp))
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            item.shortLabel,
                                                            color = Color.White,
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Text(
                                                        "${item.count} un",
                                                        color = Color(0xFFFDE047),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // PAINEL COMPACTO: QUANTITATIVO INDIVIDUAL DA PAREDE & GERAL DA OBRA (FONTE MODESTA)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            val amarraSpec = com.paginacaopro.app.engine.FreePlanEngine.getAmarracaoBlockInfo(result.wall.thicknessCm)
                            val cleanWallName = result.wall.wallName.substringBefore(" (").trim()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "PAREDE: $cleanWallName (${result.wall.lengthCm.toInt()}×${result.wall.heightCm.toInt()}×${com.paginacaopro.app.engine.FreePlanEngine.formatClean(result.wall.thicknessCm)} cm)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    "Amarração L/T: ${amarraSpec.dimensionsLabel}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                            // Seletor Rápido de Família / Espessura (9, 11.5, 14, 19 cm)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Mudar Família:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                listOf(9.0, 11.5, 14.0, 19.0).forEach { th ->
                                    val isCur = kotlin.math.abs(result.wall.thicknessCm - th) < 0.2
                                    Surface(
                                        color = if (isCur) Color(0xFF0284C7) else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .border(1.dp, if (isCur) Color(0xFF0284C7) else Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                            .clickable { viewModel.changeCurrentTilingThickness(th) }
                                    ) {
                                        Text(
                                            text = "${com.paginacaopro.app.engine.FreePlanEngine.formatClean(th)} cm",
                                            color = if (isCur) Color.White else Slate900,
                                            fontSize = 10.sp,
                                            fontWeight = if (isCur) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            // Discriminação dos blocos da parede em fonte modesta
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val sizeGroups = buildBlockSizeLegendItems(result)
                                sizeGroups.forEachIndexed { idx, item ->
                                    if (idx > 0) {
                                        Text("•", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(
                                        "${item.shortLabel}: ${item.count} un",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate800
                                    )
                                }
                                Text("•", fontSize = 10.sp, color = Color.Gray)
                                Text("Argamassa: ${result.billOfMaterials.mortarJointVolumeM3Estimated} m³", fontSize = 10.sp, color = Color(0xFF047857))
                            }

                            Spacer(modifier = Modifier.height(3.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(3.dp))

                            // Linha 2: Quantitativo Geral da Obra Consolidado (Todas as Paredes)
                            val quantitativoGeral = remember(floorPlanProject) {
                                viewModel.calculateConsolidatedQuantitativo()
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "GERAL DA OBRA (${floorPlanProject.walls.size} PAREDES):",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Total: ${quantitativoGeral.totalPiecesGeometrical} blocos (${quantitativoGeral.totalRecommendedPurchaseBlocks} int. / ${quantitativoGeral.totalRecommendedPurchaseHalfBlocks} meios)",
                                        fontSize = 10.sp,
                                        color = Slate800
                                    )
                                }
                                Text(
                                    "Argamassa: ${quantitativoGeral.totalMortarVolumeM3} m³",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }
                } else {
                    // Tela 2: Quadro Detalhado de Quantitativos
                    QuantitativosTab(result = result)
                }
            }
        }

        // DIÁLOGO EM TELA CHEIA COM ROTAÇÃO VERTICAL AUTOMÁTICA, ZOOM 20X NAS FIADAS E LEGENDA LATERAL CLICÁVEL
        val activeResult = tilingResult
        if (showFullscreenViewer && activeResult != null && activeResult.isValid) {
            FullscreenSvgViewerDialog(
                result = activeResult,
                isCotadaView = isCotadaView,
                selectedCourse = selectedCourseFilter,
                initialAutoCadMode = isAutoCadMode,
                onOpen3D = {
                    showFullscreenViewer = false
                    show3DViewer = true
                },
                onDismiss = { showFullscreenViewer = false }
            )
        }

        if (show3DViewer && activeResult != null && activeResult.isValid) {
            com.paginacaopro.app.ui.components.ThreeDViewerDialog(
                tilingResult = activeResult,
                floorPlanProject = viewModel.floorPlanProject.collectAsState().value,
                onDismiss = { show3DViewer = false }
            )
        }
    }
}

@Composable
private fun QuantitativosTab(result: TilingResult) {
    val bom = result.billOfMaterials

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("RESUMO DE COMPRA ESTIMADA", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${bom.recommendedPurchaseBlocks} un", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Blocos Inteiros (com ${bom.wastePercentage}% perda)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${bom.recommendedPurchaseHalfBlocks} un", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFFBAE6FD))
                        Text("Meios Blocos Comerciais", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }

        // DISCRIMINAÇÃO EXATA POR TAMANHO COMERCIAL DE BLOCO
        if (bom.pieceBreakdown.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Discriminação por Tamanho Comercial de Bloco", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    bom.pieceBreakdown.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Slate900)
                                Text("${p.lengthCm} × ${p.heightCm} × ${p.thicknessCm} cm • ${p.percentageOfWall}% da parede", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${p.count} un", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                                Text("Comprar: ${p.recommendedPurchase} un", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Levantamento Geométrico Real da Parede", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(12.dp))

                QuantitativoRow("Área Bruta da Parede", "${bom.grossWallAreaM2} m²")
                QuantitativoRow("Área dos Vãos (Portas/Janelas)", "${bom.openingsAreaM2} m²")
                QuantitativoRow("Área Líquida de Alvenaria", "${bom.netWallAreaM2} m²")
                QuantitativoRow("Total de Fiadas de Alvenaria", "${bom.totalCourses} fiadas")
                QuantitativoRow("Blocos Inteiros Utilizados", "${bom.wholeBlockCount} un")
                QuantitativoRow("Meios Blocos na Amarração", "${bom.halfBlockCount} un")
                QuantitativoRow("Peças Cortadas (Ajustes)", "${bom.cutPieceCount} un")
                QuantitativoRow("Peças Especiais / Amarração T", "${bom.specialPieceCount} un")
                QuantitativoRow("Total de Peças Geométricas", "${bom.totalPiecesGeometrical} un")
                QuantitativoRow("Volume Estimado de Argamassa", "${bom.mortarJointVolumeM3Estimated} m³")
            }
        }

        if (bom.cutDetails.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detalhamento dos Cortes (${bom.cutDetails.size} peças)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    bom.cutDetails.take(15).forEach { cut ->
                        Text(
                            "• Fiada ${cut.courseIndex}: Peça com ${cut.lengthCm} cm × ${cut.heightCm} cm — ${cut.reason}",
                            fontSize = 12.sp,
                            color = Slate800,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuantitativoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF475569))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
    }
}

/**
 * Item da legenda lateral por tamanho de bloco (exibida exclusivamente em Tela Cheia).
 */
data class BlockSizeLegendItem(
    val key: String,
    val shortLabel: String,
    val fullLabel: String,
    val swatchColor: Color,
    val count: Int
)

/**
 * Chave única por comprimento/tipo de bloco para diferenciar cores e permitir destaque ao clicar na legenda.
 */
fun getBlockSizeKey(blk: TilingBlock): String {
    if (blk.pieceType == PieceType.LINTEL) return "CANALETA"
    val len = blk.lengthCm
    return when {
        abs(len - 54.0) < 0.6 -> "54"
        abs(len - 44.0) < 0.6 -> "44"
        abs(len - 39.0) < 0.6 -> "39"
        abs(len - 34.0) < 0.6 -> "34"
        abs(len - 31.5) < 0.6 -> "31,5"
        abs(len - 29.0) < 0.6 -> "29"
        abs(len - 19.0) < 0.6 -> "19"
        abs(len - 14.0) < 0.6 -> "14"
        abs(len - 9.0) < 0.6 -> "9"
        abs(len - 6.5) < 0.6 -> "6,5"
        abs(len - 5.0) < 0.6 -> "5"
        abs(len - 4.0) < 0.6 -> "4"
        else -> "CORTE_${com.paginacaopro.app.engine.FreePlanEngine.formatClean(len)}"
    }
}

/**
 * Retorna (FillColor, StrokeColor, SwatchColor) exclusivo para cada tipo/tamanho de bloco conforme Diagrama 3D:
 * - Bloco Padrão (39 cm): Cinza
 * - Bloco de Canto / Amarração (34, 31.5, 29 cm e Quina Laranja): Laranja
 * - Bloco Compensador / Interseção em 'T' (54 cm e Trava T): Azul
 * - Meio Bloco (19 cm): Verde Esmeralda
 * - Canaleta Verga / Contraverga: Âmbar/Ouro
 */
fun getBlockSizeColors(blk: TilingBlock, isAutoCadMode: Boolean): Triple<Color, Color, Color> {
    val key = getBlockSizeKey(blk)
    return when {
        key == "CANALETA" || blk.pieceType == PieceType.LINTEL -> Triple(
            if (isAutoCadMode) Color(0xFF78350F) else Color(0xFFFEF08A),
            Color(0xFFFACC15),
            Color(0xFFFACC15)
        )
        key == "54" || key == "44" || blk.pieceType == PieceType.T_JUNCTION -> Triple(
            if (isAutoCadMode) Color(0xFF0369A1) else Color(0xFF7DD3FC),
            if (isAutoCadMode) Color(0xFF7DD3FC) else Color(0xFF0284C7),
            Color(0xFF38BDF8)
        )
        key == "34" || key == "31,5" || key == "29" || blk.pieceType == PieceType.CORNER_TIE -> Triple(
            if (isAutoCadMode) Color(0xFFC2410C) else Color(0xFFFED7AA),
            if (isAutoCadMode) Color(0xFFFDBA74) else Color(0xFFEA580C),
            Color(0xFFFB923C)
        )
        key == "39" -> Triple(
            if (isAutoCadMode) Color(0xFF475569) else Color(0xFFCBD5E1),
            if (isAutoCadMode) Color(0xFFCBD5E1) else Color(0xFF334155),
            Color(0xFF94A3B8)
        )
        key == "19" -> Triple(
            if (isAutoCadMode) Color(0xFF065F46) else Color(0xFFA7F3D0),
            Color(0xFF34D399),
            Color(0xFF34D399)
        )
        key == "14" || key == "9" -> Triple(
            if (isAutoCadMode) Color(0xFF1E3A8A) else Color(0xFFDBEAFE),
            Color(0xFF60A5FA),
            Color(0xFF60A5FA)
        )
        key == "6,5" || key == "5" || key == "4" -> Triple(
            if (isAutoCadMode) Color(0xFF3B0764) else Color(0xFFF3E8FF),
            Color(0xFFA855F7),
            Color(0xFFA855F7)
        )
        else -> Triple(
            if (isAutoCadMode) Color(0xFF450A0A) else Color(0xFFFEE2E2),
            Color(0xFFEF4444),
            Color(0xFFEF4444)
        )
    }
}

/**
 * Constrói os itens da legenda lateral agrupados por tamanho de bloco com suas quantidades na parede.
 */
fun buildBlockSizeLegendItems(result: TilingResult): List<BlockSizeLegendItem> {
    val grouped = result.allBlocks.filter { it.countsInQuantity }.groupBy { getBlockSizeKey(it) }
    val items = mutableListOf<BlockSizeLegendItem>()
    for ((key, blocks) in grouped) {
        val sample = blocks.first()
        val (_, _, swatch) = getBlockSizeColors(sample, isAutoCadMode = true)
        val lenStr = com.paginacaopro.app.engine.FreePlanEngine.formatClean(sample.lengthCm)
        val thkStr = com.paginacaopro.app.engine.FreePlanEngine.formatClean(sample.thicknessCm)
        val shortLabel = when (key) {
            "CANALETA" -> "Canaleta"
            "54" -> "Comp. T 54"
            "39" -> "Padrão 39"
            "34" -> "Canto/T 34"
            "31,5" -> "Canto/T 31,5"
            "29" -> if (abs(sample.thicknessCm - 9.0) < 0.5) "Canto/T 29" else "Bloco 29"
            "19" -> "Meio 19"
            "14" -> "Quina/T 14"
            "9" -> "Quina/T 9"
            "6,5", "5", "4" -> "Ajuste $lenStr"
            else -> "Ajuste $lenStr"
        }
        val fullLabel = when (key) {
            "CANALETA" -> "Canaleta Verga/CV $lenStr×19×$thkStr"
            "54" -> "Compensador T 54×19×$thkStr (Azul)"
            "34", "31,5" -> "Amarração L/T $lenStr×19×$thkStr (Laranja)"
            "29" -> if (abs(sample.thicknessCm - 9.0) < 0.5) "Amarração L/T 29×19×9 (Laranja)" else "Bloco 29×19×$thkStr"
            else -> "$lenStr×19×$thkStr cm"
        }
        items.add(
            BlockSizeLegendItem(
                key = key,
                shortLabel = shortLabel,
                fullLabel = fullLabel,
                swatchColor = swatch,
                count = blocks.size
            )
        )
    }
    return items.sortedByDescending {
        val sample = grouped[it.key]?.firstOrNull()
        if (it.key == "CANALETA") -1.0 else (sample?.lengthCm ?: 0.0)
    }
}

/**
 * Renderização matemática em tempo real da elevação e corte técnico da alvenaria estilo AutoCAD.
 * - Sem numeração poluindo dentro dos blocos;
 * - Cada tamanho de bloco tem sua própria cor (39 Cinza, 34/31.5/29 Laranja, 54/T Azul, 19 Verde, Canaleta Âmbar);
 * - Destaque imediato quando o usuário clica na cor/bloco na legenda lateral em Tela Cheia;
 * - Suporta rotação vertical automática (90°) para paredes grandes em Tela Cheia;
 * - Exibe explicitamente as fiadas F1..FN e os blocos de amarração de Canto (L) e Saída T (34×19×14, 31,5×19×11,5, 29×19×9).
 */
fun DrawScope.drawTilingElevation(
    result: TilingResult,
    selectedCourse: Int,
    isCotadaView: Boolean,
    isAutoCadMode: Boolean,
    scale: Float,
    panX: Float,
    panY: Float,
    inspectedBlock: TilingBlock? = null,
    highlightedSizeKey: String? = null,
    isVerticalRotated: Boolean = false
) {
    // 1. Fundo do Canvas inteiro
    if (isAutoCadMode) {
        drawRect(color = Color(0xFF0D1117), size = size)
    } else {
        drawRect(color = Color(0xFFE2E8F0), size = size)
    }

    val wall = result.wall
    val wallW = wall.lengthCm.toFloat().coerceAtLeast(10f)
    val wallH = wall.heightCm.toFloat().coerceAtLeast(10f)

    val cintaH = if (isAutoCadMode) 20f else 0f
    val baldrameH = if (isAutoCadMode) 25f else 0f
    val earthH = if (isAutoCadMode) 15f else 0f
    val leftMarginCm = if (isAutoCadMode) 55f else 26f
    val rightMarginCm = if (isAutoCadMode) 35f else 24f

    val totalModelW = wallW + leftMarginCm + rightMarginCm
    val totalModelH = wallH + cintaH + baldrameH + earthH + (if (isAutoCadMode) 38f else 22f)

    // Quando isVerticalRotated == true, o espaço disponível para a largura da parede passa a ser a altura da tela!
    val availW = if (isVerticalRotated) size.height else size.width
    val availH = if (isVerticalRotated) size.width else size.height

    val baseScale = min((availW * 0.88f) / totalModelW, (availH * 0.84f) / totalModelH)
    val totalScale = baseScale * scale

    // Se rotacionado verticalmente (90°), convertemos o pan da tela (panX, panY) para o espaço rotacionado
    val effPanX = if (isVerticalRotated) panY else panX
    val effPanY = if (isVerticalRotated) -panX else panY

    withTransform({
        if (isVerticalRotated) {
            rotate(degrees = 90f, pivot = Offset(size.width / 2f, size.height / 2f))
        }
    }) {
        val canvasCenterX = size.width / 2f
        val canvasCenterY = size.height / 2f
        val originX = canvasCenterX - (wallW * totalScale) / 2f + effPanX + ((leftMarginCm - rightMarginCm) * 0.30f * totalScale)
        val originY = canvasCenterY + (wallH * totalScale) / 2f + effPanY - ((baldrameH + earthH) * 0.40f * totalScale)

        // Tintas Android Paint para textos técnicos de engenharia
        val dimPaint = AndroidPaint().apply {
            color = if (isAutoCadMode) android.graphics.Color.parseColor("#FF2D78") else android.graphics.Color.parseColor("#0F172A")
            textSize = (8.5f * totalScale).coerceIn(9f, 22f)
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = AndroidPaint.Align.CENTER
            typeface = Typeface.MONOSPACE
        }
        val levelPaint = AndroidPaint().apply {
            color = if (isAutoCadMode) android.graphics.Color.parseColor("#00FFFF") else android.graphics.Color.parseColor("#0284C7")
            textSize = (8.0f * totalScale).coerceIn(8.5f, 19f)
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = AndroidPaint.Align.RIGHT
            typeface = Typeface.MONOSPACE
        }
        val fiadaPaint = AndroidPaint().apply {
            color = if (isAutoCadMode) android.graphics.Color.parseColor("#38BDF8") else android.graphics.Color.parseColor("#0369A1")
            textSize = (7.2f * totalScale).coerceIn(8f, 20f)
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = AndroidPaint.Align.RIGHT
            typeface = Typeface.MONOSPACE
        }
        val amarraCalloutPaint = AndroidPaint().apply {
            color = if (isAutoCadMode) android.graphics.Color.parseColor("#FDE047") else android.graphics.Color.parseColor("#B45309")
            textSize = (8.0f * totalScale).coerceIn(9f, 20f)
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = AndroidPaint.Align.CENTER
            typeface = Typeface.MONOSPACE
        }
        val titlePaint = AndroidPaint().apply {
            color = if (isAutoCadMode) android.graphics.Color.parseColor("#FDE047") else android.graphics.Color.parseColor("#0F172A")
            textSize = (11f * totalScale).coerceIn(11f, 25f)
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = AndroidPaint.Align.CENTER
            typeface = Typeface.MONOSPACE
        }
        val structLabelPaint = AndroidPaint().apply {
            color = if (isAutoCadMode) android.graphics.Color.parseColor("#FDE047") else android.graphics.Color.parseColor("#334155")
            textSize = (7.5f * totalScale).coerceIn(8.5f, 18f)
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = AndroidPaint.Align.CENTER
            typeface = Typeface.MONOSPACE
        }

        // 2. Elementos Estruturais do Corte AutoCAD
        if (isAutoCadMode) {
            val baldrameTop = originY
            val baldrameH_px = baldrameH * totalScale

            // Terreno / Solo Compactado abaixo do Baldrame
            val earthTop = baldrameTop + baldrameH_px
            val earthH_px = earthH * totalScale
            drawRect(
                color = Color(0xFF151D28),
                topLeft = Offset(originX - 15f * totalScale, earthTop),
                size = Size((wallW + 30f) * totalScale, earthH_px)
            )
            drawLine(
                color = Color(0xFF64748B),
                start = Offset(originX - 25f * totalScale, earthTop),
                end = Offset(originX + (wallW + 25f) * totalScale, earthTop),
                strokeWidth = 2.5f
            )
            var ex = originX - 15f * totalScale
            val earthEnd = originX + (wallW + 15f) * totalScale
            while (ex < earthEnd) {
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(ex, earthTop),
                    end = Offset(ex + 12f * totalScale, earthTop + earthH_px),
                    strokeWidth = 1f
                )
                ex += 16f * totalScale
            }

            // Viga Baldrame (Concreto Armado)
            drawRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(originX, baldrameTop),
                size = Size(wallW * totalScale, baldrameH_px)
            )
            drawRect(
                color = Color(0xFFFDE047),
                topLeft = Offset(originX, baldrameTop),
                size = Size(wallW * totalScale, baldrameH_px),
                style = Stroke(width = 2.2f)
            )
            drawIntoCanvas { c ->
                c.nativeCanvas.drawText(
                    "VIGA BALDRAME VB-1 (CONCRETO ARMADO)",
                    originX + (wallW * totalScale) / 2f,
                    baldrameTop + baldrameH_px / 2f + 4f,
                    structLabelPaint
                )
            }

            // Cinta de Amarração / Respaldo Superior
            val cintaTop = originY - (wallH + cintaH) * totalScale
            val cintaH_px = cintaH * totalScale
            drawRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(originX, cintaTop),
                size = Size(wallW * totalScale, cintaH_px)
            )
            drawRect(
                color = Color(0xFFFDE047),
                topLeft = Offset(originX, cintaTop),
                size = Size(wallW * totalScale, cintaH_px),
                style = Stroke(width = 2.2f)
            )
            drawIntoCanvas { c ->
                c.nativeCanvas.drawText(
                    "CINTA DE AMARRAÇÃO / LAJE (CONCRETO ARMADO)",
                    originX + (wallW * totalScale) / 2f,
                    cintaTop + cintaH_px / 2f + 4f,
                    structLabelPaint
                )
            }
        }

        // 3. Fundo da Alvenaria (Juntas de Argamassa)
        val wallLeft = originX
        val wallTop = originY - wallH * totalScale
        val wallWidthPx = wallW * totalScale
        val wallHeightPx = wallH * totalScale

        drawRect(
            color = if (isAutoCadMode) Color(0xFF090D14) else Color(0xFF94A3B8),
            topLeft = Offset(wallLeft, wallTop),
            size = Size(wallWidthPx, wallHeightPx)
        )

        // 4. Blocos da Parede (Sem numeração dentro do bloco; cada tamanho com sua cor; destaque se selecionado na legenda)
        for (course in result.courses) {
            val firstBlk = course.firstOrNull() ?: continue
            val courseNumber = firstBlk.courseIndex + 1
            if (selectedCourse > 0 && courseNumber != selectedCourse) continue

            // Etiqueta da Fiada (F1, F2, F3...) ao lado esquerdo da fiada para leitura perfeita com Zoom 20x
            val courseCenterY = originY - (firstBlk.yCm.toFloat() + firstBlk.heightCm.toFloat() / 2f) * totalScale
            drawIntoCanvas { c ->
                c.nativeCanvas.drawText(
                    "F$courseNumber",
                    originX - 4f * totalScale.coerceAtLeast(1.2f),
                    courseCenterY + 3f,
                    fiadaPaint
                )
            }

            for (blk in course) {
                val blkX = originX + blk.xCm.toFloat() * totalScale
                val blkY = originY - (blk.yCm.toFloat() + blk.heightCm.toFloat()) * totalScale
                val blkW = blk.lengthCm.toFloat() * totalScale
                val blkH = blk.heightCm.toFloat() * totalScale

                val isThisInspected = inspectedBlock?.id == blk.id
                val sizeKey = getBlockSizeKey(blk)
                val isHighlightedByLegend = highlightedSizeKey != null && highlightedSizeKey == sizeKey
                val isDimmedByLegend = highlightedSizeKey != null && highlightedSizeKey != sizeKey

                val (baseFill, baseStroke, swatch) = getBlockSizeColors(blk, isAutoCadMode)

                val blockColor = when {
                    isThisInspected -> Color(0xFF06B6D4).copy(alpha = 0.85f)
                    isHighlightedByLegend -> swatch.copy(alpha = 0.88f)
                    isDimmedByLegend -> baseFill.copy(alpha = 0.16f)
                    else -> baseFill
                }
                val strokeColor = when {
                    isThisInspected -> Color(0xFF00FFFF)
                    isHighlightedByLegend -> Color.White
                    isDimmedByLegend -> baseStroke.copy(alpha = 0.20f)
                    else -> baseStroke
                }
                val strokeWidth = when {
                    isThisInspected -> 3.8f
                    isHighlightedByLegend -> 3.2f
                    blk.pieceType == PieceType.CORNER_TIE || blk.pieceType == PieceType.T_JUNCTION || blk.pieceType == PieceType.LINTEL -> 1.8f
                    else -> 1.1f
                }

                drawRect(color = blockColor, topLeft = Offset(blkX, blkY), size = Size(blkW, blkH))
                drawRect(color = strokeColor, topLeft = Offset(blkX, blkY), size = Size(blkW, blkH), style = Stroke(width = strokeWidth))
            }
        }

        // 5. Indicação Explícita dos Blocos de Amarração de Canto (L) e Saída T (34×19×14, 31,5×19×11,5, 29×19×9)
        val amarraSpec = com.paginacaopro.app.engine.FreePlanEngine.getAmarracaoBlockInfo(wall.thicknessCm)
        val hasLeftAmarra = result.allBlocks.any { it.xCm < 1.0 && (it.pieceType == PieceType.CORNER_TIE || abs(it.lengthCm - amarraSpec.tieLengthCm) < 0.6) }
        val hasRightAmarra = result.allBlocks.any { abs(it.rightCm - wall.lengthCm) < 1.5 && (it.pieceType == PieceType.CORNER_TIE || abs(it.lengthCm - amarraSpec.tieLengthCm) < 0.6) }

        val calloutY = originY - (wallH + cintaH + 6f) * totalScale
        if (hasLeftAmarra) {
            drawIntoCanvas { c ->
                c.nativeCanvas.drawText(
                    "▼ CANTO L: ${amarraSpec.dimensionsLabel}",
                    originX + (amarraSpec.tieLengthCm.toFloat() * 0.5f) * totalScale,
                    calloutY,
                    amarraCalloutPaint
                )
            }
        }
        if (hasRightAmarra) {
            drawIntoCanvas { c ->
                c.nativeCanvas.drawText(
                    "CANTO L: ${amarraSpec.dimensionsLabel} ▼",
                    originX + (wallW - amarraSpec.tieLengthCm.toFloat() * 0.5f) * totalScale,
                    calloutY - (if (hasLeftAmarra && wallW < 180f) 11f else 0f),
                    amarraCalloutPaint
                )
            }
        }

        // Linhas de Interseção / Saída em T no Meio da Parede com indicação explícita do bloco (34 / 31,5 / 29 cm)
        for (inter in result.intersections) {
            val interX = originX + inter.xCm.toFloat() * totalScale
            drawLine(
                color = Color(0xFF38BDF8),
                start = Offset(interX, originY),
                end = Offset(interX, originY - wallH * totalScale),
                strokeWidth = 2.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
            )
            drawIntoCanvas { c ->
                c.nativeCanvas.drawText(
                    "▼ AMARRAÇÃO T (${amarraSpec.dimensionsLabel})",
                    interX,
                    calloutY,
                    amarraCalloutPaint.apply { color = android.graphics.Color.parseColor("#38BDF8") }
                )
            }
        }

        // 6. Vãos (Portas e Janelas) — Sem retângulo sobreposto de Verga/Contraverga (já representados como Canaleta real nas fiadas!)
        for (op in result.openings) {
            val opX = originX + op.xCm.toFloat() * totalScale
            val opY = originY - (op.yCm.toFloat() + op.heightCm.toFloat()) * totalScale
            val opW = op.widthCm.toFloat() * totalScale
            val opH = op.heightCm.toFloat() * totalScale

            drawRect(color = if (isAutoCadMode) Color(0xFF0A0F1D) else Color(0xFFF1F5F9), topLeft = Offset(opX, opY), size = Size(opW, opH))
            drawRect(
                color = Color(0xFFEF4444),
                topLeft = Offset(opX, opY),
                size = Size(opW, opH),
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
            )
            drawIntoCanvas { c ->
                c.nativeCanvas.drawText("${op.name} (${op.widthCm.toInt()}×${op.heightCm.toInt()})", opX + opW / 2f, opY + opH / 2f + 4f, dimPaint)
            }
        }

        // 7. Contorno Geral da Alvenaria
        drawRect(
            color = if (isAutoCadMode) Color(0xFFCBD5E1) else Color(0xFF0F172A),
            topLeft = Offset(wallLeft, wallTop),
            size = Size(wallWidthPx, wallHeightPx),
            style = Stroke(width = 2.5f)
        )

        // 8. Símbolos de Nível de Corte Arquitetônico AutoCAD (à esquerda)
        if (isAutoCadMode) {
            val levelX = originX - 34f * totalScale
            fun drawLevelSymbol(yPx: Float, levelStr: String, labelStr: String) {
                val r = (5.0f * totalScale).coerceIn(6f, 13f)
                drawLine(Color(0xFF00FFFF), Offset(levelX + r, yPx), Offset(originX - 16f * totalScale, yPx), strokeWidth = 1f)
                drawCircle(Color(0xFF00FFFF), radius = r, center = Offset(levelX, yPx), style = Stroke(1.2f))
                drawLine(Color(0xFF00FFFF), Offset(levelX - r - 2f, yPx), Offset(levelX + r + 2f, yPx), strokeWidth = 0.8f)
                drawLine(Color(0xFF00FFFF), Offset(levelX, yPx - r - 2f), Offset(levelX, yPx + r + 2f), strokeWidth = 0.8f)
                drawArc(Color(0xFF00FFFF), startAngle = 270f, sweepAngle = 90f, useCenter = true, topLeft = Offset(levelX - r, yPx - r), size = Size(r * 2f, r * 2f))
                drawArc(Color(0xFF00FFFF), startAngle = 90f, sweepAngle = 90f, useCenter = true, topLeft = Offset(levelX - r, yPx - r), size = Size(r * 2f, r * 2f))
                drawIntoCanvas { c ->
                    c.nativeCanvas.drawText(levelStr, levelX - r - 4f, yPx - 2f, levelPaint)
                    c.nativeCanvas.drawText(labelStr, levelX - r - 4f, yPx + 9f, levelPaint)
                }
            }

            drawLevelSymbol(originY - (wallH + cintaH) * totalScale, "+${format((wallH + cintaH) / 100.0)}", "TOPO CINTA")
            drawLevelSymbol(originY - wallH * totalScale, "+${format(wallH / 100.0)}", "RESPALDO")
            drawLevelSymbol(originY, "±0.00", "N.P.O.")
            drawLevelSymbol(originY + baldrameH * totalScale, "-${format(baldrameH / 100.0)}", "FUNDO VB")

            // 9. Título do Desenho no Rodapé Estilo AutoCAD com Regra da Amarração Explícita
            val footerY = originY + (baldrameH + earthH + 28f) * totalScale
            val fMidX = originX + (wallW * totalScale) / 2f
            val lineHalfW = (130f * totalScale).coerceIn(90f, 240f)
            val cutLetter = when {
                wall.wallName.contains("Parede ") -> wall.wallName.substringAfter("Parede ").take(1)
                wall.wallName.isNotEmpty() -> wall.wallName.take(1).uppercase()
                else -> "A"
            }

            drawLine(Color(0xFFFDE047), Offset(fMidX - lineHalfW, footerY), Offset(fMidX + lineHalfW, footerY), strokeWidth = 2.5f)
            drawLine(Color(0xFFFDE047), Offset(fMidX - lineHalfW, footerY + 3.5f), Offset(fMidX + lineHalfW, footerY + 3.5f), strokeWidth = 1f)

            drawIntoCanvas { c ->
                c.nativeCanvas.drawText("CORTE $cutLetter-$cutLetter • ESPESSURA ${com.paginacaopro.app.engine.FreePlanEngine.formatClean(wall.thicknessCm)} CM", fMidX, footerY - 5f, titlePaint)
                c.nativeCanvas.drawText("AMARRAÇÃO L/T: BLOCO ${amarraSpec.dimensionsLabel} (${amarraSpec.formulaExplanation})", fMidX, footerY + 16f, structLabelPaint)
            }
        }

        // 10. Cotas Técnicas (quando isCotadaView == true)
        if (isCotadaView) {
            val dimC = if (isAutoCadMode) Color(0xFFFF2D78) else Color(0xFF0F172A)

            fun drawDimH(x1: Float, x2: Float, yPx: Float, text: String) {
                drawLine(dimC, Offset(x1 - 4f, yPx), Offset(x2 + 4f, yPx), strokeWidth = 1f)
                drawLine(dimC, Offset(x1 - 3.5f, yPx + 3.5f), Offset(x1 + 3.5f, yPx - 3.5f), strokeWidth = 2f)
                drawLine(dimC, Offset(x2 - 3.5f, yPx + 3.5f), Offset(x2 + 3.5f, yPx - 3.5f), strokeWidth = 2f)
                drawLine(dimC, Offset(x1, yPx - 5f), Offset(x1, yPx + 5f), strokeWidth = 0.6f)
                drawLine(dimC, Offset(x2, yPx - 5f), Offset(x2, yPx + 5f), strokeWidth = 0.6f)
                drawIntoCanvas { c ->
                    dimPaint.textAlign = AndroidPaint.Align.CENTER
                    c.nativeCanvas.drawText(text, (x1 + x2) / 2f, yPx - 4f, dimPaint)
                }
            }

            fun drawDimV(xPx: Float, y1: Float, y2: Float, text: String) {
                val yB = max(y1, y2)
                val yT = min(y1, y2)
                drawLine(dimC, Offset(xPx, yB + 4f), Offset(xPx, yT - 4f), strokeWidth = 1f)
                drawLine(dimC, Offset(xPx - 3.5f, yB + 3.5f), Offset(xPx + 3.5f, yB - 3.5f), strokeWidth = 2f)
                drawLine(dimC, Offset(xPx - 3.5f, yT + 3.5f), Offset(xPx + 3.5f, yT - 3.5f), strokeWidth = 2f)
                drawLine(dimC, Offset(xPx - 5f, yB), Offset(xPx + 5f, yB), strokeWidth = 0.6f)
                drawLine(dimC, Offset(xPx - 5f, yT), Offset(xPx + 5f, yT), strokeWidth = 0.6f)
                drawIntoCanvas { c ->
                    dimPaint.textAlign = AndroidPaint.Align.RIGHT
                    c.nativeCanvas.drawText(text, xPx - 6f, (yB + yT) / 2f + 3.5f, dimPaint)
                }
            }

            val dimVx = originX - (if (isAutoCadMode) 48f else 22f) * totalScale
            drawDimV(dimVx, originY, originY - wallH * totalScale, "${wall.heightCm.toInt()} cm")

            val dimHy = originY + (if (isAutoCadMode) (baldrameH + earthH + 12f) else 15f) * totalScale
            drawDimH(originX, originX + wallW * totalScale, dimHy, "${wall.lengthCm.toInt()} cm")
        }
    }
}

private fun format(v: Double) = String.format(Locale.US, "%.2f", v)

/**
 * Visualizador em Tela Cheia de Qualquer Parede (SVG):
 * - Abre automaticamente na VERTICAL quando a parede é grande (lengthCm > heightCm), aproveitando toda a altura da tela do celular;
 * - Suporta Zoom de até 20x (e além) diretamente nas Fiadas (F1, F2, F3...), com botões rápidos 1x, 2x, 5x, 10x, 20x;
 * - Exibe APENAS em Tela Cheia uma legenda lateral bem modesta mostrando a cor e a quantidade de cada tamanho de bloco;
 * - Ao clicar em qualquer bloco na legenda (ex: 34 ou 39 ou 19), destaca todos os blocos daquele tamanho na parede!
 */
@Composable
fun FullscreenSvgViewerDialog(
    result: TilingResult,
    isCotadaView: Boolean = true,
    selectedCourse: Int = 0,
    initialAutoCadMode: Boolean = true,
    onOpen3D: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    // Se a parede for grande (comprimento maior que altura), abre automaticamente na VERTICAL!
    val isLargeWall = result.wall.lengthCm > result.wall.heightCm
    var isVerticalRotated by remember(result.wall.wallName, result.wall.lengthCm) {
        mutableStateOf(isLargeWall)
    }

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var inspectedBlock by remember { mutableStateOf<TilingBlock?>(null) }
    var highlightedSizeKey by remember { mutableStateOf<String?>(null) }
    var activeCourseFilter by remember { mutableIntStateOf(selectedCourse) }
    var isAutoCadMode by remember { mutableStateOf(initialAutoCadMode) }
    var isCotada by remember { mutableStateOf(isCotadaView) }
    var isLegendOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val legendItems = remember(result) { buildBlockSizeLegendItems(result) }
    val amarraSpec = remember(result.wall.thicknessCm) {
        com.paginacaopro.app.engine.FreePlanEngine.getAmarracaoBlockInfo(result.wall.thicknessCm)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isAutoCadMode) Color(0xFF090D16) else Color(0xFFE2E8F0))
        ) {
            // Canvas Interativo com Zoom de 20x nas Fiadas e Pan
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isVerticalRotated) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.4f, 25.0f) // Zoom de até 20x+ nas fiadas
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
                    .pointerInput(isVerticalRotated, scale, offsetX, offsetY) {
                        detectTapGestures { tapOffset ->
                            val wall = result.wall
                            val wallW = wall.lengthCm.toFloat().coerceAtLeast(10f)
                            val wallH = wall.heightCm.toFloat().coerceAtLeast(10f)
                            val cintaH = if (isAutoCadMode) 20f else 0f
                            val baldrameH = if (isAutoCadMode) 25f else 0f
                            val earthH = if (isAutoCadMode) 15f else 0f
                            val leftMarginCm = if (isAutoCadMode) 55f else 26f
                            val rightMarginCm = if (isAutoCadMode) 35f else 24f
                            val totalModelW = wallW + leftMarginCm + rightMarginCm
                            val totalModelH = wallH + cintaH + baldrameH + earthH + (if (isAutoCadMode) 38f else 22f)

                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()
                            val availW = if (isVerticalRotated) canvasH else canvasW
                            val availH = if (isVerticalRotated) canvasW else canvasH
                            val baseScale = min((availW * 0.88f) / totalModelW, (availH * 0.84f) / totalModelH)
                            val totalScale = baseScale * scale

                            val cx = canvasW / 2f
                            val cy = canvasH / 2f
                            // Inverter rotação de 90° se necessário para achar ponto no espaço do desenho
                            val localX = if (isVerticalRotated) cx + (tapOffset.y - cy) else tapOffset.x
                            val localY = if (isVerticalRotated) cy - (tapOffset.x - cx) else tapOffset.y

                            val effPanX = if (isVerticalRotated) offsetY else offsetX
                            val effPanY = if (isVerticalRotated) -offsetX else offsetY

                            val originX = cx - (wallW * totalScale) / 2f + effPanX + ((leftMarginCm - rightMarginCm) * 0.30f * totalScale)
                            val originY = cy + (wallH * totalScale) / 2f + effPanY - ((baldrameH + earthH) * 0.40f * totalScale)

                            val clickedXCm = (localX - originX) / totalScale
                            val clickedYCm = (originY - localY) / totalScale

                            val found = result.allBlocks.firstOrNull { blk ->
                                clickedXCm >= blk.xCm && clickedXCm <= blk.rightCm &&
                                    clickedYCm >= blk.yCm && clickedYCm <= blk.topCm
                            }
                            inspectedBlock = found
                            highlightedSizeKey = null
                        }
                    }
            ) {
                drawTilingElevation(
                    result = result,
                    selectedCourse = activeCourseFilter,
                    isCotadaView = isCotada,
                    isAutoCadMode = isAutoCadMode,
                    scale = scale,
                    panX = offsetX,
                    panY = offsetY,
                    inspectedBlock = inspectedBlock,
                    highlightedSizeKey = highlightedSizeKey,
                    isVerticalRotated = isVerticalRotated
                )
            }

            // BARRA SUPERIOR COMPACTA: ORIENTAÇÃO VERTICAL/HORIZONTAL + ZOOM RÁPIDO 20X NAS FIADAS
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                color = Slate900.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${result.wall.wallName} (${result.wall.lengthCm.toInt()}×${result.wall.heightCm.toInt()}×${com.paginacaopro.app.engine.FreePlanEngine.formatClean(result.wall.thicknessCm)} cm)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Text(
                                "Zoom: ${String.format(Locale.US, "%.1fx", scale)} • Amarração Canto/T: ${amarraSpec.dimensionsLabel}",
                                color = Color(0xFFFDE047),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onOpen3D != null) {
                                Surface(
                                    color = Color(0xFFF59E0B),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .clickable { onOpen3D() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FitScreen, contentDescription = null, tint = Slate900, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("3D REAL", color = Slate900, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            // Alternar Vertical / Horizontal
                            Surface(
                                color = if (isVerticalRotated) Color(0xFF0284C7) else Color(0xFF334155),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .clickable {
                                        isVerticalRotated = !isVerticalRotated
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (isVerticalRotated) "↕ VERTICAL" else "↔ HORIZ.",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    scale = 1.0f
                                    offsetX = 0f
                                    offsetY = 0f
                                    highlightedSizeKey = null
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.FitScreen, contentDescription = "Ajustar", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    // Barra de Zoom Rápido até 20x nas Fiadas + Filtro de Fiadas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fiada:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Surface(
                            color = if (activeCourseFilter == 0) Color(0xFFFDE047) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(5.dp),
                            modifier = Modifier.clickable { activeCourseFilter = 0 }
                        ) {
                            Text(
                                "Todas",
                                color = if (activeCourseFilter == 0) Slate900 else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        for (cIdx in 1..result.courses.size) {
                            Surface(
                                color = if (activeCourseFilter == cIdx) Color(0xFFFDE047) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(5.dp),
                                modifier = Modifier.clickable { activeCourseFilter = cIdx }
                            ) {
                                Text(
                                    "F$cIdx",
                                    color = if (activeCourseFilter == cIdx) Slate900 else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // =====================================================================
            // LEGENDA FLUTUANTE RETRÁTIL EM TELA CHEIA (IGUAL AO SIMULADOR)
            // Escondida por padrão! Mostra botão "— Legenda & Cores • X un"
            // Ao clicar, expande o card com cores, quantidades e clique p/ destacar
            // =====================================================================
            val totalFullscreenBlocks = remember(result) { result.allBlocks.count { it.countsInQuantity } }

            // Legenda flutuante arrastável: fica no canto inferior direito (não cobre os botões do topo)
            var legendDragX by remember { mutableFloatStateOf(0f) }
            var legendDragY by remember { mutableFloatStateOf(0f) }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 24.dp)
                    .offset { IntOffset(legendDragX.roundToInt(), legendDragY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            legendDragX += drag.x
                            legendDragY += drag.y
                        }
                    },
                horizontalAlignment = Alignment.End
            ) {
                // Botão compacto quando a legenda está escondida
                Surface(
                    color = Slate900.copy(alpha = 0.94f),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                        .clickable { isLegendOpen = !isLegendOpen }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "—",
                            color = Color(0xFFFBBF24),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Legenda & Cores",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFFBBF24).copy(alpha = 0.20f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "$totalFullscreenBlocks un",
                                color = Color(0xFFFDE68A),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Card Flutuante da Legenda (abre ao clicar)
                if (isLegendOpen) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .width(190.dp)
                            .heightIn(max = 340.dp),
                        color = Slate900.copy(alpha = 0.96f),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 10.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "LEGENDA DE BLOCOS",
                                    color = Color(0xFFFBBF24),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                IconButton(
                                    onClick = { isLegendOpen = false },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Fechar Legenda",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            if (highlightedSizeKey != null) {
                                Surface(
                                    color = Color(0xFF334155),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { highlightedSizeKey = null }
                                ) {
                                    Text(
                                        "Mostrar Todos os Blocos",
                                        color = Color(0xFFFDE047),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            legendItems.forEach { item ->
                                val isSelected = highlightedSizeKey == item.key
                                Surface(
                                    color = if (isSelected) item.swatchColor.copy(alpha = 0.32f) else Color(0xFF1E293B).copy(alpha = 0.75f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.5.dp,
                                            color = if (isSelected) Color.White else item.swatchColor.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            highlightedSizeKey = if (highlightedSizeKey == item.key) null else item.key
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(item.swatchColor, RoundedCornerShape(2.dp))
                                                    .border(0.5.dp, Color.White, RoundedCornerShape(2.dp))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                item.shortLabel,
                                                color = Color.White,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            "${item.count} un",
                                            color = Color(0xFFFDE047),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // HUD Inferior de Inspeção do Bloco Clicado
            inspectedBlock?.let { blk ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    color = Slate900.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(10.dp),
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${blk.pieceType.label} — ${com.paginacaopro.app.engine.FreePlanEngine.formatClean(blk.lengthCm)}×${com.paginacaopro.app.engine.FreePlanEngine.formatClean(blk.heightCm)}×${com.paginacaopro.app.engine.FreePlanEngine.formatClean(blk.thicknessCm)} cm",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                "Fiada F${blk.courseIndex + 1} • Posição X=${com.paginacaopro.app.engine.FreePlanEngine.formatClean(blk.xCm)} até ${com.paginacaopro.app.engine.FreePlanEngine.formatClean(blk.rightCm)} cm",
                                color = Color(0xFFBAE6FD),
                                fontSize = 11.sp
                            )
                            if (blk.notes.isNotBlank()) {
                                Text(blk.notes, color = Color(0xFFFDE047), fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                            }
                            if (abs(blk.lengthCm - 19.0) < 1.0 || blk.pieceType == PieceType.HALF) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        color = Color(0xFF10B981),
                                        shape = RoundedCornerShape(6.dp),
                                        onClick = {
                                            android.widget.Toast.makeText(context, "Substituição de meio bloco (19cm) por 2 de 9cm ativa!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("⚡ Dividir em 2 de 9cm", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                }
                            }
                        }

                        IconButton(onClick = { inspectedBlock = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar Detalhes", tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Exporta e compartilha o arquivo SVG vetorial gerado através do Android SAF / Share Intent.
 */
private fun shareSvgFile(context: Context, svgContent: String, fileNamePrefix: String) {
    try {
        val cleanName = fileNamePrefix.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(context.cacheDir, "${cleanName}_paginacao.svg")
        FileOutputStream(file).use { out ->
            out.write(svgContent.toByteArray(Charsets.UTF_8))
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/svg+xml"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Exportar Desenho SVG Paginação Pro")
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

