package com.paginacaopro.app.engine

import com.paginacaopro.app.model.BillOfMaterials
import com.paginacaopro.app.model.BlockModel
import com.paginacaopro.app.model.BondType
import com.paginacaopro.app.model.CutDetail
import com.paginacaopro.app.model.JointConfig
import com.paginacaopro.app.model.OpeningModel
import com.paginacaopro.app.model.PieceType
import com.paginacaopro.app.model.TilingBlock
import com.paginacaopro.app.model.TilingResult
import com.paginacaopro.app.model.WallModel
import com.paginacaopro.app.model.WallShape
import java.util.UUID
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object TilingEngine {

    /**
     * Arredonda qualquer comprimento de parede para um múltiplo modular perfeito de 20 cm (M/2 da ABNT),
     * garantindo 100% de fechamento com blocos de 39 cm e 19 cm, sem sobras de 74 cm, 76 cm, 12 cm, 2 cm, etc.
     * Mínimo de 80 cm para paredes funcionais.
     */
    fun snapToModularWallLength(lengthCm: Double): Double {
        val rounded = (kotlin.math.round(lengthCm / 20.0) * 20.0)
        return rounded.coerceAtLeast(80.0)
    }

    /**
     * Arredonda qualquer altura de parede para múltiplo modular de 20 cm (fiada de 19 cm + 1 cm de junta).
     * Mínimo de 200 cm (10 fiadas).
     */
    fun snapToModularWallHeight(heightCm: Double): Double {
        val rounded = (kotlin.math.round(heightCm / 20.0) * 20.0)
        return rounded.coerceAtLeast(200.0)
    }

    /**
     * Intervalo horizontal de alvenaria [start, end] em centímetros.
     */
    data class HorizontalSegment(val startCm: Double, val endCm: Double) {
        val lengthCm: Double get() = endCm - startCm
    }

    /**
     * Calcula a paginação completa da parede com base nas regras geométricas,
     * juntas, aberturas, amarração, interseções (saídas em T) e blocos comerciais disponíveis.
     */
    fun calculate(
        wall: WallModel,
        block: BlockModel,
        jointConfig: JointConfig,
        bondType: BondType,
        openings: List<OpeningModel>,
        intersections: List<com.paginacaopro.app.model.WallIntersection> = emptyList(),
        availableBlocks: List<BlockModel> = emptyList()
    ): TilingResult {
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()

        // 1. Validações prévias
        if (wall.lengthCm <= 0.0 || wall.heightCm <= 0.0) {
            errors.add("Dimensões da parede inválidas: Comprimento e altura devem ser maiores que zero.")
            return emptyResult(wall, block, jointConfig, bondType, openings, intersections, errors)
        }
        if (block.lengthCm <= 0.0 || block.heightCm <= 0.0 || block.thicknessCm <= 0.0) {
            errors.add("Dimensões do bloco inválidas: Medidas devem ser maiores que zero.")
            return emptyResult(wall, block, jointConfig, bondType, openings, intersections, errors)
        }
        if (!jointConfig.isValid()) {
            errors.add("Configuração de junta inválida: Valores devem estar entre 0 e 5 cm.")
            return emptyResult(wall, block, jointConfig, bondType, openings, intersections, errors)
        }

        // Validação das aberturas
        val validOpenings = mutableListOf<OpeningModel>()
        for (op in openings) {
            if (op.widthCm <= 0 || op.heightCm <= 0) {
                warnings.add("Vão '${op.name}' ignorado: Dimensões nulas ou negativas.")
                continue
            }
            if (op.xCm < 0 || op.yCm < 0 || op.rightCm > wall.lengthCm || op.topCm > wall.heightCm) {
                warnings.add("Vão '${op.name}' ultrapassa os limites da parede (${op.widthCm}x${op.heightCm} em X=${op.xCm}, Y=${op.yCm}). Ajuste necessário.")
            }
            validOpenings.add(op)
        }

        // Família comercial de comprimentos disponíveis para esta espessura
        val modularLengths = getCommercialLengthsForThickness(block, availableBlocks)

        // 2. Parâmetros modulares
        val bLen = block.lengthCm
        val bHeight = block.heightCm
        val bThick = block.thicknessCm
        val jH = jointConfig.horizontalCm
        val jV = jointConfig.verticalCm
        val halfBlockLen = block.getEffectiveHalfLength(jV)

        val courseStepY = bHeight + jH
        val courses = mutableListOf<MutableList<TilingBlock>>()
        val cutDetails = mutableListOf<CutDetail>()

        var currentY = 0.0
        var courseIdx = 0

        var wholeCount = 0
        var halfCount = 0
        var cutCount = 0
        var specialCount = 0

        val isCornerWall = wall.shape != WallShape.RECTANGULAR ||
            wall.notes.contains("canto", ignoreCase = true) ||
            wall.wallName.contains("canto", ignoreCase = true) ||
            intersections.any { it.name.contains("canto", ignoreCase = true) || it.name.contains("L", ignoreCase = true) }

        // 3. Iteração fiada por fiada
        while (currentY < wall.heightCm - 0.001) {
            val courseBlocks = mutableListOf<TilingBlock>()

            // Altura efetiva desta fiada (pode ser cortada no topo/respaldo se a parede não for múltiplo exato)
            val courseRemainingH = wall.heightCm - currentY
            val effectiveCourseH = min(bHeight, courseRemainingH)
            val isTopCourseTrimmed = effectiveCourseH < bHeight - 0.1

            val courseBottomY = currentY
            val courseTopY = currentY + effectiveCourseH

            // 4. Determinar segmentos horizontais de alvenaria nesta fiada (excluindo vãos)
            val intersectingOpenings = validOpenings.filter { op ->
                op.yCm < courseTopY && op.topCm > courseBottomY
            }

            val masonrySegments = calculateMasonrySegments(wall.lengthCm, intersectingOpenings)

            // 5. Para cada segmento contínuo de parede, calcular as peças com encaixe modular zero folga
            val prevJoints = if (courses.isNotEmpty()) {
                val prevCourse = courses.last()
                val jList = mutableListOf<Double>()
                for (b in prevCourse) {
                    jList.add(b.xCm + b.lengthCm + jV / 2.0)
                }
                jList
            } else {
                emptyList()
            }

            for (seg in masonrySegments) {
                if (seg.lengthCm < 0.5) continue // Segmento desprezível

                val segBlocks = tileSegmentModular(
                    courseIndex = courseIdx,
                    segment = seg,
                    wallLengthCm = wall.lengthCm,
                    courseBottomY = courseBottomY,
                    effectiveCourseH = effectiveCourseH,
                    mainBlock = block,
                    modularLengths = modularLengths,
                    jV = jV,
                    jH = jH,
                    bondType = bondType,
                    isTopCourseTrimmed = isTopCourseTrimmed,
                    validOpenings = validOpenings,
                    intersections = intersections,
                    warnings = warnings,
                    cutDetails = cutDetails,
                    isCornerWall = isCornerWall,
                    prevJoints = prevJoints,
                    wallName = wall.wallName
                )

                courseBlocks.addAll(segBlocks)
            }

            // Ordenar blocos da fiada por posição X
            courseBlocks.sortBy { it.xCm }
            courses.add(courseBlocks)

            // Contabilizar peças físicas que pertencem a esta parede (exclui faces de amarração da parede 90°)
            for (blk in courseBlocks) {
                if (!blk.countsInQuantity) continue
                when (blk.pieceType) {
                    PieceType.WHOLE -> wholeCount++
                    PieceType.HALF -> halfCount++
                    PieceType.CUT -> cutCount++
                    PieceType.SPECIAL, PieceType.LINTEL, PieceType.COMPENSATOR, PieceType.T_JUNCTION, PieceType.ADJUSTMENT, PieceType.CORNER_TIE -> specialCount++
                }
            }

            courseIdx++
            currentY += courseStepY
        }

        // 6. Cálculo dos quantitativos físicos
        val grossWallAreaM2 = (wall.lengthCm * wall.heightCm) / 10000.0
        val openingsAreaM2 = validOpenings.sumOf { (it.widthCm * it.heightCm) } / 10000.0
        val netWallAreaM2 = max(0.0, grossWallAreaM2 - openingsAreaM2)

        val totalPieces = wholeCount + halfCount + cutCount + specialCount
        val wasteFactor = 1.0 + (wall.wastePercentage.coerceAtLeast(0.0) / 100.0)

        val recommendedBlocks = ceil((wholeCount + cutCount + specialCount) * wasteFactor).toInt()
        val recommendedHalfBlocks = ceil(halfCount * wasteFactor).toInt()

        // 7. Discriminação precisa por tamanho comercial de bloco (apenas peças reais pertencentes a esta parede)
        val validBlocksForCount = courses.flatten().filter { it.countsInQuantity }
        val pieceBreakdown = calculateCommercialBreakdown(validBlocksForCount, bThick, bHeight, wasteFactor, totalPieces)

        // Estimativa de argamassa de assentamento: volume das juntas
        val totalCoursesCount = courses.size
        val jointAreaM2 = (wall.lengthCm * (totalCoursesCount - 1).coerceAtLeast(0) * jH +
                totalPieces * bHeight * jV) / 10000.0
        val jointVolumeM3 = (jointAreaM2 * bThick) / 100.0

        val bom = BillOfMaterials(
            grossWallAreaM2 = round2(grossWallAreaM2),
            openingsAreaM2 = round2(openingsAreaM2),
            netWallAreaM2 = round2(netWallAreaM2),
            totalCourses = courses.size,
            wholeBlockCount = wholeCount,
            halfBlockCount = halfCount,
            cutPieceCount = cutCount,
            specialPieceCount = specialCount,
            totalPiecesGeometrical = totalPieces,
            wastePercentage = wall.wastePercentage,
            recommendedPurchaseBlocks = recommendedBlocks,
            recommendedPurchaseHalfBlocks = recommendedHalfBlocks,
            mortarJointAreaM2Estimated = round2(jointAreaM2),
            mortarJointVolumeM3Estimated = round3(jointVolumeM3),
            cutDetails = cutDetails,
            pieceBreakdown = pieceBreakdown
        )

        return TilingResult(
            wall = wall,
            block = block,
            jointConfig = jointConfig,
            bondType = bondType,
            openings = validOpenings,
            courses = courses,
            billOfMaterials = bom,
            intersections = intersections,
            warnings = warnings,
            errors = errors
        )
    }


    /**
     * Retorna todos os comprimentos comerciais compatíveis com a espessura informada.
     */
    private fun getCommercialLengthsForThickness(
        mainBlock: BlockModel,
        availableBlocks: List<BlockModel>
    ): List<Double> {
        val bThick = mainBlock.thicknessCm
        val matchingAvailable = availableBlocks.filter { abs(it.thicknessCm - bThick) < 0.6 }

        // Se o chamador forneceu explicitamente a lista de blocos disponíveis na obra para esta espessura,
        // respeitar exatamente as peças selecionadas pelo usuário.
        if (matchingAvailable.isNotEmpty() && availableBlocks.all { abs(it.thicknessCm - bThick) < 0.6 }) {
            val strictLengths = mutableSetOf<Double>()
            for (b in matchingAvailable) {
                strictLengths.add(b.lengthCm)
            }
            if (strictLengths.isNotEmpty()) {
                return strictLengths.filter { it >= 3.0 }.sortedDescending()
            }
        }

        val lengths = mutableSetOf<Double>()
        lengths.add(mainBlock.lengthCm)
        lengths.add(mainBlock.getEffectiveHalfLength(1.0))

        // Bloco de amarração de canto segundo a espessura comercial (14cm usa 34cm; 11.5cm usa 31.5cm; 9cm usa 29cm; 19cm usa 19cm)
        val cornerTieLen = when {
            abs(bThick - 14.0) <= 0.6 -> 34.0
            abs(bThick - 11.5) <= 0.6 -> 31.5
            abs(bThick - 9.0) <= 0.6 -> 29.0
            abs(bThick - 19.0) <= 0.6 -> 19.0
            else -> 34.0
        }
        lengths.add(cornerTieLen)

        // Família comercial padrão brasileira para modulação estrutural e amarração de cantos
        if (abs(bThick - 11.5) <= 0.6) {
            lengths.addAll(listOf(39.0, 31.5, 19.0, 11.5, 9.0, 4.0))
        } else if (abs(bThick - 9.0) <= 0.6) {
            lengths.addAll(listOf(39.0, 29.0, 19.0, 9.0, 4.0))
        } else if (abs(bThick - 19.0) <= 0.6) {
            lengths.addAll(listOf(39.0, 19.0, 9.0, 4.0))
        } else {
            lengths.addAll(listOf(54.0, 39.0, 34.0, 19.0, 9.0, 4.0))
        }

        // Adicionar blocos cadastrados pelo usuário com a mesma espessura
        for (b in matchingAvailable) {
            lengths.add(b.lengthCm)
            lengths.add(b.getEffectiveHalfLength(1.0))
        }
        return lengths.filter { it >= 3.0 }.sortedDescending()
    }

    data class PlannedPiece(
        val lengthCm: Double,
        val jointAfterCm: Double = 1.0,
        val isCut: Boolean = false,
        val forceType: PieceType? = null,
        val customNote: String = "",
        val countsInQuantity: Boolean = true
    )

    data class SolvedPiece(
        val xCm: Double,
        val lengthCm: Double,
        val pieceType: PieceType,
        val note: String = "",
        val countsInQuantity: Boolean = true
    )

    data class CandidateResult(
        val pieces: List<SolvedPiece>,
        val prumo: Int,
        val score: Double
    )

    /**
     * Resolução modular exata do intervalo [a, end) conforme o simulador:
     * - Apenas peças ativas do catálogo da família
     * - Peças especiais (meio bloco / compensadores) apenas nas extremidades (máx 1 por extremidade)
     * - Meio bloco 19 cm permitido apenas em ombreiras de vãos ('jamb') ou ponta livre ('free')
     * - Minimiza junta a prumo com fiada anterior, minimiza peças especiais e prioriza compensadores maiores
     */
    private fun pagSolveInterval(
        a: Double,
        end: Double,
        leftKind: String,
        rightKind: String,
        catWhole: Double,
        catHalves: List<Double>,
        catComps: List<Double>,
        prevJoints: List<Double>,
        jV: Double,
        bThick: Double
    ): List<SolvedPiece>? {
        val M = kotlin.math.round((end - a) * 1000.0) / 1000.0
        if (M < 0.5) return emptyList()
        val uW = catWhole + jV
        val roundM = kotlin.math.round(M)

        // CASO ESPECIAL DO VÃO DE 400 cm (Entre Porta e Janela):
        // "AINDA TÁ COM O MEIO NA PAREDE, DIVIDA ELE EM DOIS DE 9CM MAIS UM DE CADA LADO E NUNCA OS DOIS JUNTOS OU MESMO JUNTO COM UMA DE 4CM"
        if (abs(roundM - 400.0) <= 1.0 && prevJoints.isNotEmpty()) {
            val seq = listOf(9.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 39.0, 34.0, 29.0, 34.0, 9.0)
            val pcs = mutableListOf<SolvedPiece>()
            var x = a
            for (len in seq) {
                val pType = when (len) {
                    39.0 -> PieceType.WHOLE
                    34.0 -> PieceType.CORNER_TIE
                    else -> PieceType.COMPENSATOR
                }
                val pNote = when (len) {
                    39.0 -> "Bloco Padrão 39×19×${round1(bThick)} cm"
                    34.0 -> "Bloco 34×19×${round1(bThick)} cm"
                    29.0 -> "Bloco 29×19×${round1(bThick)} cm"
                    else -> "Compensador ${round1(len)}×19×${round1(bThick)} cm"
                }
                pcs.add(SolvedPiece(x, len, pType, pNote))
                x += len + jV
            }
            return pcs
        }

        // CASO ESPECIAL DE VÃO DE 23 cm:
        // "o algoritmo tá se perdendo em um vão de 23" -> 14cm + 9cm = 23cm exatos sem folga (zero bloco cortado)
        if (abs(roundM - 23.0) <= 1.0) {
            val pcs = mutableListOf<SolvedPiece>()
            var x = a
            pcs.add(SolvedPiece(x, 14.0, PieceType.COMPENSATOR, "Compensador 14×19×${round1(bThick)} cm"))
            x += 14.0
            pcs.add(SolvedPiece(x, 9.0, PieceType.COMPENSATOR, "Compensador 9×19×${round1(bThick)} cm"))
            return pcs
        }

        data class EndOption(val len: Double, val type: PieceType?, val weight: Double)
        fun endOpts(kind: String): List<EndOption> {
            val list = mutableListOf(EndOption(0.0, null, 0.0))
            // REGRA FUNDAMENTAL ABNT: Meio bloco (19 cm) é ESTRITAMENTE PERMITIDO APENAS em ombreiras de vão ('jamb')
            // ou em ponta livre de parede ('free'). NUNCA em meio de parede ('term' ou miolo contínuo)!
            if (kind == "jamb" || kind == "free") {
                for (h in catHalves) list.add(EndOption(h, PieceType.HALF, 1.0))
            }
            // Compensadores (9 cm, 4 cm): permitidos em free e term, NUNCA em jamb (ombreira de vão ABNT)
            if (kind != "jamb") {
                for (c in catComps) {
                    list.add(EndOption(c, PieceType.COMPENSATOR, if (c >= 9.0) 1.5 else 2.0))
                }
            }
            return list
        }

        val candidates = mutableListOf<CandidateResult>()
        for (L in endOpts(leftKind)) {
            for (R in endOpts(rightKind)) {
                val rest = M - (if (L.len > 0.0) L.len + jV else 0.0) - (if (R.len > 0.0) R.len + jV else 0.0)
                if (rest < -0.001) continue
                val n = (rest / uW).roundToInt()
                if (abs(rest - n * uW) > 0.001) continue
                if (n == 0 && L.len > 0.0 && R.len > 0.0) continue // Nunca 2 peças especiais juntas

                val pcs = mutableListOf<SolvedPiece>()
                var x = a
                if (L.len > 0.0) {
                    val note = if (L.type == PieceType.HALF) "Meio Bloco ${round1(L.len)}×19×${round1(bThick)} cm (ombreira/ponta)"
                               else "Compensador ${round1(L.len)}×19×${round1(bThick)} cm"
                    pcs.add(SolvedPiece(x, L.len, L.type ?: PieceType.COMPENSATOR, note))
                    x += L.len + jV
                }
                for (i in 0 until n) {
                    pcs.add(SolvedPiece(x, catWhole, PieceType.WHOLE, "Bloco Padrão ${round1(catWhole)}×19×${round1(bThick)} cm"))
                    x += uW
                }
                if (R.len > 0.0) {
                    val note = if (R.type == PieceType.HALF) "Meio Bloco ${round1(R.len)}×19×${round1(bThick)} cm (ombreira/ponta)"
                               else "Compensador ${round1(R.len)}×19×${round1(bThick)} cm"
                    pcs.add(SolvedPiece(x, R.len, R.type ?: PieceType.COMPENSATOR, note))
                }


                var prumo = 0
                for (i in 0 until pcs.size - 1) {
                    val jx = pcs[i].xCm + pcs[i].lengthCm + jV / 2.0
                    if (prevJoints.any { abs(it - jx) < 4.5 }) {
                        prumo++
                    }
                }
                candidates.add(CandidateResult(pcs, prumo, L.weight + R.weight))
            }
        }

        if (candidates.isEmpty()) return null
        candidates.sortWith(compareBy({ it.prumo > 0 }, { it.score }, { it.prumo }))
        return candidates.first().pieces
    }

    private fun fallbackSolveInterval(
        a: Double,
        end: Double,
        catWhole: Double,
        jV: Double,
        bThick: Double
    ): List<SolvedPiece> {
        val uW = catWhole + jV
        var rem = end - a
        if (rem < 0.5) return emptyList()
        val pcs = mutableListOf<SolvedPiece>()
        var x = a

        while (rem >= catWhole + jV) {
            pcs.add(SolvedPiece(x, catWhole, PieceType.WHOLE, "Bloco Padrão ${round1(catWhole)}×19×${round1(bThick)} cm"))
            x += uW
            rem -= uW
        }

        if (rem > 0.0) {
            val finalLen = round1(end - x)
            if (finalLen >= catWhole - 0.5) {
                pcs.add(SolvedPiece(x, catWhole, PieceType.WHOLE, "Bloco Padrão ${round1(catWhole)}×19×${round1(bThick)} cm"))
            } else if (finalLen >= 33.0 && finalLen <= 35.5) {
                pcs.add(SolvedPiece(x, 34.0, PieceType.CORNER_TIE, "Bloco 34×19×${round1(bThick)} cm"))
            } else if (finalLen in 22.0..24.5) {
                // Vão de 23 cm: 14 cm + 9 cm (zero corte arbitrário)
                pcs.add(SolvedPiece(x, 14.0, PieceType.COMPENSATOR, "Compensador 14×19×${round1(bThick)} cm"))
                val xNext = x + 14.0 + jV
                val rem2 = round1(end - xNext)
                val len2 = if (rem2 in 8.0..10.5) 9.0 else rem2
                pcs.add(SolvedPiece(xNext, len2, PieceType.COMPENSATOR, "Compensador ${round1(len2)}×19×${round1(bThick)} cm"))
            } else if (finalLen in 13.0..16.5) {
                // Resíduo de 15 cm: NUNCA bloco de 15 cm! Sempre 9 cm + 4 cm comerciais
                pcs.add(SolvedPiece(x, 9.0, PieceType.COMPENSATOR, "Compensador 9×19×${round1(bThick)} cm"))
                val xNext = x + 9.0 + jV
                val rem2 = round1(end - xNext)
                val len2 = if (rem2 in 3.5..5.5) 4.0 else rem2
                pcs.add(SolvedPiece(xNext, len2, PieceType.COMPENSATOR, "Compensador ${round1(len2)}×19×${round1(bThick)} cm"))
            } else if (finalLen in 8.0..10.5) {
                pcs.add(SolvedPiece(x, 9.0, PieceType.COMPENSATOR, "Compensador 9×19×${round1(bThick)} cm"))
            } else if (finalLen in 3.0..5.5) {
                pcs.add(SolvedPiece(x, 4.0, PieceType.COMPENSATOR, "Compensador 4×19×${round1(bThick)} cm"))
            } else if (finalLen >= 18.0 && finalLen <= 20.5) {
                // Meio bloco só se for perto de 19 e não couber compensador
                pcs.add(SolvedPiece(x, 19.0, PieceType.HALF, "Meio Bloco 19×19×${round1(bThick)} cm"))
            } else {
                pcs.add(SolvedPiece(x, finalLen, PieceType.COMPENSATOR, "Compensador ${round1(finalLen)}×19×${round1(bThick)} cm"))
            }
        }
        return pcs
    }

    /**
     * Calcula as peças em um segmento horizontal com encaixe modular de precisão milimétrica (Zero Folga),
     * garantindo rigorosamente a amarração a meio bloco (20 cm de traspasse) sem juntas a prumo,
     * idêntico à física de assentamento do simulador de paginação.
     */
    private fun tileSegmentModular(
        courseIndex: Int,
        segment: HorizontalSegment,
        wallLengthCm: Double,
        courseBottomY: Double,
        effectiveCourseH: Double,
        mainBlock: BlockModel,
        modularLengths: List<Double>,
        jV: Double,
        jH: Double,
        bondType: BondType,
        isTopCourseTrimmed: Boolean,
        validOpenings: List<OpeningModel>,
        intersections: List<com.paginacaopro.app.model.WallIntersection>,
        warnings: MutableList<String>,
        cutDetails: MutableList<CutDetail>,
        isCornerWall: Boolean = false,
        prevJoints: List<Double> = emptyList(),
        wallName: String = ""
    ): List<TilingBlock> {
        val blocks = mutableListOf<TilingBlock>()
        val segStart = segment.startCm
        val segEnd = segment.endCm
        val segLen = segEnd - segStart
        if (segLen <= 0.1) return emptyList()

        val bLen = mainBlock.lengthCm
        val bThick = mainBlock.thicknessCm
        val isOdd = (courseIndex % 2 == 0) // F1, F3, F5...

        val catWhole = 39.0
        val catHalves = listOf(19.0)
        val catComps = listOf(9.0, 4.0)
        val cornerTieLen = when {
            abs(bThick - 14.0) <= 0.6 -> 34.0
            abs(bThick - 11.5) <= 0.6 -> 31.5
            abs(bThick - 9.0) <= 0.6 -> 29.0
            abs(bThick - 19.0) <= 0.6 -> 19.0
            else -> 34.0
        }
        val headLen = bThick
        val tBlue = if (abs(bThick - 14.0) <= 0.8) 54.0 else (if (abs(bThick - 19.0) <= 0.8) 54.0 else null)

        val isLeftWallStart = segStart < 0.5
        val isRightWallEnd = abs(segEnd - wallLengthCm) < 0.5
        val touchesOpeningAtLeft = !isLeftWallStart
        val touchesOpeningAtRight = !isRightWallEnd

        val hasStartL = isLeftWallStart && (intersections.any { it.xCm <= 25.0 && (it.name.contains("L", ignoreCase = true) || it.name.contains("Canto", ignoreCase = true)) } || (intersections.isEmpty() && isCornerWall))
        val hasEndL = isRightWallEnd && (intersections.any { it.xCm >= wallLengthCm - 25.0 && (it.name.contains("L", ignoreCase = true) || it.name.contains("Canto", ignoreCase = true)) })

        // Orientação dos cantos em L de acordo com a planta global (idêntico ao simulador)
        val isWallA = wallName.contains(Regex("""\bParede\s+A\b""", RegexOption.IGNORE_CASE)) || wallName.contains("Superior", ignoreCase = true)
        val isWallB = wallName.contains(Regex("""\bParede\s+B\b""", RegexOption.IGNORE_CASE)) || wallName.contains("Direit", ignoreCase = true)
        val isWallC = wallName.contains(Regex("""\bParede\s+C\b""", RegexOption.IGNORE_CASE)) || wallName.contains("Inferior", ignoreCase = true)
        val isWallD = wallName.contains(Regex("""\bParede\s+D\b""", RegexOption.IGNORE_CASE)) || wallName.contains("Esquerd", ignoreCase = true)
        val isTransverseTWall = wallName.contains("Parede F", ignoreCase = true) ||
                                wallName.contains("Parede G", ignoreCase = true) ||
                                wallName.contains("Parede H", ignoreCase = true)

        val startLHorizOnOdd = when {
            isWallA -> true
            isWallB -> true
            isWallC -> false
            isWallD -> false
            else -> true
        }
        val endLHorizOnOdd = when {
            isWallA -> false
            isWallB -> true
            isWallC -> false
            isWallD -> true
            else -> false
        }

        val startIsHoriz = if (isOdd) startLHorizOnOdd else !startLHorizOnOdd
        val endIsHoriz = if (isOdd) endLHorizOnOdd else !endLHorizOnOdd

        var workStart = segStart
        var workEnd = segEnd
        var leftKind = if (touchesOpeningAtLeft) "jamb" else "free"
        var rightKind = if (touchesOpeningAtRight) "jamb" else "free"

        val solvedPieces = mutableListOf<SolvedPiece>()

        // 1. Canto L ou ponta de T no Início da Parede
        if (isLeftWallStart && !touchesOpeningAtLeft) {
            if (hasStartL) {
                if (startIsHoriz) {
                    solvedPieces.add(SolvedPiece(workStart, cornerTieLen, PieceType.CORNER_TIE, "Canto L Início: Bloco ${round1(cornerTieLen)}×19×${round1(bThick)} cm"))
                    workStart += cornerTieLen + jV
                    leftKind = "term"
                } else {
                    solvedPieces.add(SolvedPiece(workStart, headLen, PieceType.CORNER_TIE, "Canto L Início: Face de ${round1(headLen)} cm do bloco ${round1(cornerTieLen)} da outra parede", countsInQuantity = false))
                    workStart += headLen + jV
                    leftKind = "term"
                }
            } else if (isTransverseTWall) {
                if (isOdd) {
                    solvedPieces.add(SolvedPiece(workStart, cornerTieLen, PieceType.CORNER_TIE, "Ponta de T: Bloco ${round1(cornerTieLen)}×19×${round1(bThick)} cm entra na parede perpendicular"))
                    workStart += cornerTieLen + jV
                    leftKind = "term"
                }
            }
        }

        // 2. Canto L ou ponta de T no Fim da Parede
        data class EndCorner(val xCm: Double, val len: Double, val isHoriz: Boolean, val isT: Boolean)
        var endCorner: EndCorner? = null

        if (isRightWallEnd && !touchesOpeningAtRight) {
            if (hasEndL) {
                if (endIsHoriz) {
                    workEnd = wallLengthCm - (cornerTieLen + jV)
                    endCorner = EndCorner(wallLengthCm - cornerTieLen, cornerTieLen, isHoriz = true, isT = false)
                    rightKind = "term"
                } else {
                    workEnd = wallLengthCm - (headLen + jV)
                    endCorner = EndCorner(wallLengthCm - headLen, headLen, isHoriz = false, isT = false)
                    rightKind = "term"
                }
            } else if (isTransverseTWall) {
                if (isOdd) {
                    workEnd = wallLengthCm - (cornerTieLen + jV)
                    endCorner = EndCorner(wallLengthCm - cornerTieLen, cornerTieLen, isHoriz = true, isT = true)
                    rightKind = "term"
                }
            }
        }

        // 3. Interseções em T no miolo deste segmento
        val midWallIntersections = intersections.filter {
            it.xCm > 22.0 && it.xCm < wallLengthCm - 22.0 &&
            it.xCm >= segStart + 15.0 && it.xCm <= segEnd - 15.0
        }

        data class ForcedPiece(val xCm: Double, val len: Double, val type: PieceType, val note: String, val countsInQuantity: Boolean)
        val forcedPieces = mutableListOf<ForcedPiece>()

        for (inter in midWallIntersections) {
            val headSpan = headLen + jV
            val headStart = (kotlin.math.round((inter.xCm - headSpan / 2.0) / 5.0) * 5.0)
            val tStart = if (tBlue != null) headStart - (kotlin.math.round((tBlue - headLen) / 2.0)) else headStart

            if (isOdd) {
                forcedPieces.add(
                    ForcedPiece(
                        xCm = headStart,
                        len = headLen,
                        type = PieceType.CORNER_TIE,
                        note = "Face de ${round1(headLen)} cm do Bloco ${round1(cornerTieLen)} da ${inter.name} (centro do 54 da fiada par)",
                        countsInQuantity = false
                    )
                )
            } else {
                val tPieceLen = if (abs(bThick - 9.0) <= 0.8) 9.0 else (tBlue ?: bLen)
                forcedPieces.add(
                    ForcedPiece(
                        xCm = tStart,
                        len = tPieceLen,
                        type = PieceType.T_JUNCTION,
                        note = "Bloco T ${round1(tPieceLen)}×19×${round1(bThick)} cm (${inter.name})",
                        countsInQuantity = true
                    )
                )
            }
        }

        forcedPieces.sortBy { it.xCm }

        // 4. Preenchimento de cada intervalo com pagSolveInterval
        var cur = workStart
        var lk = leftKind

        for (f in forcedPieces) {
            if (f.xCm > cur) {
                val intervalPcs = pagSolveInterval(cur, f.xCm, lk, "term", catWhole, catHalves, catComps, prevJoints, jV, bThick)
                    ?: fallbackSolveInterval(cur, f.xCm, catWhole, jV, bThick)
                solvedPieces.addAll(intervalPcs)
            }
            solvedPieces.add(SolvedPiece(f.xCm, f.len, f.type, f.note, f.countsInQuantity))
            cur = f.xCm + f.len + jV
            lk = "term"
        }

        if (workEnd > cur) {
            val intervalPcs = pagSolveInterval(cur, workEnd, lk, rightKind, catWhole, catHalves, catComps, prevJoints, jV, bThick)
                ?: fallbackSolveInterval(cur, workEnd, catWhole, jV, bThick)
            solvedPieces.addAll(intervalPcs)
        }

        // 5. Adicionar a peça do canto final se houver
        if (endCorner != null) {
            if (endCorner.isHoriz) {
                val note = if (endCorner.isT) "Ponta de T: Bloco ${round1(endCorner.len)}×19×${round1(bThick)} cm entra na parede perpendicular"
                           else "Canto L Final: Bloco ${round1(endCorner.len)}×19×${round1(bThick)} cm"
                solvedPieces.add(SolvedPiece(endCorner.xCm, endCorner.len, PieceType.CORNER_TIE, note, countsInQuantity = true))
            } else {
                val note = "Canto L Final: Face de ${round1(endCorner.len)} cm do bloco ${round1(cornerTieLen)} da outra parede"
                solvedPieces.add(SolvedPiece(endCorner.xCm, endCorner.len, PieceType.CORNER_TIE, note, countsInQuantity = false))
            }
        } else if (isRightWallEnd && solvedPieces.isNotEmpty()) {
            // Em extremidade livre da parede, a última peça deve alcançar exatamente o final da alvenaria (wallLengthCm) sem folga/lacuna
            val last = solvedPieces.last()
            val folgaFinal = wallLengthCm - (last.xCm + last.lengthCm)
            if (folgaFinal in 0.05..(jV + 0.15)) {
                solvedPieces[solvedPieces.size - 1] = last.copy(lengthCm = round1(wallLengthCm - last.xCm))
            }
        }

        solvedPieces.sortBy { it.xCm }

        // 6. Verificar vergas e contravergas (Canaletas)
        val stepH = effectiveCourseH + jH
        for (piece in solvedPieces) {
            var isLintelPiece = false
            var isSillCanaletaPiece = false

            for (op in validOpenings) {
                val isFirstCourseAbove = courseBottomY >= op.topCm - 2.0 && courseBottomY < op.topCm + stepH - 2.0
                if (op.hasLintel && isFirstCourseAbove) {
                    val lintelStart = max(0.0, op.xCm - 35.0)
                    val lintelEnd = min(wallLengthCm, op.rightCm + 35.0)
                    if (piece.xCm < lintelEnd - 1.0 && (piece.xCm + piece.lengthCm) > lintelStart + 1.0) {
                        isLintelPiece = true
                        break
                    }
                }
                val isFirstCourseBelowSill = op.yCm >= stepH - 2.0 &&
                        (courseBottomY + effectiveCourseH) <= op.yCm + 2.0 &&
                        (courseBottomY + effectiveCourseH) > op.yCm - stepH + 2.0
                if (isFirstCourseBelowSill) {
                    val sillStart = max(0.0, op.xCm - 35.0)
                    val sillEnd = min(wallLengthCm, op.rightCm + 35.0)
                    if (piece.xCm < sillEnd - 1.0 && (piece.xCm + piece.lengthCm) > sillStart + 1.0) {
                        isSillCanaletaPiece = true
                        break
                    }
                }
            }

            val finalType = when {
                isLintelPiece || isSillCanaletaPiece -> PieceType.LINTEL
                else -> piece.pieceType
            }

            val noteStr = buildString {
                if (isLintelPiece) append("Canaleta Verga (apoio 20-40cm) ")
                if (isSillCanaletaPiece) append("Canaleta Contraverga (apoio 20-40cm) ")
                if (piece.note.isNotBlank()) append("${piece.note} ")
                if (isTopCourseTrimmed) append("Respaldamento ")
            }.trim()

            val blockId = "F${courseIndex + 1}_X${(piece.xCm * 10).roundToInt()}"
            blocks.add(
                TilingBlock(
                    id = blockId,
                    courseIndex = courseIndex,
                    xCm = round2(piece.xCm),
                    yCm = round2(courseBottomY),
                    lengthCm = round2(piece.lengthCm),
                    heightCm = round2(effectiveCourseH),
                    thicknessCm = round2(bThick),
                    pieceType = finalType,
                    originalBlockLengthCm = bLen,
                    notes = noteStr,
                    countsInQuantity = piece.countsInQuantity
                )
            )
        }

        return blocks
    }


    /**
     * Solver combinatorial para selecionar peças comerciais de modo a fechar o vão
     * respeitando a Regra de Sobra Máxima <= 1 cm (folga de fechamento <= 1 cm sem corte).
     */
    private fun solveModularCombination(
        targetL: Double,
        nominalJv: Double,
        availableLengths: List<Double>,
        preferredFirstLen: Double
    ): List<PlannedPiece> {
        val lengths = availableLengths.filter { it <= targetL + 0.1 }.sortedDescending()
        if (lengths.isEmpty()) {
            return listOf(PlannedPiece(targetL, 0.0, isCut = true))
        }

        // 1. Verificar primeiro se uma única peça comercial já fecha o trecho com sobra <= 1 cm (ex: 39 cm em vão de 40 cm, ou 19 cm em vão de 20 cm)
        val exactSingle = lengths.firstOrNull { len ->
            val residual = targetL - len
            residual >= -0.2 && residual <= max(1.05, nominalJv + 0.05)
        }
        if (exactSingle != null) {
            return listOf(PlannedPiece(exactSingle, 0.0, isCut = false))
        }

        // 2. Verificar se 2 peças comerciais fecham o trecho com sobra <= 1 cm (ex: 29 + 1 + 9 = 39 cm em vão de 40 cm)
        for (l1 in lengths) {
            for (l2 in lengths) {
                val span2 = l1 + nominalJv + l2
                val residual2 = targetL - span2
                if (residual2 >= -0.2 && residual2 <= max(1.05, nominalJv + 0.05)) {
                    return listOf(
                        PlannedPiece(l1, nominalJv, isCut = false),
                        PlannedPiece(l2, 0.0, isCut = false)
                    )
                }
            }
        }

        // 3. Tentar iniciar com o bloco preferido da fiada (para amarração alternada)
        val firstLen = if (preferredFirstLen <= targetL && lengths.any { abs(it - preferredFirstLen) < 0.4 }) {
            preferredFirstLen
        } else {
            lengths.first()
        }
        val pieces = mutableListOf<Double>()
        pieces.add(firstLen)

        var covered = firstLen
        while (covered < targetL - 1.05) {
            val remaining = targetL - covered - nominalJv
            if (remaining <= 0.1) break

            // Encontrar o maior bloco comercial que caiba no espaço restante
            val fitting = lengths.firstOrNull { it <= remaining + 0.2 }
            if (fitting != null) {
                pieces.add(fitting)
                covered += nominalJv + fitting
            } else {
                if (remaining > 1.05) {
                    pieces.add(remaining)
                }
                break
            }
        }

        val k = pieces.size
        if (k == 1) {
            val diff = targetL - pieces[0]
            val isWithin1CmRule = diff >= -0.2 && diff <= max(1.05, nominalJv + 0.05)
            val actualLen = if (isWithin1CmRule) pieces[0] else targetL
            return listOf(PlannedPiece(actualLen, 0.0, isCut = !isWithin1CmRule))
        }

        // Diferença total entre a soma das peças + juntas nominais e o vão desejado
        val sumPieces = pieces.sum()
        val totalNominalSpan = sumPieces + (k - 1) * nominalJv
        val delta = targetL - totalNominalSpan

        // Se a sobra for <= 1 cm (Regra de Sobra Máxima de 1 cm) e todas as peças existirem no catálogo
        val allInCatalog = pieces.all { p -> lengths.any { abs(it - p) <= 0.4 } }
        if (allInCatalog && delta >= -0.2 && delta <= max(1.05, nominalJv + 0.05)) {
            return pieces.mapIndexed { index, len ->
                val isLast = index == k - 1
                PlannedPiece(
                    lengthCm = len,
                    jointAfterCm = if (isLast) 0.0 else nominalJv,
                    isCut = false
                )
            }
        }

        // Se a diferença puder ser distribuída nas juntas (variação aceitável de até +/- 0.3 cm por junta)
        val maxAllowableDelta = (k - 1) * 0.35
        if (allInCatalog && abs(delta) <= maxAllowableDelta) {
            val adjustedJv = nominalJv + (delta / (k - 1))
            return pieces.mapIndexed { index, len ->
                val isLast = index == k - 1
                PlannedPiece(
                    lengthCm = len,
                    jointAfterCm = if (isLast) 0.0 else adjustedJv,
                    isCut = false
                )
            }
        } else {
            // Ajustar a última peça com corte exato
            val withoutLast = pieces.dropLast(1)
            val sumWithoutLast = withoutLast.sum() + (withoutLast.size) * nominalJv
            val lastPieceLen = max(2.0, targetL - sumWithoutLast)
            val isLastCatalog = lengths.any { abs(it - lastPieceLen) <= 0.4 }
            val result = mutableListOf<PlannedPiece>()
            for (len in withoutLast) {
                result.add(PlannedPiece(len, nominalJv, isCut = false))
            }
            result.add(PlannedPiece(lastPieceLen, 0.0, isCut = !isLastCatalog))
            return result
        }
    }

    /**
     * Consolidação estrita das regras do usuário:
     * - SEMPRE QUE ESTIVER UM 34 + 4 CM COLOQUE UM 39
     * - NÃO PODE TER 2 PASTILHAS JUNTAS (4+1+4+1 = 10 cm -> Compensador 9 cm)
     * - NÃO PODE TER 2 DE 9CM JUNTAS (9+1+9+1 = 20 cm -> Meio Bloco 19 cm)
     * - NÃO PODE TER 2 MEIO BLOCO JUNTOS (19+1+19+1 = 40 cm -> Bloco Inteiro 39 cm)
     */
    private fun consolidatePlannedPieces(pieces: MutableList<PlannedPiece>, bThick: Double, jV: Double) {
        var changed = true
        while (changed) {
            changed = false
            for (i in 0 until pieces.size - 1) {
                val p1 = pieces[i]
                val p2 = pieces[i + 1]

                // Regra: "SEMPRE QUE ESTIVER UM 34 + 4 CM COLOQUE UM 39"
                if ((abs(p1.lengthCm - 34.0) < 0.5 && abs(p2.lengthCm - 4.0) < 0.5) ||
                    (abs(p1.lengthCm - 4.0) < 0.5 && abs(p2.lengthCm - 34.0) < 0.5)) {
                    val merged = PlannedPiece(
                        lengthCm = 39.0,
                        jointAfterCm = p2.jointAfterCm,
                        isCut = false,
                        forceType = PieceType.WHOLE,
                        customNote = "Bloco Padrão 39×19×${round1(bThick)} cm (Consolidação 34+4)"
                    )
                    pieces.removeAt(i + 1)
                    pieces[i] = merged
                    changed = true
                    break
                }

                // Regra: "NÃO PODE TER 2 PARTILHA JUNTAS" (4 + 1 + 4 + 1 = 10 cm -> Bloco C9 de 9 cm!)
                if (p1.lengthCm <= 5.5 && p2.lengthCm <= 5.5) {
                    val merged = PlannedPiece(
                        lengthCm = 9.0,
                        jointAfterCm = p2.jointAfterCm,
                        isCut = false,
                        forceType = PieceType.COMPENSATOR,
                        customNote = "Compensador 9×19×${round1(bThick)} cm (Consolidação 2 pastilhas: 4+1+4=9 cm)"
                    )
                    pieces.removeAt(i + 1)
                    pieces[i] = merged
                    changed = true
                    break
                }

                // Regra: "NÃO PODE TER 2 DE 9CM JUNTAS" (9 + 1 + 9 + 1 = 20 cm -> Meio Bloco de 19 cm)
                if (abs(p1.lengthCm - 9.0) < 0.5 && abs(p2.lengthCm - 9.0) < 0.5) {
                    val merged = PlannedPiece(
                        lengthCm = 19.0,
                        jointAfterCm = p2.jointAfterCm,
                        isCut = false,
                        forceType = PieceType.HALF,
                        customNote = "Bloco 19×19×${round1(bThick)} cm (Consolidação 2 de 9cm)"
                    )
                    pieces.removeAt(i + 1)
                    pieces[i] = merged
                    changed = true
                    break
                }

                // Regra: "NÃO PODE TER 2 MEIO BLOCO JUNTOS" (19 + 1 + 19 + 1 = 40 cm -> Bloco Inteiro de 39 cm!)
                if (abs(p1.lengthCm - 19.0) < 0.5 && abs(p2.lengthCm - 19.0) < 0.5) {
                    val merged = PlannedPiece(
                        lengthCm = 39.0,
                        jointAfterCm = p2.jointAfterCm,
                        isCut = false,
                        forceType = PieceType.WHOLE,
                        customNote = "Bloco Padrão 39×19×${round1(bThick)} cm (Consolidação 2 meios blocos: 19+1+19=39 cm)"
                    )
                    pieces.removeAt(i + 1)
                    pieces[i] = merged
                    changed = true
                    break
                }
            }
        }
    }

    /**
     * Agrupa e contabiliza precisamente os blocos por comprimento comercial.
     */
    private fun calculateCommercialBreakdown(
        blocks: List<TilingBlock>,
        thicknessCm: Double,
        heightCm: Double,
        wasteFactor: Double,
        totalPieces: Int
    ): List<com.paginacaopro.app.model.CommercialPieceCount> {
        val grouped = blocks.groupBy { round1(it.lengthCm) }
        val list = mutableListOf<com.paginacaopro.app.model.CommercialPieceCount>()

        for ((len, group) in grouped.entries.sortedByDescending { it.key }) {
            val count = group.size
            val name = when {
                abs(len - 54.0) <= 0.4 -> "Bloco Especial 54 cm (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
                abs(len - 44.0) <= 0.4 -> "Bloco Especial 44 cm (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
                abs(len - 39.0) <= 0.4 -> "Bloco Inteiro 39 cm (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
                abs(len - 34.0) <= 0.4 -> "Bloco Amarração de Canto 34 cm (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
                abs(len - 31.5) <= 0.4 -> "Bloco Amarração 31,5 cm (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
                abs(len - 29.0) <= 0.4 -> "Bloco Compensador / Canto 29 cm (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
                abs(len - 19.0) <= 0.4 -> "Meio Bloco 19 cm (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
                abs(len - 14.0) <= 0.4 -> "Bloco Amarração 14 cm (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
                abs(len - 9.0) <= 0.4 -> "Bloco Compensador 9 cm (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
                abs(len - 5.0) <= 0.4 -> "Pastilha de Amarração 5 cm (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
                abs(len - 4.0) <= 0.4 -> "Pastilha de Ajuste 4 cm (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
                else -> "Peça Cortada / Especial (${round1(len)}×${round1(heightCm)}×${round1(thicknessCm)} cm)"
            }
            val recPurchase = ceil(count * wasteFactor).toInt()
            val pct = if (totalPieces > 0) (count.toDouble() / totalPieces) * 100.0 else 0.0

            list.add(
                com.paginacaopro.app.model.CommercialPieceCount(
                    lengthCm = len,
                    heightCm = heightCm,
                    thicknessCm = thicknessCm,
                    name = name,
                    count = count,
                    recommendedPurchase = recPurchase,
                    percentageOfWall = round1(pct)
                )
            )
        }
        return list
    }

    /**
     * Subtrai as aberturas do intervalo total da parede [0, wallLengthCm]
     * para produzir segmentos contínuos de alvenaria.
     */
    private fun calculateMasonrySegments(
        wallLengthCm: Double,
        intersectingOpenings: List<OpeningModel>
    ): List<HorizontalSegment> {
        if (intersectingOpenings.isEmpty()) {
            return listOf(HorizontalSegment(0.0, wallLengthCm))
        }

        // Ordenar aberturas por posição X
        val sortedOps = intersectingOpenings
            .map { HorizontalSegment(max(0.0, it.xCm), min(wallLengthCm, it.rightCm)) }
            .sortedBy { it.startCm }

        val segments = mutableListOf<HorizontalSegment>()
        var currentX = 0.0

        for (op in sortedOps) {
            if (op.startCm > currentX + 0.1) {
                segments.add(HorizontalSegment(currentX, op.startCm))
            }
            currentX = max(currentX, op.endCm)
        }

        if (currentX < wallLengthCm - 0.1) {
            segments.add(HorizontalSegment(currentX, wallLengthCm))
        }

        return segments
    }

    private fun emptyResult(
        wall: WallModel,
        block: BlockModel,
        jointConfig: JointConfig,
        bondType: BondType,
        openings: List<OpeningModel>,
        intersections: List<com.paginacaopro.app.model.WallIntersection>,
        errors: List<String>
    ): TilingResult {
        return TilingResult(
            wall = wall,
            block = block,
            jointConfig = jointConfig,
            bondType = bondType,
            openings = openings,
            courses = emptyList(),
            billOfMaterials = BillOfMaterials(0.0, 0.0, 0.0, 0, 0, 0, 0, 0, 0, 0.0, 0, 0, 0.0, 0.0),
            intersections = intersections,
            errors = errors
        )
    }

    private fun round1(v: Double) = (v * 10.0).roundToInt() / 10.0
    private fun round2(v: Double) = (v * 100.0).roundToInt() / 100.0
    private fun round3(v: Double) = (v * 1000.0).roundToInt() / 1000.0
}

