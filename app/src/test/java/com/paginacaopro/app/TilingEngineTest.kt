package com.paginacaopro.app

import com.paginacaopro.app.engine.TilingEngine
import com.paginacaopro.app.model.BlockModel
import com.paginacaopro.app.model.BondType
import com.paginacaopro.app.model.JointConfig
import com.paginacaopro.app.model.OpeningModel
import com.paginacaopro.app.model.OpeningType
import com.paginacaopro.app.model.PieceType
import com.paginacaopro.app.model.WallModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class TilingEngineTest {

    // ========================================================
    // CENÁRIO 1 — PAREDE SIMPLES
    // ========================================================
    @Test
    fun testScenario1_SimpleWall() {
        // Parede: 400 cm de comprimento, 280 cm de altura, espessura 14 cm
        val wall = WallModel(
            projectName = "Obra Teste 1",
            wallName = "Parede Simples 400x280",
            lengthCm = 400.0,
            heightCm = 280.0,
            thicknessCm = 14.0
        )

        // Bloco: 39 x 19 x 14 cm
        val block = BlockModel(
            name = "Bloco Concreto 14x19x39",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 14.0,
            halfBlockLengthCm = 19.0
        )

        // Junta vertical: 1 cm, horizontal: 1 cm
        val joint = JointConfig(horizontalCm = 1.0, verticalCm = 1.0)
        val bond = BondType.RUNNING_HALF

        val result = TilingEngine.calculate(
            wall = wall,
            block = block,
            jointConfig = joint,
            bondType = bond,
            openings = emptyList()
        )

        // 1. Verificação de erros e validade
        assertTrue("O resultado da paginação deve ser válido", result.isValid)
        assertTrue("Não deve conter erros", result.errors.isEmpty())

        // 2. Número de fiadas
        // Altura 280 cm, bloco 19 + junta 1 = 20 cm por fiada -> Exatamente 14 fiadas (14 * 20 = 280 cm)
        assertEquals("Deve ter exatamente 14 fiadas", 14, result.courses.size)
        assertEquals("Total de fiadas no quantitativo deve ser 14", 14, result.billOfMaterials.totalCourses)

        // 3. Ausência de blocos fora dos limites e ausência de sobreposições
        for ((cIdx, course) in result.courses.withIndex()) {
            assertFalse("Fiada $cIdx não deve ser vazia", course.isEmpty())

            var previousBlockRight = 0.0
            for (blk in course) {
                // Limites horizontais
                assertTrue("Bloco ${blk.id} não deve iniciar antes de 0: x=${blk.xCm}", blk.xCm >= 0.0)
                assertTrue("Bloco ${blk.id} não deve ultrapassar 400: right=${blk.rightCm}", blk.rightCm <= 400.001)

                // Limites verticais
                assertTrue("Bloco ${blk.id} não deve iniciar abaixo de 0: y=${blk.yCm}", blk.yCm >= 0.0)
                assertTrue("Bloco ${blk.id} não deve ultrapassar 280: top=${blk.topCm}", blk.topCm <= 280.001)

                // Ausência de sobreposição horizontal na mesma fiada
                assertTrue(
                    "Não deve haver sobreposição na fiada $cIdx: x=${blk.xCm} vs prevRight=$previousBlockRight",
                    blk.xCm >= previousBlockRight - 0.05
                )

                previousBlockRight = blk.rightCm + joint.verticalCm
            }
        }

        // 4. Verificação da modulação alternada (fiada 0 inicia com inteiro 39cm, fiada 1 inicia com meio bloco 19cm)
        val course0FirstBlock = result.courses[0].first()
        val course1FirstBlock = result.courses[1].first()

        assertEquals("Fiada 0 (par) deve iniciar com bloco inteiro", PieceType.WHOLE, course0FirstBlock.pieceType)
        assertEquals("Comprimento da primeira peça da fiada 0 deve ser 39cm", 39.0, course0FirstBlock.lengthCm, 0.01)

        assertEquals("Fiada 1 (ímpar) deve iniciar com meio bloco para amarração alternada", PieceType.HALF, course1FirstBlock.pieceType)
        assertEquals("Comprimento da primeira peça da fiada 1 deve ser 19cm", 19.0, course1FirstBlock.lengthCm, 0.01)

        // 5. Coerência dos quantitativos
        val bom = result.billOfMaterials
        assertEquals("Área bruta deve ser 11.2 m² (4x2.8)", 11.2, bom.grossWallAreaM2, 0.01)
        assertEquals("Área de aberturas deve ser 0", 0.0, bom.openingsAreaM2, 0.01)
        assertEquals("Área líquida deve ser 11.2 m²", 11.2, bom.netWallAreaM2, 0.01)
        assertTrue("Deve haver blocos inteiros contabilizados", bom.wholeBlockCount > 0)
        assertTrue("Deve haver meios blocos contabilizados", bom.halfBlockCount > 0)
        assertTrue("Total de peças físicas calculadas", bom.totalPiecesGeometrical > 0)
    }

    // ========================================================
    // CENÁRIO 2 — PAREDE COM PORTA
    // ========================================================
    @Test
    fun testScenario2_WallWithDoor() {
        val wall = WallModel(
            lengthCm = 400.0,
            heightCm = 280.0,
            thicknessCm = 14.0
        )
        val block = BlockModel(
            name = "Bloco Concreto 14x19x39",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 14.0,
            halfBlockLengthCm = 19.0
        )
        val joint = JointConfig(horizontalCm = 1.0, verticalCm = 1.0)
        val door = OpeningModel(
            id = 1,
            name = "Porta Sala",
            type = OpeningType.DOOR,
            xCm = 100.0,
            yCm = 0.0,
            widthCm = 90.0,
            heightCm = 220.0,
            hasLintel = true
        )

        val result = TilingEngine.calculate(
            wall = wall,
            block = block,
            jointConfig = joint,
            bondType = BondType.RUNNING_HALF,
            openings = listOf(door)
        )

        assertTrue(result.isValid)

        // 1. Ausência de blocos dentro do vão da porta (X entre 100 e 190, Y entre 0 e 220)
        val doorLeft = door.xCm
        val doorRight = door.rightCm
        val doorTop = door.topCm

        for (blk in result.allBlocks) {
            // Se o bloco está na faixa de altura da porta
            if (blk.yCm < doorTop && blk.topCm > 0.0) {
                val overlapsHorizontally = blk.xCm < doorRight - 0.05 && blk.rightCm > doorLeft + 0.05
                assertFalse("Nenhum bloco deve invadir o vão da porta: Bloco ${blk.id} em X=[${blk.xCm}, ${blk.rightCm}], Y=[${blk.yCm}, ${blk.topCm}]", overlapsHorizontally)
            }
        }

        // 2. Paginação acima da porta
        // A porta tem 220 cm de altura. Acima de 220 cm, a parede deve ser contínua de 0 a 400 cm.
        val coursesAboveDoor = result.courses.filter { course ->
            val firstBlk = course.first()
            firstBlk.yCm >= doorTop - 0.1
        }
        assertTrue("Deve haver fiadas sobre a porta", coursesAboveDoor.isNotEmpty())

        for (course in coursesAboveDoor) {
            val first = course.first()
            val last = course.last()
            assertEquals("Fiada acima da porta deve iniciar em X=0", 0.0, first.xCm, 0.01)
            assertTrue("Fiada acima da porta deve alcançar o final da parede", last.rightCm >= 390.0)
        }

        // 3. Verificação de verga
        val lintelPieces = result.allBlocks.filter { it.pieceType == PieceType.LINTEL }
        assertTrue("Deve haver identificação de verga sobre a abertura", lintelPieces.isNotEmpty())

        // 4. Quantitativos com porta subtraída
        val bom = result.billOfMaterials
        val expectedDoorArea = (90.0 * 220.0) / 10000.0 // 1.98 m²
        assertEquals("Área do vão deve ser 1.98 m²", 1.98, bom.openingsAreaM2, 0.01)
        assertEquals("Área líquida deve ser 11.2 - 1.98 = 9.22 m²", 9.22, bom.netWallAreaM2, 0.01)
    }

    // ========================================================
    // CENÁRIO 3 — BLOCO COM DIMENSÕES PERSONALIZADAS
    // ========================================================
    @Test
    fun testScenario3_CustomBlockDimensions() {
        val wall = WallModel(lengthCm = 350.0, heightCm = 260.0)

        // Bloco customizado: 29 x 14 x 11.5 cm com meio bloco 14 cm
        val customBlock = BlockModel(
            name = "Bloco Cerâmico Sob Medida",
            material = "Cerâmico",
            lengthCm = 29.0,
            heightCm = 14.0,
            thicknessCm = 11.5,
            halfBlockLengthCm = 14.0,
            isCustom = true
        )

        val result = TilingEngine.calculate(
            wall = wall,
            block = customBlock,
            jointConfig = JointConfig(horizontalCm = 1.0, verticalCm = 1.0),
            bondType = BondType.RUNNING_HALF,
            openings = emptyList()
        )

        assertTrue("O motor deve gerar paginação com bloco personalizado sem falhar", result.isValid)
        assertTrue(result.courses.isNotEmpty())

        // Verificar altura das fiadas baseada no bloco customizado (14 + 1 = 15 cm por fiada)
        val expectedCourses = Math.ceil(260.0 / 15.0).toInt()
        assertEquals(expectedCourses, result.courses.size)
    }

    // ========================================================
    // CENÁRIO 4 — ALTERAÇÃO DE JUNTA
    // ========================================================
    @Test
    fun testScenario4_JointAdjustment() {
        val wall = WallModel(lengthCm = 400.0, heightCm = 280.0)
        val block = BlockModel(
            name = "Bloco 14x19x39",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 14.0
        )

        // Cálculo com junta de 1.0 cm
        val res1 = TilingEngine.calculate(
            wall = wall,
            block = block,
            jointConfig = JointConfig(1.0, 1.0),
            bondType = BondType.RUNNING_HALF,
            openings = emptyList()
        )

        // Cálculo com junta alterada para 1.2 cm
        val res2 = TilingEngine.calculate(
            wall = wall,
            block = block,
            jointConfig = JointConfig(1.2, 1.2),
            bondType = BondType.RUNNING_HALF,
            openings = emptyList()
        )

        assertTrue(res1.isValid)
        assertTrue(res2.isValid)

        // Com junta maior, o volume de argamassa aumenta e a modulação é recalculada
        assertTrue(
            "Volume de argamassa deve ser maior com junta de 1.2 cm",
            res2.billOfMaterials.mortarJointVolumeM3Estimated > res1.billOfMaterials.mortarJointVolumeM3Estimated
        )
    }

    // ========================================================
    // CENÁRIO 5 — PAREDE MODULAR DE 2,90 m COM ZERO FOLGA
    // ========================================================
    @Test
    fun testScenario5_ZeroGapModularCombination() {
        // Parede de 290 cm (2.90m) conforme solicitado pelo usuário
        val wall = WallModel(
            lengthCm = 290.0,
            heightCm = 260.0,
            thicknessCm = 14.0
        )
        val block = BlockModel(
            name = "Bloco Concreto 14x19x39",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 14.0,
            halfBlockLengthCm = 19.0
        )
        val joint = JointConfig(horizontalCm = 1.0, verticalCm = 1.0)

        val result = TilingEngine.calculate(
            wall = wall,
            block = block,
            jointConfig = joint,
            bondType = BondType.RUNNING_HALF,
            openings = emptyList()
        )

        assertTrue(result.isValid)
        assertTrue(result.courses.isNotEmpty())

        // Em cada fiada, a última peça deve alcançar exatamente o final da parede (290 cm) SEM FOLGA
        for ((cIdx, course) in result.courses.withIndex()) {
            val lastPiece = course.last()
            val folgaFinal = abs(290.0 - lastPiece.rightCm)
            assertTrue(
                "Fiada $cIdx: Não pode haver folga de 1 cm! Folga encontrada: $folgaFinal cm (Right: ${lastPiece.rightCm})",
                folgaFinal <= 0.1
            )
        }

        // Discriminação por tipo de bloco deve existir e conter blocos inteiros, meios ou compensadores
        val breakdown = result.billOfMaterials.pieceBreakdown
        assertTrue("Deve haver discriminação por tipo de bloco", breakdown.isNotEmpty())
        assertTrue("Deve conter contagem de peças de 39 cm", breakdown.any { abs(it.lengthCm - 39.0) <= 0.5 })
    }

    // ========================================================
    // CENÁRIO 6 — PAREDE COM SAÍDA EM T (INTERSEÇÃO A 2,00 m)
    // ========================================================
    @Test
    fun testScenario6_TJunctionIntersection() {
        val wall = WallModel(
            lengthCm = 400.0,
            heightCm = 260.0,
            thicknessCm = 14.0
        )
        val block = BlockModel(
            name = "Bloco Concreto 14x19x39",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 14.0
        )
        val intersection = com.paginacaopro.app.model.WallIntersection(
            xCm = 200.0,
            thicknessCm = 14.0,
            name = "Saída em T - Quarto"
        )

        val result = TilingEngine.calculate(
            wall = wall,
            block = block,
            jointConfig = JointConfig(1.0, 1.0),
            bondType = BondType.RUNNING_HALF,
            openings = emptyList(),
            intersections = listOf(intersection)
        )

        assertTrue(result.isValid)
        assertEquals(1, result.intersections.size)
        assertEquals(200.0, result.intersections[0].xCm, 0.01)

        // Deve haver blocos identificados como T_JUNCTION na região de X=200cm
        val tJunctionBlocks = result.allBlocks.filter { it.pieceType == PieceType.T_JUNCTION }
        assertTrue("Deve haver blocos de amarração da saída em T", tJunctionBlocks.isNotEmpty())

        for (tBlk in tJunctionBlocks) {
            val distToIntersection = abs((tBlk.xCm + tBlk.lengthCm / 2.0) - 200.0)
            assertTrue(
                "Bloco de amarração T deve estar próximo a X=200cm: dist=$distToIntersection",
                distToIntersection < 25.0
            )
        }
    }

    // ========================================================
    // CENÁRIO 7 — PAREDE DE CANTO COM BLOCO DE AMARRAÇÃO (34cm e 29cm)
    // ========================================================
    @Test
    fun testScenario7_CornerWallWithCornerTieBlock() {
        // 1. Parede de 14cm de espessura com canto
        val wall14 = WallModel(
            wallName = "Parede de Canto A",
            lengthCm = 400.0,
            heightCm = 280.0,
            thicknessCm = 14.0,
            notes = "canto"
        )
        val block14 = BlockModel(
            name = "Bloco Concreto 14x19x39",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 14.0,
            halfBlockLengthCm = 19.0
        )
        val result14 = TilingEngine.calculate(
            wall = wall14,
            block = block14,
            jointConfig = JointConfig(1.0, 1.0),
            bondType = BondType.RUNNING_HALF,
            openings = emptyList()
        )

        assertTrue(result14.isValid)
        val course1FirstBlock14 = result14.courses[0].first()
        assertEquals("Em parede de canto 14cm, 1ª fiada (index 0) deve iniciar com bloco de amarração de 34cm", 34.0, course1FirstBlock14.lengthCm, 0.01)
        assertEquals("Peça inicial de 34cm deve ser classificada como CORNER_TIE", PieceType.CORNER_TIE, course1FirstBlock14.pieceType)

        // 2. Parede de 9cm de espessura com canto
        val wall9 = WallModel(
            wallName = "Parede de Canto 9cm",
            lengthCm = 400.0,
            heightCm = 280.0,
            thicknessCm = 9.0,
            notes = "canto"
        )
        val block9 = BlockModel(
            name = "Bloco 9x19x39",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 9.0,
            halfBlockLengthCm = 19.0
        )
        val result9 = TilingEngine.calculate(
            wall = wall9,
            block = block9,
            jointConfig = JointConfig(1.0, 1.0),
            bondType = BondType.RUNNING_HALF,
            openings = emptyList()
        )

        assertTrue(result9.isValid)
        val course1FirstBlock9 = result9.courses[0].first()
        assertEquals("Em parede de canto 9cm, 1ª fiada (index 0) deve iniciar com bloco de amarração de 29cm", 29.0, course1FirstBlock9.lengthCm, 0.01)
        assertEquals("Peça inicial de 29cm deve ser classificada como CORNER_TIE", PieceType.CORNER_TIE, course1FirstBlock9.pieceType)
    }

    // ========================================================
    // CENÁRIO 6 — REGRA CRÍTICA: PAREDE DE 225 CM, PROIBIDO BLOCO 15 CM E PROIBIDO MEIO BLOCO NO MEIO
    // ========================================================
    @Test
    fun testRule_225cm_and_No15cmBlocks_and_NoHalfInMiddle() {
        val wall = WallModel(
            wallName = "Parede 225cm",
            lengthCm = 225.0,
            heightCm = 280.0,
            thicknessCm = 14.0
        )
        val block = BlockModel(
            name = "Bloco 14x19x39",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 14.0,
            halfBlockLengthCm = 19.0
        )
        val result = TilingEngine.calculate(
            wall = wall,
            block = block,
            jointConfig = JointConfig(1.0, 1.0),
            bondType = BondType.RUNNING_HALF,
            openings = emptyList()
        )

        assertTrue(result.isValid)

        // 1. NUNCA deve existir nenhum bloco com comprimento 15 cm em nenhuma fiada
        for ((cIdx, course) in result.courses.withIndex()) {
            for (blk in course) {
                assertFalse(
                    "PROIBIDO BLOCO DE 15CM: fiada $cIdx possui bloco de ${blk.lengthCm}cm",
                    abs(blk.lengthCm - 15.0) < 0.2
                )
            }
        }

        // 2. Meio bloco (19 cm) NUNCA deve estar no meio da parede (apenas nas extremidades x=0 ou x+len=wallLength)
        for ((cIdx, course) in result.courses.withIndex()) {
            for (blk in course) {
                if (blk.pieceType == PieceType.HALF || abs(blk.lengthCm - 19.0) < 0.2) {
                    val isAtStart = blk.xCm < 1.0
                    val isAtEnd = abs(blk.rightCm - wall.lengthCm) < 1.5
                    assertTrue(
                        "PROIBIDO MEIO BLOCO NO MEIO DA PAREDE: fiada $cIdx, x=${blk.xCm}",
                        isAtStart || isAtEnd
                    )
                }
            }
        }
    }
}

