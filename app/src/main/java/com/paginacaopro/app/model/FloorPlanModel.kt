package com.paginacaopro.app.model

import java.util.UUID

/**
 * Representa uma parede individual desenhada na Planta Baixa.
 * Identificada por letra (Parede A, Parede B, Parede C, ...).
 */
data class FloorPlanWall(
    val id: String = UUID.randomUUID().toString(),
    val label: String, // "A", "B", "C", "D", "E", "F", "G"...
    val name: String,  // "Fachada Frontal / Sala", "Divisória Quarto 1 / Banheiro", etc.
    val lengthCm: Double,
    val heightCm: Double = 280.0,
    val thicknessCm: Double = 14.0,
    val startX: Float = 0f, // Coordenadas 2D na planta (metros)
    val startY: Float = 0f,
    val endX: Float = 0f,
    val endY: Float = 0f,
    val openings: List<OpeningModel> = emptyList(),
    val intersections: List<WallIntersection> = emptyList()
) {
    fun toWallModel(projectName: String): WallModel {
        return WallModel(
            projectName = projectName,
            wallName = "Parede $label — $name",
            lengthCm = lengthCm,
            heightCm = heightCm,
            thicknessCm = thicknessCm,
            notes = "",
            wastePercentage = 5.0
        )
    }
}

/**
 * Representa o projeto completo da Planta Baixa da Casa/Apartamento.
 */
data class FloorPlanProject(
    val id: Long = 0,
    val name: String = "Residencial Jardim das Araucárias",
    val floor: String = "Pavimento Tipo",
    val walls: List<FloorPlanWall> = emptyList(),
    val defaultBlock: BlockModel = BlockModel(
        id = 1,
        name = "Bloco de Concreto 14×19×39 cm",
        material = "Concreto",
        lengthCm = 39.0,
        heightCm = 19.0,
        thicknessCm = 14.0,
        halfBlockLengthCm = 19.0
    ),
    val defaultJointConfig: JointConfig = JointConfig(horizontalCm = 1.0, verticalCm = 1.0),
    val defaultBondType: BondType = BondType.RUNNING_HALF,
    val defaultHeightCm: Double = 280.0,
    val defaultThicknessCm: Double = 14.0
) {
    companion object {
        /**
         * Gera a planta baixa técnica modelo baseada exatamente na planta baixa enviada pelo usuário
         * (Residencial Jardim das Araucárias) com as paredes demarcadas (A até G).
         */
        fun createDefaultAraucariasTemplate(): FloorPlanProject {
            val walls = listOf(
                FloorPlanWall(
                    label = "A",
                    name = "Parede Superior (Sala / Cozinha / Lavanderia)",
                    lengthCm = 800.0,
                    heightCm = 280.0,
                    thicknessCm = 14.0,
                    startX = 1.0f, startY = 1.0f, endX = 9.0f, endY = 1.0f,
                    openings = listOf(
                        OpeningModel(name = "J1 (Sala)", widthCm = 160.0, heightCm = 120.0, xCm = 115.0, yCm = 100.0, hasLintel = true),
                        OpeningModel(name = "J2 (Cozinha)", widthCm = 120.0, heightCm = 100.0, xCm = 515.0, yCm = 100.0, hasLintel = true)
                    ),
                    intersections = listOf(
                        WallIntersection(xCm = 400.0, thicknessCm = 14.0, name = "Saída T Parede F (Sala/Cozinha)")
                    )
                ),
                FloorPlanWall(
                    label = "B",
                    name = "Parede Lateral Direita",
                    lengthCm = 600.0,
                    heightCm = 280.0,
                    thicknessCm = 14.0,
                    startX = 9.0f, startY = 1.0f, endX = 9.0f, endY = 7.0f,
                    openings = listOf(
                        OpeningModel(name = "P2 (Serviço)", widthCm = 80.0, heightCm = 220.0, xCm = 120.0, yCm = 0.0, hasLintel = true)
                    ),
                    intersections = listOf(
                        WallIntersection(xCm = 300.0, thicknessCm = 14.0, name = "Saída T Parede E (Divisória Central)")
                    )
                ),
                FloorPlanWall(
                    label = "C",
                    name = "Parede Inferior (Quarto 1 / Banheiro / Quarto 2)",
                    lengthCm = 800.0,
                    heightCm = 280.0,
                    thicknessCm = 14.0,
                    startX = 1.0f, startY = 7.0f, endX = 9.0f, endY = 7.0f,
                    openings = listOf(
                        OpeningModel(name = "P1 (Acesso)", widthCm = 80.0, heightCm = 220.0, xCm = 120.0, yCm = 0.0, hasLintel = true),
                        OpeningModel(name = "J3 (Quarto 2)", widthCm = 120.0, heightCm = 120.0, xCm = 600.0, yCm = 100.0, hasLintel = true)
                    ),
                    intersections = listOf(
                        WallIntersection(xCm = 240.0, thicknessCm = 14.0, name = "Saída T Parede G (Banheiro Esq.)"),
                        WallIntersection(xCm = 480.0, thicknessCm = 14.0, name = "Saída T Parede H (Banheiro Dir.)")
                    )
                ),
                FloorPlanWall(
                    label = "D",
                    name = "Parede Lateral Esquerda",
                    lengthCm = 600.0,
                    heightCm = 280.0,
                    thicknessCm = 14.0,
                    startX = 1.0f, startY = 1.0f, endX = 1.0f, endY = 7.0f,
                    intersections = listOf(
                        WallIntersection(xCm = 300.0, thicknessCm = 14.0, name = "Saída T Parede E (Divisória Central)")
                    )
                ),
                FloorPlanWall(
                    label = "E",
                    name = "Parede Divisória Meio (Sala / Banheiro / Quartos)",
                    lengthCm = 800.0,
                    heightCm = 280.0,
                    thicknessCm = 14.0,
                    startX = 1.0f, startY = 4.0f, endX = 9.0f, endY = 4.0f,
                    openings = listOf(
                        OpeningModel(name = "P3 (Quarto 1)", widthCm = 80.0, heightCm = 220.0, xCm = 120.0, yCm = 0.0, hasLintel = true),
                        OpeningModel(name = "P4 (Banheiro)", widthCm = 80.0, heightCm = 220.0, xCm = 320.0, yCm = 0.0, hasLintel = true),
                        OpeningModel(name = "P5 (Quarto 2)", widthCm = 80.0, heightCm = 220.0, xCm = 640.0, yCm = 0.0, hasLintel = true)
                    ),
                    intersections = listOf(
                        WallIntersection(xCm = 240.0, thicknessCm = 14.0, name = "Saída T Parede G (Banheiro Esq.)"),
                        WallIntersection(xCm = 400.0, thicknessCm = 14.0, name = "Saída T Parede F (Sala/Cozinha)"),
                        WallIntersection(xCm = 480.0, thicknessCm = 14.0, name = "Saída T Parede H (Banheiro Dir.)")
                    )
                ),
                FloorPlanWall(
                    label = "F",
                    name = "Divisória Sala / Cozinha (Saída T)",
                    lengthCm = 300.0,
                    heightCm = 280.0,
                    thicknessCm = 14.0,
                    startX = 5.0f, startY = 1.0f, endX = 5.0f, endY = 4.0f
                ),
                FloorPlanWall(
                    label = "G",
                    name = "Parede Banheiro Esq. (Saída T)",
                    lengthCm = 300.0,
                    heightCm = 280.0,
                    thicknessCm = 14.0,
                    startX = 3.4f, startY = 4.0f, endX = 3.4f, endY = 7.0f
                ),
                FloorPlanWall(
                    label = "H",
                    name = "Parede Banheiro Dir. (Saída T)",
                    lengthCm = 300.0,
                    heightCm = 280.0,
                    thicknessCm = 14.0,
                    startX = 5.8f, startY = 4.0f, endX = 5.8f, endY = 7.0f
                )
            )
            return FloorPlanProject(
                name = "Residencial Jardim das Araucárias",
                floor = "Térreo / Pavimento Tipo",
                walls = walls
            )
        }
    }
}

/**
 * Quantitativo Geral Consolidado de toda a Obra somando todas as paredes da planta.
 */
data class ConsolidatedQuantitativo(
    val project: FloorPlanProject,
    val wallResults: Map<String, TilingResult>,
    val totalGrossAreaM2: Double,
    val totalOpeningsAreaM2: Double,
    val totalNetAreaM2: Double,
    val totalPiecesGeometrical: Int,
    val totalRecommendedPurchaseBlocks: Int,
    val totalRecommendedPurchaseHalfBlocks: Int,
    val totalMortarVolumeM3: Double,
    val globalBreakdown: List<CommercialPieceCount>
)
