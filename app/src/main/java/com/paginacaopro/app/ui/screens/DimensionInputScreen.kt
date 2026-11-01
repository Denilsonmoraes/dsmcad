package com.paginacaopro.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paginacaopro.app.ui.components.RealisticBlockCard
import com.paginacaopro.app.model.BondType
import com.paginacaopro.app.model.JointRenderStyle
import com.paginacaopro.app.model.OpeningModel
import com.paginacaopro.app.model.OpeningType
import com.paginacaopro.app.ui.theme.PrimaryBlue
import com.paginacaopro.app.ui.theme.SecondaryOrange
import com.paginacaopro.app.ui.theme.Slate800
import com.paginacaopro.app.ui.theme.Slate900
import com.paginacaopro.app.ui.viewmodel.PaginacaoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DimensionInputScreen(
    viewModel: PaginacaoViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToViewer: () -> Unit
) {
    val currentWall by viewModel.currentWall.collectAsState()
    val selectedBlock by viewModel.selectedBlock.collectAsState()
    val blockCatalog by viewModel.blockCatalog.collectAsState()
    val jointConfig by viewModel.jointConfig.collectAsState()
    val bondType by viewModel.bondType.collectAsState()
    val openings by viewModel.openings.collectAsState()

    var projectName by remember(currentWall) { mutableStateOf(currentWall.projectName) }
    var wallName by remember(currentWall) { mutableStateOf(currentWall.wallName) }
    var floor by remember(currentWall) { mutableStateOf(currentWall.floor) }
    var environment by remember(currentWall) { mutableStateOf(currentWall.environment) }

    var lengthText by remember(currentWall) { mutableStateOf(currentWall.lengthCm.toString()) }
    var heightText by remember(currentWall) { mutableStateOf(currentWall.heightCm.toString()) }
    var thicknessText by remember(currentWall) { mutableStateOf(currentWall.thicknessCm.toString()) }
    var wasteText by remember(currentWall) { mutableStateOf(currentWall.wastePercentage.toString()) }

    var jointHorizText by remember(jointConfig) { mutableStateOf(jointConfig.horizontalCm.toString()) }
    var jointVertText by remember(jointConfig) { mutableStateOf(jointConfig.verticalCm.toString()) }

    var showAddOpeningDialog by remember { mutableStateOf(false) }
    var showAddIntersectionDialog by remember { mutableStateOf(false) }
    var blockDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dimensões da Parede", fontWeight = FontWeight.Bold) },
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
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        val len = lengthText.toDoubleOrNull() ?: 400.0
                        val h = heightText.toDoubleOrNull() ?: 280.0
                        val modLen = com.paginacaopro.app.engine.TilingEngine.snapToModularWallLength(len)
                        val modH = com.paginacaopro.app.engine.TilingEngine.snapToModularWallHeight(h)
                        val th = thicknessText.toDoubleOrNull() ?: 14.0
                        val waste = wasteText.toDoubleOrNull() ?: 5.0
                        val jH = jointHorizText.toDoubleOrNull() ?: 1.0
                        val jV = jointVertText.toDoubleOrNull() ?: 1.0

                        viewModel.updateWallDimensions(
                            projectName = projectName,
                            wallName = wallName,
                            floor = floor,
                            environment = environment,
                            lengthCm = modLen,
                            heightCm = modH,
                            thicknessCm = th,
                            wastePercentage = waste
                        )
                        viewModel.updateJoints(jH, jV, jointConfig.renderStyle)
                        onNavigateToViewer()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "CALCULAR E VISUALIZAR PAGINAÇÃO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Seção 1: Identificação
            SectionHeader("1. Identificação da Parede")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Nome da Obra / Edificação") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = wallName,
                        onValueChange = { wallName = it },
                        label = { Text("Nome da Parede / Elevação") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = floor,
                            onValueChange = { floor = it },
                            label = { Text("Pavimento") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = environment,
                            onValueChange = { environment = it },
                            label = { Text("Ambiente") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seção 2: Dimensões da Parede
            SectionHeader("2. Dimensões Reais da Parede")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = lengthText,
                            onValueChange = { lengthText = it },
                            label = { Text("Comprimento (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it },
                            label = { Text("Altura (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = thicknessText,
                            onValueChange = { thicknessText = it },
                            label = { Text("Espessura (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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

            Spacer(modifier = Modifier.height(20.dp))

            // Seção 3: Seleção do Bloco
            SectionHeader("3. Seleção do Bloco do Catálogo")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = blockDropdownExpanded,
                        onExpandedChange = { blockDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${selectedBlock.name} (${selectedBlock.lengthCm}×${selectedBlock.heightCm}×${selectedBlock.thicknessCm} cm)",
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
                                            Text(block.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${block.lengthCm}×${block.heightCm}×${block.thicknessCm} cm • ${block.material}",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectBlock(block)
                                        thicknessText = block.thicknessCm.toString()
                                        blockDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Visualização Realista 3D do Bloco Selecionado
                    RealisticBlockCard(
                        block = selectedBlock,
                        onRegenerateAi = { updated ->
                            viewModel.generateAiBlockImage(updated)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seção 4: Juntas de Argamassa e Amarração
            SectionHeader("4. Juntas e Modo de Amarração")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = jointHorizText,
                            onValueChange = { jointHorizText = it },
                            label = { Text("Junta Horizontal (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = jointVertText,
                            onValueChange = { jointVertText = it },
                            label = { Text("Junta Vertical (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Tipo de Amarração:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    BondType.values().forEach { bond ->
                        FilterChip(
                            selected = bondType == bond,
                            onClick = { viewModel.updateBondType(bond) },
                            label = { Text(bond.displayName) },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seção 5: Vãos e Aberturas (Portas e Janelas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("5. Vãos e Aberturas (Portas/Janelas)")
                OutlinedButton(
                    onClick = { showAddOpeningDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar Vão", fontSize = 12.sp)
                }
            }

            if (openings.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Nenhuma abertura cadastrada (Parede cega). Clique em 'Adicionar Vão' para incluir portas ou janelas.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            } else {
                openings.forEach { op ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (op.type == OpeningType.DOOR) Icons.Default.DoorFront else Icons.Default.Window,
                                contentDescription = null,
                                tint = SecondaryOrange
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(op.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "Largura: ${op.widthCm} cm | Altura: ${op.heightCm} cm\nPosição X: ${op.xCm} cm | Y: ${op.yCm} cm",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            IconButton(onClick = { viewModel.removeOpening(op.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.Red)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seção 6: Saídas em T e Interseções (Paredes Perpendiculares)
            val intersections by viewModel.intersections.collectAsState()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("6. Saídas em T e Paredes Perpendiculares")
                OutlinedButton(
                    onClick = { showAddIntersectionDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar Saída", fontSize = 12.sp)
                }
            }

            if (intersections.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Nenhuma saída em T cadastrada (Parede reta). Clique em 'Adicionar Saída' para configurar encontros de paredes com amarração.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            } else {
                intersections.forEach { inter ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFDF2F8), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("T", fontWeight = FontWeight.Bold, color = Color(0xFFDB2777), fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(inter.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "Posição X: ${inter.xCm} cm (${inter.xCm / 100.0} m) | Espessura: ${inter.thicknessCm} cm",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            IconButton(onClick = { viewModel.removeIntersection(inter.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.Red)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Diálogo para Adicionar Vão
        if (showAddOpeningDialog) {
            AddOpeningDialog(
                onDismiss = { showAddOpeningDialog = false },
                onAddOpening = { newOp ->
                    viewModel.addOpening(newOp)
                    showAddOpeningDialog = false
                }
            )
        }

        // Diálogo para Adicionar Saída em T / Interseção
        if (showAddIntersectionDialog) {
            AddIntersectionDialog(
                wallLengthCm = currentWall.lengthCm,
                defaultThickness = selectedBlock.thicknessCm,
                onDismiss = { showAddIntersectionDialog = false },
                onAdd = { x, thick, name ->
                    viewModel.addIntersection(x, thick, name)
                    showAddIntersectionDialog = false
                }
            )
        }
    }
}

@Composable
private fun AddIntersectionDialog(
    wallLengthCm: Double,
    defaultThickness: Double,
    onDismiss: () -> Unit,
    onAdd: (Double, Double, String) -> Unit
) {
    var nameText by remember { mutableStateOf("Saída em T (Encontro)") }
    var xText by remember { mutableStateOf("200.0") }
    var thicknessText by remember { mutableStateOf(defaultThickness.toString()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Saída em T / Encontro", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Configure o encontro da parede perpendicular (saída). O motor geométrico gerará as fiadas com peças de amarração e marcação no SVG.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nome da Saída / Cômodo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = xText,
                    onValueChange = { xText = it },
                    label = { Text("Posição X na Parede (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Ex: 200.0 para 2 metros") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = thicknessText,
                    onValueChange = { thicknessText = it },
                    label = { Text("Espessura da Saída (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                errorText?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val x = xText.toDoubleOrNull()
                    val th = thicknessText.toDoubleOrNull() ?: defaultThickness
                    if (x == null || x <= 0 || x >= wallLengthCm) {
                        errorText = "Posição X deve estar entre 0 e ${wallLengthCm} cm."
                        return@Button
                    }
                    onAdd(x, th, nameText.ifBlank { "Saída em T" })
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Slate900,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun AddOpeningDialog(
    onDismiss: () -> Unit,
    onAddOpening: (OpeningModel) -> Unit
) {
    var name by remember { mutableStateOf("Porta 1") }
    var selectedType by remember { mutableStateOf(OpeningType.DOOR) }
    var widthText by remember { mutableStateOf("90") }
    var heightText by remember { mutableStateOf("220") }
    var xText by remember { mutableStateOf("100") }
    var yText by remember { mutableStateOf("0") }
    var hasLintel by remember { mutableStateOf(true) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Abertura / Vão", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Identificação do Vão") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    FilterChip(
                        selected = selectedType == OpeningType.DOOR,
                        onClick = {
                            selectedType = OpeningType.DOOR
                            name = "Porta"
                            widthText = "90"
                            heightText = "220"
                            yText = "0"
                        },
                        label = { Text("Porta") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedType == OpeningType.WINDOW,
                        onClick = {
                            selectedType = OpeningType.WINDOW
                            name = "Janela"
                            widthText = "120"
                            heightText = "100"
                            yText = "110"
                        },
                        label = { Text("Janela") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = widthText,
                        onValueChange = { widthText = it },
                        label = { Text("Largura (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Altura (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = xText,
                        onValueChange = { xText = it },
                        label = { Text("Posição X (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = yText,
                        onValueChange = { yText = it },
                        label = { Text("Posição Y (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = hasLintel, onCheckedChange = { hasLintel = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Prever Verga / Canaleta superior", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = widthText.toDoubleOrNull() ?: 90.0
                    val h = heightText.toDoubleOrNull() ?: (if (selectedType == OpeningType.DOOR) 220.0 else 100.0)
                    val x = xText.toDoubleOrNull() ?: 0.0
                    val y = yText.toDoubleOrNull() ?: 0.0

                    onAddOpening(
                        OpeningModel(
                            id = System.currentTimeMillis(),
                            name = name,
                            type = selectedType,
                            widthCm = w,
                            heightCm = h,
                            xCm = x,
                            yCm = y,
                            hasLintel = hasLintel
                        )
                    )
                }
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
