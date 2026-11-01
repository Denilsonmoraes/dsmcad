package com.paginacaopro.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Window
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import com.paginacaopro.app.model.TilingResult
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.paginacaopro.app.engine.FreePlanEngine
import com.paginacaopro.app.model.AuxLine90
import com.paginacaopro.app.model.BlockPieceCategory
import com.paginacaopro.app.model.CatalogBlockPiece
import com.paginacaopro.app.model.DetectedRoom
import com.paginacaopro.app.model.DetectedWallJunction
import com.paginacaopro.app.model.DimensionAdjustRule
import com.paginacaopro.app.model.DoorStyle
import com.paginacaopro.app.model.DrawingTool
import com.paginacaopro.app.model.FreePlanOpening
import com.paginacaopro.app.model.FreePlanWall
import com.paginacaopro.app.model.FreePlanWallPagination
import com.paginacaopro.app.model.JunctionKind
import com.paginacaopro.app.model.NominalWallThickness
import com.paginacaopro.app.model.OpeningType
import com.paginacaopro.app.model.PieceType
import com.paginacaopro.app.model.PlanEditorMode
import com.paginacaopro.app.ui.theme.PrimaryBlue
import com.paginacaopro.app.ui.theme.Slate800
import com.paginacaopro.app.ui.theme.Slate900
import com.paginacaopro.app.ui.viewmodel.PaginacaoViewModel
import com.paginacaopro.app.util.SvgExportUtil
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface as AndroidTypeface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreePlanStudioScreen(
    viewModel: PaginacaoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToElevationViewer: () -> Unit
) {
    val context = LocalContext.current
    val allWallsState by viewModel.freePlanWalls.collectAsState()
    val auxLines by viewModel.freePlanAuxLines.collectAsState()
    val catalog by viewModel.freePlanCatalog.collectAsState()
    val roomCustomNames by viewModel.freePlanRoomNames.collectAsState()
    val canUndo by viewModel.canUndoFreePlan.collectAsState()
    val canRedo by viewModel.canRedoFreePlan.collectAsState()
    val jointConfig by viewModel.jointConfig.collectAsState()
    val bondType by viewModel.bondType.collectAsState()

    // Modo 1 (Desenho Visual) vs Modo 2 (Execução & Cotas)
    var editorMode by remember { mutableStateOf(PlanEditorMode.VISUAL_DRAWING) }

    // Ferramenta ativa no Canvas
    var activeTool by remember { mutableStateOf(DrawingTool.WALL) }

    // Controles de Visibilidade, Movimentação e Redimensionamento da Planta Baixa e Painel Inferior
    var isStarterPlanHidden by remember { mutableStateOf(false) }
    var isMoveEntirePlanMode by remember { mutableStateOf(false) }
    var isCotagemPanelHidden by remember { mutableStateOf(false) }
    var cotagemOffsetX by remember { mutableFloatStateOf(0f) }
    var cotagemOffsetY by remember { mutableFloatStateOf(0f) }

    val starterLabels = remember { setOf("A", "B", "C", "D", "E", "F", "G", "H") }
    val walls = remember(allWallsState, isStarterPlanHidden) {
        if (isStarterPlanHidden) {
            allWallsState.filter { it.label !in starterLabels || !it.customName.startsWith("Contorno") && !it.customName.startsWith("Divisória") && !it.customName.startsWith("Parede ") }
        } else {
            allWallsState
        }
    }

    // 4 Menus Retráteis nas Bordas da Tela (Escondidos por padrão para deixar 100% da Tela Cheia livre para desenhar!)
    // O usuário clica no sinalzinho de menos/traço (—) na borda desejada (Cima, Baixo, Esquerda, Direita),
    // escolhe a função e o menu volta a se esconder automaticamente (ou clicando de novo no —).
    var isTopMenuOpen by remember { mutableStateOf(false) }
    var isBottomMenuOpen by remember { mutableStateOf(false) }
    var isLeftMenuOpen by remember { mutableStateOf(false) }
    var isRightMenuOpen by remember { mutableStateOf(false) }

    // Configurações da Ferramenta Parede (Espessura inicial = 14 cm conforme Padrão ABNT)
    var selectedThickness by remember { mutableStateOf(NominalWallThickness.THICK_14) }
    var defaultWallHeightCm by remember { mutableDoubleStateOf(280.0) }
    var drawAsExternalWall by remember { mutableStateOf(true) }
    var includeTopCintaCanaleta by remember { mutableStateOf(false) }

    // Parede selecionada para cotagem, abertura ou movimentação
    var selectedWallId by remember { mutableStateOf<String?>(walls.firstOrNull()?.id) }
    val selectedWall = walls.find { it.id == selectedWallId }

    // Estado de arraste no Canvas (desenho contínuo sem tirar o dedo, colando cantos sem passar além do encontro)
    var dragStartWorld by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var dragCurrentWorld by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var chainVertices by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
    var lockedSegmentAxis by remember { mutableStateOf<Char?>(null) } // 'H' ou 'V'
    var peakCoordAlongAxis by remember { mutableStateOf<Float?>(null) }
    var isSquareRoomQuickDrag by remember { mutableStateOf(false) }

    // Escala e Pan do Canvas 2D (px por metro)
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Diálogos e Modais Executivos
    var showBlockCatalogDialog by remember { mutableStateOf(false) }
    var pendingOpeningTarget by remember { mutableStateOf<Pair<FreePlanWall, Double>?>(null) }
    var editingRoom by remember { mutableStateOf<DetectedRoom?>(null) }
    var showExecutivePaginationSheet by remember { mutableStateOf(false) }
    var conflictBannerMessage by remember { mutableStateOf<String?>(null) }
    var fullscreenWallResult by remember { mutableStateOf<com.paginacaopro.app.model.TilingResult?>(null) }

    // Cálculos topológicos em tempo real: Encontros (L, T, Cruz) e Ambientes Fechados
    val detectedJunctions = remember(walls) {
        FreePlanEngine.detectWallJunctions(walls)
    }
    val detectedRooms = remember(walls, roomCustomNames) {
        FreePlanEngine.detectClosedRooms(walls, roomCustomNames)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(Color(0xFF0B0F17))
    ) {
        // =========================================================================
        // 1. ÁREA DE DESENHO INTERATIVA 100% TELA CHEIA (SEM MENUS FIXOS OCUPANDO ESPAÇO)
        // =========================================================================
        FreePlanInteractiveCanvas(
            walls = walls,
            auxLines = auxLines,
            rooms = detectedRooms,
            junctions = detectedJunctions,
            selectedWallId = selectedWallId,
            activeTool = activeTool,
            selectedThicknessCm = selectedThickness.thicknessCm,
            zoomScale = zoomScale,
            panOffsetX = panOffsetX,
            panOffsetY = panOffsetY,
            dragStartWorld = dragStartWorld,
            dragCurrentWorld = dragCurrentWorld,
            chainVertices = chainVertices,
            isSquareRoomQuickDrag = isSquareRoomQuickDrag,
            onSelectWall = { wallId ->
                selectedWallId = wallId
            },
            onSelectRoom = { room ->
                editingRoom = room
            },
            onTapWallForOpening = { wall, distCm ->
                selectedWallId = wall.id
                pendingOpeningTarget = wall to distCm
            },
            onEraseWall = { wallId ->
                viewModel.deleteFreePlanWall(wallId)
                if (selectedWallId == wallId) {
                    selectedWallId = walls.firstOrNull { it.id != wallId }?.id
                }
            },
            onEraseAuxLine = { lineId ->
                viewModel.deleteFreePlanAuxLine(lineId)
            },
            onDragStart = { wx, wy ->
                // Ao começar a desenhar no canvas, esconde qualquer menu aberto para dar 100% de visão
                isTopMenuOpen = false
                isLeftMenuOpen = false
                isRightMenuOpen = false
                if (isMoveEntirePlanMode) {
                    dragStartWorld = wx to wy
                    dragCurrentWorld = wx to wy
                } else {
                    val snapped = FreePlanEngine.snapPointToPlan(wx, wy, walls, auxLines, snapRadiusMeters = 0.68f)
                    dragStartWorld = snapped
                    dragCurrentWorld = snapped
                    chainVertices = listOf(snapped)
                    lockedSegmentAxis = null
                    peakCoordAlongAxis = null
                }
            },
            onDragMove = { wx, wy ->
                val start = dragStartWorld ?: return@FreePlanInteractiveCanvas
                if (isMoveEntirePlanMode) {
                    val basePx = 64f * zoomScale
                    panOffsetX += (wx - start.first) * basePx
                    panOffsetY += (wy - start.second) * basePx
                    return@FreePlanInteractiveCanvas
                }

                if (activeTool == DrawingTool.WALL && isSquareRoomQuickDrag) {
                    val snapped = FreePlanEngine.snapPointToPlan(wx, wy, walls, auxLines, snapRadiusMeters = 0.65f)
                    dragCurrentWorld = snapped
                } else if (activeTool == DrawingTool.WALL) {
                    val dx = wx - start.first
                    val dy = wy - start.second
                    if (lockedSegmentAxis == null && hypot(dx.toDouble(), dy.toDouble()) >= 0.18) {
                        lockedSegmentAxis = if (abs(dx) >= abs(dy)) 'H' else 'V'
                        peakCoordAlongAxis = if (lockedSegmentAxis == 'H') wx else wy
                    }

                    val axis = lockedSegmentAxis ?: if (abs(dx) >= abs(dy)) 'H' else 'V'
                    if (axis == 'H') {
                        val prevPeakX = peakCoordAlongAxis ?: wx
                        val updatedPeakX = if (abs(wx - start.first) >= abs(prevPeakX - start.first)) wx else prevPeakX
                        peakCoordAlongAxis = updatedPeakX

                        val clampedH = FreePlanEngine.snapAndClampWallEndpoint(
                            startX = start.first,
                            startY = start.second,
                            rawEndX = updatedPeakX,
                            rawEndY = start.second,
                            walls = walls,
                            auxLines = auxLines,
                            chainVertices = chainVertices,
                            snapTolMeters = 0.75f
                        )
                        val segLenH = abs(clampedH.first - start.first)
                        val perpDistV = abs(wy - start.second)
                        if (segLenH >= 0.40f && perpDistV >= 0.36f) {
                            val created = viewModel.addFreePlanWall(
                                startX = start.first,
                                startY = start.second,
                                endX = clampedH.first,
                                endY = clampedH.second,
                                thicknessCm = selectedThickness.thicknessCm,
                                heightCm = defaultWallHeightCm,
                                isExternal = drawAsExternalWall,
                                includeTopCintaCanaleta = includeTopCintaCanaleta
                            )
                            val newCorner = if (created != null) {
                                selectedWallId = created.id
                                created.endX to created.endY
                            } else {
                                clampedH.first to clampedH.second
                            }
                            val updatedChain = chainVertices + newCorner
                            chainVertices = updatedChain
                            dragStartWorld = newCorner
                            lockedSegmentAxis = 'V'
                            peakCoordAlongAxis = wy
                            dragCurrentWorld = FreePlanEngine.snapAndClampWallEndpoint(
                                startX = newCorner.first,
                                startY = newCorner.second,
                                rawEndX = newCorner.first,
                                rawEndY = wy,
                                walls = walls,
                                auxLines = auxLines,
                                chainVertices = updatedChain,
                                snapTolMeters = 0.75f
                            )
                        } else {
                            dragCurrentWorld = FreePlanEngine.snapAndClampWallEndpoint(
                                startX = start.first,
                                startY = start.second,
                                rawEndX = wx,
                                rawEndY = start.second,
                                walls = walls,
                                auxLines = auxLines,
                                chainVertices = chainVertices,
                                snapTolMeters = 0.75f
                            )
                        }
                    } else {
                        val prevPeakY = peakCoordAlongAxis ?: wy
                        val updatedPeakY = if (abs(wy - start.second) >= abs(prevPeakY - start.second)) wy else prevPeakY
                        peakCoordAlongAxis = updatedPeakY

                        val clampedV = FreePlanEngine.snapAndClampWallEndpoint(
                            startX = start.first,
                            startY = start.second,
                            rawEndX = start.first,
                            rawEndY = updatedPeakY,
                            walls = walls,
                            auxLines = auxLines,
                            chainVertices = chainVertices,
                            snapTolMeters = 0.75f
                        )
                        val segLenV = abs(clampedV.second - start.second)
                        val perpDistH = abs(wx - start.first)
                        if (segLenV >= 0.40f && perpDistH >= 0.36f) {
                            val created = viewModel.addFreePlanWall(
                                startX = start.first,
                                startY = start.second,
                                endX = clampedV.first,
                                endY = clampedV.second,
                                thicknessCm = selectedThickness.thicknessCm,
                                heightCm = defaultWallHeightCm,
                                isExternal = drawAsExternalWall,
                                includeTopCintaCanaleta = includeTopCintaCanaleta
                            )
                            val newCorner = if (created != null) {
                                selectedWallId = created.id
                                created.endX to created.endY
                            } else {
                                clampedV.first to clampedV.second
                            }
                            val updatedChain = chainVertices + newCorner
                            chainVertices = updatedChain
                            dragStartWorld = newCorner
                            lockedSegmentAxis = 'H'
                            peakCoordAlongAxis = wx
                            dragCurrentWorld = FreePlanEngine.snapAndClampWallEndpoint(
                                startX = newCorner.first,
                                startY = newCorner.second,
                                rawEndX = wx,
                                rawEndY = newCorner.second,
                                walls = walls,
                                auxLines = auxLines,
                                chainVertices = updatedChain,
                                snapTolMeters = 0.75f
                            )
                        } else {
                            dragCurrentWorld = FreePlanEngine.snapAndClampWallEndpoint(
                                startX = start.first,
                                startY = start.second,
                                rawEndX = start.first,
                                rawEndY = wy,
                                walls = walls,
                                auxLines = auxLines,
                                chainVertices = chainVertices,
                                snapTolMeters = 0.75f
                            )
                        }
                    }
                } else if (activeTool == DrawingTool.AUX_LINE_90) {
                    val (orthoX, orthoY) = FreePlanEngine.projectOrthogonal90(start.first, start.second, wx, wy)
                    val snapped = FreePlanEngine.snapPointToPlan(orthoX, orthoY, walls, auxLines)
                    dragCurrentWorld = FreePlanEngine.projectOrthogonal90(start.first, start.second, snapped.first, snapped.second)
                } else if (activeTool == DrawingTool.SELECT_MOVE) {
                    dragCurrentWorld = wx to wy
                }
            },
            onDragEnd = {
                val start = dragStartWorld
                val end = dragCurrentWorld
                if (!isMoveEntirePlanMode && start != null && end != null) {
                    when (activeTool) {
                        DrawingTool.WALL -> {
                            if (isSquareRoomQuickDrag) {
                                val x1 = min(start.first, end.first)
                                val x2 = max(start.first, end.first)
                                val y1 = min(start.second, end.second)
                                val y2 = max(start.second, end.second)
                                if (x2 - x1 >= 0.5f && y2 - y1 >= 0.5f) {
                                    viewModel.addFreePlanWall(x1, y1, x2, y1, selectedThickness.thicknessCm, defaultWallHeightCm, drawAsExternalWall, includeTopCintaCanaleta)
                                    viewModel.addFreePlanWall(x2, y1, x2, y2, selectedThickness.thicknessCm, defaultWallHeightCm, drawAsExternalWall, includeTopCintaCanaleta)
                                    viewModel.addFreePlanWall(x2, y2, x1, y2, selectedThickness.thicknessCm, defaultWallHeightCm, drawAsExternalWall, includeTopCintaCanaleta)
                                    val lastW = viewModel.addFreePlanWall(x1, y2, x1, y1, selectedThickness.thicknessCm, defaultWallHeightCm, drawAsExternalWall, includeTopCintaCanaleta)
                                    if (lastW != null) selectedWallId = lastW.id
                                }
                            } else {
                                var clampedFinal = FreePlanEngine.snapAndClampWallEndpoint(
                                    startX = start.first,
                                    startY = start.second,
                                    rawEndX = end.first,
                                    rawEndY = end.second,
                                    walls = walls,
                                    auxLines = auxLines,
                                    chainVertices = chainVertices,
                                    snapTolMeters = 0.85f
                                )
                                val firstVertex = chainVertices.firstOrNull()
                                if (firstVertex != null && chainVertices.size >= 3) {
                                    if (hypot(clampedFinal.first - firstVertex.first, clampedFinal.second - firstVertex.second) <= 0.95f) {
                                        clampedFinal = firstVertex
                                    }
                                }
                                val created = viewModel.addFreePlanWall(
                                    startX = start.first,
                                    startY = start.second,
                                    endX = clampedFinal.first,
                                    endY = clampedFinal.second,
                                    thicknessCm = selectedThickness.thicknessCm,
                                    heightCm = defaultWallHeightCm,
                                    isExternal = drawAsExternalWall,
                                    includeTopCintaCanaleta = includeTopCintaCanaleta
                                )
                                if (created != null) {
                                    selectedWallId = created.id
                                }
                            }
                        }


                        DrawingTool.AUX_LINE_90 -> {
                            viewModel.addFreePlanAuxLine90(
                                startX = start.first,
                                startY = start.second,
                                endX = end.first,
                                endY = end.second
                            )
                        }
                        DrawingTool.SELECT_MOVE -> {
                            val dx = end.first - start.first
                            val dy = end.second - start.second
                            val selId = selectedWallId
                            if (selId != null && hypot(dx.toDouble(), dy.toDouble()) >= 0.15) {
                                viewModel.moveFreePlanWall(selId, dx, dy)
                            } else if (hypot(dx.toDouble(), dy.toDouble()) >= 0.15) {
                                viewModel.moveEntireFreePlan(dx, dy)
                            }
                        }
                        else -> {}
                    }
                }
                dragStartWorld = null
                dragCurrentWorld = null
                chainVertices = emptyList()
                lockedSegmentAxis = null
                peakCoordAlongAxis = null
            }
        )

        // =========================================================================
        // BORDA 1 (PARA CIMA - Alignment.TopCenter): MENU SUPERIOR RETRÁTIL COM SINAL DE MENOS "—"
        // =========================================================================
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp, start = 12.dp, end = 12.dp)
        ) {
            if (!isTopMenuOpen) {
                // Sinalzinho de menos / traço discreto no topo
                Surface(
                    onClick = { isTopMenuOpen = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Slate900.copy(alpha = 0.85f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("—", color = Color(0xFFFDE047), fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text("Topo / Paginar", color = Color(0xFFCBD5E1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Slate900.copy(alpha = 0.96f),
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        isTopMenuOpen = false
                                        onNavigateBack()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(
                                        text = "Planta Livre (${walls.size} paredes • ${detectedRooms.size} cômodos)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { viewModel.undoFreePlan() },
                                    enabled = canUndo,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Undo, contentDescription = "Desfazer", tint = if (canUndo) Color.White else Color(0xFF475569), modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.redoFreePlan() },
                                    enabled = canRedo,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Redo, contentDescription = "Refazer", tint = if (canRedo) Color.White else Color(0xFF475569), modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = {
                                        isTopMenuOpen = false
                                        showBlockCatalogDialog = true
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.GridOn, contentDescription = "Blocos", tint = Color(0xFFFDE047), modifier = Modifier.size(18.dp))
                                }
                                Button(
                                    onClick = {
                                        isTopMenuOpen = false
                                        showExecutivePaginationSheet = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PAGINAR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                // Botão Sinal de Menos (—) para esconder o menu de volta para cima
                                Surface(
                                    onClick = { isTopMenuOpen = false },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.border(1.dp, Color(0xFFFDE047), RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = " — ",
                                        color = Color(0xFFFDE047),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }



                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PlanEditorMode.entries.forEach { mode ->
                                val isSelected = editorMode == mode
                                Surface(
                                    onClick = {
                                        editorMode = mode
                                        if (mode == PlanEditorMode.EXECUTIVE_ENGINEERING && activeTool == DrawingTool.WALL) {
                                            activeTool = DrawingTool.SELECT_MOVE
                                            isBottomMenuOpen = true
                                        } else if (mode == PlanEditorMode.VISUAL_DRAWING && activeTool == DrawingTool.SELECT_MOVE) {
                                            activeTool = DrawingTool.WALL
                                        }
                                        isTopMenuOpen = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) PrimaryBlue else Color(0xFF0F172A)
                                ) {
                                    Text(
                                        text = mode.title,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Família de Blocos:", color = Color(0xFFFDE047), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            NominalWallThickness.entries.forEach { thick ->
                                val isAllCurrent = walls.isNotEmpty() && walls.all { kotlin.math.abs(it.thicknessCm - thick.thicknessCm) < 0.2 }
                                Surface(
                                    onClick = {
                                        viewModel.changeAllFreePlanWallsThickness(thick.thicknessCm)
                                        selectedThickness = thick
                                    },
                                    color = if (isAllCurrent) Color(0xFF0284C7) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.border(1.dp, if (isAllCurrent) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = thick.label,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = if (isAllCurrent) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }


                    }
                }
            }
        }

        // =========================================================================
        // BORDA 2 (LADO ESQUERDO - Alignment.CenterStart): MENU DE FERRAMENTAS E ESPESSURA RETRÁTIL COM "—"
        // =========================================================================
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
        ) {
            if (!isLeftMenuOpen) {
                // Sinalzinho de menos / traço na borda esquerda
                Surface(
                    onClick = { isLeftMenuOpen = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Slate900.copy(alpha = 0.85f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("—", color = Color(0xFF38BDF8), fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Text("Ferram.", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(selectedThickness.label, color = Color(0xFFFDE047), fontSize = 8.sp)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Slate900.copy(alpha = 0.96f),
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .width(220.dp)
                        .heightIn(max = 360.dp)
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("FERRAMENTAS", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            // Botão Sinal de Menos (—) para esconder para a esquerda
                            Surface(
                                onClick = { isLeftMenuOpen = false },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.border(1.dp, Color(0xFFFDE047), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = " — ",
                                    color = Color(0xFFFDE047),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        ToolPillButton(
                            title = "Parede Contínua (${selectedThickness.label})",
                            icon = Icons.Default.Architecture,
                            isSelected = activeTool == DrawingTool.WALL && !isSquareRoomQuickDrag && !isMoveEntirePlanMode,
                            accent = Color(0xFF38BDF8),
                            onClick = {
                                isMoveEntirePlanMode = false
                                activeTool = DrawingTool.WALL
                                isSquareRoomQuickDrag = false
                                isLeftMenuOpen = false // Esconde automaticamente após escolher!
                            }
                        )

                        ToolPillButton(
                            title = "Quadrado 1-Toque",
                            icon = Icons.Default.SquareFoot,
                            isSelected = activeTool == DrawingTool.WALL && isSquareRoomQuickDrag && !isMoveEntirePlanMode,
                            accent = Color(0xFF34D399),
                            onClick = {
                                isMoveEntirePlanMode = false
                                activeTool = DrawingTool.WALL
                                isSquareRoomQuickDrag = true
                                isLeftMenuOpen = false
                            }
                        )

                        ToolPillButton(
                            title = "Linha Auxiliar 90°",
                            icon = Icons.Default.Straighten,
                            isSelected = activeTool == DrawingTool.AUX_LINE_90 && !isMoveEntirePlanMode,
                            accent = Color(0xFF22D3EE),
                            onClick = {
                                isMoveEntirePlanMode = false
                                activeTool = DrawingTool.AUX_LINE_90
                                isLeftMenuOpen = false
                            }
                        )

                        ToolPillButton(
                            title = "Inserir Porta",
                            icon = Icons.Default.DoorFront,
                            isSelected = activeTool == DrawingTool.DOOR && !isMoveEntirePlanMode,
                            accent = Color(0xFFFB923C),
                            onClick = {
                                isMoveEntirePlanMode = false
                                activeTool = DrawingTool.DOOR
                                isLeftMenuOpen = false
                                Toast.makeText(context, "Toque na parede para inserir Porta", Toast.LENGTH_SHORT).show()
                            }
                        )

                        ToolPillButton(
                            title = "Inserir Janela",
                            icon = Icons.Default.Window,
                            isSelected = activeTool == DrawingTool.WINDOW && !isMoveEntirePlanMode,
                            accent = Color(0xFF60A5FA),
                            onClick = {
                                isMoveEntirePlanMode = false
                                activeTool = DrawingTool.WINDOW
                                isLeftMenuOpen = false
                                Toast.makeText(context, "Toque na parede para inserir Janela", Toast.LENGTH_SHORT).show()
                            }
                        )

                        ToolPillButton(
                            title = "Apagar Parede/Linha",
                            icon = Icons.Default.Delete,
                            isSelected = activeTool == DrawingTool.ERASER && !isMoveEntirePlanMode,
                            accent = Color(0xFFEF4444),
                            onClick = {
                                isMoveEntirePlanMode = false
                                activeTool = DrawingTool.ERASER
                                isLeftMenuOpen = false
                            }
                        )

                        Divider(color = Color(0xFF334155))
                        Text("Espessura do Bloco:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            NominalWallThickness.entries.forEach { thick ->
                                val isSel = selectedThickness == thick
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        selectedThickness = thick
                                        if (selectedWallId != null) {
                                            viewModel.changeFreePlanWallThickness(selectedWallId!!, thick.thicknessCm)
                                        }
                                        isLeftMenuOpen = false
                                    },
                                    label = { Text(thick.label, fontSize = 10.sp) }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.changeAllFreePlanWallsThickness(selectedThickness.thicknessCm)
                                isLeftMenuOpen = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("Mudar TODA a Planta para ${selectedThickness.label}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = drawAsExternalWall,
                                onClick = {
                                    drawAsExternalWall = true
                                    isLeftMenuOpen = false
                                },
                                label = { Text("Externa", fontSize = 9.sp) }
                            )
                            FilterChip(
                                selected = !drawAsExternalWall,
                                onClick = {
                                    drawAsExternalWall = false
                                    isLeftMenuOpen = false
                                },
                                label = { Text("Interna", fontSize = 9.sp) }
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                isLeftMenuOpen = false
                                showBlockCatalogDialog = true
                            },
                            modifier = Modifier.fillMaxWidth().height(28.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Catálogo de Blocos & Canaletas", fontSize = 9.sp, color = Color(0xFFFDE047))
                        }
                    }
                }
            }
        }

        // =========================================================================
        // BORDA 3 (LADO DIREITO - Alignment.CenterEnd): MENU DE PLANTA / ZOOM / MOVER / REDIMENSIONAR COM "—"
        // =========================================================================
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
        ) {
            if (!isRightMenuOpen) {
                // Sinalzinho de menos / traço na borda direita
                Surface(
                    onClick = { isRightMenuOpen = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Slate900.copy(alpha = 0.85f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.border(1.dp, Color(0xFFFDE047), RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("—", color = Color(0xFFFDE047), fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Text("Planta", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("Zoom", color = Color(0xFF38BDF8), fontSize = 8.sp)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Slate900.copy(alpha = 0.96f),
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .width(205.dp)
                        .heightIn(max = 360.dp)
                        .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PLANTA & ZOOM", color = Color(0xFFFDE047), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            // Botão Sinal de Menos (—) para esconder para a direita
                            Surface(
                                onClick = { isRightMenuOpen = false },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.border(1.dp, Color(0xFFFDE047), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = " — ",
                                    color = Color(0xFFFDE047),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { zoomScale = (zoomScale * 1.25f).coerceIn(0.35f, 4.0f) },
                                modifier = Modifier.weight(1f).height(30.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                            ) {
                                Text("Zoom +", fontSize = 10.sp, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { zoomScale = (zoomScale / 1.25f).coerceIn(0.35f, 4.0f) },
                                modifier = Modifier.weight(1f).height(30.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                            ) {
                                Text("Zoom -", fontSize = 10.sp, color = Color.White)
                            }
                        }

                        // Redimensionar a Planta Baixa (+15% / -15%)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.scaleEntireFreePlan(1.15f)
                                    isRightMenuOpen = false
                                },
                                modifier = Modifier.weight(1f).height(30.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                            ) {
                                Text("Tam. +15%", fontSize = 10.sp, color = Color(0xFF34D399))
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.scaleEntireFreePlan(0.85f)
                                    isRightMenuOpen = false
                                },
                                modifier = Modifier.weight(1f).height(30.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                            ) {
                                Text("Tam. -15%", fontSize = 10.sp, color = Color(0xFFFB923C))
                            }
                        }

                        ToolPillButton(
                            title = if (isMoveEntirePlanMode) "Parar de Mover Planta" else "Mover Planta (Arrastar)",
                            icon = Icons.Default.OpenWith,
                            isSelected = isMoveEntirePlanMode,
                            accent = Color(0xFFFDE047),
                            onClick = {
                                isMoveEntirePlanMode = !isMoveEntirePlanMode
                                isRightMenuOpen = false
                            }
                        )

                        ToolPillButton(
                            title = if (isStarterPlanHidden) "Mostrar Planta" else "Ocultar Planta",
                            icon = if (isStarterPlanHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            isSelected = isStarterPlanHidden,
                            accent = Color(0xFFA78BFA),
                            onClick = {
                                isStarterPlanHidden = !isStarterPlanHidden
                                isRightMenuOpen = false
                            }
                        )

                        OutlinedButton(
                            onClick = {
                                zoomScale = 1.0f
                                panOffsetX = 0f
                                panOffsetY = 0f
                                isRightMenuOpen = false
                            },
                            modifier = Modifier.fillMaxWidth().height(28.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                        ) {
                            Text("Centralizar Vista", fontSize = 10.sp, color = Color(0xFF38BDF8))
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.clearFreePlanCanvas()
                                selectedWallId = null
                                isRightMenuOpen = false
                            },
                            modifier = Modifier.fillMaxWidth().height(28.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                        ) {
                            Text("Limpar Tudo (Tela Limpa)", fontSize = 10.sp, color = Color(0xFFFCA5A5))
                        }

                        OutlinedButton(
                            onClick = {
                                isStarterPlanHidden = false
                                viewModel.loadStarterFreePlan()
                                isRightMenuOpen = false
                            },
                            modifier = Modifier.fillMaxWidth().height(28.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp)
                        ) {
                            Text("Recarregar Planta Exemplo", fontSize = 10.sp, color = Color(0xFF93C5FD))
                        }
                    }
                }
            }
        }

        // =========================================================================
        // BORDA 4 (PARA BAIXO - Alignment.BottomCenter): COTAS & SVG DA PAREDE RETRÁTIL COM "—"
        // =========================================================================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp, start = 8.dp, end = 8.dp)
        ) {
            if (!isBottomMenuOpen || selectedWall == null) {
                // Sinalzinho de menos / traço discreto na borda inferior
                Surface(
                    onClick = {
                        if (selectedWall == null && walls.isNotEmpty()) {
                            selectedWallId = walls.first().id
                        }
                        isBottomMenuOpen = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = Slate900.copy(alpha = 0.85f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("—", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = if (selectedWall != null) "Cotas & SVG (${selectedWall.displayName})" else "Cotas & SVG da Parede",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                WallDimensionCotagemCard(
                    wall = selectedWall,
                    allWalls = walls,
                    catalog = catalog,
                    jointConfig = jointConfig,
                    bondType = bondType,
                    modifier = Modifier
                        .offset { IntOffset(cotagemOffsetX.roundToInt(), cotagemOffsetY.roundToInt()) }
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                cotagemOffsetX += dragAmount.x
                                cotagemOffsetY += dragAmount.y
                            }
                        },
                    onHideCard = {
                        isBottomMenuOpen = false
                    },
                    onApplyDimension = { lenCm, hCm, thickCm, locked, cinta, rule ->
                        val outcome = viewModel.applyRealDimensionToFreePlanWall(
                            wallId = selectedWall.id,
                            newLengthCm = lenCm,
                            heightCm = hCm,
                            thicknessCm = thickCm,
                            isLocked = locked,
                            includeCintaCanaleta = cinta,
                            rule = rule
                        )
                        if (outcome.hasConflict) {
                            conflictBannerMessage = outcome.message
                        } else {
                            conflictBannerMessage = null
                            Toast.makeText(context, outcome.message, Toast.LENGTH_SHORT).show()
                        }
                        isBottomMenuOpen = false // Esconde automaticamente após cotar!
                    },
                    onApplyAllThickness = { th ->
                        viewModel.changeAllFreePlanWallsThickness(th)
                    },
                    onOpenAddDoor = {
                        pendingOpeningTarget = selectedWall to (selectedWall.effectiveLengthCm * 0.25)
                        activeTool = DrawingTool.DOOR
                        isBottomMenuOpen = false
                    },
                    onOpenAddWindow = {
                        pendingOpeningTarget = selectedWall to (selectedWall.effectiveLengthCm * 0.35)
                        activeTool = DrawingTool.WINDOW
                        isBottomMenuOpen = false
                    },
                    onRemoveOpening = { opId ->
                        viewModel.removeOpeningFromFreePlan(selectedWall.id, opId)
                    },
                    onOpenWallElevation = {
                        isBottomMenuOpen = false
                        viewModel.openFreePlanWallInElevationViewer(selectedWall.id)
                        onNavigateToElevationViewer()
                    },
                    onExpandWallSvgFullscreen = { tilingRes ->
                        isBottomMenuOpen = false
                        fullscreenWallResult = tilingRes
                    }
                )
            }
        }
    }

    // =========================================================================
    // MODAL 1: CADASTRO E SELEÇÃO DE BLOCOS E CANALETAS DA OBRA (Seção 3)
    // =========================================================================
    if (showBlockCatalogDialog) {
        BlockCatalogSelectionDialog(
            catalog = catalog,
            initialThickness = selectedThickness,
            verticalJointCm = jointConfig.verticalCm,
            onTogglePiece = { viewModel.toggleCatalogPieceAvailability(it) },
            onAddCustomPiece = { viewModel.addCustomCatalogPiece(it) },
            onDismiss = { showBlockCatalogDialog = false }
        )
    }

    // =========================================================================
    // MODAL 2: INSERÇÃO DE PORTA OU JANELA NA PAREDE (Seção 7)
    // =========================================================================
    pendingOpeningTarget?.let { (targetWall, suggestedDistCm) ->
        AddFreePlanOpeningDialog(
            wall = targetWall,
            initialType = if (activeTool == DrawingTool.WINDOW) OpeningType.WINDOW else OpeningType.DOOR,
            suggestedDistCm = suggestedDistCm,
            onConfirm = { newOpening ->
                viewModel.addOpeningToFreePlanWall(targetWall.id, newOpening)
                pendingOpeningTarget = null
            },
            onDismiss = { pendingOpeningTarget = null }
        )
    }

    // =========================================================================
    // MODAL 3: RENOMEAR AMBIENTE RECONHECIDO AUTOMATICAMENTE (Seção 10.1)
    // =========================================================================
    editingRoom?.let { room ->
        var roomNameInput by remember(room) { mutableStateOf(room.customName ?: room.suggestedType) }
        AlertDialog(
            onDismissRequest = { editingRoom = null },
            title = { Text("Identificação do Ambiente", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text(
                        "Área reconhecida: ${room.areaM2} m² (Paredes: ${room.boundingWallLabels.joinToString(", ")})",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = roomNameInput,
                        onValueChange = { roomNameInput = it },
                        label = { Text("Nome do Ambiente (ex: Sala, Quarto 1, Banheiro)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (roomNameInput.isNotBlank()) {
                        viewModel.renameDetectedRoom(room.id, "Ambiente ${room.defaultIndex}: ${roomNameInput.trim()}")
                    }
                    editingRoom = null
                }) {
                    Text("Salvar Nome")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRoom = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // =========================================================================
    // MODAL 4: PAINEL EXECUTIVO DE PAGINAÇÃO AUTOMÁTICA & EXPORTAÇÃO SVG/PDF
    // =========================================================================
    if (showExecutivePaginationSheet) {
        val allPaginations = remember(walls, catalog, jointConfig, bondType) {
            walls.map { w ->
                FreePlanEngine.paginateFreePlanWall(
                    wall = w,
                    allWalls = walls,
                    catalogPieces = catalog,
                    jointConfig = jointConfig,
                    bondType = bondType
                )
            }
        }

        ExecutivePaginationReportDialog(
            walls = walls,
            auxLines = auxLines,
            rooms = detectedRooms,
            junctions = detectedJunctions,
            paginations = allPaginations,
            onOpenWallInAutoCadViewer = { wallId ->
                showExecutivePaginationSheet = false
                viewModel.openFreePlanWallInElevationViewer(wallId)
                onNavigateToElevationViewer()
            },
            onExpandWallSvgFullscreen = { tilingRes ->
                fullscreenWallResult = tilingRes
            },
            onDismiss = { showExecutivePaginationSheet = false }
        )
    }

    // =========================================================================
    // MODAL 5: VISUALIZADOR SVG EM TELA CHEIA (VERTICAL P/ PAREDES GRANDES + ZOOM 20X + LEGENDA LATERAL INTERATIVA)
    // =========================================================================
    var show3DViewerInStudio by remember { mutableStateOf(false) }

    fullscreenWallResult?.let { activeRes ->
        FullscreenSvgViewerDialog(
            result = activeRes,
            isCotadaView = true,
            selectedCourse = 0,
            initialAutoCadMode = true,
            onOpen3D = { show3DViewerInStudio = true },
            onDismiss = { fullscreenWallResult = null }
        )
    }

    if (show3DViewerInStudio && fullscreenWallResult != null) {
        com.paginacaopro.app.ui.components.ThreeDViewerDialog(
            tilingResult = fullscreenWallResult!!,
            onDismiss = { show3DViewerInStudio = false }
        )
    }
}

@Composable
private fun ToolPillButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) accent.copy(alpha = 0.22f) else Color(0xFF1E293B),
        modifier = Modifier.border(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) accent else Color(0xFF334155),
            shape = RoundedCornerShape(10.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = if (isSelected) accent else Color.White, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                color = if (isSelected) accent else Color.White,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Canvas 2D Interativo para Desenho Livre de Paredes, Linhas Auxiliares 90°, Portas, Janelas e Ambientes.
 */
@Composable
private fun FreePlanInteractiveCanvas(
    walls: List<FreePlanWall>,
    auxLines: List<AuxLine90>,
    rooms: List<DetectedRoom>,
    junctions: List<DetectedWallJunction>,
    selectedWallId: String?,
    activeTool: DrawingTool,
    selectedThicknessCm: Double,
    zoomScale: Float,
    panOffsetX: Float,
    panOffsetY: Float,
    dragStartWorld: Pair<Float, Float>?,
    dragCurrentWorld: Pair<Float, Float>?,
    chainVertices: List<Pair<Float, Float>>,
    isSquareRoomQuickDrag: Boolean,
    onSelectWall: (String) -> Unit,
    onSelectRoom: (DetectedRoom) -> Unit,
    onTapWallForOpening: (FreePlanWall, Double) -> Unit,
    onEraseWall: (String) -> Unit,
    onEraseAuxLine: (String) -> Unit,
    onDragStart: (Float, Float) -> Unit,
    onDragMove: (Float, Float) -> Unit,
    onDragEnd: () -> Unit
) {
    var canvasSize by remember { mutableStateOf(Size(1000f, 1000f)) }

    // Escala base: 1 metro = 64 pixels * zoomScale
    val basePxPerMeter = (min(canvasSize.width, canvasSize.height) / 11.5f).coerceAtLeast(36f)
    val pxPerMeter = basePxPerMeter * zoomScale
    val originX = canvasSize.width * 0.08f + panOffsetX
    val originY = canvasSize.height * 0.14f + panOffsetY

    fun worldToScreenX(wx: Float) = originX + wx * pxPerMeter
    fun worldToScreenY(wy: Float) = originY + wy * pxPerMeter
    fun screenToWorldX(sx: Float) = (sx - originX) / pxPerMeter
    fun screenToWorldY(sy: Float) = (sy - originY) / pxPerMeter

    val currentWalls by rememberUpdatedState(walls)
    val currentAuxLines by rememberUpdatedState(auxLines)
    val currentRooms by rememberUpdatedState(rooms)
    val currentActiveTool by rememberUpdatedState(activeTool)
    val currentPxPerMeter by rememberUpdatedState(pxPerMeter)
    val currentOriginX by rememberUpdatedState(originX)
    val currentOriginY by rememberUpdatedState(originY)
    val currentOnSelectWall by rememberUpdatedState(onSelectWall)
    val currentOnSelectRoom by rememberUpdatedState(onSelectRoom)
    val currentOnTapWallForOpening by rememberUpdatedState(onTapWallForOpening)
    val currentOnEraseWall by rememberUpdatedState(onEraseWall)
    val currentOnEraseAuxLine by rememberUpdatedState(onEraseAuxLine)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragMove by rememberUpdatedState(onDragMove)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    var lastRawDownOffset by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                        val firstChange = event.changes.firstOrNull()
                        if (firstChange != null && firstChange.pressed && !firstChange.previousPressed) {
                            lastRawDownOffset = firstChange.position
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val wx = (tapOffset.x - currentOriginX) / currentPxPerMeter
                    val wy = (tapOffset.y - currentOriginY) / currentPxPerMeter

                    val hitWall = findNearestWall(wx, wy, currentWalls, maxDistMeters = 0.45f)

                    when (currentActiveTool) {
                        DrawingTool.ERASER -> {
                            if (hitWall != null) {
                                currentOnEraseWall(hitWall.id)
                            } else {
                                val hitAux = currentAuxLines.firstOrNull { line ->
                                    distancePointToSegment(wx, wy, line.startX, line.startY, line.endX, line.endY) < 0.35f
                                }
                                if (hitAux != null) currentOnEraseAuxLine(hitAux.id)
                            }
                        }

                        DrawingTool.DOOR, DrawingTool.WINDOW -> {
                            if (hitWall != null) {
                                val minWX = min(hitWall.startX, hitWall.endX)
                                val minWY = min(hitWall.startY, hitWall.endY)
                                val ratio = if (hitWall.isHorizontal) {
                                    ((wx - minWX) / hitWall.visualLengthMeters.toFloat()).coerceIn(0.05f, 0.85f)
                                } else {
                                    ((wy - minWY) / hitWall.visualLengthMeters.toFloat()).coerceIn(0.05f, 0.85f)
                                }
                                val distCm = (ratio * hitWall.effectiveLengthCm / 10.0).roundToInt() * 10.0
                                currentOnTapWallForOpening(hitWall, distCm)
                            }
                        }

                        else -> {
                            if (hitWall != null) {
                                currentOnSelectWall(hitWall.id)
                            } else {
                                val hitRoom = currentRooms.firstOrNull { r ->
                                    wx in r.minX..r.maxX && wy in r.minY..r.maxY
                                }
                                if (hitRoom != null) {
                                    currentOnSelectRoom(hitRoom)
                                }
                            }
                        }


                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val actualStart = lastRawDownOffset ?: startOffset
                        val wx0 = (actualStart.x - currentOriginX) / currentPxPerMeter
                        val wy0 = (actualStart.y - currentOriginY) / currentPxPerMeter
                        currentOnDragStart(wx0, wy0)
                        val wx1 = (startOffset.x - currentOriginX) / currentPxPerMeter
                        val wy1 = (startOffset.y - currentOriginY) / currentPxPerMeter
                        currentOnDragMove(wx1, wy1)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val wx = (change.position.x - currentOriginX) / currentPxPerMeter
                        val wy = (change.position.y - currentOriginY) / currentPxPerMeter
                        currentOnDragMove(wx, wy)
                    },
                    onDragEnd = { currentOnDragEnd() },
                    onDragCancel = { currentOnDragEnd() }
                )
            }
    ) {
        canvasSize = size

        // 1. Malha Quadriculada CAD (passo de 0,50 m e 1,00 m)
        val stepHalfM = pxPerMeter * 0.5f
        if (stepHalfM >= 10f) {
            var gx = originX % stepHalfM
            while (gx < size.width) {
                drawLine(Color(0xFF161F30), Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 1f)
                gx += stepHalfM
            }
            var gy = originY % stepHalfM
            while (gy < size.height) {
                drawLine(Color(0xFF161F30), Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1f)
                gy += stepHalfM
            }
        }

        // 2. Ambientes Reconhecidos Automaticamente (Regiões Fechadas)
        val roomTitlePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#38BDF8")
            textSize = (11f * zoomScale).coerceIn(18f, 30f)
            textAlign = AndroidPaint.Align.CENTER
            typeface = AndroidTypeface.create(AndroidTypeface.MONOSPACE, AndroidTypeface.BOLD)
        }
        val roomSubPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = (9f * zoomScale).coerceIn(15f, 24f)
            textAlign = AndroidPaint.Align.CENTER
            typeface = AndroidTypeface.create(AndroidTypeface.MONOSPACE, AndroidTypeface.NORMAL)
        }

        for (room in rooms) {
            val left = worldToScreenX(room.minX)
            val top = worldToScreenY(room.minY)
            val right = worldToScreenX(room.maxX)
            val bottom = worldToScreenY(room.maxY)
            drawRect(
                color = Color(0xFF0284C7).copy(alpha = 0.10f),
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top)
            )
            val cx = worldToScreenX(room.centerX)
            val cy = worldToScreenY(room.centerY)
            val conciseRoomTitle = room.customName ?: room.suggestedType
            drawIntoCanvas { c ->
                c.nativeCanvas.drawText(conciseRoomTitle, cx, cy - 14f, roomTitlePaint)
                c.nativeCanvas.drawText("${room.areaM2} m²", cx, cy + 8f, roomSubPaint)
            }
        }

        // 3. Linhas Auxiliares Exclusivamente em 90° (Tracejadas Ciano)
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
        for (aux in auxLines) {
            drawLine(
                color = Color(0xFF22D3EE),
                start = Offset(worldToScreenX(aux.startX), worldToScreenY(aux.startY)),
                end = Offset(worldToScreenX(aux.endX), worldToScreenY(aux.endY)),
                strokeWidth = 2.5f,
                pathEffect = dashEffect
            )
        }

        // 4. Paredes Desenhadas (com espessura proporcional, portas, janelas e cotas)
        val labelPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 21f
            textAlign = AndroidPaint.Align.CENTER
            typeface = AndroidTypeface.create(AndroidTypeface.MONOSPACE, AndroidTypeface.BOLD)
        }
        val cotaPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            textSize = 17f
            textAlign = AndroidPaint.Align.CENTER
            typeface = AndroidTypeface.create(AndroidTypeface.MONOSPACE, AndroidTypeface.BOLD)
        }

        for (w in walls) {
            val isSelected = w.id == selectedWallId
            val x1 = worldToScreenX(w.startX)
            val y1 = worldToScreenY(w.startY)
            val x2 = worldToScreenX(w.endX)
            val y2 = worldToScreenY(w.endY)

            val wallStrokePx = ((w.thicknessCm / 100.0) * pxPerMeter * 1.35).toFloat().coerceIn(8f, 28f)
            val wallColor = when {
                isSelected -> Color(0xFF38BDF8)
                w.isFixedLocked -> Color(0xFFF59E0B)
                w.isExternal -> Color(0xFFE2E8F0)
                else -> Color(0xFF94A3B8)
            }

            // Corpo da parede
            drawLine(
                color = wallColor,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = wallStrokePx,
                cap = StrokeCap.Square
            )

            // Aberturas (Portas com arco de giro e Janelas com peitoril)
            val totalLenCm = w.effectiveLengthCm.coerceAtLeast(1.0)
            for (op in w.openings) {
                val r1 = (op.distanceFromStartCm / totalLenCm).toFloat().coerceIn(0f, 1f)
                val r2 = ((op.distanceFromStartCm + op.widthCm) / totalLenCm).toFloat().coerceIn(0f, 1f)
                val ox1 = x1 + (x2 - x1) * r1
                val oy1 = y1 + (y2 - y1) * r1
                val ox2 = x1 + (x2 - x1) * r2
                val oy2 = y1 + (y2 - y1) * r2

                drawLine(
                    color = Color(0xFF0B0F17),
                    start = Offset(ox1, oy1),
                    end = Offset(ox2, oy2),
                    strokeWidth = wallStrokePx + 3f
                )

                if (op.type == OpeningType.DOOR) {
                    drawLine(
                        color = Color(0xFFFB923C),
                        start = Offset(ox1, oy1),
                        end = Offset(ox2, oy2),
                        strokeWidth = 4.5f
                    )
                    val spanPx = hypot((ox2 - ox1).toDouble(), (oy2 - oy1).toDouble()).toFloat().coerceAtLeast(12f)
                    if (w.isHorizontal) {
                        drawLine(Color(0xFFFB923C), Offset(ox1, oy1), Offset(ox1, oy1 + spanPx * 0.8f), strokeWidth = 3f)
                    } else {
                        drawLine(Color(0xFFFB923C), Offset(ox1, oy1), Offset(ox1 + spanPx * 0.8f, oy1), strokeWidth = 3f)
                    }
                } else {
                    drawLine(
                        color = Color(0xFF38BDF8),
                        start = Offset(ox1, oy1),
                        end = Offset(ox2, oy2),
                        strokeWidth = 5f
                    )
                }
            }

            // Medalhão da letra da Parede (A, B, C...) e Cota
            var mx = (x1 + x2) / 2f
            var my = (y1 + y2) / 2f
            val hasMidJunction = junctions.any { j ->
                hypot(worldToScreenX(j.x) - mx, worldToScreenY(j.y) - my) < 24f
            }
            if (hasMidJunction) {
                if (w.isHorizontal) mx -= 36f else my -= 36f
            }

            drawCircle(
                color = if (isSelected) PrimaryBlue else Color(0xFF1E293B),
                radius = 16f,
                center = Offset(mx, my)
            )
            drawCircle(
                color = Color.White,
                radius = 16f,
                center = Offset(mx, my),
                style = Stroke(width = 2f)
            )

            val amarraInfo = FreePlanEngine.getAmarracaoBlockInfo(w.thicknessCm)
            val cotaText = if (w.isCotada) {
                String.format(Locale.US, "%.2fm (%scm)", w.effectiveLengthCm / 100.0, FreePlanEngine.formatClean(w.thicknessCm)) + (if (w.isFixedLocked) " 🔒" else "")
            } else {
                "~${String.format(Locale.US, "%.2fm (%scm)", w.visualLengthMeters, FreePlanEngine.formatClean(w.thicknessCm))}"
            }
            cotaPaint.color = if (w.isCotada) {
                android.graphics.Color.parseColor("#FF2D78")
            } else {
                android.graphics.Color.parseColor("#38BDF8")
            }

            drawIntoCanvas { c ->
                c.nativeCanvas.drawText(w.label, mx, my + 7f, labelPaint)
                if (w.isHorizontal) {
                    c.nativeCanvas.drawText(cotaText, mx, my - 24f, cotaPaint)
                } else {
                    c.nativeCanvas.drawText(cotaText, mx + 56f, my + 5f, cotaPaint)
                }
            }
        }

        // 5. Símbolos de Encontros Detectados (L, T, Cruz +) com Bloco de Amarração Explícito (34, 31.5, 29 cm)
        val juncPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 14f
            textAlign = AndroidPaint.Align.CENTER
            typeface = AndroidTypeface.create(AndroidTypeface.MONOSPACE, AndroidTypeface.BOLD)
        }
        for (junc in junctions) {
            val jx = worldToScreenX(junc.x)
            val jy = worldToScreenY(junc.y)
            val refWall = walls.firstOrNull { it.id in junc.connectedWallIds }
            val tieLenStr = if (refWall != null) {
                FreePlanEngine.formatClean(FreePlanEngine.getAmarracaoBlockInfo(refWall.thicknessCm).tieLengthCm)
            } else {
                FreePlanEngine.formatClean(FreePlanEngine.getAmarracaoBlockInfo(selectedThicknessCm).tieLengthCm)
            }
            val badgeColor = when (junc.kind) {
                JunctionKind.CORNER_L -> Color(0xFFFDE047)
                JunctionKind.JUNCTION_T -> Color(0xFFF472B6)
                JunctionKind.CROSS_X -> Color(0xFF34D399)
            }
            drawRoundRect(
                color = badgeColor,
                topLeft = Offset(jx - 26f, jy - 11f),
                size = Size(52f, 22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(jx - 26f, jy - 11f),
                size = Size(52f, 22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                style = Stroke(width = 1.5f)
            )
            drawIntoCanvas { c ->
                c.nativeCanvas.drawText("${junc.kind.code}:$tieLenStr", jx, jy + 5f, juncPaint)
            }
        }

        // 6. Preview em Tempo Real durante o arraste do dedo (Parede Contínua, Quadrado 1-Toque ou Linha 90°)
        if (dragStartWorld != null && dragCurrentWorld != null) {
            val sx = worldToScreenX(dragStartWorld.first)
            val sy = worldToScreenY(dragStartWorld.second)
            val ex = worldToScreenX(dragCurrentWorld.first)
            val ey = worldToScreenY(dragCurrentWorld.second)

            if (activeTool == DrawingTool.WALL && isSquareRoomQuickDrag) {
                val left = min(sx, ex)
                val top = min(sy, ey)
                val right = max(sx, ex)
                val bottom = max(sy, ey)
                val previewStroke = ((selectedThicknessCm / 100.0) * pxPerMeter * 1.35).toFloat().coerceIn(8f, 26f)
                drawRect(
                    color = Color(0xFF34D399).copy(alpha = 0.15f),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top)
                )
                drawRect(
                    color = Color(0xFF34D399),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = previewStroke)
                )
                val wMeters = abs(dragCurrentWorld.first - dragStartWorld.first)
                val hMeters = abs(dragCurrentWorld.second - dragStartWorld.second)
                val hudPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#FDE047")
                    textSize = 22f
                    textAlign = AndroidPaint.Align.CENTER
                    typeface = AndroidTypeface.create(AndroidTypeface.MONOSPACE, AndroidTypeface.BOLD)
                }
                drawIntoCanvas { c ->
                    c.nativeCanvas.drawText(
                        String.format(Locale.US, "QUADRADO/CÔMODO: %.2f m × %.2f m", wMeters, hMeters),
                        (left + right) / 2f,
                        top - 16f,
                        hudPaint
                    )
                }
            } else if (activeTool == DrawingTool.WALL) {
                val previewStroke = ((selectedThicknessCm / 100.0) * pxPerMeter * 1.35).toFloat().coerceIn(8f, 26f)
                drawLine(
                    color = Color(0xFF38BDF8).copy(alpha = 0.85f),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = previewStroke,
                    cap = StrokeCap.Square
                )
                // Anéis magnéticos de colagem de canto
                for (cv in chainVertices) {
                    val cvx = worldToScreenX(cv.first)
                    val cvy = worldToScreenY(cv.second)
                    drawCircle(Color(0xFFFDE047), radius = 13f, center = Offset(cvx, cvy), style = Stroke(width = 3f))
                }
                drawCircle(Color(0xFFFDE047), radius = 10f, center = Offset(sx, sy))
                drawCircle(Color(0xFF10B981), radius = 12f, center = Offset(ex, ey))

                val segMeters = hypot((dragCurrentWorld.first - dragStartWorld.first).toDouble(), (dragCurrentWorld.second - dragStartWorld.second).toDouble())
                val hudPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#FDE047")
                    textSize = 21f
                    textAlign = AndroidPaint.Align.CENTER
                    typeface = AndroidTypeface.create(AndroidTypeface.MONOSPACE, AndroidTypeface.BOLD)
                }
                drawIntoCanvas { c ->
                    c.nativeCanvas.drawText(
                        String.format(Locale.US, "%.2f m (Vire 90° s/ soltar p/ próx. parede)", segMeters),
                        (sx + ex) / 2f,
                        min(sy, ey) - 22f,
                        hudPaint
                    )
                }
            } else if (activeTool == DrawingTool.AUX_LINE_90) {
                drawLine(
                    color = Color(0xFF22D3EE),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = 3f,
                    pathEffect = dashEffect
                )
            }
        }
    }
}

private fun findNearestWall(
    wx: Float,
    wy: Float,
    walls: List<FreePlanWall>,
    maxDistMeters: Float
): FreePlanWall? {
    var best: FreePlanWall? = null
    var minDist = maxDistMeters
    for (w in walls) {
        val d = distancePointToSegment(wx, wy, w.startX, w.startY, w.endX, w.endY)
        if (d < minDist) {
            minDist = d
            best = w
        }
    }
    return best
}

private fun distancePointToSegment(
    px: Float,
    py: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float
): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    val lenSq = dx * dx + dy * dy
    if (lenSq < 0.0001f) return hypot(px - x1, py - y1)
    val t = (((px - x1) * dx + (py - y1) * dy) / lenSq).coerceIn(0f, 1f)
    val projX = x1 + t * dx
    val projY = y1 + t * dy
    return hypot(px - projX, py - projY)
}

/**
 * 6. MÓDULO DE COTAGEM CONTEXTUAL: Campo ao lado/abaixo da parede selecionada para informar
 * a medida real (ex: Comprimento da parede: [ 3,20 m ]), recalcular conexões e paginar.
 */
@Composable
private fun WallDimensionCotagemCard(
    wall: FreePlanWall,
    allWalls: List<FreePlanWall>,
    catalog: List<CatalogBlockPiece>,
    jointConfig: com.paginacaopro.app.model.JointConfig,
    bondType: com.paginacaopro.app.model.BondType,
    modifier: Modifier = Modifier,
    onApplyDimension: (Double, Double, Double, Boolean, Boolean, DimensionAdjustRule) -> Unit,
    onApplyAllThickness: ((Double) -> Unit)? = null,
    onOpenAddDoor: () -> Unit,
    onOpenAddWindow: () -> Unit,
    onRemoveOpening: (Long) -> Unit,
    onOpenWallElevation: () -> Unit,
    onExpandWallSvgFullscreen: (com.paginacaopro.app.model.TilingResult) -> Unit,
    onHideCard: () -> Unit = {}
) {
    var lengthMetersText by remember(wall.id, wall.effectiveLengthCm) {
        mutableStateOf(String.format(Locale.US, "%.2f", wall.effectiveLengthCm / 100.0))
    }
    var heightMetersText by remember(wall.id, wall.heightCm) {
        mutableStateOf(String.format(Locale.US, "%.2f", wall.heightCm / 100.0))
    }
    var selectedThick by remember(wall.id, wall.thicknessCm) {
        mutableStateOf(NominalWallThickness.fromCm(wall.thicknessCm))
    }
    var isLocked by remember(wall.id, wall.isFixedLocked) {
        mutableStateOf(wall.isFixedLocked)
    }
    var includeCinta by remember(wall.id, wall.includeTopCintaCanaleta) {
        mutableStateOf(wall.includeTopCintaCanaleta)
    }
    var adjustRule by remember { mutableStateOf(DimensionAdjustRule.AUTO_ADJUST_ROOM) }
    var isDetailsExpanded by remember { mutableStateOf(false) }

    val paginationPreview = remember(wall, allWalls, catalog, jointConfig, bondType) {
        FreePlanEngine.paginateFreePlanWall(wall, allWalls, catalog, jointConfig, bondType)
    }
    val closing = paginationPreview.closingAnalysis
    val amarraSpec = remember(wall.thicknessCm) {
        FreePlanEngine.getAmarracaoBlockInfo(wall.thicknessCm)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                .padding(10.dp)
        ) {
            // Cabeçalho: Nome da Parede + Bloco de Amarração Explícito (34/31,5/29) + Botões SVG
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(wall.label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${wall.displayName} • Esp. ${FreePlanEngine.formatClean(wall.thicknessCm)} cm",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Canto L / Saída T: Bloco ${amarraSpec.dimensionsLabel} (${amarraSpec.formulaExplanation}) • Arraste este painel",
                            color = Color(0xFFFDE047),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        onClick = onHideCard,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.border(1.dp, Color(0xFFFDE047), RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = " — ",
                            color = Color(0xFFFDE047),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    IconButton(
                        onClick = { isDetailsExpanded = !isDetailsExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isDetailsExpanded) Icons.Default.ExpandLess else Icons.Default.Tune,
                            contentDescription = "Mais opções da parede",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { onExpandWallSvgFullscreen(paginationPreview.tilingResult) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SVG TELA CHEIA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mini SVG Clicável da Parede Selecionada (Toque para expandir em Tela Cheia Vertical + Zoom 20x + Legenda)
            CodedWallElevationPreview(
                pagination = paginationPreview,
                onClickExpand = { onExpandWallSvgFullscreen(paginationPreview.tilingResult) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .background(Color(0xFF090D14), RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Linha Principal de Cotagem Rápida: Comprimento da parede: [ 3,20 m ] + Confirmar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = lengthMetersText,
                    onValueChange = { lengthMetersText = it },
                    label = { Text("Comprimento (m)", fontSize = 10.sp, color = Color(0xFF94A3B8)) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = heightMetersText,
                    onValueChange = { heightMetersText = it },
                    label = { Text("Altura (m)", fontSize = 10.sp, color = Color(0xFF94A3B8)) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(0.85f)
                )

                IconButton(
                    onClick = { isLocked = !isLocked },
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isLocked) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF1E293B),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Fixar parede",
                        tint = if (isLocked) Color(0xFFFDE047) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = {
                        val lenM = lengthMetersText.replace(',', '.').toDoubleOrNull() ?: 3.20
                        val hM = heightMetersText.replace(',', '.').toDoubleOrNull() ?: 2.80
                        val modLenCm = com.paginacaopro.app.engine.TilingEngine.snapToModularWallLength(lenM * 100.0)
                        val modHCm = com.paginacaopro.app.engine.TilingEngine.snapToModularWallHeight(hM * 100.0)
                        lengthMetersText = String.format(java.util.Locale.US, "%.2f", modLenCm / 100.0)
                        heightMetersText = String.format(java.util.Locale.US, "%.2f", modHCm / 100.0)
                        onApplyDimension(
                            modLenCm,
                            modHCm,
                            selectedThick.thicknessCm,
                            isLocked,
                            includeCinta,
                            adjustRule
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(44.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Cotar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Indicador de modulação ABNT 100% perfeita
            val currentRawLenM = lengthMetersText.replace(',', '.').toDoubleOrNull() ?: 3.20
            val currentModLenCm = com.paginacaopro.app.engine.TilingEngine.snapToModularWallLength(currentRawLenM * 100.0)
            val wasAdjusted = kotlin.math.abs(currentModLenCm - (currentRawLenM * 100.0)) > 0.05
            if (wasAdjusted) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "⚡ Modulação ABNT: ${currentModLenCm.roundToInt()} cm (${String.format(java.util.Locale.US, "%.2f", currentModLenCm/100.0)} m) fecha 100% sem sobras!",
                    color = Color(0xFFFDE047),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Resumo instantâneo dos blocos e Folga/Corte/Desperdício
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${closing.statusTitle}  |  Folga: ${closing.closingGapCm}cm • Corte: ${closing.adjustmentCutCm}cm • Desp.: ${closing.estimatedWasteCm}cm",
                color = Color(0xFFCBD5E1),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Opções Expandidas (Regra de Conexão, Espessura Individual, Portas e Janelas)
            AnimatedVisibility(visible = isDetailsExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Divider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Espessura desta parede:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NominalWallThickness.entries.forEach { t ->
                            FilterChip(
                                selected = selectedThick == t,
                                onClick = {
                                    selectedThick = t
                                    val lenM = lengthMetersText.replace(',', '.').toDoubleOrNull() ?: ((wall.realLengthCm ?: wall.effectiveLengthCm) / 100.0)
                                    val hM = heightMetersText.replace(',', '.').toDoubleOrNull() ?: (wall.heightCm / 100.0)
                                    val modLenCm = com.paginacaopro.app.engine.TilingEngine.snapToModularWallLength(lenM * 100.0)
                                    val modHCm = com.paginacaopro.app.engine.TilingEngine.snapToModularWallHeight(hM * 100.0)
                                    onApplyDimension(
                                        modLenCm,
                                        modHCm,
                                        t.thicknessCm,
                                        isLocked,
                                        includeCinta,
                                        adjustRule
                                    )
                                },
                                label = { Text(t.label, fontSize = 10.sp) }
                            )
                        }
                        FilterChip(
                            selected = includeCinta,
                            onClick = { includeCinta = !includeCinta },
                            label = { Text("Canaleta na Cinta", fontSize = 10.sp) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        TextButton(
                            onClick = { onApplyAllThickness?.invoke(selectedThick.thicknessCm) },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 0.dp, horizontal = 2.dp)
                        ) {
                            Text("⚡ Aplicar ${selectedThick.label} a TODAS as paredes da planta", fontSize = 10.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("Comportamento das paredes conectadas ao alterar a medida:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DimensionAdjustRule.entries.forEach { r ->
                            FilterChip(
                                selected = adjustRule == r,
                                onClick = { adjustRule = r },
                                label = { Text(r.label, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aberturas na parede (${wall.openings.size}):",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = onOpenAddDoor,
                                modifier = Modifier.height(28.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("+ Porta", fontSize = 10.sp, color = Color(0xFFFB923C))
                            }
                            OutlinedButton(
                                onClick = onOpenAddWindow,
                                modifier = Modifier.height(28.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("+ Janela", fontSize = 10.sp, color = Color(0xFF38BDF8))
                            }
                        }
                    }

                    wall.openings.forEach { op ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${op.label}: ${op.widthCm.roundToInt()}×${op.heightCm.roundToInt()} cm (Peitoril ${op.sillHeightCm.roundToInt()} cm) a ${op.distanceFromStartCm.roundToInt()} cm do ${op.referenceEnd}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onRemoveOpening(op.id) }, modifier = Modifier.size(22.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover vão", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. DIÁLOGO DE CADASTRO E SELEÇÃO DE BLOCOS E CANALETAS POR ESPESSURA (14, 11.5, 9, 19 cm)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockCatalogSelectionDialog(
    catalog: List<CatalogBlockPiece>,
    initialThickness: NominalWallThickness,
    verticalJointCm: Double,
    onTogglePiece: (String) -> Unit,
    onAddCustomPiece: (CatalogBlockPiece) -> Unit,
    onDismiss: () -> Unit
) {
    var filterThick by remember { mutableStateOf(initialThickness) }
    var showNewPieceForm by remember { mutableStateOf(false) }
    var customCode by remember { mutableStateOf("B10") }
    var customLen by remember { mutableStateOf("24") }
    var customHeight by remember { mutableStateOf("19") }
    var isCustomCanaleta by remember { mutableStateOf(false) }

    val filteredPieces = catalog.filter { abs(it.thicknessCm - filterThick.thicknessCm) < 0.6 }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cadastro e Seleção de Blocos da Obra",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selecione os modelos disponíveis para paginação combinatória",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Abas de Família por Espessura (14 cm, 11,5 cm, 9 cm, 19 cm)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NominalWallThickness.entries.forEach { thick ->
                        FilterChip(
                            selected = filterThick == thick,
                            onClick = { filterThick = thick },
                            label = { Text("Blocos ${thick.label}", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredPieces.forEach { piece ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (piece.isAvailableAtSite) Color(0xFF1E293B) else Color(0xFF111827),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTogglePiece(piece.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = piece.isAvailableAtSite,
                                    onCheckedChange = { onTogglePiece(piece.id) }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = if (piece.isCanaleta) Color(0xFFFDE047) else PrimaryBlue,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = piece.code,
                                                color = if (piece.isCanaleta) Slate900 else Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = piece.name,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Real: ${piece.dimensionsFormatted()} • Módulo c/ junta (${FreePlanEngine.formatClean(verticalJointCm)}cm): ${FreePlanEngine.formatClean(piece.modularLengthCm(verticalJointCm))}×${FreePlanEngine.formatClean(piece.modularHeightCm(1.0))} cm • ${piece.category.label}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }


                    }

                    // Formulário rápido para cadastrar nova peça sem duplicata
                    AnimatedVisibility(visible = showNewPieceForm) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Cadastrar Nova Peça (${filterThick.label})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = customCode,
                                        onValueChange = { customCode = it },
                                        label = { Text("Código", fontSize = 10.sp) },
                                        modifier = Modifier.weight(0.8f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = customLen,
                                        onValueChange = { customLen = it },
                                        label = { Text("Comp. (cm)", fontSize = 10.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = customHeight,
                                        onValueChange = { customHeight = it },
                                        label = { Text("Altura (cm)", fontSize = 10.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                                ) {
                                    FilterChip(
                                        selected = isCustomCanaleta,
                                        onClick = { isCustomCanaleta = !isCustomCanaleta },
                                        label = { Text("É Canaleta (C0x)", fontSize = 11.sp) }
                                    )
                                    Button(onClick = {
                                        val l = customLen.replace(',', '.').toDoubleOrNull() ?: 24.0
                                        val h = customHeight.replace(',', '.').toDoubleOrNull() ?: 19.0
                                        onAddCustomPiece(
                                            CatalogBlockPiece(
                                                code = customCode.trim().ifBlank { "B99" },
                                                name = "${if (isCustomCanaleta) "Canaleta" else "Bloco"} ${FreePlanEngine.formatClean(l)}×${FreePlanEngine.formatClean(h)}×${filterThick.label}",
                                                familyName = "Família Modular ${filterThick.label}",
                                                lengthCm = l,
                                                heightCm = h,
                                                thicknessCm = filterThick.thicknessCm,
                                                category = if (isCustomCanaleta) BlockPieceCategory.CANALETA else BlockPieceCategory.COMPENSATOR,
                                                isAvailableAtSite = true
                                            )
                                        )
                                        showNewPieceForm = false
                                    }) {
                                        Text("Adicionar ao Lote", fontSize = 11.sp)
                                    }
                                }
                            }
                        }


                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { showNewPieceForm = !showNewPieceForm }) {
                        Text(if (showNewPieceForm) "Ocultar Cadastro" else "+ Cadastrar Peça", fontSize = 11.sp, color = Color.White)
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Concluir Seleção", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 7. MÓDULO DE PORTAS E JANELAS: Diálogo para posicionar abertura na parede.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFreePlanOpeningDialog(
    wall: FreePlanWall,
    initialType: OpeningType,
    suggestedDistCm: Double,
    onConfirm: (FreePlanOpening) -> Unit,
    onDismiss: () -> Unit
) {
    var openingType by remember { mutableStateOf(initialType) }
    var labelText by remember(openingType) {
        mutableStateOf(
            if (openingType == OpeningType.DOOR) "P${wall.openings.count { it.type == OpeningType.DOOR } + 1}"
            else "J${wall.openings.count { it.type == OpeningType.WINDOW } + 1}"
        )
    }
    var widthCmText by remember(openingType) {
        mutableStateOf(if (openingType == OpeningType.DOOR) "80" else "120")
    }
    var heightCmText by remember(openingType) {
        mutableStateOf(if (openingType == OpeningType.DOOR) "220" else "100")
    }
    var sillCmText by remember(openingType) {
        mutableStateOf(if (openingType == OpeningType.DOOR) "0" else "110")
    }
    var distCmText by remember { mutableStateOf(suggestedDistCm.roundToInt().toString()) }
    var referenceEnd by remember { mutableStateOf("Início") }
    var doorStyle by remember { mutableStateOf(DoorStyle.SINGLE_SWING) }
    var useCanaletaLintel by remember { mutableStateOf(true) }
    var useCanaletaSill by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Inserir ${if (openingType == OpeningType.DOOR) "Porta" else "Janela"} na ${wall.displayName}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = openingType == OpeningType.DOOR,
                        onClick = {
                            openingType = OpeningType.DOOR
                            labelText = "P${wall.openings.count { it.type == OpeningType.DOOR } + 1}"
                            widthCmText = "80"
                            heightCmText = "220"
                            sillCmText = "0"
                        },
                        label = { Text("Porta") }
                    )
                    FilterChip(
                        selected = openingType == OpeningType.WINDOW,
                        onClick = {
                            openingType = OpeningType.WINDOW
                            labelText = "J${wall.openings.count { it.type == OpeningType.WINDOW } + 1}"
                            widthCmText = "120"
                            heightCmText = "100"
                            sillCmText = "110"
                        },
                        label = { Text("Janela") }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = labelText,
                        onValueChange = { labelText = it },
                        label = { Text("Código (P1/J1)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = widthCmText,
                        onValueChange = { widthCmText = it },
                        label = { Text("Largura (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = heightCmText,
                        onValueChange = { heightCmText = it },
                        label = { Text("Altura (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    if (openingType == OpeningType.WINDOW) {
                        OutlinedTextField(
                            value = sillCmText,
                            onValueChange = { sillCmText = it },
                            label = { Text("Peitoril (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = distCmText,
                        onValueChange = { distCmText = it },
                        label = { Text("Dist. da extremidade (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    FilterChip(
                        selected = referenceEnd == "Início",
                        onClick = { referenceEnd = if (referenceEnd == "Início") "Fim" else "Início" },
                        label = { Text("Ref: $referenceEnd", fontSize = 11.sp) }
                    )
                }

                if (openingType == OpeningType.DOOR) {
                    Text("Tipo de Porta:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DoorStyle.entries.forEach { style ->
                            FilterChip(
                                selected = doorStyle == style,
                                onClick = { doorStyle = style },
                                label = { Text(style.label, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = useCanaletaLintel,
                        onClick = { useCanaletaLintel = !useCanaletaLintel },
                        label = { Text("Verga c/ Canaleta", fontSize = 10.sp) }
                    )
                    if (openingType == OpeningType.WINDOW) {
                        FilterChip(
                            selected = useCanaletaSill,
                            onClick = { useCanaletaSill = !useCanaletaSill },
                            label = { Text("Contraverga Canaleta", fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val wCm = widthCmText.replace(',', '.').toDoubleOrNull() ?: 80.0
                val hCm = heightCmText.replace(',', '.').toDoubleOrNull() ?: (if (openingType == OpeningType.DOOR) 220.0 else 100.0)
                val sCm = if (openingType == OpeningType.DOOR) 0.0 else (sillCmText.replace(',', '.').toDoubleOrNull() ?: 110.0)
                val rawDist = distCmText.replace(',', '.').toDoubleOrNull() ?: 100.0
                val startDist = if (referenceEnd == "Início") {
                    rawDist.coerceIn(0.0, (wall.effectiveLengthCm - wCm).coerceAtLeast(0.0))
                } else {
                    (wall.effectiveLengthCm - rawDist - wCm).coerceIn(0.0, (wall.effectiveLengthCm - wCm).coerceAtLeast(0.0))
                }

                onConfirm(
                    FreePlanOpening(
                        type = openingType,
                        label = labelText.ifBlank { "V1" },
                        widthCm = wCm,
                        heightCm = hCm,
                        sillHeightCm = sCm,
                        distanceFromStartCm = startDist,
                        referenceEnd = referenceEnd,
                        doorStyle = doorStyle,
                        useCanaletaLintel = useCanaletaLintel,
                        useCanaletaSill = useCanaletaSill
                    )
                )
            }) {
                Text("Inserir na Parede")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * 8. MODAL EXECUTIVO DE PAGINAÇÃO, NUMERAÇÃO B01..C01, REGRA <= 1 CM E EXPORTAÇÃO SVG / PDF
 */
@Composable
private fun ExecutivePaginationReportDialog(
    walls: List<FreePlanWall>,
    auxLines: List<AuxLine90>,
    rooms: List<DetectedRoom>,
    junctions: List<DetectedWallJunction>,
    paginations: List<FreePlanWallPagination>,
    onOpenWallInAutoCadViewer: (String) -> Unit,
    onExpandWallSvgFullscreen: (TilingResult) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedWallIndex by remember { mutableStateOf(0) }
    val activePag = paginations.getOrNull(selectedWallIndex)

    // Consolidado global de peças por tamanho
    val globalCodeCounts = remember(paginations) {
        val map = linkedMapOf<String, Int>()
        for (p in paginations) {
            for ((code, count) in p.closingAnalysis.pieceCodeSummary) {
                map[code] = (map[code] ?: 0) + count
            }
        }
        map
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Cabeçalho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Relatório de Paginação Automática e Modulação",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toque no SVG p/ Tela Cheia Vertical (Zoom 20x + Legenda Interativa) • Amarração L/T",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Seletor de Paredes A, B, C...
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paginations.forEachIndexed { index, pag ->
                        val isSel = selectedWallIndex == index
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedWallIndex = index },
                            label = {
                                Text(
                                    "Parede ${pag.wall.label} (${String.format(Locale.US, "%.2fm", pag.wall.effectiveLengthCm / 100.0)})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color(0xFF0F172A) else Color(0xFFE2E8F0)
                                )
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activePag != null) {
                        val ca = activePag.closingAnalysis
                        val amarracaoSpec = FreePlanEngine.getAmarracaoBlockInfo(activePag.wall.thicknessCm)

                        // 1. Elevação SVG Clicável (Sem numeração interna nos blocos, cores por tamanho)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "SVG da Parede — ${activePag.wall.displayName}",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Amarração Canto L / Saída T: ${amarracaoSpec.shortLabel} (${amarracaoSpec.formulaText})",
                                            color = Color(0xFFFACC15),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { onExpandWallSvgFullscreen(activePag.tilingResult) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Tela Cheia 20x", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { onOpenWallInAutoCadViewer(activePag.wall.id) },
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("Aba Paginação", fontSize = 10.sp, color = Color(0xFF38BDF8))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(185.dp)
                                        .background(Color(0xFF090D14), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                        .clickable { onExpandWallSvgFullscreen(activePag.tilingResult) }
                                ) {
                                    CodedWallElevationPreview(
                                        pagination = activePag,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        color = Color(0xCC0F172A),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "🔍 Toque p/ Expandir Vertical (Zoom 20x + Legenda)",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }



                        // 2. Diagnóstico Técnico da Regra de Sobra <= 1 cm (Seção 4)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = ca.statusTitle,
                                    color = if (ca.hasExactCommercialFit) Color(0xFF34D399) else Color(0xFFFBBF24),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ca.technicalExplanation,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // 4 Indicadores distintos: Folga, Corte, Desperdício e Junta
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TechnicalMetricBox("Folga Fechamento", "${ca.closingGapCm} cm", Color(0xFF34D399))
                                    TechnicalMetricBox("Corte de Ajuste", "${ca.adjustmentCutCm} cm", Color(0xFFFB923C))
                                    TechnicalMetricBox("Desperdício Est.", "${ca.estimatedWasteCm} cm", Color(0xFFF87171))
                                    TechnicalMetricBox("Junta Argamassa", "${ca.mortarJointCm} cm", Color(0xFF38BDF8))
                                }

                                if (ca.recommendedAlternatives.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ca.recommendedAlternatives.forEach { alt ->
                                        Text(
                                            text = "💡 Alternativa: $alt",
                                            color = Color(0xFFFDE047),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }



                        // 3. Quantitativo de Peças desta Parede vs Geral da Obra
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Quantitativo por Tamanho (Parede ${activePag.wall.label} vs. Total da Planta)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                ca.pieceCodeSummary.forEach { (codeName, qty) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(codeName, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                        Text(
                                            "$qty un (Compra +5%: ${ceil(qty * 1.05).toInt()} un)",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 6.dp))
                                Text(
                                    text = "CONSOLIDADO DE TODAS AS PAREDES (${walls.size} paredes):",
                                    color = Color(0xFFFDE047),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                globalCodeCounts.forEach { (codeName, totalQty) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(codeName, color = Color.White, fontSize = 11.sp)
                                        Text(
                                            "$totalQty un (Compra: ${ceil(totalQty * 1.05).toInt()} un)",
                                            color = Color(0xFF34D399),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }


                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botões de Exportação (SVG Planta Baixa, SVG Parede, PDF Completo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val svg = FreePlanEngine.generateFloorPlanSvg(
                                projectName = "Planta_Baixa_Executiva",
                                walls = walls,
                                auxLines = auxLines,
                                rooms = rooms,
                                junctions = junctions
                            )
                            SvgExportUtil.shareSvgFile(context, svg, "Planta_Baixa_Completa")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SVG PLANTA", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val pdfFile = FreePlanEngine.generateExecutivePdfReport(
                                context = context,
                                projectName = "Planta Livre Executiva",
                                walls = walls,
                                rooms = rooms,
                                junctions = junctions,
                                paginations = paginations
                            )
                            SvgExportUtil.sharePdfFile(context, pdfFile)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXPORTAR PDF & QUANTITATIVOS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TechnicalMetricBox(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        modifier = Modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = Color(0xFF94A3B8), fontSize = 9.sp)
            Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Renderiza a elevação da parede sem numeração interna nos blocos e com cor distinta por tamanho de bloco.
 */
@Composable
private fun CodedWallElevationPreview(
    pagination: FreePlanWallPagination,
    modifier: Modifier = Modifier,
    onClickExpand: (() -> Unit)? = null
) {
    val res = pagination.tilingResult
    val canvasMod = if (onClickExpand != null) {
        modifier.clickable { onClickExpand() }
    } else {
        modifier
    }
    Canvas(modifier = canvasMod) {
        val wallW = res.wall.lengthCm.toFloat().coerceAtLeast(10f)
        val wallH = res.wall.heightCm.toFloat().coerceAtLeast(10f)
        val scale = min(size.width / (wallW + 40f), size.height / (wallH + 30f))
        val originX = (size.width - wallW * scale) / 2f
        val originY = size.height - (size.height - wallH * scale) / 2f

        for (course in res.courses) {
            for (blk in course) {
                val bx = originX + blk.xCm.toFloat() * scale
                val by = originY - (blk.yCm.toFloat() + blk.heightCm.toFloat()) * scale
                val bw = blk.lengthCm.toFloat() * scale
                val bh = blk.heightCm.toFloat() * scale

                val (fillColor, strokeColor) = getBlockSizeColors(blk, isAutoCadMode = true)

                drawRect(color = fillColor, topLeft = Offset(bx, by), size = Size(bw, bh))
                drawRect(color = strokeColor, topLeft = Offset(bx, by), size = Size(bw, bh), style = Stroke(width = 1.2f))
            }
        }
    }
}

