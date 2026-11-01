package com.paginacaopro.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.paginacaopro.app.model.BlockModel
import com.paginacaopro.app.ui.components.RealisticBlockCard
import com.paginacaopro.app.ui.theme.PrimaryBlue
import com.paginacaopro.app.ui.theme.SecondaryOrange
import com.paginacaopro.app.ui.theme.Slate900
import com.paginacaopro.app.ui.viewmodel.PaginacaoViewModel
import com.paginacaopro.app.util.BlockRealisticImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: PaginacaoViewModel,
    onNavigateBack: () -> Unit
) {
    val blockCatalog by viewModel.blockCatalog.collectAsState()
    val selectedBlock by viewModel.selectedBlock.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedMaterialFilter by remember { mutableStateOf("Todos") }
    var showAddBlockDialog by remember { mutableStateOf(false) }

    val filteredBlocks = blockCatalog.filter { block ->
        val matchesSearch = block.name.contains(searchQuery, ignoreCase = true) ||
                block.material.contains(searchQuery, ignoreCase = true) ||
                block.manufacturer.contains(searchQuery, ignoreCase = true)

        val matchesMaterial = selectedMaterialFilter == "Todos" ||
                block.material.equals(selectedMaterialFilter, ignoreCase = true)

        matchesSearch && matchesMaterial
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Blocos e Tijolos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.restoreDefaultBlocks() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restaurar Catálogo Padrão", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBlockDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Bloco")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Barra de Busca
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Pesquisar por nome, material ou norma...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtros de Material
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todos", "Baiano", "Concreto", "Cerâmico").forEach { mat ->
                    FilterChip(
                        selected = selectedMaterialFilter == mat,
                        onClick = { selectedMaterialFilter = mat },
                        label = { Text(mat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de Blocos
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredBlocks, key = { it.id }) { block ->
                    val isCurrentSelected = block.id == selectedBlock.id

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentSelected) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Miniatura Realista 3D do Bloco
                                    BlockThumbnailView(
                                        block = block,
                                        modifier = Modifier.size(52.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            block.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isCurrentSelected) PrimaryBlue else Slate900
                                        )
                                        Text(
                                            "${block.material} • ${block.applicationType}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                IconButton(onClick = {
                                    viewModel.saveBlock(block.copy(isFavorite = !block.isFavorite))
                                }) {
                                    Icon(
                                        imageVector = if (block.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Favorito",
                                        tint = if (block.isFavorite) Color(0xFFEAB308) else Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid com medidas nominais separadas
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    DimensionBadge("Comprimento", "${block.lengthCm} cm")
                                    DimensionBadge("Altura", "${block.heightCm} cm")
                                    DimensionBadge("Espessura", "${block.thicknessCm} cm")
                                }
                            }

                            if (block.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(block.notes, fontSize = 11.sp, color = Color(0xFF475569))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.deleteBlock(block) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir Bloco", tint = Color(0xFFEF4444))
                                }

                                Button(
                                    onClick = {
                                        viewModel.selectBlock(block)
                                        onNavigateBack()
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    if (isCurrentSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SELECIONADO", fontSize = 11.sp)
                                    } else {
                                        Text("USAR NESTA PAREDE", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddBlockDialog) {
            AddBlockDialog(
                onDismiss = { showAddBlockDialog = false },
                onSave = { newBlock ->
                    viewModel.saveBlock(newBlock)
                    showAddBlockDialog = false
                }
            )
        }
    }
}

@Composable
private fun DimensionBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color(0xFF64748B))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
    }
}

@Composable
private fun AddBlockDialog(
    onDismiss: () -> Unit,
    onSave: (BlockModel) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("Cerâmico") }
    var lengthText by remember { mutableStateOf("29") }
    var heightText by remember { mutableStateOf("19") }
    var thicknessText by remember { mutableStateOf("14") }
    var halfLengthText by remember { mutableStateOf("14") }
    var applicationType by remember { mutableStateOf("Vedação") }
    var notes by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastrar Novo Bloco Comercial", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Comercial (Ex: Bloco 14×19×29)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    label = { Text("Fabricante / Fornecedor (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Material / Tipo do Bloco:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("Baiano", "Cimento", "Concreto", "Cerâmico", "Ecológico").forEach { mat ->
                        FilterChip(
                            selected = material == mat,
                            onClick = { material = mat },
                            label = { Text(mat) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Campos Separados Obrigatórios: Comprimento, Altura, Espessura
                Text("Dimensões Reais da Peça (cm):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = lengthText,
                        onValueChange = { lengthText = it },
                        label = { Text("Compr. (X)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Alt. (Y)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = thicknessText,
                        onValueChange = { thicknessText = it },
                        label = { Text("Esp. (Z)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = halfLengthText,
                    onValueChange = { halfLengthText = it },
                    label = { Text("Comprimento Meio Bloco (cm - opcional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações Técnicas") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // PREVIEW REALISTA 3D GERADO POR IA
                val previewLength = lengthText.toDoubleOrNull() ?: 29.0
                val previewHeight = heightText.toDoubleOrNull() ?: 19.0
                val previewThickness = thicknessText.toDoubleOrNull() ?: 14.0

                val previewModel = remember(material, previewLength, previewHeight, previewThickness, name) {
                    BlockModel(
                        id = 0,
                        name = name.ifBlank { "$material ${previewLength.toInt()}×${previewHeight.toInt()}×${previewThickness.toInt()} cm" },
                        material = material,
                        lengthCm = previewLength,
                        heightCm = previewHeight,
                        thicknessCm = previewThickness
                    )
                }

                Text("Renderização Realista 3D (IA):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate900)
                Spacer(modifier = Modifier.height(4.dp))
                RealisticBlockCard(
                    block = previewModel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val l = lengthText.toDoubleOrNull() ?: 29.0
                    val h = heightText.toDoubleOrNull() ?: 19.0
                    val t = thicknessText.toDoubleOrNull() ?: 14.0
                    val halfL = halfLengthText.toDoubleOrNull() ?: 0.0

                    onSave(
                        BlockModel(
                            name = name.ifBlank { "Bloco $material ${l.toInt()}×${h.toInt()}×${t.toInt()} cm" },
                            manufacturer = manufacturer,
                            material = material,
                            lengthCm = l,
                            heightCm = h,
                            thicknessCm = t,
                            halfBlockLengthCm = halfL,
                            applicationType = applicationType,
                            notes = notes,
                            isCustom = true
                        )
                    )
                }
            ) {
                Text("SALVAR NO BANCO")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Miniatura do Bloco Realista 3D usada nas listas do catálogo.
 */
@Composable
fun BlockThumbnailView(
    block: BlockModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(block.id, block.imageUri, block.material) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(block.id, block.imageUri, block.material) {
        withContext(Dispatchers.IO) {
            val loaded = BlockRealisticImageGenerator.getOrGenerateBlockBitmap(context, block)
            bitmap = loaded
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = block.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text("3D", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

