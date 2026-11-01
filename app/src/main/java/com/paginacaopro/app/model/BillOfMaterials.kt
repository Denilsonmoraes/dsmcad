package com.paginacaopro.app.model

/**
 * Quantitativo detalhado levantado peça por peça a partir da geometria real gerada.
 */
data class BillOfMaterials(
    val grossWallAreaM2: Double,
    val openingsAreaM2: Double,
    val netWallAreaM2: Double,
    val totalCourses: Int,
    val wholeBlockCount: Int,
    val halfBlockCount: Int,
    val cutPieceCount: Int,
    val specialPieceCount: Int,
    val totalPiecesGeometrical: Int,
    val wastePercentage: Double,
    val recommendedPurchaseBlocks: Int,
    val recommendedPurchaseHalfBlocks: Int,
    val mortarJointAreaM2Estimated: Double,
    val mortarJointVolumeM3Estimated: Double,
    val cutDetails: List<CutDetail> = emptyList(),
    val pieceBreakdown: List<CommercialPieceCount> = emptyList()
)

data class CommercialPieceCount(
    val lengthCm: Double,
    val heightCm: Double,
    val thicknessCm: Double,
    val name: String,
    val count: Int,
    val recommendedPurchase: Int,
    val percentageOfWall: Double
)

data class CutDetail(
    val courseIndex: Int,
    val lengthCm: Double,
    val heightCm: Double,
    val reason: String
)

/**
 * Resultado completo do motor geométrico de paginação.
 */
data class TilingResult(
    val wall: WallModel,
    val block: BlockModel,
    val jointConfig: JointConfig,
    val bondType: BondType,
    val openings: List<OpeningModel>,
    val courses: List<List<TilingBlock>>, // Lista de fiadas, cada uma com sua lista de blocos ordenados por X
    val billOfMaterials: BillOfMaterials,
    val intersections: List<WallIntersection> = emptyList(),
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList()
) {
    val allBlocks: List<TilingBlock> get() = courses.flatten()
    val isValid: Boolean get() = errors.isEmpty() && courses.isNotEmpty()
}

