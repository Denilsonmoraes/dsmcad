package com.paginacaopro.app.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paginacaopro.app.engine.TilingEngine
import com.paginacaopro.app.model.BlockModel
import com.paginacaopro.app.model.PieceType
import com.paginacaopro.app.model.TilingResult
import com.paginacaopro.app.model.WallModel
import com.paginacaopro.app.ui.components.RealisticBlockCard
import com.paginacaopro.app.ui.theme.PrimaryBlue
import com.paginacaopro.app.ui.theme.SecondaryOrange
import com.paginacaopro.app.ui.theme.Slate800
import com.paginacaopro.app.ui.theme.Slate900
import com.paginacaopro.app.ui.viewmodel.PaginacaoViewModel
import com.paginacaopro.app.util.SvgExportUtil
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCalibrationScreen(
    viewModel: PaginacaoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToViewer: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val blockCatalog by viewModel.blockCatalog.collectAsState()
    val currentSelectedBlock by viewModel.selectedBlock.collectAsState()
    val jointConfig by viewModel.jointConfig.collectAsState()

    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageName by remember { mutableStateOf("Nenhuma foto selecionada") }

    // Dimensões do Canvas onde a foto é renderizada
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    // 4 Pontos de Delimitação do Vão (em coordenadas relativas normatizadas [0.0..1.0])
    // Inicialização padrão voltada para o vão da janela na foto da obra
    var normP1 by remember { mutableStateOf(Offset(0.60f, 0.24f)) } // Canto Superior Esquerdo
    var normP2 by remember { mutableStateOf(Offset(0.93f, 0.24f)) } // Canto Superior Direito
    var normP3 by remember { mutableStateOf(Offset(0.93f, 0.76f)) } // Canto Inferior Direito
    var normP4 by remember { mutableStateOf(Offset(0.60f, 0.76f)) } // Canto Inferior Esquerdo

    // Ponto ativo durante arraste (0=P1, 1=P2, 2=P3, 3=P4)
    var activeCornerIndex by remember { mutableStateOf<Int?>(null) }

    // Dimensões reais do Vão a preencher (X = Largura, Y = Altura)
    var xLengthText by remember { mutableStateOf("120.0") }
    var yHeightText by remember { mutableStateOf("120.0") }
    var thicknessText by remember { mutableStateOf("14.0") }
    var wasteText by remember { mutableStateOf("5.0") }
    var linkAspectRatio by remember { mutableStateOf(true) }

    var jointHorizText by remember(jointConfig) { mutableStateOf(jointConfig.horizontalCm.toString()) }
    var jointVertText by remember(jointConfig) { mutableStateOf(jointConfig.verticalCm.toString()) }

    var chosenBlock by remember(currentSelectedBlock) { mutableStateOf(currentSelectedBlock) }
    var blockDropdownExpanded by remember { mutableStateOf(false) }

    // Resultado local da paginação calculada para o Vão
    var openingTilingResult by remember { mutableStateOf<TilingResult?>(null) }
    var generatedSvgCode by remember { mutableStateOf<String?>(null) }

    // Launchers para importar foto
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val bmp = loadBitmapFromUri(context, uri)
            if (bmp != null) {
                loadedBitmap = bmp
                imageName = "Foto da Galeria"
                normP1 = Offset(0.25f, 0.25f)
                normP2 = Offset(0.75f, 0.25f)
                normP3 = Offset(0.75f, 0.75f)
                normP4 = Offset(0.25f, 0.75f)
                openingTilingResult = null
                generatedSvgCode = null
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp: Bitmap? ->
        if (bmp != null) {
            loadedBitmap = bmp
            imageName = "Foto da Câmera"
            normP1 = Offset(0.25f, 0.25f)
            normP2 = Offset(0.75f, 0.25f)
            normP3 = Offset(0.75f, 0.75f)
            normP4 = Offset(0.25f, 0.75f)
            openingTilingResult = null
            generatedSvgCode = null
        }
    }

    // Carregar foto de exemplo da obra ao iniciar se ainda não tiver foto
    LaunchedEffect(Unit) {
        if (loadedBitmap == null) {
            val assetBmp = loadBitmapFromAsset(context, "exemplo_vao_obra.png")
            if (assetBmp != null) {
                loadedBitmap = assetBmp
                imageName = "Vão de Janela em Alvenaria (Foto da Obra)"
                normP1 = Offset(0.61f, 0.24f)
                normP2 = Offset(0.93f, 0.24f)
                normP3 = Offset(0.93f, 0.76f)
                normP4 = Offset(0.61f, 0.76f)
            }
        }
    }

    // Cálculo da proporção do Vão demarcado na foto
    val pixelWidthEst = remember(normP1, normP2, normP3, normP4, canvasWidth) {
        val topW = abs(normP2.x - normP1.x) * canvasWidth
        val botW = abs(normP3.x - normP4.x) * canvasWidth
        max(topW, botW)
    }
    val pixelHeightEst = remember(normP1, normP2, normP3, normP4, canvasHeight) {
        val leftH = abs(normP4.y - normP1.y) * canvasHeight
        val rightH = abs(normP3.y - normP2.y) * canvasHeight
        max(leftH, rightH)
    }
    val openingAspect = if (pixelHeightEst > 0f) pixelWidthEst / pixelHeightEst else 1.0f

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Paginação de Vão por Imagem", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Foto → Análise → Formato do Vão → SVG", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Seção 1: Fonte da Fotografia
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("1. Fotografia do Vão / Parede Existente", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        imageName,
                        fontSize = 12.sp,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Câmera", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Galeria", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val assetBmp = loadBitmapFromAsset(context, "exemplo_vao_obra.png")
                                if (assetBmp != null) {
                                    loadedBitmap = assetBmp
                                    imageName = "Vão de Janela em Alvenaria (Foto da Obra)"
                                    normP1 = Offset(0.61f, 0.24f)
                                    normP2 = Offset(0.93f, 0.24f)
                                    normP3 = Offset(0.93f, 0.76f)
                                    normP4 = Offset(0.61f, 0.76f)
                                    openingTilingResult = null
                                    generatedSvgCode = null
                                }
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Exemplo Obra", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Seção 2: Imagem com Demarcação dos 4 Cantos do Vão
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Contorno e Formato do Vão a Preencher",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        IconButton(
                            onClick = {
                                normP1 = Offset(0.61f, 0.24f)
                                normP2 = Offset(0.93f, 0.24f)
                                normP3 = Offset(0.93f, 0.76f)
                                normP4 = Offset(0.61f, 0.76f)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Redefinir", tint = Color(0xFF38BDF8))
                        }
                    }
                    Text(
                        "Arraste os 4 círculos azuis sobre os cantos do vão na imagem.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Área Gráfica Interativa
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .onGloballyPositioned { coordinates ->
                                canvasWidth = coordinates.size.width.toFloat()
                                canvasHeight = coordinates.size.height.toFloat()
                            }
                    ) {
                        // Renderizar Imagem de Fundo se disponível
                        loadedBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Foto do Vão da Obra",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Canvas Interativo com o Polígono do Vão e os 4 Marcadores
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures { tapOffset ->
                                        // Achar o ponto mais próximo do toque
                                        val cW = size.width.toFloat()
                                        val cH = size.height.toFloat()
                                        if (cW > 0 && cH > 0) {
                                            val normTap = Offset(tapOffset.x / cW, tapOffset.y / cH)
                                            val pList = listOf(normP1, normP2, normP3, normP4)
                                            var closestIdx = 0
                                            var minDist = Float.MAX_VALUE
                                            for (i in pList.indices) {
                                                val dist = hypot(pList[i].x - normTap.x, pList[i].y - normTap.y)
                                                if (dist < minDist) {
                                                    minDist = dist
                                                    closestIdx = i
                                                }
                                            }
                                            // Atualiza o canto mais próximo para a posição do toque
                                            when (closestIdx) {
                                                0 -> normP1 = normTap
                                                1 -> normP2 = normTap
                                                2 -> normP3 = normTap
                                                3 -> normP4 = normTap
                                            }
                                        }
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { startOffset ->
                                            val cW = size.width.toFloat()
                                            val cH = size.height.toFloat()
                                            if (cW > 0 && cH > 0) {
                                                val normStart = Offset(startOffset.x / cW, startOffset.y / cH)
                                                val pList = listOf(normP1, normP2, normP3, normP4)
                                                var closestIdx = 0
                                                var minDist = Float.MAX_VALUE
                                                for (i in pList.indices) {
                                                    val dist = hypot(pList[i].x - normStart.x, pList[i].y - normStart.y)
                                                    if (dist < minDist) {
                                                        minDist = dist
                                                        closestIdx = i
                                                    }
                                                }
                                                if (minDist < 0.18f) {
                                                    activeCornerIndex = closestIdx
                                                }
                                            }
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val cW = size.width.toFloat()
                                            val cH = size.height.toFloat()
                                            if (cW > 0 && cH > 0 && activeCornerIndex != null) {
                                                val normPt = Offset(
                                                    x = (change.position.x / cW).coerceIn(0f, 1f),
                                                    y = (change.position.y / cH).coerceIn(0f, 1f)
                                                )
                                                when (activeCornerIndex) {
                                                    0 -> normP1 = normPt
                                                    1 -> normP2 = normPt
                                                    2 -> normP3 = normPt
                                                    3 -> normP4 = normPt
                                                }
                                            }
                                        },
                                        onDragEnd = { activeCornerIndex = null },
                                        onDragCancel = { activeCornerIndex = null }
                                    )
                                }
                        ) {
                            val cW = size.width
                            val cH = size.height

                            val p1 = Offset(normP1.x * cW, normP1.y * cH)
                            val p2 = Offset(normP2.x * cW, normP2.y * cH)
                            val p3 = Offset(normP3.x * cW, normP3.y * cH)
                            val p4 = Offset(normP4.x * cW, normP4.y * cH)

                            // Desenhar preenchimento translúcido do Vão
                            val openingPath = Path().apply {
                                moveTo(p1.x, p1.y)
                                lineTo(p2.x, p2.y)
                                lineTo(p3.x, p3.y)
                                lineTo(p4.x, p4.y)
                                close()
                            }
                            drawPath(
                                path = openingPath,
                                color = Color(0xFF0284C7).copy(alpha = 0.28f),
                                style = Fill
                            )

                            // Linha de contorno tracejada
                            drawPath(
                                path = openingPath,
                                color = Color(0xFF38BDF8),
                                style = Stroke(
                                    width = 3.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                                )
                            )

                            // Desenhar os 4 Marcadores / Pinos de Canto
                            val cornerLabels = listOf("P1: Sup. Esq", "P2: Sup. Dir", "P3: Inf. Dir", "P4: Inf. Esq")
                            val corners = listOf(p1, p2, p3, p4)

                            for (i in corners.indices) {
                                val c = corners[i]
                                val isActive = activeCornerIndex == i
                                drawCircle(
                                    color = if (isActive) SecondaryOrange else PrimaryBlue,
                                    radius = if (isActive) 18f else 14f,
                                    center = c
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 6f,
                                    center = c
                                )
                            }
                        }

                        // Legenda no rodapé da imagem
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Vão na Foto: ~${pixelWidthEst.toInt()} × ${pixelHeightEst.toInt()} px (Proporção: ${String.format("%.2f", openingAspect)})",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seção 3: Entrada das Medidas Reais de X e Y
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "2. Dimensões Reais do Vão a Preencher (X e Y)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Informe a largura e altura reais conhecidas para gerar a modulação exata dos blocos.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = xLengthText,
                            onValueChange = { newVal ->
                                xLengthText = newVal
                                if (linkAspectRatio && openingAspect > 0.05f) {
                                    val xVal = newVal.toDoubleOrNull()
                                    if (xVal != null) {
                                        val yCalc = xVal / openingAspect
                                        yHeightText = String.format(java.util.Locale.US, "%.1f", yCalc)
                                    }
                                }
                            },
                            label = { Text("Largura Real X (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = yHeightText,
                            onValueChange = { newVal ->
                                yHeightText = newVal
                            },
                            label = { Text("Altura Real Y (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Vincular proporção geométrica da foto",
                            fontSize = 12.sp,
                            color = Slate800
                        )
                        Switch(
                            checked = linkAspectRatio,
                            onCheckedChange = { linkAspectRatio = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = thicknessText,
                            onValueChange = { thicknessText = it },
                            label = { Text("Espessura (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = wasteText,
                            onValueChange = { wasteText = it },
                            label = { Text("Perdas (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Seção 4: Seleção do Bloco para Fechamento
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3. Bloco ou Tijolo para Fechamento", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = blockDropdownExpanded,
                        onExpandedChange = { blockDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${chosenBlock.name} (${chosenBlock.lengthCm}×${chosenBlock.heightCm}×${chosenBlock.thicknessCm} cm)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Bloco Selecionado") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = blockDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = blockDropdownExpanded,
                            onDismissRequest = { blockDropdownExpanded = false }
                        ) {
                            blockCatalog.forEach { block ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(block.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(
                                                "${block.material} • ${block.lengthCm}×${block.heightCm}×${block.thicknessCm} cm",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    },
                                    onClick = {
                                        chosenBlock = block
                                        thicknessText = block.thicknessCm.toString()
                                        blockDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = jointHorizText,
                            onValueChange = { jointHorizText = it },
                            label = { Text("Junta Horiz. (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = jointVertText,
                            onValueChange = { jointVertText = it },
                            label = { Text("Junta Vert. (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Botão Principal de Cálculo e Geração do SVG
            Button(
                onClick = {
                    val lenX = xLengthText.toDoubleOrNull() ?: 120.0
                    val hgtY = yHeightText.toDoubleOrNull() ?: 120.0
                    val thk = thicknessText.toDoubleOrNull() ?: chosenBlock.thicknessCm
                    val waste = wasteText.toDoubleOrNull() ?: 5.0
                    val jH = jointHorizText.toDoubleOrNull() ?: 1.0
                    val jV = jointVertText.toDoubleOrNull() ?: 1.0

                    // Configurar o modelo de parede correspondente ao Vão
                    val wall = WallModel(
                        projectName = "Fechamento de Vão por Fotografia",
                        wallName = "Vão ${lenX.toInt()}×${hgtY.toInt()} cm",
                        floor = "Obra",
                        environment = "Abertura Existente",
                        lengthCm = lenX,
                        heightCm = hgtY,
                        thicknessCm = thk,
                        horizontalJointCm = jH,
                        verticalJointCm = jV,
                        wastePercentage = waste
                    )

                    val joints = jointConfig.copy(horizontalCm = jH, verticalCm = jV)
                    val result = TilingEngine.calculate(
                        wall = wall,
                        block = chosenBlock,
                        jointConfig = joints,
                        bondType = com.paginacaopro.app.model.BondType.RUNNING_HALF,
                        openings = emptyList()
                    )

                    openingTilingResult = result

                    // Atualizar ViewModel para persistência e navegação
                    viewModel.selectBlock(chosenBlock)
                    viewModel.updateWallDimensions(
                        projectName = wall.projectName,
                        wallName = wall.wallName,
                        floor = wall.floor,
                        environment = wall.environment,
                        lengthCm = lenX,
                        heightCm = hgtY,
                        thicknessCm = thk,
                        wastePercentage = waste
                    )
                    viewModel.clearOpenings()
                    viewModel.updateJoints(jH, jV, joints.renderStyle)

                    // Gerar código SVG 8K
                    val svg = viewModel.generateSvgOutput(isSimplified = false)
                    generatedSvgCode = svg

                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Paginação do Vão calculada e SVG 8K gerado com sucesso!")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Layers, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GERAR SVG E PAGINAÇÃO DO VÃO", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Seção 5: RESULTADO DA PAGINAÇÃO DO VÃO E EXPORTAÇÃO SVG
            openingTilingResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Paginação Técnica do Vão (${result.wall.lengthCm} × ${result.wall.heightCm} cm)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "SVG Pronto",
                                    color = Color(0xFF15803D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preview Gráfico da Elevação do Vão Paginado
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                val cW = size.width
                                val cH = size.height

                                val wallW = result.wall.lengthCm.toFloat()
                                val wallH = result.wall.heightCm.toFloat()

                                val scaleX = cW / wallW
                                val scaleY = cH / wallH
                                val scale = min(scaleX, scaleY) * 0.92f

                                val renderW = wallW * scale
                                val renderH = wallH * scale

                                val originX = (cW - renderW) / 2f
                                val originY = (cH - renderH) / 2f + renderH // Base da elevação

                                // Desenhar blocos do vão
                                for (blk in result.allBlocks) {
                                    val bX = originX + blk.xCm.toFloat() * scale
                                    val bY = originY - (blk.yCm.toFloat() + blk.heightCm.toFloat()) * scale
                                    val bW = blk.lengthCm.toFloat() * scale
                                    val bH = blk.heightCm.toFloat() * scale

                                    val fillColor = when (blk.pieceType) {
                                        PieceType.CUT -> Color(0xFFFED7AA)
                                        PieceType.HALF -> Color(0xFFBAE6FD)
                                        else -> Color(0xFFF1F5F9)
                                    }
                                    val strokeColor = when (blk.pieceType) {
                                        PieceType.CUT -> Color(0xFFEA580C)
                                        PieceType.HALF -> Color(0xFF0284C7)
                                        else -> Color(0xFF334155)
                                    }

                                    drawRect(fillColor, topLeft = Offset(bX, bY), size = Size(bW, bH))
                                    drawRect(strokeColor, topLeft = Offset(bX, bY), size = Size(bW, bH), style = Stroke(1.2f))
                                }

                                // Contorno do vão com linha técnica escura
                                drawRect(
                                    color = Color(0xFF38BDF8),
                                    topLeft = Offset(originX, originY - renderH),
                                    size = Size(renderW, renderH),
                                    style = Stroke(3f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visualização Realista do Bloco com IA (Abaixo da elevação do Vão)
                        RealisticBlockCard(
                            block = chosenBlock,
                            onRegenerateAi = { updated ->
                                viewModel.generateAiBlockImage(updated)
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quadro de Quantitativos do Vão
                        val bom = result.billOfMaterials
                        val mortarL = (bom.mortarJointVolumeM3Estimated * 1000).toInt()
                        val mortarKg = (mortarL * 1.8).toInt()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "QUANTITATIVOS PARA FECHAR O VÃO",
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            "${bom.recommendedPurchaseBlocks} un",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp
                                        )
                                        Text("Blocos Inteiros (c/ ${bom.wastePercentage}% perda)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "${bom.recommendedPurchaseHalfBlocks} un",
                                            color = Color(0xFFBAE6FD),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp
                                        )
                                        Text("Meios Blocos / Cortes", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Área do Vão: ${bom.grossWallAreaM2} m² • ${result.courses.size} Fiadas • Argamassa: ~$mortarL L ($mortarKg kg)",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Botões de Ação para o SVG
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val svg = generatedSvgCode ?: viewModel.generateSvgOutput(false)
                                    SvgExportUtil.shareSvgFile(
                                        context = context,
                                        svgContent = svg,
                                        fileNamePrefix = "paginacao_fechamento_vao_${result.wall.lengthCm.toInt()}x${result.wall.heightCm.toInt()}"
                                    )
                                },
                                modifier = Modifier.weight(1.3f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("EXPORTAR SVG 8K", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = onNavigateToViewer,
                                modifier = Modifier.weight(1.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.OpenInFull, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("VER COMPLETO", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Utilitários para carregar Bitmap com suporte a Assets e URIs no Android.
 */
private fun loadBitmapFromAsset(context: Context, assetName: String): Bitmap? {
    return try {
        context.assets.open(assetName).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        null
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        }
    } catch (e: Exception) {
        null
    }
}
