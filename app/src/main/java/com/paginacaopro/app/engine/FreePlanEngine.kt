package com.paginacaopro.app.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.paginacaopro.app.model.AuxLine90
import com.paginacaopro.app.model.BlockModel
import com.paginacaopro.app.model.BlockPieceCategory
import com.paginacaopro.app.model.BondType
import com.paginacaopro.app.model.CatalogBlockPiece
import com.paginacaopro.app.model.ClosingAnalysis
import com.paginacaopro.app.model.DetectedRoom
import com.paginacaopro.app.model.DetectedWallJunction
import com.paginacaopro.app.model.DimensionAdjustRule
import com.paginacaopro.app.model.DoorStyle
import com.paginacaopro.app.model.FreePlanOpening
import com.paginacaopro.app.model.FreePlanWall
import com.paginacaopro.app.model.FreePlanWallPagination
import com.paginacaopro.app.model.JointConfig
import com.paginacaopro.app.model.JunctionKind
import com.paginacaopro.app.model.OpeningType
import com.paginacaopro.app.model.PieceType
import com.paginacaopro.app.model.TilingResult
import com.paginacaopro.app.model.WallIntersection
import com.paginacaopro.app.model.WallModel
import com.paginacaopro.app.model.WallShape
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object FreePlanEngine {

    /**
     * 3. MÓDULO DE CADASTRO E SELEÇÃO DOS BLOCOS (Seções 3.1, 3.2, 3.3, 3.4 e 8.C)
     * Retorna o catálogo completo organizado por espessura (sem duplicatas), incluindo canaletas compatíveis.
     */
    fun createInitialBlockCatalog(): List<CatalogBlockPiece> {
        return listOf(
            // =========================================================================
            // 3.1. BLOCOS DE 14 CM (Comprimento × Altura 19 cm × Largura 14 cm)
            // =========================================================================
            CatalogBlockPiece(
                code = "B01",
                name = "Bloco Principal 39×19×14 cm",
                familyName = "Família Modular 14 cm",
                lengthCm = 39.0, heightCm = 19.0, thicknessCm = 14.0,
                category = BlockPieceCategory.STANDARD,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B02",
                name = "Bloco Amarração Canto 34×19×14 cm",
                familyName = "Família Modular 14 cm",
                lengthCm = 34.0, heightCm = 19.0, thicknessCm = 14.0,
                category = BlockPieceCategory.CORNER_TIE,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B04",
                name = "Meio Bloco 19×19×14 cm",
                familyName = "Família Modular 14 cm",
                lengthCm = 19.0, heightCm = 19.0, thicknessCm = 14.0,
                category = BlockPieceCategory.HALF,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B06",
                name = "Compensador 9×19×14 cm",
                familyName = "Família Modular 14 cm",
                lengthCm = 9.0, heightCm = 19.0, thicknessCm = 14.0,
                category = BlockPieceCategory.COMPENSATOR,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B07",
                name = "Pastilha de Ajuste 4×19×14 cm",
                familyName = "Família Modular 14 cm",
                lengthCm = 4.0, heightCm = 19.0, thicknessCm = 14.0,
                category = BlockPieceCategory.PASTILHA,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B09",
                name = "Bloco Especial 54×19×14 cm",
                familyName = "Família Modular 14 cm",
                lengthCm = 54.0, heightCm = 19.0, thicknessCm = 14.0,
                category = BlockPieceCategory.SPECIAL_LENGTH,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "C01",
                name = "Canaleta U 39×19×14 cm",
                familyName = "Família Modular 14 cm",
                lengthCm = 39.0, heightCm = 19.0, thicknessCm = 14.0,
                category = BlockPieceCategory.CANALETA,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "C02",
                name = "Meia Canaleta U 19×19×14 cm",
                familyName = "Família Modular 14 cm",
                lengthCm = 19.0, heightCm = 19.0, thicknessCm = 14.0,
                category = BlockPieceCategory.CANALETA,
                isAvailableAtSite = true
            ),

            // =========================================================================
            // 3.2. BLOCOS DE 11,5 CM (Comprimento × Altura 19 cm × Largura 11,5 cm)
            // =========================================================================
            CatalogBlockPiece(
                code = "B01",
                name = "Bloco Principal 39×19×11,5 cm",
                familyName = "Família Modular 11,5 cm",
                lengthCm = 39.0, heightCm = 19.0, thicknessCm = 11.5,
                category = BlockPieceCategory.STANDARD,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B02",
                name = "Bloco Amarração Canto 31,5×19×11,5 cm",
                familyName = "Família Modular 11,5 cm",
                lengthCm = 31.5, heightCm = 19.0, thicknessCm = 11.5,
                category = BlockPieceCategory.CORNER_TIE,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B03",
                name = "Meio Bloco 19×19×11,5 cm",
                familyName = "Família Modular 11,5 cm",
                lengthCm = 19.0, heightCm = 19.0, thicknessCm = 11.5,
                category = BlockPieceCategory.HALF,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B04",
                name = "Compensador 9×19×11,5 cm",
                familyName = "Família Modular 11,5 cm",
                lengthCm = 9.0, heightCm = 19.0, thicknessCm = 11.5,
                category = BlockPieceCategory.COMPENSATOR,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B05",
                name = "Pastilha de Ajuste 4×19×11,5 cm",
                familyName = "Família Modular 11,5 cm",
                lengthCm = 4.0, heightCm = 19.0, thicknessCm = 11.5,
                category = BlockPieceCategory.PASTILHA,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "C01",
                name = "Canaleta U 39×19×11,5 cm",
                familyName = "Família Modular 11,5 cm",
                lengthCm = 39.0, heightCm = 19.0, thicknessCm = 11.5,
                category = BlockPieceCategory.CANALETA,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "C02",
                name = "Meia Canaleta U 19×19×11,5 cm",
                familyName = "Família Modular 11,5 cm",
                lengthCm = 19.0, heightCm = 19.0, thicknessCm = 11.5,
                category = BlockPieceCategory.CANALETA,
                isAvailableAtSite = true
            ),

            // =========================================================================
            // 3.3. BLOCOS DE 9 CM (Comprimento × Altura 19 cm × Largura 9 cm)
            // =========================================================================
            CatalogBlockPiece(
                code = "B01",
                name = "Bloco Principal 39×19×9 cm",
                familyName = "Família Modular 9 cm",
                lengthCm = 39.0, heightCm = 19.0, thicknessCm = 9.0,
                category = BlockPieceCategory.STANDARD,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B02",
                name = "Meio Bloco 19×19×9 cm",
                familyName = "Família Modular 9 cm",
                lengthCm = 19.0, heightCm = 19.0, thicknessCm = 9.0,
                category = BlockPieceCategory.HALF,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B03",
                name = "Compensador 9×19×9 cm",
                familyName = "Família Modular 9 cm",
                lengthCm = 9.0, heightCm = 19.0, thicknessCm = 9.0,
                category = BlockPieceCategory.COMPENSATOR,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B04",
                name = "Pastilha de Ajuste 4×19×9 cm",
                familyName = "Família Modular 9 cm",
                lengthCm = 4.0, heightCm = 19.0, thicknessCm = 9.0,
                category = BlockPieceCategory.PASTILHA,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B05",
                name = "Bloco Amarração Canto 29×19×9 cm",
                familyName = "Família Modular 9 cm",
                lengthCm = 29.0, heightCm = 19.0, thicknessCm = 9.0,
                category = BlockPieceCategory.CORNER_TIE,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "C01",
                name = "Canaleta U 39×19×9 cm",
                familyName = "Família Modular 9 cm",
                lengthCm = 39.0, heightCm = 19.0, thicknessCm = 9.0,
                category = BlockPieceCategory.CANALETA,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "C02",
                name = "Meia Canaleta U 19×19×9 cm",
                familyName = "Família Modular 9 cm",
                lengthCm = 19.0, heightCm = 19.0, thicknessCm = 9.0,
                category = BlockPieceCategory.CANALETA,
                isAvailableAtSite = true
            ),

            // =========================================================================
            // 3.4. BLOCOS DE 19 CM (Comprimento × Altura 19 cm × Largura 19 cm)
            // =========================================================================
            CatalogBlockPiece(
                code = "B01",
                name = "Bloco Principal 39×19×19 cm",
                familyName = "Família Modular 19 cm",
                lengthCm = 39.0, heightCm = 19.0, thicknessCm = 19.0,
                category = BlockPieceCategory.STANDARD,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B02",
                name = "Meio Bloco / Canto 19×19×19 cm",
                familyName = "Família Modular 19 cm",
                lengthCm = 19.0, heightCm = 19.0, thicknessCm = 19.0,
                category = BlockPieceCategory.HALF,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B03",
                name = "Compensador 9×19×19 cm",
                familyName = "Família Modular 19 cm",
                lengthCm = 9.0, heightCm = 19.0, thicknessCm = 19.0,
                category = BlockPieceCategory.COMPENSATOR,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "B04",
                name = "Pastilha de Ajuste 4×19×19 cm",
                familyName = "Família Modular 19 cm",
                lengthCm = 4.0, heightCm = 19.0, thicknessCm = 19.0,
                category = BlockPieceCategory.PASTILHA,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "C01",
                name = "Canaleta U 39×19×19 cm",
                familyName = "Família Modular 19 cm",
                lengthCm = 39.0, heightCm = 19.0, thicknessCm = 19.0,
                category = BlockPieceCategory.CANALETA,
                isAvailableAtSite = true
            ),

            // =========================================================================
            // 3.5. TIJOLO BAIANO 9×15×19 cm (Módulo 20×15 cm)
            // =========================================================================
            CatalogBlockPiece(
                code = "BA01",
                name = "Tijolo Baiano Inteiro 19×15×9 cm",
                familyName = "Tijolo Baiano 9×15×19 cm",
                lengthCm = 19.0, heightCm = 15.0, thicknessCm = 9.0,
                category = BlockPieceCategory.STANDARD,
                isAvailableAtSite = true
            ),
            CatalogBlockPiece(
                code = "BA02",
                name = "Meio Tijolo Baiano 9×15×9 cm",
                familyName = "Tijolo Baiano 9×15×19 cm",
                lengthCm = 9.0, heightCm = 15.0, thicknessCm = 9.0,
                category = BlockPieceCategory.HALF,
                isAvailableAtSite = true
            )
        )
    }

    /**
     * Cria um modelo inicial de planta baixa desenhada para demonstração imediata,
     * permitindo também que o usuário limpe a tela e desenhe do zero com o dedo.
     */
    fun createStarterDrawnPlan(): List<FreePlanWall> {
        return listOf(
            // Contorno externo (8,00m x 6,00m) padrão Família 14 cm
            FreePlanWall(
                label = "A",
                customName = "Contorno Superior",
                startX = 1.0f, startY = 1.0f, endX = 9.0f, endY = 1.0f,
                thicknessCm = 14.0, heightCm = 280.0,
                realLengthCm = 800.0, isExternal = true,
                openings = listOf(
                    FreePlanOpening(
                        type = OpeningType.WINDOW,
                        label = "J1",
                        widthCm = 160.0, heightCm = 120.0, sillHeightCm = 100.0,
                        distanceFromStartCm = 115.0
                    ),
                    FreePlanOpening(
                        type = OpeningType.WINDOW,
                        label = "J2",
                        widthCm = 120.0, heightCm = 100.0, sillHeightCm = 100.0,
                        distanceFromStartCm = 515.0
                    )
                )
            ),
            FreePlanWall(
                label = "B",
                customName = "Contorno Direito",
                startX = 9.0f, startY = 1.0f, endX = 9.0f, endY = 7.0f,
                thicknessCm = 14.0, heightCm = 280.0,
                realLengthCm = 600.0, isExternal = true,
                openings = listOf(
                    FreePlanOpening(
                        type = OpeningType.DOOR,
                        label = "P2",
                        widthCm = 80.0, heightCm = 220.0, sillHeightCm = 0.0,
                        distanceFromStartCm = 120.0
                    )
                )
            ),
            FreePlanWall(
                label = "C",
                customName = "Contorno Inferior",
                startX = 1.0f, startY = 7.0f, endX = 9.0f, endY = 7.0f,
                thicknessCm = 14.0, heightCm = 280.0,
                realLengthCm = 800.0, isExternal = true,
                openings = listOf(
                    FreePlanOpening(
                        type = OpeningType.DOOR,
                        label = "P1",
                        widthCm = 80.0, heightCm = 220.0, sillHeightCm = 0.0,
                        distanceFromStartCm = 80.0
                    ),
                    FreePlanOpening(
                        type = OpeningType.WINDOW,
                        label = "J3",
                        widthCm = 120.0, heightCm = 120.0, sillHeightCm = 100.0,
                        distanceFromStartCm = 600.0
                    )
                )
            ),
            FreePlanWall(
                label = "D",
                customName = "Contorno Esquerdo",
                startX = 1.0f, startY = 1.0f, endX = 1.0f, endY = 7.0f,
                thicknessCm = 14.0, heightCm = 280.0,
                realLengthCm = 600.0, isExternal = true
            ),
            // Paredes Internas (Divisórias com Saídas em T do Banheiro e Sala/Cozinha)
            FreePlanWall(
                label = "E",
                customName = "Divisória Central Horizontal",
                startX = 1.0f, startY = 4.0f, endX = 9.0f, endY = 4.0f,
                thicknessCm = 14.0, heightCm = 280.0,
                realLengthCm = 800.0, isExternal = false,
                openings = listOf(
                    FreePlanOpening(
                        type = OpeningType.DOOR,
                        label = "P3",
                        widthCm = 80.0, heightCm = 220.0, sillHeightCm = 0.0,
                        distanceFromStartCm = 40.0
                    ),
                    FreePlanOpening(
                        type = OpeningType.DOOR,
                        label = "P4",
                        widthCm = 80.0, heightCm = 220.0, sillHeightCm = 0.0,
                        distanceFromStartCm = 320.0
                    ),
                    FreePlanOpening(
                        type = OpeningType.DOOR,
                        label = "P5",
                        widthCm = 80.0, heightCm = 220.0, sillHeightCm = 0.0,
                        distanceFromStartCm = 640.0
                    )
                )
            ),
            FreePlanWall(
                label = "F",
                customName = "Divisória Sala / Cozinha",
                startX = 5.0f, startY = 1.0f, endX = 5.0f, endY = 4.0f,
                thicknessCm = 14.0, heightCm = 280.0,
                realLengthCm = 300.0, isExternal = false
            ),
            FreePlanWall(
                label = "G",
                customName = "Parede Esq. Banheiro",
                startX = 3.4f, startY = 4.0f, endX = 3.4f, endY = 7.0f,
                thicknessCm = 14.0, heightCm = 280.0,
                realLengthCm = 300.0, isExternal = false
            ),
            FreePlanWall(
                label = "H",
                customName = "Parede Dir. Banheiro",
                startX = 5.8f, startY = 4.0f, endX = 5.8f, endY = 7.0f,
                thicknessCm = 14.0, heightCm = 280.0,
                realLengthCm = 300.0, isExternal = false
            )
        )
    }

    data class AmarracaoBlockSpec(
        val thicknessCm: Double,
        val amarracaoLengthCm: Double,
        val shortLabel: String,
        val blockDimensionsLabel: String,
        val formulaExplanation: String
    ) {
        val tieLengthCm: Double get() = amarracaoLengthCm
        val dimensionsLabel: String get() = blockDimensionsLabel
        val formulaText: String get() = formulaExplanation
    }

    /**
     * Retorna a especificação explícita do Bloco de Amarração de Canto (L) e Saída em T
     * conforme a espessura da parede:
     * - 14 cm -> 34×19×14 cm (34 - 14 = 20 cm = metade do módulo de 40 cm)
     * - 11,5 cm -> 31,5×19×11,5 cm (31,5 - 11,5 = 20 cm = metade do módulo de 40 cm)
     * - 9 cm -> 29×19×9 cm (29 - 9 = 20 cm = metade do módulo de 40 cm)
     * - 19 cm -> 39×19×19 cm (39 - 19 = 20 cm = metade do módulo de 40 cm)
     */
    fun getAmarracaoBlockInfo(thicknessCm: Double): AmarracaoBlockSpec {
        return when {
            abs(thicknessCm - 14.0) <= 0.6 -> AmarracaoBlockSpec(
                thicknessCm = 14.0,
                amarracaoLengthCm = 34.0,
                shortLabel = "34cm",
                blockDimensionsLabel = "34×19×14 cm",
                formulaExplanation = "Bloco de Saída 34×19×14 cm (tira 14 cm da espessura e sobra 20 cm = metade do 39/40 cm)"
            )
            abs(thicknessCm - 11.5) <= 0.6 -> AmarracaoBlockSpec(
                thicknessCm = 11.5,
                amarracaoLengthCm = 31.5,
                shortLabel = "31,5cm",
                blockDimensionsLabel = "31,5×19×11,5 cm",
                formulaExplanation = "Bloco de Amarração 31,5×19×11,5 cm (tira 11,5 cm da espessura e sobra 20 cm = metade do 39/40 cm)"
            )
            abs(thicknessCm - 9.0) <= 0.6 -> AmarracaoBlockSpec(
                thicknessCm = 9.0,
                amarracaoLengthCm = 29.0,
                shortLabel = "29cm",
                blockDimensionsLabel = "29×19×9 cm",
                formulaExplanation = "Bloco de Canto/Saída T 29×19×9 cm (tira 9 cm da espessura e sobra 20 cm = metade do 39/40 cm)"
            )
            else -> AmarracaoBlockSpec(
                thicknessCm = thicknessCm,
                amarracaoLengthCm = 39.0,
                shortLabel = "39cm",
                blockDimensionsLabel = "39×19×${formatClean(thicknessCm)} cm",
                formulaExplanation = "Bloco de Amarração 39×19×${formatClean(thicknessCm)} cm"
            )
        }
    }

    /**
     * Projeta o ponto final exclusivamente em ângulo de 90° (horizontal ou vertical).
     * Não permite diagonais na Ferramenta Linha nem na Ferramenta Parede ortogonal.
     */
    fun projectOrthogonal90(
        startX: Float,
        startY: Float,
        rawEndX: Float,
        rawEndY: Float
    ): Pair<Float, Float> {
        val dx = abs(rawEndX - startX)
        val dy = abs(rawEndY - startY)
        return if (dx >= dy) {
            Pair(rawEndX, startY) // Linha/Parede estritamente Horizontal
        } else {
            Pair(startX, rawEndY) // Linha/Parede estritamente Vertical
        }
    }

    /**
     * Faz o "snap" magnético do ponto (x, y) a vértices existentes, segmentos de parede
     * ou linhas auxiliares próximas para evitar frestas e garantir conexões perfeitas.
     */
    fun snapPointToPlan(
        rawX: Float,
        rawY: Float,
        walls: List<FreePlanWall>,
        auxLines: List<AuxLine90> = emptyList(),
        snapToleranceMeters: Float = 0.55f,
        gridStepMeters: Float = 0.10f,
        snapRadiusMeters: Float = 0.68f
    ): Pair<Float, Float> {
        var bestX = (rawX / gridStepMeters).roundToInt() * gridStepMeters
        var bestY = (rawY / gridStepMeters).roundToInt() * gridStepMeters
        var minDist = snapRadiusMeters
        var snappedToEndpoint = false

        // 1. Priorizar extremidades de paredes existentes (conexão magnética forte de canto em L ou Cruz)
        for (w in walls) {
            val dStart = hypot(rawX - w.startX, rawY - w.startY)
            if (dStart < minDist) {
                minDist = dStart
                bestX = w.startX
                bestY = w.startY
                snappedToEndpoint = true
            }
            val dEnd = hypot(rawX - w.endX, rawY - w.endY)
            if (dEnd < minDist) {
                minDist = dEnd
                bestX = w.endX
                bestY = w.endY
                snappedToEndpoint = true
            }
        }

        // 2. Snap para o corpo de paredes existentes (conexão de encontro em T)
        if (!snappedToEndpoint) {
            for (w in walls) {
                if (w.isHorizontal) {
                    val minWX = min(w.startX, w.endX)
                    val maxWX = max(w.startX, w.endX)
                    if (rawX in (minWX - 0.25f)..(maxWX + 0.25f) && abs(rawY - w.startY) < snapToleranceMeters) {
                        bestY = w.startY
                        bestX = ((rawX.coerceIn(minWX, maxWX) / gridStepMeters).roundToInt() * gridStepMeters)
                    }
                } else {
                    val minWY = min(w.startY, w.endY)
                    val maxWY = max(w.startY, w.endY)
                    if (rawY in (minWY - 0.25f)..(maxWY + 0.25f) && abs(rawX - w.startX) < snapToleranceMeters) {
                        bestX = w.startX
                        bestY = ((rawY.coerceIn(minWY, maxWY) / gridStepMeters).roundToInt() * gridStepMeters)
                    }
                }
            }
        }

        // 3. Snap para linhas auxiliares de 90°
        if (!snappedToEndpoint) {
            for (line in auxLines) {
                if (line.isHorizontal && abs(rawY - line.startY) < snapToleranceMeters * 0.8f) {
                    bestY = line.startY
                } else if (!line.isHorizontal && abs(rawX - line.startX) < snapToleranceMeters * 0.8f) {
                    bestX = line.startX
                }
            }
        }

        return Pair(
            (bestX * 100f).roundToInt() / 100f,
            (bestY * 100f).roundToInt() / 100f
        )
    }

    /**
     * Solda automaticamente uma extremidade (x, y) a qualquer vértice ou parede próxima,
     * garantindo que os pontos sempre se liguem sem frestas ao desenhar com o dedo.
     */
    fun weldEndpointToExistingWalls(
        x: Float,
        y: Float,
        walls: List<FreePlanWall>,
        weldRadiusMeters: Float = 0.60f
    ): Pair<Float, Float> {
        var bestX = x
        var bestY = y
        var bestDist = weldRadiusMeters
        for (w in walls) {
            val d1 = hypot(x - w.startX, y - w.startY)
            if (d1 < bestDist) {
                bestDist = d1
                bestX = w.startX
                bestY = w.startY
            }
            val d2 = hypot(x - w.endX, y - w.endY)
            if (d2 < bestDist) {
                bestDist = d2
                bestX = w.endX
                bestY = w.endY
            }
        }
        return bestX to bestY
    }

    /**
     * Move toda a planta baixa (paredes e linhas auxiliares) para qualquer lugar do canvas.
     */
    fun translateEntirePlan(
        walls: List<FreePlanWall>,
        dxMeters: Float,
        dyMeters: Float
    ): List<FreePlanWall> {
        return walls.map { w ->
            w.copy(
                startX = ((w.startX + dxMeters) * 100f).roundToInt() / 100f,
                startY = ((w.startY + dyMeters) * 100f).roundToInt() / 100f,
                endX = ((w.endX + dxMeters) * 100f).roundToInt() / 100f,
                endY = ((w.endY + dyMeters) * 100f).roundToInt() / 100f
            )
        }
    }

    /**
     * Redimensiona toda a planta baixa em torno do seu centro geométrico.
     */
    fun scaleEntirePlan(
        walls: List<FreePlanWall>,
        scaleFactor: Float
    ): List<FreePlanWall> {
        if (walls.isEmpty()) return walls
        val factor = scaleFactor.coerceIn(0.5f, 2.0f)
        val minX = walls.minOf { min(it.startX, it.endX) }
        val maxX = walls.maxOf { max(it.startX, it.endX) }
        val minY = walls.minOf { min(it.startY, it.endY) }
        val maxY = walls.maxOf { max(it.startY, it.endY) }
        val cx = (minX + maxX) / 2f
        val cy = (minY + maxY) / 2f

        return walls.map { w ->
            val sx = ((cx + (w.startX - cx) * factor) * 10f).roundToInt() / 10f
            val sy = ((cy + (w.startY - cy) * factor) * 10f).roundToInt() / 10f
            val ex = ((cx + (w.endX - cx) * factor) * 10f).roundToInt() / 10f
            val ey = ((cy + (w.endY - cy) * factor) * 10f).roundToInt() / 10f
            val newLenCm = w.realLengthCm?.let {
                val rawScaled = it * factor
                (rawScaled / 20.0).roundToInt() * 20.0
            }
            val newOpenings = w.openings.map { op ->
                val scaledDist = ((op.distanceFromStartCm * factor) / 20.0).roundToInt() * 20.0
                op.copy(distanceFromStartCm = scaledDist.coerceAtLeast(20.0))
            }
            w.copy(
                startX = sx,
                startY = sy,
                endX = ex,
                endY = ey,
                realLengthCm = newLenCm,
                openings = newOpenings
            )
        }
    }

    /**
     * Snap inteligente e trava de canto (Anti-Overshoot):
     * - Ao chegar perto de um canto, alinhamento de ponto anterior ou encontro com outra parede,
     *   "cola" o ponto exatamente no encontro sem deixar ponta de parede passando além do canto!
     * - Só permite atravessar uma parede existente se o usuário arrastar deliberadamente > 0.60m além dela.
     */
    fun snapAndClampWallEndpoint(
        startX: Float,
        startY: Float,
        rawEndX: Float,
        rawEndY: Float,
        walls: List<FreePlanWall>,
        auxLines: List<AuxLine90> = emptyList(),
        chainVertices: List<Pair<Float, Float>> = emptyList(),
        snapTolMeters: Float = 0.65f,
        overshootClampMeters: Float = 0.65f
    ): Pair<Float, Float> {
        val (orthoX, orthoY) = projectOrthogonal90(startX, startY, rawEndX, rawEndY)
        val isHoriz = abs(orthoY - startY) < 0.001f

        var targetX = (orthoX / 0.10f).roundToInt() * 0.10f
        var targetY = (orthoY / 0.10f).roundToInt() * 0.10f

        if (isHoriz) {
            targetY = startY
            var bestXDiff = snapTolMeters

            // 1. Alinhar com vértices do traço contínuo atual (ex: fechando o 3º e 4º canto do quadrado sem tirar o dedo!)
            for ((vx, _) in chainVertices) {
                if (abs(vx - startX) > 0.25f) {
                    val d = abs(orthoX - vx)
                    if (d < bestXDiff) {
                        bestXDiff = d
                        targetX = vx
                    }
                }
            }

            // 2. Alinhar com extremidades de paredes existentes
            for (w in walls) {
                for (wx in listOf(w.startX, w.endX)) {
                    if (abs(wx - startX) > 0.25f) {
                        val d = abs(orthoX - wx)
                        if (d < bestXDiff) {
                            bestXDiff = d
                            targetX = wx
                        }
                    }
                }
            }

            // 3. Se estiver indo de encontro a uma parede vertical, travar exatamente nela para não deixar sobra passando do canto!
            val minSegX = min(startX, orthoX)
            val maxSegX = max(startX, orthoX)
            for (w in walls) {
                if (!w.isHorizontal) {
                    val wallX = w.startX
                    val minWY = min(w.startY, w.endY) - snapTolMeters
                    val maxWY = max(w.startY, w.endY) + snapTolMeters
                    val distFromStart = abs(wallX - startX)
                    if (distFromStart > 0.25f && startY in minWY..maxWY) {
                        val overshoot = if (orthoX >= startX) orthoX - wallX else wallX - orthoX
                        // Se chegou perto da parede vertical ou passou até overshootClampMeters além dela, cola na parede!
                        if (overshoot in -snapTolMeters..overshootClampMeters && wallX in (minSegX - snapTolMeters)..(maxSegX + snapTolMeters)) {
                            targetX = wallX
                            break
                        }
                    }
                }
            }
        } else {
            targetX = startX
            var bestYDiff = snapTolMeters

            // 1. Alinhar com vértices do traço contínuo atual (fechando o quadrado sem tirar o dedo!)
            for ((_, vy) in chainVertices) {
                if (abs(vy - startY) > 0.25f) {
                    val d = abs(orthoY - vy)
                    if (d < bestYDiff) {
                        bestYDiff = d
                        targetY = vy
                    }
                }
            }

            // 2. Alinhar com extremidades de paredes existentes
            for (w in walls) {
                for (wy in listOf(w.startY, w.endY)) {
                    if (abs(wy - startY) > 0.25f) {
                        val d = abs(orthoY - wy)
                        if (d < bestYDiff) {
                            bestYDiff = d
                            targetY = wy
                        }
                    }
                }
            }

            // 3. Se estiver indo de encontro a uma parede horizontal, travar exatamente nela sem deixar ponta passando!
            val minSegY = min(startY, orthoY)
            val maxSegY = max(startY, orthoY)
            for (w in walls) {
                if (w.isHorizontal) {
                    val wallY = w.startY
                    val minWX = min(w.startX, w.endX) - snapTolMeters
                    val maxWX = max(w.startX, w.endX) + snapTolMeters
                    val distFromStart = abs(wallY - startY)
                    if (distFromStart > 0.25f && startX in minWX..maxWX) {
                        val overshoot = if (orthoY >= startY) orthoY - wallY else wallY - orthoY
                        if (overshoot in -snapTolMeters..overshootClampMeters && wallY in (minSegY - snapTolMeters)..(maxSegY + snapTolMeters)) {
                            targetY = wallY
                            break
                        }
                    }
                }
            }
        }

        // Snap adicional para linhas auxiliares
        for (line in auxLines) {
            if (isHoriz && !line.isHorizontal && abs(targetX - line.startX) < snapTolMeters * 0.7f) {
                targetX = line.startX
            } else if (!isHoriz && line.isHorizontal && abs(targetY - line.startY) < snapTolMeters * 0.7f) {
                targetY = line.startY
            }
        }

        return Pair(
            (targetX * 100f).roundToInt() / 100f,
            (targetY * 100f).roundToInt() / 100f
        )
    }

    /**
     * 10.2 & 10.3. DETECÇÃO AUTOMÁTICA DE ENCONTROS ENTRE PAREDES (L, T e Cruz)
     * Analisa a geometria das paredes desenhadas e identifica todos os encontros em L, T e Cruz.
     */
    fun detectWallJunctions(walls: List<FreePlanWall>): List<DetectedWallJunction> {
        if (walls.size < 2) return emptyList()
        val tol = 0.18f
        val candidatePoints = mutableListOf<Pair<Float, Float>>()

        for (w in walls) {
            candidatePoints.add(w.startX to w.startY)
            candidatePoints.add(w.endX to w.endY)
        }

        // Também verificar interseções entre paredes horizontais e verticais (T ou Cruz no meio do vão)
        val horizWalls = walls.filter { it.isHorizontal }
        val vertWalls = walls.filter { !it.isHorizontal }
        for (h in horizWalls) {
            val hMinX = min(h.startX, h.endX)
            val hMaxX = max(h.startX, h.endX)
            val hY = h.startY
            for (v in vertWalls) {
                val vMinY = min(v.startY, v.endY)
                val vMaxY = max(v.startY, v.endY)
                val vX = v.startX
                if (vX in (hMinX - tol)..(hMaxX + tol) && hY in (vMinY - tol)..(vMaxY + tol)) {
                    candidatePoints.add(vX to hY)
                }
            }
        }

        // Agrupar pontos próximos (clusterização espacial)
        val uniqueClusters = mutableListOf<Pair<Float, Float>>()
        for (pt in candidatePoints) {
            if (uniqueClusters.none { hypot(it.first - pt.first, it.second - pt.second) < tol }) {
                uniqueClusters.add(pt)
            }
        }

        val junctions = mutableListOf<DetectedWallJunction>()
        val globalMinX = walls.minOfOrNull { min(it.startX, it.endX) } ?: 0f
        val globalMaxX = walls.maxOfOrNull { max(it.startX, it.endX) } ?: 10f
        val globalMinY = walls.minOfOrNull { min(it.startY, it.endY) } ?: 0f
        val globalMaxY = walls.maxOfOrNull { max(it.startY, it.endY) } ?: 10f

        for ((px, py) in uniqueClusters) {
            var branchCount = 0
            val connectedWalls = mutableListOf<FreePlanWall>()

            for (w in walls) {
                val minX = min(w.startX, w.endX)
                val maxX = max(w.startX, w.endX)
                val minY = min(w.startY, w.endY)
                val maxY = max(w.startY, w.endY)

                val touchesWall = if (w.isHorizontal) {
                    abs(py - w.startY) <= tol && px in (minX - tol)..(maxX + tol)
                } else {
                    abs(px - w.startX) <= tol && py in (minY - tol)..(maxY + tol)
                }

                if (touchesWall) {
                    connectedWalls.add(w)
                    val isAtStart = hypot(px - w.startX, py - w.startY) <= tol * 1.2f
                    val isAtEnd = hypot(px - w.endX, py - w.endY) <= tol * 1.2f
                    branchCount += if (isAtStart || isAtEnd) 1 else 2 // Se cruza pelo meio da parede, conta 2 ramos opostos
                }
            }

            val hasHoriz = connectedWalls.any { it.isHorizontal }
            val hasVert = connectedWalls.any { !it.isHorizontal }

            if (connectedWalls.size >= 2 && hasHoriz && hasVert) {
                val kind = when {
                    branchCount >= 4 -> JunctionKind.CROSS_X
                    branchCount == 3 -> JunctionKind.JUNCTION_T
                    else -> JunctionKind.CORNER_L
                }
                val isExt = abs(px - globalMinX) <= tol || abs(px - globalMaxX) <= tol ||
                        abs(py - globalMinY) <= tol || abs(py - globalMaxY) <= tol

                junctions.add(
                    DetectedWallJunction(
                        x = px,
                        y = py,
                        kind = kind,
                        connectedWallIds = connectedWalls.map { it.id }.distinct(),
                        isExternalCorner = isExt
                    )
                )
            }
        }

        return junctions
    }

    /**
     * 10.1. RECONHECIMENTO AUTOMÁTICO DOS AMBIENTES (CÔMODOS FECHADOS)
     * Identifica regiões fechadas delimitadas pelas paredes horizontais e verticais e sugere nomes.
     */
    fun detectClosedRooms(
        walls: List<FreePlanWall>,
        customRoomNames: Map<String, String> = emptyMap()
    ): List<DetectedRoom> {
        if (walls.size < 4) return emptyList()

        val xCoords = sortedSetOf<Float>()
        val yCoords = sortedSetOf<Float>()
        val snapTol = 0.15f

        fun addUniqueCoord(set: MutableSet<Float>, value: Float) {
            if (set.none { abs(it - value) < snapTol }) {
                set.add(value)
            }
        }

        for (w in walls) {
            addUniqueCoord(xCoords, w.startX)
            addUniqueCoord(xCoords, w.endX)
            addUniqueCoord(yCoords, w.startY)
            addUniqueCoord(yCoords, w.endY)
        }

        val xs = xCoords.sorted()
        val ys = yCoords.sorted()
        if (xs.size < 2 || ys.size < 2) return emptyList()

        val cols = xs.size - 1
        val rows = ys.size - 1

        // Verifica se existe parede separando duas células adjacentes
        fun hasHorizontalWallBetween(x1: Float, x2: Float, y: Float): Pair<Boolean, String?> {
            val midX = (x1 + x2) / 2f
            for (w in walls) {
                if (w.isHorizontal && abs(w.startY - y) < snapTol * 1.4f) {
                    val minWX = min(w.startX, w.endX) - snapTol
                    val maxWX = max(w.startX, w.endX) + snapTol
                    if (midX in minWX..maxWX) return true to w.label
                }
            }
            return false to null
        }

        fun hasVerticalWallBetween(x: Float, y1: Float, y2: Float): Pair<Boolean, String?> {
            val midY = (y1 + y2) / 2f
            for (w in walls) {
                if (!w.isHorizontal && abs(w.startX - x) < snapTol * 1.4f) {
                    val minWY = min(w.startY, w.endY) - snapTol
                    val maxWY = max(w.startY, w.endY) + snapTol
                    if (midY in minWY..maxWY) return true to w.label
                }
            }
            return false to null
        }

        val visited = Array(rows) { BooleanArray(cols) }
        val rooms = mutableListOf<DetectedRoom>()
        var roomIndex = 1

        val roomSuggestionsOrder = listOf(
            "Sala",
            "Cozinha",
            "Quarto 1",
            "Banheiro",
            "Quarto 2",
            "Lavanderia",
            "Corredor",
            "Suíte"
        )

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (visited[r][c]) continue

                // Flood-fill (BFS) para encontrar componente conexa de células
                val queue = ArrayDeque<Pair<Int, Int>>()
                val cells = mutableListOf<Pair<Int, Int>>()
                val boundaryLabels = mutableSetOf<String>()
                var touchesExterior = false

                queue.add(r to c)
                visited[r][c] = true

                while (queue.isNotEmpty()) {
                    val (cr, cc) = queue.removeFirst()
                    cells.add(cr to cc)

                    val xLeft = xs[cc]
                    val xRight = xs[cc + 1]
                    val yTop = ys[cr]
                    val yBottom = ys[cr + 1]

                    // Topo (cr - 1)
                    val (topBlocked, topLbl) = hasHorizontalWallBetween(xLeft, xRight, yTop)
                    if (topBlocked) {
                        if (topLbl != null) boundaryLabels.add(topLbl)
                    } else {
                        if (cr == 0) touchesExterior = true
                        else if (!visited[cr - 1][cc]) {
                            visited[cr - 1][cc] = true
                            queue.add((cr - 1) to cc)
                        }
                    }

                    // Base (cr + 1)
                    val (botBlocked, botLbl) = hasHorizontalWallBetween(xLeft, xRight, yBottom)
                    if (botBlocked) {
                        if (botLbl != null) boundaryLabels.add(botLbl)
                    } else {
                        if (cr == rows - 1) touchesExterior = true
                        else if (!visited[cr + 1][cc]) {
                            visited[cr + 1][cc] = true
                            queue.add((cr + 1) to cc)
                        }
                    }

                    // Esquerda (cc - 1)
                    val (leftBlocked, leftLbl) = hasVerticalWallBetween(xLeft, yTop, yBottom)
                    if (leftBlocked) {
                        if (leftLbl != null) boundaryLabels.add(leftLbl)
                    } else {
                        if (cc == 0) touchesExterior = true
                        else if (!visited[cr][cc - 1]) {
                            visited[cr][cc - 1] = true
                            queue.add(cr to (cc - 1))
                        }
                    }

                    // Direita (cc + 1)
                    val (rightBlocked, rightLbl) = hasVerticalWallBetween(xRight, yTop, yBottom)
                    if (rightBlocked) {
                        if (rightLbl != null) boundaryLabels.add(rightLbl)
                    } else {
                        if (cc == cols - 1) touchesExterior = true
                        else if (!visited[cr][cc + 1]) {
                            visited[cr][cc + 1] = true
                            queue.add(cr to (cc + 1))
                        }
                    }
                }

                if (!touchesExterior && cells.isNotEmpty()) {
                    var minRX = Float.MAX_VALUE
                    var minRY = Float.MAX_VALUE
                    var maxRX = -Float.MAX_VALUE
                    var maxRY = -Float.MAX_VALUE
                    var totalArea = 0.0

                    for ((cr, cc) in cells) {
                        val x1 = xs[cc]
                        val x2 = xs[cc + 1]
                        val y1 = ys[cr]
                        val y2 = ys[cr + 1]
                        minRX = min(minRX, x1)
                        minRY = min(minRY, y1)
                        maxRX = max(maxRX, x2)
                        maxRY = max(maxRY, y2)
                        totalArea += (x2 - x1) * (y2 - y1)
                    }

                    if (totalArea >= 0.8) {
                        val roomId = "ROOM_${(minRX * 10).roundToInt()}_${(minRY * 10).roundToInt()}"
                        val suggested = when {
                            totalArea <= 5.2 -> "Banheiro"
                            totalArea > 14.0 -> "Sala"
                            else -> roomSuggestionsOrder.getOrElse(roomIndex - 1) { "Ambiente $roomIndex" }
                        }

                        rooms.add(
                            DetectedRoom(
                                id = roomId,
                                defaultIndex = roomIndex,
                                suggestedType = suggested,
                                customName = customRoomNames[roomId],
                                centerX = (minRX + maxRX) / 2f,
                                centerY = (minRY + maxRY) / 2f,
                                minX = minRX,
                                minY = minRY,
                                maxX = maxRX,
                                maxY = maxRY,
                                areaM2 = (totalArea * 100.0).roundToInt() / 100.0,
                                boundingWallLabels = boundaryLabels.sorted()
                            )
                        )
                        roomIndex++
                    }
                }
            }
        }

        return rooms
    }

    /**
     * 6. MÓDULO DE COTAGEM: APLICAR MEDIDA REAL E RECALCULAR CONEXÕES ENTRE PAREDES
     * Preserva ortogonalidade, conexões de canto/T e detecta conflitos geométricos.
     */
    data class DimensionUpdateOutcome(
        val updatedWalls: List<FreePlanWall>,
        val hasConflict: Boolean,
        val message: String
    )

    fun applyWallRealDimension(
        walls: List<FreePlanWall>,
        targetWallId: String,
        newLengthCm: Double,
        rule: DimensionAdjustRule
    ): DimensionUpdateOutcome {
        val target = walls.find { it.id == targetWallId }
            ?: return DimensionUpdateOutcome(walls, true, "Parede selecionada não encontrada.")

        // Arredondamento modular obrigatório ABNT: sempre fecha em 100% da modulação sem sobras de 74, 76, 12, 2 cm
        val modularLengthCm = TilingEngine.snapToModularWallLength(newLengthCm)

        // Verificar se há aberturas que ficariam fora do novo comprimento
        val maxOpeningEnd = target.openings.maxOfOrNull { it.distanceFromStartCm + it.widthCm } ?: 0.0
        if (maxOpeningEnd > modularLengthCm + 0.1) {
            return DimensionUpdateOutcome(
                walls,
                true,
                "Conflito geométrico: A parede possui abertura posicionada até ${maxOpeningEnd.roundToInt()} cm, maior que a nova medida modular (${modularLengthCm.roundToInt()} cm)."
            )
        }

        val newLengthM = (modularLengthCm / 100.0).toFloat()
        val tol = 0.18f

        // Determinar extremidade esquerda/superior como âncora e mover extremidade direita/inferior
        val isHoriz = target.isHorizontal
        val startIsMin = if (isHoriz) target.startX <= target.endX else target.startY <= target.endY

        val anchorX = if (startIsMin) target.startX else target.endX
        val anchorY = if (startIsMin) target.startY else target.endY
        val oldMovingX = if (startIsMin) target.endX else target.startX
        val oldMovingY = if (startIsMin) target.endY else target.startY

        val newMovingX = if (isHoriz) anchorX + newLengthM else oldMovingX
        val newMovingY = if (!isHoriz) anchorY + newLengthM else oldMovingY

        val deltaX = newMovingX - oldMovingX
        val deltaY = newMovingY - oldMovingY

        // Se a regra for KEEP_FIXED_WALLS, verificar se alguma parede paralela no mesmo alinhamento está travada com medida incompatível
        if (rule == DimensionAdjustRule.KEEP_FIXED_WALLS) {
            val conflictingLocked = walls.firstOrNull { other ->
                val otherMin = if (isHoriz) min(other.startX, other.endX) else min(other.startY, other.endY)
                val otherMax = if (isHoriz) max(other.startX, other.endX) else max(other.startY, other.endY)
                other.id != target.id &&
                        other.isFixedLocked &&
                        other.isHorizontal == target.isHorizontal &&
                        other.realLengthCm != null &&
                        abs(otherMin - (if (isHoriz) anchorX else anchorY)) < tol &&
                        abs(other.realLengthCm - newLengthCm) > 1.0 &&
                        abs(otherMax - (if (isHoriz) oldMovingX else oldMovingY)) < tol
            }

            if (conflictingLocked != null) {
                return DimensionUpdateOutcome(
                    updatedWalls = walls,
                    hasConflict = true,
                    message = "Conflito detectado: A ${conflictingLocked.displayName} está FIXADA com ${(conflictingLocked.realLengthCm ?: 0.0) / 100.0} m no mesmo eixo. Destrave-a ou use o modo 'Ajustar ambiente'."
                )
            }
        }

        val updatedList = walls.map { w ->
            if (w.id == target.id) {
                if (startIsMin) {
                    w.copy(
                        endX = newMovingX,
                        endY = newMovingY,
                        realLengthCm = modularLengthCm
                    )
                } else {
                    w.copy(
                        startX = newMovingX,
                        startY = newMovingY,
                        realLengthCm = modularLengthCm
                    )
                }
            } else {
                when (rule) {
                    DimensionAdjustRule.ADJUST_CONNECTED, DimensionAdjustRule.AUTO_ADJUST_ROOM -> {
                        var sx = w.startX
                        var sy = w.startY
                        var ex = w.endX
                        var ey = w.endY
                        var updatedRealLen = w.realLengthCm

                        // Se a parede for perpendicular e estiver na coordenada que se moveu, transladá-la mantendo a conexão
                        if (!w.isHorizontal && isHoriz) {
                            if (abs(w.startX - oldMovingX) <= tol && abs(w.endX - oldMovingX) <= tol) {
                                sx += deltaX
                                ex += deltaX
                            }
                        } else if (w.isHorizontal && !isHoriz) {
                            if (abs(w.startY - oldMovingY) <= tol && abs(w.endY - oldMovingY) <= tol) {
                                sy += deltaY
                                ey += deltaY
                            }
                        }

                        // Se for parede paralela do mesmo ambiente que terminava no mesmo alinhamento, manter conexão do canto
                        if (w.isHorizontal == isHoriz && (!w.isFixedLocked || rule == DimensionAdjustRule.ADJUST_CONNECTED)) {
                            if (isHoriz) {
                                if (abs(w.endX - oldMovingX) <= tol && w.endX >= w.startX) {
                                    ex = newMovingX
                                    if (rule == DimensionAdjustRule.AUTO_ADJUST_ROOM || updatedRealLen != null) {
                                        updatedRealLen = (abs(ex - sx) * 100.0).roundToInt().toDouble()
                                    }
                                } else if (abs(w.startX - oldMovingX) <= tol && w.startX > w.endX) {
                                    sx = newMovingX
                                    if (rule == DimensionAdjustRule.AUTO_ADJUST_ROOM || updatedRealLen != null) {
                                        updatedRealLen = (abs(ex - sx) * 100.0).roundToInt().toDouble()
                                    }
                                }
                            } else {
                                if (abs(w.endY - oldMovingY) <= tol && w.endY >= w.startY) {
                                    ey = newMovingY
                                    if (rule == DimensionAdjustRule.AUTO_ADJUST_ROOM || updatedRealLen != null) {
                                        updatedRealLen = (abs(ey - sy) * 100.0).roundToInt().toDouble()
                                    }
                                } else if (abs(w.startY - oldMovingY) <= tol && w.startY > w.endY) {
                                    sy = newMovingY
                                    if (rule == DimensionAdjustRule.AUTO_ADJUST_ROOM || updatedRealLen != null) {
                                        updatedRealLen = (abs(ey - sy) * 100.0).roundToInt().toDouble()
                                    }
                                }
                            }
                        }

                        w.copy(startX = sx, startY = sy, endX = ex, endY = ey, realLengthCm = updatedRealLen)
                    }

                    DimensionAdjustRule.KEEP_FIXED_WALLS -> {
                        if (w.isFixedLocked) {
                            w
                        } else {
                            var sx = w.startX
                            var sy = w.startY
                            var ex = w.endX
                            var ey = w.endY
                            if (hypot(sx - oldMovingX, sy - oldMovingY) <= tol) {
                                sx += deltaX
                                sy += deltaY
                            }
                            if (hypot(ex - oldMovingX, ey - oldMovingY) <= tol) {
                                ex += deltaX
                                ey += deltaY
                            }
                            w.copy(startX = sx, startY = sy, endX = ex, endY = ey)
                        }
                    }
                }
            }
        }

        return DimensionUpdateOutcome(
            updatedWalls = updatedList,
            hasConflict = false,
            message = "Medida de ${String.format(Locale.US, "%.2f", newLengthCm / 100.0)} m aplicada na ${target.displayName} com conexões recalculadas."
        )
    }

    /**
     * 4. MÓDULO DE PAGINAÇÃO AUTOMÁTICA COM REGRA DE SOBRA MÁXIMA <= 1 CM
     * Calcula a paginação de uma parede desenhada considerando:
     * - Apenas os blocos marcados como disponíveis na obra para aquela espessura.
     * - Encontros em L, T e Cruz detectados na planta.
     * - Aberturas de portas e janelas + canaletas para vergas/contravergas/cintas.
     * - Distinção explícita entre Folga de Fechamento, Corte de Ajuste, Desperdício e Junta de Argamassa.
     */
    fun paginateFreePlanWall(
        wall: FreePlanWall,
        allWalls: List<FreePlanWall>,
        catalogPieces: List<CatalogBlockPiece>,
        jointConfig: JointConfig,
        bondType: BondType = BondType.RUNNING_HALF
    ): FreePlanWallPagination {
        val thickness = wall.thicknessCm
        val compatiblePieces = catalogPieces.filter {
            abs(it.thicknessCm - thickness) < 0.6 && it.isAvailableAtSite
        }

        val masonryPieces = compatiblePieces.filter { !it.isCanaleta }.sortedByDescending { it.lengthCm }
        val canaletaPieces = compatiblePieces.filter { it.isCanaleta }.sortedByDescending { it.lengthCm }

        // Bloco principal (padrão 39 cm ou o maior bloco padrão selecionado)
        val primaryCatalogPiece = masonryPieces.firstOrNull { it.category == BlockPieceCategory.STANDARD }
            ?: masonryPieces.firstOrNull()
            ?: CatalogBlockPiece(
                code = "B01",
                name = "Bloco Principal 39×19×${thickness} cm",
                familyName = "Família $thickness cm",
                lengthCm = 39.0, heightCm = 19.0, thicknessCm = thickness,
                category = BlockPieceCategory.STANDARD
            )

        val halfCatalogPiece = masonryPieces.firstOrNull { it.category == BlockPieceCategory.HALF }

        val mainBlockModel = BlockModel(
            id = 1,
            name = "${primaryCatalogPiece.code} - ${primaryCatalogPiece.name}",
            material = "Alvenaria Modular",
            lengthCm = primaryCatalogPiece.lengthCm,
            heightCm = primaryCatalogPiece.heightCm,
            thicknessCm = thickness,
            halfBlockLengthCm = halfCatalogPiece?.lengthCm ?: 19.0
        )

        val availableBlockModels = masonryPieces.mapIndexed { idx, cp ->
            BlockModel(
                id = (idx + 1).toLong(),
                name = "${cp.code}: ${cp.name}",
                material = "Modular",
                lengthCm = cp.lengthCm,
                heightCm = cp.heightCm,
                thicknessCm = cp.thicknessCm,
                halfBlockLengthCm = cp.lengthCm
            )
        }

        // Detectar encontros (L, T, Cruz) desta parede com as demais paredes da planta
        val allJunctions = detectWallJunctions(allWalls)
        val wallJunctions = allJunctions.filter { it.connectedWallIds.contains(wall.id) }
        val hasCornerL = wallJunctions.isNotEmpty()

        // Converter encontros em T e Cruz ao longo da parede em WallIntersections em cm (ex: Paredes do Banheiro G e H)
        val wallIntersections = mutableListOf<WallIntersection>()
        val wallMinX = min(wall.startX, wall.endX)
        val wallMinY = min(wall.startY, wall.endY)
        val scaleFactor = if (wall.visualLengthMeters > 0.05) {
            wall.effectiveLengthCm / wall.visualLengthMeters
        } else 100.0

        for (junc in wallJunctions) {
            val distM = if (wall.isHorizontal) abs(junc.x - wallMinX) else abs(junc.y - wallMinY)
            val posCm = (distM * scaleFactor).coerceIn(0.0, wall.effectiveLengthCm)
            val otherWalls = allWalls.filter { it.id in junc.connectedWallIds && it.id != wall.id }
            val otherNames = otherWalls.joinToString("/") { "Parede ${it.label}" }
            val labelName = if (otherNames.isNotEmpty()) {
                "Saída ${junc.kind.code} ($otherNames)"
            } else {
                "${junc.kind.label} (${junc.kind.code})"
            }
            wallIntersections.add(
                WallIntersection(
                    xCm = posCm,
                    thicknessCm = otherWalls.firstOrNull()?.thicknessCm ?: thickness,
                    name = labelName
                )
            )
        }

        val wallModel = WallModel(
            projectName = "Planta Baixa Executiva",
            wallName = wall.displayName,
            lengthCm = wall.effectiveLengthCm,
            heightCm = wall.heightCm,
            thicknessCm = thickness,
            shape = if (hasCornerL) WallShape.L_SHAPED else WallShape.RECTANGULAR,
            notes = if (hasCornerL) "canto L" else "",
            horizontalJointCm = jointConfig.horizontalCm,
            verticalJointCm = jointConfig.verticalCm,
            bondTypeName = bondType.name,
            wastePercentage = 5.0
        )

        val openingModels = wall.openings.map { it.toOpeningModel() }

        val tilingResult = TilingEngine.calculate(
            wall = wallModel,
            block = mainBlockModel,
            jointConfig = jointConfig,
            bondType = bondType,
            openings = openingModels,
            intersections = wallIntersections,
            availableBlocks = availableBlockModels
        )

        // Mapa de comprimento -> CatalogBlockPiece para codificação B01, B02, C01...
        val codeMap = mutableMapOf<Double, CatalogBlockPiece>()
        for (cp in masonryPieces) {
            codeMap[round1(cp.lengthCm)] = cp
        }

        val closingAnalysis = evaluateClosingRuleAndCodes(
            wall = wall,
            tilingResult = tilingResult,
            masonryPieces = masonryPieces,
            canaletaPieces = canaletaPieces,
            jointV = jointConfig.verticalCm
        )

        return FreePlanWallPagination(
            wall = wall,
            tilingResult = tilingResult,
            closingAnalysis = closingAnalysis,
            blockCodeMap = codeMap,
            canaletaPiece = canaletaPieces.firstOrNull(),
            junctionsAtWall = wallJunctions
        )
    }

    /**
     * Avalia tecnicamente o fechamento da parede segundo a Regra de Sobra Máxima <= 1 cm (Seção 4):
     * - Folga de fechamento (cm)
     * - Corte de ajuste (cm)
     * - Desperdício estimado (cm)
     * - Junta de argamassa (cm)
     * - Contagem por código (B01, B02..., C01...)
     */
    private fun evaluateClosingRuleAndCodes(
        wall: FreePlanWall,
        tilingResult: TilingResult,
        masonryPieces: List<CatalogBlockPiece>,
        canaletaPieces: List<CatalogBlockPiece>,
        jointV: Double
    ): ClosingAnalysis {
        val wallLen = wall.effectiveLengthCm
        val baseLengths = (masonryPieces.map { it.lengthCm } + listOfNotNull(
            if (abs(wall.thicknessCm - 11.5) < 0.5) 6.5 else null,
            if (abs(wall.thicknessCm - 14.0) < 0.5) 4.0 else null,
            if (abs(wall.thicknessCm - 9.0) < 0.5) 9.0 else null
        )).distinct()
        // Blocos no final de ponta livre podem absorver a junta de borda sem corte comercial (ex: 39+1=40, 19+1=20, 9+1=10, 4+1=5)
        val availableLengths = (baseLengths + baseLengths.map { it + jointV }).distinct()

        // Testar fechamento combinatório na primeira fiada corrida
        val firstCourse = tilingResult.courses.firstOrNull().orEmpty()
        val cutBlocksInFirst = firstCourse.filter { piece ->
            piece.pieceType == PieceType.CUT || availableLengths.none { abs(it - piece.lengthCm) <= 0.45 }
        }

        var minResidualGapCm = Double.MAX_VALUE
        val targetWithJoint = wallLen + jointV
        val modules = availableLengths.map { (it + jointV) }.filter { it > 1.0 }

        if (modules.isNotEmpty()) {
            val scale = 2
            val maxUnits = (targetWithJoint * scale).roundToInt()
            val reachable = BooleanArray(maxUnits + 25)
            reachable[0] = true
            for (u in 0..maxUnits) {
                if (!reachable[u]) continue
                for (m in modules) {
                    val step = (m * scale).roundToInt()
                    if (u + step < reachable.size) {
                        reachable[u + step] = true
                    }
                }
            }
            for (u in 1 until reachable.size) {
                if (reachable[u]) {
                    val sumModules = u.toDouble() / scale
                    val gapWithEndJoint = abs(wallLen - sumModules)
                    val gapInternalOnly = abs(wallLen - (sumModules - jointV))
                    val bestLocalGap = min(gapWithEndJoint, gapInternalOnly)
                    if (bestLocalGap < minResidualGapCm) {
                        minResidualGapCm = bestLocalGap
                    }
                }
            }
        } else {
            minResidualGapCm = 0.0
        }

        val allBlocks = tilingResult.allBlocks
        val nonCatalogBlocks = allBlocks.filter { blk ->
            blk.countsInQuantity && (blk.pieceType == PieceType.CUT || availableLengths.none { abs(it - blk.lengthCm) <= 0.45 })
        }

        val hasExactFit = minResidualGapCm <= 1.0 && nonCatalogBlocks.isEmpty()
        val closingGap = if (minResidualGapCm <= 1.0) round1(minResidualGapCm) else round1(minResidualGapCm % 10.0)

        val adjustmentCutLen = if (cutBlocksInFirst.isNotEmpty()) {
            round1(cutBlocksInFirst.first().lengthCm)
        } else if (nonCatalogBlocks.isNotEmpty()) {
            round1(nonCatalogBlocks.first().lengthCm)
        } else {
            0.0
        }

        var totalWasteCm = 0.0
        for (cutBlk in nonCatalogBlocks) {
            val sourceLen = availableLengths.filter { it >= cutBlk.lengthCm }.minOrNull()
                ?: availableLengths.maxOrNull() ?: 39.0
            totalWasteCm += max(0.0, sourceLen - cutBlk.lengthCm)
        }

        val codeSummary = linkedMapOf<String, Int>()
        var canaletaTotal = 0
        val primaryCanaleta = canaletaPieces.firstOrNull()

        for (blk in allBlocks) {
            if (blk.pieceType == PieceType.LINTEL && primaryCanaleta != null) {
                val key = "${primaryCanaleta.code} (${primaryCanaleta.dimensionsFormatted()})"
                codeSummary[key] = (codeSummary[key] ?: 0) + 1
                canaletaTotal++
            } else {
                val matched = masonryPieces.minByOrNull { abs(it.lengthCm - blk.lengthCm) }
                if (matched != null && abs(matched.lengthCm - blk.lengthCm) <= (jointV + 0.15)) {
                    val key = "${matched.code} (${matched.dimensionsFormatted()})"
                    codeSummary[key] = (codeSummary[key] ?: 0) + 1
                } else {
                    val key = "Bloco ${formatClean(blk.lengthCm)}×${formatClean(blk.heightCm)}×${formatClean(blk.thicknessCm)} cm"
                    codeSummary[key] = (codeSummary[key] ?: 0) + 1
                }
            }
        }

        if (wall.includeTopCintaCanaleta && primaryCanaleta != null) {
            val cintaUnits = ceil(wallLen / (primaryCanaleta.lengthCm + jointV)).toInt()
            val key = "${primaryCanaleta.code} (${primaryCanaleta.dimensionsFormatted()}) [Cinta]"
            codeSummary[key] = (codeSummary[key] ?: 0) + cintaUnits
            canaletaTotal += cintaUnits
        }

        val alternatives = mutableListOf<String>()
        if (!hasExactFit) {
            val nextMult10 = ceil(wallLen / 10.0) * 10.0
            val prevMult10 = (wallLen / 10.0).toInt() * 10.0
            if (abs(nextMult10 - wallLen) > 0.1) {
                alternatives.add("Ajustar comprimento da parede para ${String.format(Locale.US, "%.2f", nextMult10 / 100.0)} m ou ${String.format(Locale.US, "%.2f", prevMult10 / 100.0)} m (múltiplo modular sem corte).")
            }
            if (masonryPieces.none { abs(it.lengthCm - 9.0) < 0.5 || abs(it.lengthCm - 4.0) < 0.5 }) {
                alternatives.add("Habilitar os blocos compensadores de 9 cm e pastilha de 4 cm no cadastro da obra.")
            }
        }

        val statusTitle = if (hasExactFit) {
            "Fechamento 100% Modular (Folga ${closingGap} cm ≤ 1 cm)"
        } else {
            "Requer Peça de Ajuste de ${adjustmentCutLen} cm"
        }

        val explanation = if (hasExactFit) {
            "A parede de ${round1(wallLen)} cm fecha dentro da tolerância máxima de 1 cm utilizando exclusivamente os blocos comerciais selecionados na obra, com junta vertical de ${round1(jointV)} cm."
        } else {
            "Não há combinação exata apenas com blocos inteiros para ${round1(wallLen)} cm. O sistema calculou peça de ajuste de ${adjustmentCutLen} cm (desperdício estimado de ${round1(totalWasteCm)} cm na parede)."
        }

        return ClosingAnalysis(
            wallLabel = wall.label,
            wallLengthCm = wallLen,
            closingGapCm = closingGap,
            adjustmentCutCm = adjustmentCutLen,
            estimatedWasteCm = round1(totalWasteCm),
            mortarJointCm = jointV,
            hasExactCommercialFit = hasExactFit,
            statusTitle = statusTitle,
            technicalExplanation = explanation,
            recommendedAlternatives = alternatives,
            pieceCodeSummary = codeSummary,
            canaletaCount = canaletaTotal
        )
    }

    /**
     * 8.A & 8.D. GERAÇÃO DO SVG VETORIAL DA PLANTA BAIXA COMPLETA
     * Exporta paredes externas/internas, espessuras, portas, janelas, cotas, encontros L/T/Cruz com blocos de amarração explícitos.
     */
    fun generateFloorPlanSvg(
        projectName: String,
        walls: List<FreePlanWall>,
        auxLines: List<AuxLine90>,
        rooms: List<DetectedRoom>,
        junctions: List<DetectedWallJunction>
    ): String {
        if (walls.isEmpty()) return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"800\" height=\"600\"></svg>"

        val minX = walls.minOf { min(it.startX, it.endX) } - 1.2f
        val maxX = walls.maxOf { max(it.startX, it.endX) } + 1.2f
        val minY = walls.minOf { min(it.startY, it.endY) } - 1.2f
        val maxY = walls.maxOf { max(it.startY, it.endY) } + 1.5f

        val scale = 120f // 120 px por metro
        val svgW = ((maxX - minX) * scale).roundToInt().coerceAtLeast(900)
        val svgH = ((maxY - minY) * scale).roundToInt().coerceAtLeast(700)

        fun tx(x: Float) = ((x - minX) * scale)
        fun ty(y: Float) = ((y - minY) * scale)

        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $svgW $svgH" width="100%" height="100%">""")
        sb.appendLine("""  <rect width="100%" height="100%" fill="#0D1117"/>""")

        // Grid técnico
        sb.appendLine("""  <g stroke="#1E293B" stroke-width="1">""")
        var gx = 0
        while (gx < svgW) {
            sb.appendLine("""    <line x1="$gx" y1="0" x2="$gx" y2="$svgH"/>""")
            gx += 60
        }
        var gy = 0
        while (gy < svgH) {
            sb.appendLine("""    <line x1="0" y1="$gy" x2="$svgW" y2="$gy"/>""")
            gy += 60
        }
        sb.appendLine("""  </g>""")

        // Ambientes reconhecidos
        for (room in rooms) {
            val rx = tx(room.minX)
            val ry = ty(room.minY)
            val rw = (room.maxX - room.minX) * scale
            val rh = (room.maxY - room.minY) * scale
            val cx = tx(room.centerX)
            val cy = ty(room.centerY)
            sb.appendLine("""  <rect x="$rx" y="$ry" width="$rw" height="$rh" fill="#0284C7" fill-opacity="0.08" stroke="#0284C7" stroke-opacity="0.3" stroke-dasharray="6,4"/>""")
            sb.appendLine("""  <text x="$cx" y="$cy" fill="#38BDF8" font-family="monospace" font-size="15" font-weight="bold" text-anchor="middle">${escapeXml(room.displayTitle)}</text>""")
            sb.appendLine("""  <text x="$cx" y="${cy + 18}" fill="#94A3B8" font-family="monospace" font-size="12" text-anchor="middle">${room.areaM2} m² (Paredes: ${room.boundingWallLabels.joinToString(",")})</text>""")
        }

        // Linhas auxiliares 90°
        for (aux in auxLines) {
            sb.appendLine("""  <line x1="${tx(aux.startX)}" y1="${ty(aux.startY)}" x2="${tx(aux.endX)}" y2="${ty(aux.endY)}" stroke="#22D3EE" stroke-width="1.5" stroke-dasharray="8,6"/>""")
        }

        // Paredes com espessura proporcional e aberturas
        for (w in walls) {
            val x1 = tx(w.startX)
            val y1 = ty(w.startY)
            val x2 = tx(w.endX)
            val y2 = ty(w.endY)
            val strokePx = ((w.thicknessCm / 100.0) * scale * 1.4).toFloat().coerceIn(8f, 26f)
            val wallColor = if (w.isExternal) "#E2E8F0" else "#94A3B8"
            val amarraSpec = getAmarracaoBlockInfo(w.thicknessCm)

            sb.appendLine("""  <line x1="$x1" y1="$y1" x2="$x2" y2="$y2" stroke="$wallColor" stroke-width="$strokePx" stroke-linecap="square"/>""")

            val wallLenCm = w.effectiveLengthCm.coerceAtLeast(1.0)
            for (op in w.openings) {
                val startRatio = (op.distanceFromStartCm / wallLenCm).toFloat().coerceIn(0f, 1f)
                val endRatio = ((op.distanceFromStartCm + op.widthCm) / wallLenCm).toFloat().coerceIn(0f, 1f)
                val ox1 = x1 + (x2 - x1) * startRatio
                val oy1 = y1 + (y2 - y1) * startRatio
                val ox2 = x1 + (x2 - x1) * endRatio
                val oy2 = y1 + (y2 - y1) * endRatio
                val opColor = if (op.type == OpeningType.DOOR) "#FB923C" else "#38BDF8"
                sb.appendLine("""  <line x1="$ox1" y1="$oy1" x2="$ox2" y2="$oy2" stroke="#0D1117" stroke-width="${strokePx + 2f}"/>""")
                sb.appendLine("""  <line x1="$ox1" y1="$oy1" x2="$ox2" y2="$oy2" stroke="$opColor" stroke-width="4"/>""")
                sb.appendLine("""  <text x="${(ox1 + ox2) / 2f}" y="${(oy1 + oy2) / 2f - 8f}" fill="$opColor" font-family="monospace" font-size="11" font-weight="bold" text-anchor="middle">${op.label} (${op.widthCm.roundToInt()}×${op.heightCm.roundToInt()})</text>""")
            }

            // Cota e etiqueta da parede + bloco de amarração explícito
            val mx = (x1 + x2) / 2f
            val my = (y1 + y2) / 2f
            val lenStr = String.format(Locale.US, "%.2f m", w.effectiveLengthCm / 100.0)
            val offsetCota = if (w.isHorizontal) -20f else 24f
            sb.appendLine("""  <circle cx="$mx" cy="$my" r="13" fill="#0284C7" stroke="#FFFFFF" stroke-width="1.5"/>""")
            sb.appendLine("""  <text x="$mx" y="${my + 4}" fill="#FFFFFF" font-family="monospace" font-size="12" font-weight="bold" text-anchor="middle">${w.label}</text>""")
            sb.appendLine("""  <text x="${if (w.isHorizontal) mx else mx + offsetCota}" y="${if (w.isHorizontal) my + offsetCota else my}" fill="#FF2D78" font-family="monospace" font-size="11" font-weight="bold" text-anchor="middle">$lenStr (${formatClean(w.thicknessCm)}cm • Amarr:${formatClean(amarraSpec.tieLengthCm)}cm)</text>""")
        }

        // Encontros L, T e Cruz com indicação explícita do bloco de amarração (29 / 31,5 / 34 cm)
        for (j in junctions) {
            val jx = tx(j.x)
            val jy = ty(j.y)
            val refWall = walls.firstOrNull { it.id in j.connectedWallIds }
            val tieLen = if (refWall != null) formatClean(getAmarracaoBlockInfo(refWall.thicknessCm).tieLengthCm) else "34"
            val badgeText = "${j.kind.code}:${tieLen}cm"
            sb.appendLine("""  <rect x="${jx - 24}" y="${jy - 10}" width="48" height="20" rx="5" fill="#FDE047" stroke="#0F172A" stroke-width="1.4"/>""")
            sb.appendLine("""  <text x="$jx" y="${jy + 4}" fill="#0F172A" font-family="monospace" font-size="10" font-weight="bold" text-anchor="middle">$badgeText</text>""")
        }

        sb.appendLine("""  <text x="${svgW / 2}" y="${svgH - 28}" fill="#FDE047" font-family="monospace" font-size="18" font-weight="bold" text-anchor="middle">PLANTA BAIXA EXECUTIVA E MODULAÇÃO — ${escapeXml(projectName.uppercase())}</text>""")
        sb.appendLine("""</svg>""")
        return sb.toString()
    }

    /**
     * 8.D. EXPORTAÇÃO EM PDF EXECUTIVO (Planta Baixa, Tabela de Blocos B01..C01, Cortes/Folgas e Paredes)
     */
    fun generateExecutivePdfReport(
        context: Context,
        projectName: String,
        walls: List<FreePlanWall>,
        rooms: List<DetectedRoom>,
        junctions: List<DetectedWallJunction>,
        paginations: List<FreePlanWallPagination>
    ): File {
        val pdf = PdfDocument()
        val pageWidth = 842 // A4 Paisagem (pt)
        val pageHeight = 595

        // PÁGINA 1: PLANTA BAIXA + QUADRO DE AMBIENTES + RESUMO DE PEÇAS POR CÓDIGO
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page1 = pdf.startPage(pageInfo1)
        val c1 = page1.canvas

        val bgPaint = Paint().apply { color = Color.parseColor("#0F172A") }
        c1.drawRect(0f, 0f, pageWidth.toFloat(), 58f, bgPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 16f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            textSize = 10f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        c1.drawText("PAGINAÇÃO PRO — PROJETO EXECUTIVO DE ALVENARIA: ${projectName.uppercase()}", 24f, 28f, titlePaint)
        c1.drawText("Planta Baixa Cotada • Reconhecimento de Ambientes • Encontros L/T/Cruz • Quantitativo por Código", 24f, 46f, subPaint)

        // Desenhar Planta Baixa no lado esquerdo da Página 1
        drawFloorPlanOnPdfCanvas(c1, walls, rooms, junctions, RectF(24f, 75f, 480f, 550f))

        // Quadro Executivo no lado direito da Página 1
        val boxTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        var rightY = 88f
        c1.drawText("1. AMBIENTES RECONHECIDOS (${rooms.size})", 500f, rightY, boxTitlePaint)
        rightY += 16f
        if (rooms.isEmpty()) {
            c1.drawText("• Nenhum ambiente fechado detectado", 500f, rightY, bodyPaint)
            rightY += 14f
        } else {
            for (r in rooms) {
                c1.drawText("• ${r.displayTitle}: ${r.areaM2} m² [Paredes ${r.boundingWallLabels.joinToString(",")}]", 500f, rightY, bodyPaint)
                rightY += 14f
            }
        }

        rightY += 10f
        c1.drawText("2. ENCONTROS DE PAREDES (${junctions.size})", 500f, rightY, boxTitlePaint)
        rightY += 16f
        val countL = junctions.count { it.kind == JunctionKind.CORNER_L }
        val countT = junctions.count { it.kind == JunctionKind.JUNCTION_T }
        val countX = junctions.count { it.kind == JunctionKind.CROSS_X }
        c1.drawText("• Encontros em L (90°): $countL  |  Em T: $countT  |  Em Cruz (+): $countX", 500f, rightY, bodyPaint)
        rightY += 20f

        c1.drawText("3. QUADRO GERAL DE BLOCOS E CANALETAS POR CÓDIGO", 500f, rightY, boxTitlePaint)
        rightY += 16f

        val globalCodeCounts = linkedMapOf<String, Int>()
        for (p in paginations) {
            for ((k, v) in p.closingAnalysis.pieceCodeSummary) {
                globalCodeCounts[k] = (globalCodeCounts[k] ?: 0) + v
            }
        }
        for ((codeDesc, totalQty) in globalCodeCounts) {
            val withWaste = ceil(totalQty * 1.05).toInt()
            c1.drawText("$codeDesc: $totalQty un (Compra +5%: $withWaste un)", 500f, rightY, boldBodyPaint)
            rightY += 14f
            if (rightY > 420f) break
        }

        rightY += 10f
        c1.drawText("4. ANÁLISE DE FECHAMENTO (REGRA SOBRA ≤ 1 CM)", 500f, rightY, boxTitlePaint)
        rightY += 16f
        for (p in paginations) {
            val ca = p.closingAnalysis
            val line = "Parede ${ca.wallLabel} (${formatClean(ca.wallLengthCm)}cm): Folga=${ca.closingGapCm}cm | Corte=${ca.adjustmentCutCm}cm | Desp.=${ca.estimatedWasteCm}cm"
            c1.drawText(line, 500f, rightY, bodyPaint)
            rightY += 13f
            if (rightY > 565f) break
        }

        pdf.finishPage(page1)

        // PÁGINAS SEGUINTES: ELEVAÇÃO E MODULAÇÃO DE CADA PAREDE
        for ((idx, pag) in paginations.withIndex()) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, idx + 2).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas

            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 52f, bgPaint)
            canvas.drawText(
                "ELEVAÇÃO EXECUTIVA — ${pag.wall.displayName.uppercase()} (${formatClean(pag.wall.effectiveLengthCm)} × ${formatClean(pag.wall.heightCm)} × ${formatClean(pag.wall.thicknessCm)} cm)",
                24f, 26f, titlePaint
            )
            canvas.drawText(
                "${pag.closingAnalysis.statusTitle} • Junta: ${pag.closingAnalysis.mortarJointCm} cm • Total de Fiadas: ${pag.tilingResult.courses.size}",
                24f, 42f, subPaint
            )

            drawWallElevationOnPdfCanvas(canvas, pag, RectF(30f, 70f, 810f, 470f))

            // Rodapé com resumo de códigos da parede
            var footY = 495f
            canvas.drawText("Peças desta parede: " + pag.closingAnalysis.pieceCodeSummary.entries.joinToString("  |  ") { "${it.key}: ${it.value} un" }, 30f, footY, boldBodyPaint)
            footY += 16f
            canvas.drawText("Diagnóstico Técnico: ${pag.closingAnalysis.technicalExplanation}", 30f, footY, bodyPaint)
            if (pag.closingAnalysis.recommendedAlternatives.isNotEmpty()) {
                footY += 14f
                canvas.drawText("Recomendação: ${pag.closingAnalysis.recommendedAlternatives.first()}", 30f, footY, bodyPaint)
            }

            pdf.finishPage(page)
        }

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val pdfFile = File(exportDir, "PaginacaoPro_Planta_Executiva_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdf.writeTo(out)
        }
        pdf.close()
        return pdfFile
    }

    private fun drawFloorPlanOnPdfCanvas(
        canvas: Canvas,
        walls: List<FreePlanWall>,
        rooms: List<DetectedRoom>,
        junctions: List<DetectedWallJunction>,
        bounds: RectF
    ) {
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1.5f
        }
        val bgPaint = Paint().apply { color = Color.parseColor("#F8FAFC") }
        canvas.drawRect(bounds, bgPaint)
        canvas.drawRect(bounds, borderPaint)

        if (walls.isEmpty()) return

        val minX = walls.minOf { min(it.startX, it.endX) } - 0.6f
        val maxX = walls.maxOf { max(it.startX, it.endX) } + 0.6f
        val minY = walls.minOf { min(it.startY, it.endY) } - 0.6f
        val maxY = walls.maxOf { max(it.startY, it.endY) } + 0.6f

        val scale = min(bounds.width() / (maxX - minX), bounds.height() / (maxY - minY))
        val offsetX = bounds.left + (bounds.width() - (maxX - minX) * scale) / 2f
        val offsetY = bounds.top + (bounds.height() - (maxY - minY) * scale) / 2f

        fun tx(x: Float) = offsetX + (x - minX) * scale
        fun ty(y: Float) = offsetY + (y - minY) * scale

        val roomFill = Paint().apply {
            color = Color.parseColor("#E0F2FE")
            style = Paint.Style.FILL
        }
        val roomText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0369A1")
            textSize = 9f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        for (r in rooms) {
            canvas.drawRect(tx(r.minX), ty(r.minY), tx(r.maxX), ty(r.maxY), roomFill)
            canvas.drawText(r.displayTitle, tx(r.centerX), ty(r.centerY), roomText)
            canvas.drawText("${r.areaM2} m²", tx(r.centerX), ty(r.centerY) + 11f, roomText)
        }

        val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.SQUARE
        }
        val cotaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#BE123C")
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        for (w in walls) {
            wallPaint.strokeWidth = ((w.thicknessCm / 100.0) * scale * 1.3).toFloat().coerceIn(4f, 12f)
            val x1 = tx(w.startX)
            val y1 = ty(w.startY)
            val x2 = tx(w.endX)
            val y2 = ty(w.endY)
            canvas.drawLine(x1, y1, x2, y2, wallPaint)

            val mx = (x1 + x2) / 2f
            val my = (y1 + y2) / 2f
            val labelText = "${w.label}: ${String.format(Locale.US, "%.2fm", w.effectiveLengthCm / 100.0)}"
            canvas.drawText(labelText, mx, if (w.isHorizontal) my - 8f else my, cotaPaint)
        }

        val juncPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#Facc15")
        }
        for (j in junctions) {
            canvas.drawCircle(tx(j.x), ty(j.y), 5f, juncPaint)
        }
    }

    private fun drawWallElevationOnPdfCanvas(
        canvas: Canvas,
        pag: FreePlanWallPagination,
        bounds: RectF
    ) {
        val res = pag.tilingResult
        val wallW = res.wall.lengthCm.toFloat().coerceAtLeast(10f)
        val wallH = res.wall.heightCm.toFloat().coerceAtLeast(10f)

        val scale = min(bounds.width() / wallW, bounds.height() / wallH) * 0.92f
        val originX = bounds.left + (bounds.width() - wallW * scale) / 2f
        val originY = bounds.bottom - (bounds.height() - wallH * scale) / 2f

        val blkFill = Paint().apply { style = Paint.Style.FILL }
        val blkStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#1E293B")
            strokeWidth = 0.8f
        }

        for (course in res.courses) {
            for (blk in course) {
                val left = originX + blk.xCm.toFloat() * scale
                val top = originY - (blk.yCm.toFloat() + blk.heightCm.toFloat()) * scale
                val right = left + blk.lengthCm.toFloat() * scale
                val bottom = originY - blk.yCm.toFloat() * scale

                val len = blk.lengthCm
                blkFill.color = when {
                    blk.pieceType == PieceType.LINTEL -> Color.parseColor("#FDE047")
                    abs(len - 39.0) < 0.8 -> Color.parseColor("#E2E8F0")
                    abs(len - 34.0) < 0.8 -> Color.parseColor("#F59E0B")
                    abs(len - 31.5) < 0.8 -> Color.parseColor("#FB923C")
                    abs(len - 29.0) < 0.8 -> Color.parseColor("#EC4899")
                    abs(len - 19.0) < 0.8 -> Color.parseColor("#38BDF8")
                    abs(len - 14.0) < 0.8 -> Color.parseColor("#818CF8")
                    abs(len - 9.0) < 0.8 -> Color.parseColor("#34D399")
                    abs(len - 6.5) < 0.8 || abs(len - 4.0) < 0.8 -> Color.parseColor("#A855F7")
                    else -> Color.parseColor("#F87171")
                }
                canvas.drawRect(left, top, right, bottom, blkFill)
                canvas.drawRect(left, top, right, bottom, blkStroke)
            }
        }
    }

    private fun escapeXml(s: String): String {
        return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    private fun round1(v: Double): Double = (v * 10.0).roundToInt() / 10.0

    fun formatClean(v: Double): String {
        return if (abs(v - v.roundToInt()) < 0.05) {
            v.roundToInt().toString()
        } else {
            String.format(Locale.US, "%.1f", v).replace('.', ',')
        }
    }
}
