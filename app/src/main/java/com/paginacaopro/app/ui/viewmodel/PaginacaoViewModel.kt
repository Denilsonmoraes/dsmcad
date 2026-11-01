package com.paginacaopro.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paginacaopro.app.data.AppDatabase
import com.paginacaopro.app.engine.TilingEngine
import com.paginacaopro.app.model.BlockModel
import com.paginacaopro.app.model.BondType
import com.paginacaopro.app.model.JointConfig
import com.paginacaopro.app.model.JointRenderStyle
import com.paginacaopro.app.model.OpeningModel
import com.paginacaopro.app.model.OpeningType
import com.paginacaopro.app.model.PhotoCalibration
import com.paginacaopro.app.model.TilingResult
import com.paginacaopro.app.model.WallModel
import com.paginacaopro.app.model.FloorPlanProject
import com.paginacaopro.app.model.FloorPlanWall
import com.paginacaopro.app.model.ConsolidatedQuantitativo
import com.paginacaopro.app.model.CommercialPieceCount
import kotlin.math.roundToInt
import com.paginacaopro.app.svg.SvgGenerator
import com.paginacaopro.app.util.BlockRealisticImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaginacaoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val blockDao = db.blockDao()
    private val wallDao = db.wallDao()
    private val openingDao = db.openingDao()

    val blockCatalog: StateFlow<List<BlockModel>> = blockDao.getAllActiveBlocks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedWalls: StateFlow<List<WallModel>> = wallDao.getAllWalls()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Estado da Parede em edição
    private val _currentWall = MutableStateFlow(
        WallModel(
            projectName = "Residência Modelo",
            wallName = "Parede Sala - Elevação 1",
            floor = "Térreo",
            environment = "Sala de Estar",
            lengthCm = 400.0,
            heightCm = 280.0,
            thicknessCm = 14.0,
            horizontalJointCm = 1.0,
            verticalJointCm = 1.0,
            bondTypeName = BondType.RUNNING_HALF.name,
            wastePercentage = 5.0
        )
    )
    val currentWall: StateFlow<WallModel> = _currentWall.asStateFlow()

    // Bloco selecionado atualmente
    private val _selectedBlock = MutableStateFlow(
        BlockModel(
            id = 1,
            name = "Bloco de Concreto 14×19×39 cm",
            manufacturer = "Padrão NBR 6136",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 14.0,
            halfBlockLengthCm = 19.0,
            isModular = true
        )
    )
    val selectedBlock: StateFlow<BlockModel> = _selectedBlock.asStateFlow()

    // Configuração de Juntas
    private val _jointConfig = MutableStateFlow(
        JointConfig(
            horizontalCm = 1.0,
            verticalCm = 1.0,
            renderStyle = JointRenderStyle.SOLID_MORTAR
        )
    )
    val jointConfig: StateFlow<JointConfig> = _jointConfig.asStateFlow()

    // Tipo de Amarração
    private val _bondType = MutableStateFlow(BondType.RUNNING_HALF)
    val bondType: StateFlow<BondType> = _bondType.asStateFlow()

    // Vãos / Aberturas da parede atual
    private val _openings = MutableStateFlow<List<OpeningModel>>(emptyList())
    val openings: StateFlow<List<OpeningModel>> = _openings.asStateFlow()

    // Saídas em T / Interseções da parede atual
    private val _intersections = MutableStateFlow<List<com.paginacaopro.app.model.WallIntersection>>(emptyList())
    val intersections: StateFlow<List<com.paginacaopro.app.model.WallIntersection>> = _intersections.asStateFlow()

    // Resultado Geométrico Calculado
    private val _tilingResult = MutableStateFlow<TilingResult?>(null)
    val tilingResult: StateFlow<TilingResult?> = _tilingResult.asStateFlow()

    // Calibração de Foto
    private val _photoCalibration = MutableStateFlow<PhotoCalibration?>(null)
    val photoCalibration: StateFlow<PhotoCalibration?> = _photoCalibration.asStateFlow()

    init {
        // Inicializar banco e catálogo
        viewModelScope.launch(Dispatchers.IO) {
            val count = blockDao.getCount()
            if (count == 0) {
                AppDatabase.populateInitialBlocks(blockDao)
            }
            val firstWall = _floorPlanProject.value.walls.firstOrNull()
            if (firstWall != null) {
                selectWallForIndividualTiling(firstWall)
            } else {
                recalculateTiling()
            }
        }
    }

    /**
     * Recalcula a geometria de paginação imediatamente.
     */
    fun recalculateTiling() {
        val wall = _currentWall.value
        val block = _selectedBlock.value
        val joints = _jointConfig.value
        val bond = _bondType.value
        val ops = _openings.value
        val inters = _intersections.value
        val catalog = blockCatalog.value

        val result = TilingEngine.calculate(
            wall = wall,
            block = block,
            jointConfig = joints,
            bondType = bond,
            openings = ops,
            intersections = inters,
            availableBlocks = catalog
        )
        _tilingResult.value = result
    }

    fun addIntersection(xCm: Double, thicknessCm: Double, name: String = "Saída em T") {
        val updated = _intersections.value.toMutableList()
        updated.add(
            com.paginacaopro.app.model.WallIntersection(
                xCm = xCm,
                thicknessCm = thicknessCm,
                name = name
            )
        )
        _intersections.value = updated
        recalculateTiling()
    }

    fun removeIntersection(id: String) {
        _intersections.value = _intersections.value.filter { it.id != id }
        recalculateTiling()
    }

    fun clearIntersections() {
        _intersections.value = emptyList()
        recalculateTiling()
    }


    fun updateWallDimensions(
        projectName: String,
        wallName: String,
        floor: String,
        environment: String,
        lengthCm: Double,
        heightCm: Double,
        thicknessCm: Double,
        wastePercentage: Double
    ) {
        val modLen = com.paginacaopro.app.engine.TilingEngine.snapToModularWallLength(lengthCm)
        val modH = com.paginacaopro.app.engine.TilingEngine.snapToModularWallHeight(heightCm)
        _currentWall.value = _currentWall.value.copy(
            projectName = projectName,
            wallName = wallName,
            floor = floor,
            environment = environment,
            lengthCm = modLen,
            heightCm = modH,
            thicknessCm = thicknessCm,
            wastePercentage = wastePercentage,
            updatedAt = System.currentTimeMillis()
        )
        recalculateTiling()
    }

    fun selectBlock(block: BlockModel) {
        _selectedBlock.value = block
        // Atualiza a espessura da parede se aplicável
        _currentWall.value = _currentWall.value.copy(thicknessCm = block.thicknessCm)
        recalculateTiling()
    }

    fun updateJoints(horizontalCm: Double, verticalCm: Double, style: JointRenderStyle) {
        _jointConfig.value = JointConfig(horizontalCm, verticalCm, style)
        _currentWall.value = _currentWall.value.copy(
            horizontalJointCm = horizontalCm,
            verticalJointCm = verticalCm
        )
        recalculateTiling()
    }

    fun updateBondType(bond: BondType) {
        _bondType.value = bond
        _currentWall.value = _currentWall.value.copy(bondTypeName = bond.name)
        recalculateTiling()
    }

    fun addOpening(opening: OpeningModel) {
        val updated = _openings.value.toMutableList()
        updated.add(opening)
        _openings.value = updated
        recalculateTiling()
    }

    fun removeOpening(openingId: Long) {
        _openings.value = _openings.value.filter { it.id != openingId }
        recalculateTiling()
    }

    fun clearOpenings() {
        _openings.value = emptyList()
        recalculateTiling()
    }

    fun saveBlock(block: BlockModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            // Se o bloco não tem imagem ou foi editado, gera imagem realista com IA
            val imageUri = block.imageUri ?: BlockRealisticImageGenerator.generateAndSaveBlockImage(app, block)
            val blockToSave = block.copy(imageUri = imageUri)

            if (blockToSave.id == 0L) {
                val newId = blockDao.insertBlock(blockToSave)
                val persisted = blockToSave.copy(id = newId)
                if (_selectedBlock.value.name == block.name) {
                    _selectedBlock.value = persisted
                    recalculateTiling()
                }
            } else {
                blockDao.updateBlock(blockToSave)
                if (_selectedBlock.value.id == blockToSave.id) {
                    _selectedBlock.value = blockToSave
                    recalculateTiling()
                }
            }
        }
    }

    /**
     * Regera explicitamente a imagem com IA para o bloco e atualiza no banco de dados.
     */
    fun generateAiBlockImage(block: BlockModel, onComplete: ((BlockModel) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val uri = BlockRealisticImageGenerator.generateAndSaveBlockImage(app, block)
            val updated = block.copy(imageUri = uri)
            if (updated.id != 0L) {
                blockDao.updateBlock(updated)
            }
            if (_selectedBlock.value.id == updated.id) {
                _selectedBlock.value = updated
                recalculateTiling()
            }
            onComplete?.invoke(updated)
        }
    }

    fun deleteBlock(block: BlockModel) {
        viewModelScope.launch(Dispatchers.IO) {
            blockDao.deleteBlock(block)
        }
    }

    fun restoreDefaultBlocks() {
        viewModelScope.launch(Dispatchers.IO) {
            com.paginacaopro.app.data.AppDatabase.populateInitialBlocks(blockDao)
        }
    }

    fun saveCurrentWallProject() {
        viewModelScope.launch(Dispatchers.IO) {
            val wallId = wallDao.insertWall(_currentWall.value)
            _currentWall.value = _currentWall.value.copy(id = wallId)
            // Salvar aberturas
            openingDao.deleteOpeningsByWallId(wallId)
            val ops = _openings.value.map { it.copy(wallId = wallId) }
            openingDao.insertOpenings(ops)
        }
    }

    fun loadWallProject(wall: WallModel) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentWall.value = wall
            val block = blockDao.getBlockById(wall.selectedBlockId)
            if (block != null) {
                _selectedBlock.value = block
            }
            _jointConfig.value = JointConfig(
                horizontalCm = wall.horizontalJointCm,
                verticalCm = wall.verticalJointCm
            )
            _bondType.value = try {
                BondType.valueOf(wall.bondTypeName)
            } catch (e: Exception) {
                BondType.RUNNING_HALF
            }
            val ops = openingDao.getOpeningsListByWallId(wall.id)
            _openings.value = ops
            recalculateTiling()
        }
    }

    fun setPhotoCalibration(calib: PhotoCalibration) {
        _photoCalibration.value = calib
    }

    fun generateSvgOutput(
        isSimplified: Boolean = false,
        cadStyle: com.paginacaopro.app.svg.CadStyle = com.paginacaopro.app.svg.CadStyle.AUTOCAD_DARK
    ): String {
        val result = _tilingResult.value ?: return ""
        return SvgGenerator.generateSvg(result, isSimplified = isSimplified, highDefinition = true, cadStyle = cadStyle)
    }

    // ========================================================
    // PLANTA BAIXA & GESTÃO MULTI-PAREDES (ARQUITETURA & ALVENARIA)
    // ========================================================

    private val _floorPlanProject = MutableStateFlow(FloorPlanProject.createDefaultAraucariasTemplate())
    val floorPlanProject: StateFlow<FloorPlanProject> = _floorPlanProject.asStateFlow()

    private val _selectedFloorPlanWallId = MutableStateFlow<String?>(
        FloorPlanProject.createDefaultAraucariasTemplate().walls.firstOrNull()?.id
    )
    val selectedFloorPlanWallId: StateFlow<String?> = _selectedFloorPlanWallId.asStateFlow()

    fun selectFloorPlanWall(id: String?) {
        _selectedFloorPlanWallId.value = id
        val wall = _floorPlanProject.value.walls.firstOrNull { it.id == id }
        if (wall != null) {
            selectWallForIndividualTiling(wall)
        }
    }

    fun loadFloorPlanTemplateAraucarias() {
        val template = FloorPlanProject.createDefaultAraucariasTemplate()
        _floorPlanProject.value = template
        val firstWall = template.walls.firstOrNull()
        _selectedFloorPlanWallId.value = firstWall?.id
        if (firstWall != null) {
            selectWallForIndividualTiling(firstWall)
        }
    }

    fun addWallToFloorPlan(name: String, lengthCm: Double, heightCm: Double, thicknessCm: Double) {
        val current = _floorPlanProject.value
        val nextLetter = ('A'.code + current.walls.size).toChar().toString()
        val modLen = com.paginacaopro.app.engine.TilingEngine.snapToModularWallLength(lengthCm)
        val modH = com.paginacaopro.app.engine.TilingEngine.snapToModularWallHeight(heightCm)
        val newWall = FloorPlanWall(
            label = nextLetter,
            name = name,
            lengthCm = modLen,
            heightCm = modH,
            thicknessCm = thicknessCm
        )
        _floorPlanProject.value = current.copy(walls = current.walls + newWall)
        _selectedFloorPlanWallId.value = newWall.id
        selectWallForIndividualTiling(newWall)
    }

    fun removeWallFromFloorPlan(wallId: String) {
        val current = _floorPlanProject.value
        _floorPlanProject.value = current.copy(walls = current.walls.filter { it.id != wallId })
        if (_selectedFloorPlanWallId.value == wallId) {
            val nextWall = _floorPlanProject.value.walls.firstOrNull()
            _selectedFloorPlanWallId.value = nextWall?.id
            if (nextWall != null) {
                selectWallForIndividualTiling(nextWall)
            }
        }
    }

    fun addOpeningToFloorPlanWall(wallId: String, opening: OpeningModel) {
        val current = _floorPlanProject.value
        val updatedWalls = current.walls.map { w ->
            if (w.id == wallId) w.copy(openings = w.openings + opening) else w
        }
        _floorPlanProject.value = current.copy(walls = updatedWalls)
        val updatedWall = updatedWalls.firstOrNull { it.id == wallId }
        if (updatedWall != null && _selectedFloorPlanWallId.value == wallId) {
            selectWallForIndividualTiling(updatedWall)
        }
    }

    fun removeOpeningFromFloorPlanWall(wallId: String, openingId: Long) {
        val current = _floorPlanProject.value
        val updatedWalls = current.walls.map { w ->
            if (w.id == wallId) w.copy(openings = w.openings.filter { it.id != openingId }) else w
        }
        _floorPlanProject.value = current.copy(walls = updatedWalls)
        val updatedWall = updatedWalls.firstOrNull { it.id == wallId }
        if (updatedWall != null && _selectedFloorPlanWallId.value == wallId) {
            selectWallForIndividualTiling(updatedWall)
        }
    }

    fun selectWallForIndividualTiling(wall: FloorPlanWall) {
        _currentWall.value = wall.toWallModel(_floorPlanProject.value.name)
        _openings.value = wall.openings
        _intersections.value = wall.intersections
        val currBlk = _selectedBlock.value
        if (kotlin.math.abs(currBlk.thicknessCm - wall.thicknessCm) > 0.1) {
            val matching = blockCatalog.value.firstOrNull { kotlin.math.abs(it.thicknessCm - wall.thicknessCm) < 0.2 }
            if (matching != null) {
                _selectedBlock.value = matching
            } else {
                _selectedBlock.value = currBlk.copy(
                    thicknessCm = wall.thicknessCm,
                    name = "Bloco Modular ${com.paginacaopro.app.engine.FreePlanEngine.formatClean(wall.thicknessCm)}×19×39 cm"
                )
            }
        }
        recalculateTiling()
    }

    fun changeAllFloorPlanWallsThickness(newThicknessCm: Double) {
        val current = _floorPlanProject.value
        val updatedWalls = current.walls.map { w ->
            w.copy(
                thicknessCm = newThicknessCm,
                intersections = w.intersections.map { it.copy(thicknessCm = newThicknessCm) }
            )
        }
        val matchingBlk = blockCatalog.value.firstOrNull { kotlin.math.abs(it.thicknessCm - newThicknessCm) < 0.2 }
        val newDefaultBlock = matchingBlk ?: current.defaultBlock.copy(
            thicknessCm = newThicknessCm,
            name = "Bloco Modular ${com.paginacaopro.app.engine.FreePlanEngine.formatClean(newThicknessCm)}×19×39 cm"
        )
        _floorPlanProject.value = current.copy(
            defaultThicknessCm = newThicknessCm,
            defaultBlock = newDefaultBlock,
            walls = updatedWalls
        )
        // Sincroniza a parede em edição se pertencer a esta planta
        val currW = _currentWall.value
        val matchCurr = updatedWalls.firstOrNull { it.label == currW.wallName.substringBefore(" ").trim() }
        if (matchCurr != null) {
            selectWallForIndividualTiling(matchCurr)
        } else {
            _selectedBlock.value = newDefaultBlock
            _currentWall.value = _currentWall.value.copy(thicknessCm = newThicknessCm)
            recalculateTiling()
        }
    }

    fun changeFloorPlanWallThickness(wallId: String, newThicknessCm: Double) {
        val current = _floorPlanProject.value
        val updatedWalls = current.walls.map { w ->
            if (w.id == wallId) {
                w.copy(
                    thicknessCm = newThicknessCm,
                    intersections = w.intersections.map { it.copy(thicknessCm = newThicknessCm) }
                )
            } else w
        }
        _floorPlanProject.value = current.copy(walls = updatedWalls)
        val updatedWall = updatedWalls.firstOrNull { it.id == wallId }
        if (updatedWall != null && _currentWall.value.wallName.startsWith("Parede ${updatedWall.label}")) {
            selectWallForIndividualTiling(updatedWall)
        }
    }

    fun changeAllFreePlanWallsThickness(newThicknessCm: Double) {
        pushFreePlanUndoSnapshot()
        _freePlanWalls.value = _freePlanWalls.value.map { w ->
            w.copy(thicknessCm = newThicknessCm)
        }
    }

    fun changeFreePlanWallThickness(wallId: String, newThicknessCm: Double) {
        pushFreePlanUndoSnapshot()
        _freePlanWalls.value = _freePlanWalls.value.map { w ->
            if (w.id == wallId) w.copy(thicknessCm = newThicknessCm) else w
        }
    }

    fun changeCurrentTilingThickness(newThicknessCm: Double) {
        val currW = _currentWall.value
        _currentWall.value = currW.copy(thicknessCm = newThicknessCm)
        val matchingBlk = blockCatalog.value.firstOrNull { kotlin.math.abs(it.thicknessCm - newThicknessCm) < 0.2 }
        _selectedBlock.value = matchingBlk ?: _selectedBlock.value.copy(
            thicknessCm = newThicknessCm,
            name = "Bloco Modular ${com.paginacaopro.app.engine.FreePlanEngine.formatClean(newThicknessCm)}×19×39 cm"
        )
        // Se estiver vinculado à planta baixa ou planta livre, sincroniza a parede correspondente
        val fp = _floorPlanProject.value
        val fpWall = fp.walls.firstOrNull { currW.wallName.startsWith("Parede ${it.label}") || currW.wallName == it.label }
        if (fpWall != null) {
            changeFloorPlanWallThickness(fpWall.id, newThicknessCm)
        }
        val freeWall = _freePlanWalls.value.firstOrNull { currW.wallName.startsWith("Parede ${it.label}") || currW.wallName == it.label }
        if (freeWall != null) {
            changeFreePlanWallThickness(freeWall.id, newThicknessCm)
        }
        recalculateTiling()
    }

    fun calculateConsolidatedQuantitativo(): ConsolidatedQuantitativo {
        val proj = _floorPlanProject.value
        val block = _selectedBlock.value
        val joints = _jointConfig.value
        val bond = _bondType.value
        val catalog = blockCatalog.value

        val resultMap = mutableMapOf<String, TilingResult>()
        var totalGross = 0.0
        var totalOps = 0.0
        var totalNet = 0.0
        var totalPieces = 0
        var totalRecBlocks = 0
        var totalRecHalf = 0
        var totalMortar = 0.0

        val pieceGroupMap = mutableMapOf<Double, Pair<CommercialPieceCount, Int>>()

        for (w in proj.walls) {
            val wallModel = w.toWallModel(proj.name)
            val res = TilingEngine.calculate(
                wall = wallModel,
                block = block,
                jointConfig = joints,
                bondType = bond,
                openings = w.openings,
                intersections = w.intersections,
                availableBlocks = catalog
            )
            resultMap[w.label] = res
            val bom = res.billOfMaterials
            totalGross += bom.grossWallAreaM2
            totalOps += bom.openingsAreaM2
            totalNet += bom.netWallAreaM2
            totalPieces += bom.totalPiecesGeometrical
            totalRecBlocks += bom.recommendedPurchaseBlocks
            totalRecHalf += bom.recommendedPurchaseHalfBlocks
            totalMortar += bom.mortarJointVolumeM3Estimated

            for (p in bom.pieceBreakdown) {
                val existing = pieceGroupMap[p.lengthCm]
                if (existing != null) {
                    val combinedCount = existing.second + p.count
                    val combinedPurchase = existing.first.recommendedPurchase + p.recommendedPurchase
                    pieceGroupMap[p.lengthCm] = existing.first.copy(
                        count = combinedCount,
                        recommendedPurchase = combinedPurchase
                    ) to combinedCount
                } else {
                    pieceGroupMap[p.lengthCm] = p to p.count
                }
            }
        }

        val globalBreakdown = pieceGroupMap.values
            .map { it.first }
            .sortedByDescending { it.lengthCm }
            .map { p ->
                val pct = if (totalPieces > 0) (p.count.toDouble() / totalPieces) * 100.0 else 0.0
                p.copy(percentageOfWall = (pct * 10.0).roundToInt() / 10.0)
            }

        return ConsolidatedQuantitativo(
            project = proj,
            wallResults = resultMap,
            totalGrossAreaM2 = (totalGross * 100.0).roundToInt() / 100.0,
            totalOpeningsAreaM2 = (totalOps * 100.0).roundToInt() / 100.0,
            totalNetAreaM2 = (totalNet * 100.0).roundToInt() / 100.0,
            totalPiecesGeometrical = totalPieces,
            totalRecommendedPurchaseBlocks = totalRecBlocks,
            totalRecommendedPurchaseHalfBlocks = totalRecHalf,
            totalMortarVolumeM3 = (totalMortar * 1000.0).roundToInt() / 1000.0,
            globalBreakdown = globalBreakdown
        )
    }

    // ========================================================
    // ESTÚDIO DE PLANTA LIVRE & PAGINAÇÃO EXECUTIVA (NOVA FUNCIONALIDADE)
    // ========================================================

    private val _freePlanWalls = MutableStateFlow(com.paginacaopro.app.engine.FreePlanEngine.createStarterDrawnPlan())
    val freePlanWalls: StateFlow<List<com.paginacaopro.app.model.FreePlanWall>> = _freePlanWalls.asStateFlow()

    private val _freePlanAuxLines = MutableStateFlow<List<com.paginacaopro.app.model.AuxLine90>>(emptyList())
    val freePlanAuxLines: StateFlow<List<com.paginacaopro.app.model.AuxLine90>> = _freePlanAuxLines.asStateFlow()

    private val _freePlanCatalog = MutableStateFlow(com.paginacaopro.app.engine.FreePlanEngine.createInitialBlockCatalog())
    val freePlanCatalog: StateFlow<List<com.paginacaopro.app.model.CatalogBlockPiece>> = _freePlanCatalog.asStateFlow()

    private val _freePlanRoomNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val freePlanRoomNames: StateFlow<Map<String, String>> = _freePlanRoomNames.asStateFlow()

    private val _freePlanHistoryUndo = mutableListOf<com.paginacaopro.app.model.FreePlanSnapshot>()
    private val _freePlanHistoryRedo = mutableListOf<com.paginacaopro.app.model.FreePlanSnapshot>()

    private val _canUndoFreePlan = MutableStateFlow(false)
    val canUndoFreePlan: StateFlow<Boolean> = _canUndoFreePlan.asStateFlow()

    private val _canRedoFreePlan = MutableStateFlow(false)
    val canRedoFreePlan: StateFlow<Boolean> = _canRedoFreePlan.asStateFlow()

    private fun pushFreePlanUndoSnapshot() {
        _freePlanHistoryUndo.add(
            com.paginacaopro.app.model.FreePlanSnapshot(
                walls = _freePlanWalls.value,
                auxLines = _freePlanAuxLines.value,
                roomCustomNames = _freePlanRoomNames.value
            )
        )
        if (_freePlanHistoryUndo.size > 40) {
            _freePlanHistoryUndo.removeAt(0)
        }
        _freePlanHistoryRedo.clear()
        _canUndoFreePlan.value = _freePlanHistoryUndo.isNotEmpty()
        _canRedoFreePlan.value = false
    }

    fun undoFreePlan() {
        if (_freePlanHistoryUndo.isEmpty()) return
        val currentSnap = com.paginacaopro.app.model.FreePlanSnapshot(
            walls = _freePlanWalls.value,
            auxLines = _freePlanAuxLines.value,
            roomCustomNames = _freePlanRoomNames.value
        )
        _freePlanHistoryRedo.add(currentSnap)
        val prev = _freePlanHistoryUndo.removeAt(_freePlanHistoryUndo.lastIndex)
        _freePlanWalls.value = prev.walls
        _freePlanAuxLines.value = prev.auxLines
        _freePlanRoomNames.value = prev.roomCustomNames
        _canUndoFreePlan.value = _freePlanHistoryUndo.isNotEmpty()
        _canRedoFreePlan.value = _freePlanHistoryRedo.isNotEmpty()
    }

    fun redoFreePlan() {
        if (_freePlanHistoryRedo.isEmpty()) return
        val currentSnap = com.paginacaopro.app.model.FreePlanSnapshot(
            walls = _freePlanWalls.value,
            auxLines = _freePlanAuxLines.value,
            roomCustomNames = _freePlanRoomNames.value
        )
        _freePlanHistoryUndo.add(currentSnap)
        val next = _freePlanHistoryRedo.removeAt(_freePlanHistoryRedo.lastIndex)
        _freePlanWalls.value = next.walls
        _freePlanAuxLines.value = next.auxLines
        _freePlanRoomNames.value = next.roomCustomNames
        _canUndoFreePlan.value = _freePlanHistoryUndo.isNotEmpty()
        _canRedoFreePlan.value = _freePlanHistoryRedo.isNotEmpty()
    }

    fun addFreePlanWall(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        thicknessCm: Double,
        heightCm: Double = 280.0,
        isExternal: Boolean = true,
        includeTopCintaCanaleta: Boolean = false
    ): com.paginacaopro.app.model.FreePlanWall? {
        val currentWalls = _freePlanWalls.value
        val (weldedStartX, weldedStartY) = com.paginacaopro.app.engine.FreePlanEngine.weldEndpointToExistingWalls(
            startX, startY, currentWalls, weldRadiusMeters = 0.55f
        )
        val (rawEndX, rawEndY) = com.paginacaopro.app.engine.FreePlanEngine.weldEndpointToExistingWalls(
            endX, endY, currentWalls, weldRadiusMeters = 0.55f
        )
        var (weldedEndX, weldedEndY) = com.paginacaopro.app.engine.FreePlanEngine.projectOrthogonal90(
            weldedStartX, weldedStartY, rawEndX, rawEndY
        )

        val dist = kotlin.math.hypot((weldedEndX - weldedStartX).toDouble(), (weldedEndY - weldedStartY).toDouble())
        if (dist < 0.25) return null
        pushFreePlanUndoSnapshot()

        val isNewHoriz = kotlin.math.abs(weldedEndY - weldedStartY) <= kotlin.math.abs(weldedEndX - weldedStartX)
        var adjustedExistingWalls = currentWalls

        // Se esta parede está fechando um canto/quadrado com uma parede perpendicular cujo início estava aberto,
        // colamos automaticamente os dois cantos para fechar o quadrado perfeitamente sem sobras nem falhas!
        for (i in adjustedExistingWalls.indices) {
            val w = adjustedExistingWalls[i]
            val startConnected = adjustedExistingWalls.any { other ->
                other.id != w.id && (
                    kotlin.math.hypot((other.startX - w.startX).toDouble(), (other.startY - w.startY).toDouble()) < 0.12 ||
                    kotlin.math.hypot((other.endX - w.startX).toDouble(), (other.endY - w.startY).toDouble()) < 0.12
                )
            }
            if (!startConnected) {
                if (!isNewHoriz && w.isHorizontal) {
                    // Nova parede vertical chegando na linha Y da parede horizontal inicial w
                    if (kotlin.math.abs(weldedEndY - w.startY) <= 0.75f &&
                        kotlin.math.abs(weldedStartX - w.startX) <= 3.0f &&
                        kotlin.math.abs(w.endX - weldedStartX) >= 0.4f
                    ) {
                        weldedEndY = w.startY
                        adjustedExistingWalls = adjustedExistingWalls.toMutableList().also { list ->
                            list[i] = w.copy(startX = weldedEndX)
                        }
                        break
                    }
                } else if (isNewHoriz && !w.isHorizontal) {
                    // Nova parede horizontal chegando na linha X da parede vertical inicial w
                    if (kotlin.math.abs(weldedEndX - w.startX) <= 0.75f &&
                        kotlin.math.abs(weldedStartY - w.startY) <= 3.0f &&
                        kotlin.math.abs(w.endY - weldedStartY) >= 0.4f
                    ) {
                        weldedEndX = w.startX
                        adjustedExistingWalls = adjustedExistingWalls.toMutableList().also { list ->
                            list[i] = w.copy(startY = weldedEndY)
                        }
                        break
                    }
                }
            }
        }

        val index = adjustedExistingWalls.size
        val label = if (index < 26) ('A'.code + index).toChar().toString() else "P${index + 1}"
        val newWall = com.paginacaopro.app.model.FreePlanWall(
            label = label,
            startX = weldedStartX,
            startY = weldedStartY,
            endX = weldedEndX,
            endY = weldedEndY,
            thicknessCm = thicknessCm,
            heightCm = heightCm,
            realLengthCm = null, // No Modo 1 (Desenho Visual) inicia sem exigir medida real
            isExternal = isExternal,
            includeTopCintaCanaleta = includeTopCintaCanaleta
        )
        _freePlanWalls.value = adjustedExistingWalls + newWall
        return newWall
    }

    fun addFreePlanAuxLine90(startX: Float, startY: Float, endX: Float, endY: Float) {
        val (orthoX, orthoY) = com.paginacaopro.app.engine.FreePlanEngine.projectOrthogonal90(startX, startY, endX, endY)
        if (kotlin.math.hypot((orthoX - startX).toDouble(), (orthoY - startY).toDouble()) < 0.2) return
        pushFreePlanUndoSnapshot()
        _freePlanAuxLines.value = _freePlanAuxLines.value + com.paginacaopro.app.model.AuxLine90(
            startX = startX,
            startY = startY,
            endX = orthoX,
            endY = orthoY
        )
    }

    fun moveFreePlanWall(wallId: String, deltaXMeters: Float, deltaYMeters: Float) {
        pushFreePlanUndoSnapshot()
        _freePlanWalls.value = _freePlanWalls.value.map { w ->
            if (w.id == wallId && !w.isFixedLocked) {
                w.copy(
                    startX = w.startX + deltaXMeters,
                    startY = w.startY + deltaYMeters,
                    endX = w.endX + deltaXMeters,
                    endY = w.endY + deltaYMeters
                )
            } else w
        }
    }

    fun moveEntireFreePlan(deltaXMeters: Float, deltaYMeters: Float) {
        if (kotlin.math.hypot(deltaXMeters.toDouble(), deltaYMeters.toDouble()) < 0.02) return
        pushFreePlanUndoSnapshot()
        _freePlanWalls.value = com.paginacaopro.app.engine.FreePlanEngine.translateEntirePlan(
            _freePlanWalls.value, deltaXMeters, deltaYMeters
        )
    }

    fun scaleEntireFreePlan(scaleFactor: Float) {
        pushFreePlanUndoSnapshot()
        _freePlanWalls.value = com.paginacaopro.app.engine.FreePlanEngine.scaleEntirePlan(
            _freePlanWalls.value, scaleFactor
        )
    }

    fun deleteFreePlanWall(wallId: String) {
        pushFreePlanUndoSnapshot()
        _freePlanWalls.value = _freePlanWalls.value.filter { it.id != wallId }
    }

    fun deleteFreePlanAuxLine(lineId: String) {
        pushFreePlanUndoSnapshot()
        _freePlanAuxLines.value = _freePlanAuxLines.value.filter { it.id != lineId }
    }

    fun clearFreePlanCanvas() {
        pushFreePlanUndoSnapshot()
        _freePlanWalls.value = emptyList()
        _freePlanAuxLines.value = emptyList()
        _freePlanRoomNames.value = emptyMap()
    }

    fun loadStarterFreePlan() {
        pushFreePlanUndoSnapshot()
        _freePlanWalls.value = com.paginacaopro.app.engine.FreePlanEngine.createStarterDrawnPlan()
        _freePlanAuxLines.value = emptyList()
    }

    fun applyRealDimensionToFreePlanWall(
        wallId: String,
        newLengthCm: Double,
        heightCm: Double,
        thicknessCm: Double,
        isLocked: Boolean,
        includeCintaCanaleta: Boolean,
        rule: com.paginacaopro.app.model.DimensionAdjustRule
    ): com.paginacaopro.app.engine.FreePlanEngine.DimensionUpdateOutcome {
        pushFreePlanUndoSnapshot()
        val modH = com.paginacaopro.app.engine.TilingEngine.snapToModularWallHeight(heightCm)
        val outcome = com.paginacaopro.app.engine.FreePlanEngine.applyWallRealDimension(
            walls = _freePlanWalls.value,
            targetWallId = wallId,
            newLengthCm = newLengthCm,
            rule = rule
        )
        if (!outcome.hasConflict) {
            _freePlanWalls.value = outcome.updatedWalls.map { w ->
                if (w.id == wallId) {
                    w.copy(
                        heightCm = modH,
                        thicknessCm = thicknessCm,
                        isFixedLocked = isLocked,
                        includeTopCintaCanaleta = includeCintaCanaleta
                    )
                } else w
            }
        }
        return outcome
    }

    fun addOpeningToFreePlanWall(wallId: String, opening: com.paginacaopro.app.model.FreePlanOpening) {
        pushFreePlanUndoSnapshot()
        _freePlanWalls.value = _freePlanWalls.value.map { w ->
            if (w.id == wallId) w.copy(openings = w.openings + opening) else w
        }
    }

    fun removeOpeningFromFreePlan(wallId: String, openingId: Long) {
        pushFreePlanUndoSnapshot()
        _freePlanWalls.value = _freePlanWalls.value.map { w ->
            if (w.id == wallId) w.copy(openings = w.openings.filter { it.id != openingId }) else w
        }
    }

    fun toggleCatalogPieceAvailability(pieceId: String) {
        _freePlanCatalog.value = _freePlanCatalog.value.map { p ->
            if (p.id == pieceId) p.copy(isAvailableAtSite = !p.isAvailableAtSite) else p
        }
    }

    fun addCustomCatalogPiece(piece: com.paginacaopro.app.model.CatalogBlockPiece) {
        // Evitar duplicatas exatas da mesma família, espessura e comprimento
        val exists = _freePlanCatalog.value.any {
            kotlin.math.abs(it.thicknessCm - piece.thicknessCm) < 0.2 &&
                    kotlin.math.abs(it.lengthCm - piece.lengthCm) < 0.2 &&
                    kotlin.math.abs(it.heightCm - piece.heightCm) < 0.2 &&
                    it.isCanaleta == piece.isCanaleta
        }
        if (!exists) {
            _freePlanCatalog.value = _freePlanCatalog.value + piece
        }
    }

    fun renameDetectedRoom(roomId: String, newName: String) {
        pushFreePlanUndoSnapshot()
        val updated = _freePlanRoomNames.value.toMutableMap()
        updated[roomId] = newName
        _freePlanRoomNames.value = updated
    }

    /**
     * Abre uma parede do Estúdio de Planta Livre diretamente no Visualizador de Elevação AutoCAD/SVG,
     * sincronizando todas as paredes da planta para permitir alternar entre Paredes A, B, C, D...
     */
    fun openFreePlanWallInElevationViewer(wallId: String) {
        val walls = _freePlanWalls.value
        val target = walls.find { it.id == wallId } ?: walls.firstOrNull() ?: return
        val pag = com.paginacaopro.app.engine.FreePlanEngine.paginateFreePlanWall(
            wall = target,
            allWalls = walls,
            catalogPieces = _freePlanCatalog.value,
            jointConfig = _jointConfig.value,
            bondType = _bondType.value
        )

        // Sincronizar _floorPlanProject para que a barra "Paredes: A, B, C..." do TilingViewerScreen funcione com a planta desenhada
        val convertedWalls = walls.map { fw ->
            val fwPag = com.paginacaopro.app.engine.FreePlanEngine.paginateFreePlanWall(
                wall = fw,
                allWalls = walls,
                catalogPieces = _freePlanCatalog.value,
                jointConfig = _jointConfig.value,
                bondType = _bondType.value
            )
            FloorPlanWall(
                id = fw.id,
                label = fw.label,
                name = fw.customName.ifBlank { "Alvenaria ${fw.thicknessCm} cm" },
                lengthCm = fw.effectiveLengthCm,
                heightCm = fw.heightCm,
                thicknessCm = fw.thicknessCm,
                startX = fw.startX,
                startY = fw.startY,
                endX = fw.endX,
                endY = fw.endY,
                openings = fw.openings.map { it.toOpeningModel() },
                intersections = fwPag.tilingResult.intersections
            )
        }
        _floorPlanProject.value = _floorPlanProject.value.copy(
            name = "Planta Livre Executiva",
            walls = convertedWalls
        )

        _currentWall.value = pag.tilingResult.wall
        _selectedBlock.value = pag.tilingResult.block
        _openings.value = pag.tilingResult.openings
        _intersections.value = pag.tilingResult.intersections
        _tilingResult.value = pag.tilingResult
    }
}


