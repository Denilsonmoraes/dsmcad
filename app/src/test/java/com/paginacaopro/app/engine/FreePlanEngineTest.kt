package com.paginacaopro.app.engine

import com.paginacaopro.app.model.AuxLine90
import com.paginacaopro.app.model.DimensionAdjustRule
import com.paginacaopro.app.model.FreePlanOpening
import com.paginacaopro.app.model.FreePlanWall
import com.paginacaopro.app.model.JointConfig
import com.paginacaopro.app.model.JunctionKind
import com.paginacaopro.app.model.OpeningType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreePlanEngineTest {

    @Test
    fun `projectOrthogonal90 constrains diagonals to strictly horizontal or vertical`() {
        val horiz = FreePlanEngine.projectOrthogonal90(10f, 10f, 80f, 30f)
        assertEquals(80f, horiz.first, 0.001f)
        assertEquals(10f, horiz.second, 0.001f)

        val vert = FreePlanEngine.projectOrthogonal90(10f, 10f, 25f, 95f)
        assertEquals(10f, vert.first, 0.001f)
        assertEquals(95f, vert.second, 0.001f)
    }

    @Test
    fun `snapPointToPlan snaps to wall endpoints, wall body and aux lines`() {
        val walls = listOf(
            FreePlanWall(
                id = "W1", label = "A",
                startX = 1.0f, startY = 1.0f, endX = 5.0f, endY = 1.0f,
                thicknessCm = 9.0, heightCm = 280.0
            )
        )
        val auxLines = listOf(
            AuxLine90(startX = 1.0f, startY = 3.5f, endX = 6.0f, endY = 3.5f)
        )

        // Snap to endpoint (5.0, 1.0)
        val snappedEnd = FreePlanEngine.snapPointToPlan(4.85f, 1.12f, walls, auxLines)
        assertEquals(5.0f, snappedEnd.first, 0.01f)
        assertEquals(1.0f, snappedEnd.second, 0.01f)

        // Snap to middle of wall body for T-junction (3.0, 1.0)
        val snappedBody = FreePlanEngine.snapPointToPlan(3.0f, 1.18f, walls, auxLines)
        assertEquals(3.0f, snappedBody.first, 0.01f)
        assertEquals(1.0f, snappedBody.second, 0.01f)

        // Snap to auxiliary 90° line at y = 3.5
        val snappedAux = FreePlanEngine.snapPointToPlan(2.4f, 3.62f, walls, auxLines)
        assertEquals(3.5f, snappedAux.second, 0.01f)
    }

    @Test
    fun `createInitialBlockCatalog contains deduplicated 14, 11_5, 9 and 19 cm families with canaletas`() {
        val catalog = FreePlanEngine.createInitialBlockCatalog()
        // Check 14cm pieces: strictly 54, 39, 34, 19, 9, 4 cm (NO 14, 29 or 44 cm pieces exist!)
        val widths14 = catalog.filter { it.thicknessCm == 14.0 && !it.isCanaleta }.map { it.lengthCm }.toSet()
        assertEquals(setOf(54.0, 39.0, 34.0, 19.0, 9.0, 4.0), widths14)

        // Check 11.5cm pieces: 39, 31.5, 19, 9, 4 cm
        val widths115 = catalog.filter { it.thicknessCm == 11.5 && !it.isCanaleta }.map { it.lengthCm }.toSet()
        assertEquals(setOf(39.0, 31.5, 19.0, 9.0, 4.0), widths115)

        // Check 9cm pieces: 39, 29, 19, 9, 4 cm
        val widths9 = catalog.filter { it.thicknessCm == 9.0 && !it.isCanaleta }.map { it.lengthCm }.toSet()
        assertEquals(setOf(39.0, 29.0, 19.0, 9.0, 4.0), widths9)

        // Check canaletas exist for all thicknesses
        assertTrue(catalog.any { it.thicknessCm == 14.0 && it.isCanaleta })
        assertTrue(catalog.any { it.thicknessCm == 11.5 && it.isCanaleta })
        assertTrue(catalog.any { it.thicknessCm == 9.0 && it.isCanaleta })
        assertTrue(catalog.any { it.thicknessCm == 19.0 && it.isCanaleta })
    }

    @Test
    fun `detectWallJunctions identifies L, T and Cross junctions and ignores collinear segments`() {
        val walls = listOf(
            FreePlanWall(id = "W1", label = "A", startX = 0f, startY = 0f, endX = 4f, endY = 0f, thicknessCm = 14.0, heightCm = 280.0, realLengthCm = 400.0),
            FreePlanWall(id = "W2", label = "B", startX = 4f, startY = 0f, endX = 4f, endY = 3f, thicknessCm = 14.0, heightCm = 280.0, realLengthCm = 300.0), // L at (4,0)
            FreePlanWall(id = "W3", label = "C", startX = 2f, startY = 0f, endX = 2f, endY = 3f, thicknessCm = 9.0, heightCm = 280.0, realLengthCm = 300.0),  // T on W1 at (2,0)
            FreePlanWall(id = "W4", label = "D", startX = 0f, startY = 1.5f, endX = 4f, endY = 1.5f, thicknessCm = 9.0, heightCm = 280.0, realLengthCm = 400.0) // Cross with W3 at (2, 1.5)
        )
        val junctions = FreePlanEngine.detectWallJunctions(walls)
        assertTrue(junctions.any { it.kind == JunctionKind.CORNER_L })
        assertTrue(junctions.any { it.kind == JunctionKind.JUNCTION_T })
        assertTrue(junctions.any { it.kind == JunctionKind.CROSS_X })

        // Collinear horizontal segments should NOT be classified as a 90° CORNER_L
        val collinear = listOf(
            FreePlanWall(id = "H1", label = "A", startX = 0f, startY = 0f, endX = 2f, endY = 0f),
            FreePlanWall(id = "H2", label = "B", startX = 2f, startY = 0f, endX = 4f, endY = 0f)
        )
        assertTrue(FreePlanEngine.detectWallJunctions(collinear).isEmpty())
    }

    @Test
    fun `detectClosedRooms identifies all 5 enclosed rooms in starter plan including Banheiro`() {
        val walls = FreePlanEngine.createStarterDrawnPlan()
        val rooms = FreePlanEngine.detectClosedRooms(walls, emptyMap())
        assertEquals(5, rooms.size)
        assertTrue(rooms.all { it.areaM2 > 0.0 })
        assertTrue(rooms.any { it.suggestedType == "Banheiro" })
    }

    @Test
    fun `paginateFreePlanWall satisfies 1cm closing rule without false 40cm cuts and aligns corner blocks at 40cm`() {
        val walls = listOf(
            FreePlanWall(
                id = "W1", label = "A",
                startX = 0f, startY = 0f, endX = 3.2f, endY = 0f,
                thicknessCm = 14.0, heightCm = 280.0, realLengthCm = 320.0,
                includeTopCintaCanaleta = true
            ),
            FreePlanWall(
                id = "W2", label = "B",
                startX = 0f, startY = 0f, endX = 0f, endY = 3.0f,
                thicknessCm = 14.0, heightCm = 280.0, realLengthCm = 300.0
            )
        )
        val catalog = FreePlanEngine.createInitialBlockCatalog()
        val pag = FreePlanEngine.paginateFreePlanWall(
            wall = walls.first(),
            allWalls = walls,
            catalogPieces = catalog,
            jointConfig = JointConfig(1.0, 1.0)
        )

        // 320 cm wall with 14 cm corner L must have 0 cuts and exact commercial fit (closingGap <= 1.0 cm)
        assertTrue("Expected exact commercial fit on 320cm wall", pag.closingAnalysis.hasExactCommercialFit)
        assertTrue(pag.closingAnalysis.closingGapCm <= 1.0)
        assertEquals(0.0, pag.closingAnalysis.adjustmentCutCm, 0.01)
        assertTrue(pag.closingAnalysis.canaletaCount > 0)

        // Verify 1st course (F1, index 0) starts with Bloco Laranja (34cm) at the corner,
        // and 2nd course (F2, index 1) inverts the corner with 14cm (face lateral da Parede B) + 39cm giving exact 20cm half-unit offset!
        val firstCourse = pag.tilingResult.courses[0]
        val secondCourse = pag.tilingResult.courses[1]
        assertTrue(firstCourse.any { kotlin.math.abs(it.lengthCm - 34.0) < 0.01 })
        assertEquals(14.0, secondCourse.first().lengthCm, 0.01)
    }

    @Test
    fun `applyWallRealDimension propagates dimensions and detects geometric loop and opening conflicts`() {
        val walls = listOf(
            FreePlanWall(id = "W1", label = "A", startX = 0f, startY = 0f, endX = 4f, endY = 0f, thicknessCm = 14.0, heightCm = 280.0, realLengthCm = 400.0, isFixedLocked = true),
            FreePlanWall(id = "W2", label = "B", startX = 4f, startY = 0f, endX = 4f, endY = 3f, thicknessCm = 14.0, heightCm = 280.0, realLengthCm = 300.0),
            FreePlanWall(id = "W3", label = "C", startX = 0f, startY = 3f, endX = 4f, endY = 3f, thicknessCm = 14.0, heightCm = 280.0, realLengthCm = 400.0),
            FreePlanWall(id = "W4", label = "D", startX = 0f, startY = 0f, endX = 0f, endY = 3f, thicknessCm = 14.0, heightCm = 280.0, realLengthCm = 300.0)
        )

        // 1. Conflict with locked parallel wall W1 when using KEEP_FIXED_WALLS
        val conflictRes = FreePlanEngine.applyWallRealDimension(
            walls = walls,
            targetWallId = "W3",
            newLengthCm = 320.0,
            rule = DimensionAdjustRule.KEEP_FIXED_WALLS
        )
        assertTrue(conflictRes.hasConflict)

        // 2. AUTO_ADJUST_ROOM on unlocked walls adjusts both parallel walls and translates perpendicular wall W2
        val unlockedWalls = walls.map { it.copy(isFixedLocked = false) }
        val adjustRes = FreePlanEngine.applyWallRealDimension(
            walls = unlockedWalls,
            targetWallId = "W1",
            newLengthCm = 320.0,
            rule = DimensionAdjustRule.AUTO_ADJUST_ROOM
        )
        assertFalse(adjustRes.hasConflict)
        val updatedW1 = adjustRes.updatedWalls.first { it.id == "W1" }
        val updatedW3 = adjustRes.updatedWalls.first { it.id == "W3" }
        val updatedW2 = adjustRes.updatedWalls.first { it.id == "W2" }
        assertEquals(320.0, updatedW1.effectiveLengthCm, 0.1)
        assertEquals(320.0, updatedW3.effectiveLengthCm, 0.1)
        assertEquals(3.2f, updatedW2.startX, 0.01f)

        // 3. Conflict when shrinking wall below existing door/window position
        val wallWithDoor = unlockedWalls.first().copy(
            openings = listOf(
                FreePlanOpening(
                    type = OpeningType.DOOR,
                    label = "P1",
                    widthCm = 90.0,
                    heightCm = 220.0,
                    distanceFromStartCm = 280.0
                )
            )
        )
        val doorConflict = FreePlanEngine.applyWallRealDimension(
            walls = listOf(wallWithDoor),
            targetWallId = wallWithDoor.id,
            newLengthCm = 300.0, // Door ends at 280 + 90 = 370 cm > 300 cm!
            rule = DimensionAdjustRule.ADJUST_CONNECTED
        )
        assertTrue(doorConflict.hasConflict)
    }

    @Test
    fun `amarracao block sizes and mid-wall T-junction placement work for 14cm, 11_5cm and 9cm walls`() {
        val spec14 = FreePlanEngine.getAmarracaoBlockInfo(14.0)
        assertEquals(34.0, spec14.tieLengthCm, 0.01)

        val spec115 = FreePlanEngine.getAmarracaoBlockInfo(11.5)
        assertEquals(31.5, spec115.tieLengthCm, 0.01)

        val spec9 = FreePlanEngine.getAmarracaoBlockInfo(9.0)
        assertEquals(29.0, spec9.tieLengthCm, 0.01)

        val catalog = FreePlanEngine.createInitialBlockCatalog()

        // Test 11.5cm wall with Corner L and a mid-wall Saída T at X = 2.0m
        val walls115 = listOf(
            FreePlanWall(id = "W1", label = "A", startX = 0f, startY = 0f, endX = 4.0f, endY = 0f, thicknessCm = 11.5, heightCm = 280.0, realLengthCm = 400.0),
            FreePlanWall(id = "W2", label = "B", startX = 0f, startY = 0f, endX = 0f, endY = 3.0f, thicknessCm = 11.5, heightCm = 280.0, realLengthCm = 300.0),
            FreePlanWall(id = "WT", label = "C", startX = 2.0f, startY = 0f, endX = 2.0f, endY = 3.0f, thicknessCm = 11.5, heightCm = 280.0, realLengthCm = 300.0)
        )
        val pag115 = FreePlanEngine.paginateFreePlanWall(walls115[0], walls115, catalog, JointConfig(1.0, 1.0))
        val allLengths115 = pag115.tilingResult.courses.flatten().map { it.lengthCm }
        assertTrue("11.5cm wall with corner and T-junction must include 31.5cm amarração block", allLengths115.any { kotlin.math.abs(it - 31.5) < 0.1 })

        // Test 14cm wall with Corner L and mid-wall Saída T:
        // 1ª Fiada: Bloco Laranja 34cm at Corner L + 14cm penetration of Bloco Laranja 34cm at T; 2ª Fiada: Bloco Azul 54cm over T-junction
        val walls14 = walls115.map { it.copy(thicknessCm = 14.0) }
        val pag14 = FreePlanEngine.paginateFreePlanWall(walls14[0], walls14, catalog, JointConfig(1.0, 1.0))
        val f1Blocks14 = pag14.tilingResult.courses[0]
        val f2Blocks14 = pag14.tilingResult.courses[1]
        assertTrue("14cm 1st course must start with 34cm Bloco Laranja", kotlin.math.abs(f1Blocks14.first().lengthCm - 34.0) < 0.1)
        assertTrue("14cm 2nd course at T-junction must use 54cm Bloco Azul", f2Blocks14.any { kotlin.math.abs(it.lengthCm - 54.0) < 0.1 && it.pieceType == com.paginacaopro.app.model.PieceType.T_JUNCTION })

        // Test 9cm wall with Corner L and mid-wall Saída T:
        // 1ª Fiada: 29cm at Corner L + 9cm penetration at T-junction; 2ª Fiada: 9cm at Corner L + 39 + 9 (Travamento T) and NO 29cm in F2!
        val walls9 = walls115.map { it.copy(thicknessCm = 9.0) }
        val pag9 = FreePlanEngine.paginateFreePlanWall(walls9[0], walls9, catalog, JointConfig(1.0, 1.0))
        val f1Blocks9 = pag9.tilingResult.courses[0]
        val f2Blocks9 = pag9.tilingResult.courses[1]
        assertTrue("9cm 1st course must start with 29cm Bloco Laranja and penetrate 9cm at T-junction", kotlin.math.abs(f1Blocks9.first().lengthCm - 29.0) < 0.1 && f1Blocks9.any { kotlin.math.abs(it.lengthCm - 9.0) < 0.1 && it.pieceType == com.paginacaopro.app.model.PieceType.CORNER_TIE })
        assertTrue("9cm 2nd course at T-junction must use 39 + 9 (Travamento T) and never 29+9=40cm", f2Blocks9.any { kotlin.math.abs(it.lengthCm - 9.0) < 0.1 && it.pieceType == com.paginacaopro.app.model.PieceType.T_JUNCTION } && f2Blocks9.none { kotlin.math.abs(it.lengthCm - 29.0) < 0.1 })
    }

    @Test
    fun `door and window jambs strictly use 39x19 or 19x19 and never 29x19, 9x19 or 4x19`() {
        val starterWalls = FreePlanEngine.createStarterDrawnPlan()
        val catalog = FreePlanEngine.createInitialBlockCatalog()
        for (wall in starterWalls) {
            if (wall.openings.isEmpty()) continue
            val pag = FreePlanEngine.paginateFreePlanWall(wall, starterWalls, catalog, JointConfig(1.0, 1.0))
            for ((courseIdx, course) in pag.tilingResult.courses.withIndex()) {
                val yMid = courseIdx * 20.0 + 10.0
                for (op in wall.openings) {
                    val opStart = op.distanceFromStartCm
                    val opEnd = op.distanceFromStartCm + op.widthCm
                    val opBottom = op.sillHeightCm
                    val opTop = op.sillHeightCm + op.heightCm
                    if (yMid > opBottom && yMid < opTop) {
                        // Find block immediately to the left of opening (xCm + lengthCm + 1.0 == opStart)
                        val leftJambBlock = course.firstOrNull { kotlin.math.abs((it.xCm + it.lengthCm + 1.0) - opStart) < 1.5 }
                        if (leftJambBlock != null) {
                            assertTrue(
                                "Wall ${wall.label} opening ${op.label} left jamb on F${courseIdx + 1} used ${leftJambBlock.lengthCm}cm instead of 39 or 19!",
                                kotlin.math.abs(leftJambBlock.lengthCm - 39.0) < 0.1 || kotlin.math.abs(leftJambBlock.lengthCm - 19.0) < 0.1
                            )
                        }
                        // Find block immediately to the right of opening (xCm == opEnd)
                        val rightJambBlock = course.firstOrNull { kotlin.math.abs(it.xCm - opEnd) < 1.5 }
                        if (rightJambBlock != null) {
                            assertTrue(
                                "Wall ${wall.label} opening ${op.label} right jamb on F${courseIdx + 1} used ${rightJambBlock.lengthCm}cm instead of 39 or 19!",
                                kotlin.math.abs(rightJambBlock.lengthCm - 39.0) < 0.1 || kotlin.math.abs(rightJambBlock.lengthCm - 19.0) < 0.1
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `starter plan bathroom walls G and H generate Saida T blocks on Parede C and Parede E`() {
        val starterWalls = FreePlanEngine.createStarterDrawnPlan()
        val catalog = FreePlanEngine.createInitialBlockCatalog()
        val paredeE = starterWalls.first { it.label == "E" }
        val pagE = FreePlanEngine.paginateFreePlanWall(paredeE, starterWalls, catalog, JointConfig(1.0, 1.0))
        val allBlocksE = pagE.tilingResult.courses.flatten()
        assertTrue("Parede E (9cm) must have Saída T blocks for Bathroom walls G and H", allBlocksE.any { it.notes.contains("Saída T") || it.notes.contains("Travamento T") })
    }

    @Test
    fun `snapAndClampWallEndpoint clamps slight overshoot at perpendicular wall corner`() {
        val existingWalls = listOf(
            FreePlanWall(id = "V1", label = "A", startX = 4.0f, startY = 0.0f, endX = 4.0f, endY = 3.0f, thicknessCm = 14.0, heightCm = 280.0)
        )
        // Dragging a horizontal wall from (0, 0) slightly past X = 4.0 (e.g. X = 4.18) clamps cleanly to (4.0, 0.0)
        val clamped = FreePlanEngine.snapAndClampWallEndpoint(
            startX = 0.0f,
            startY = 0.0f,
            rawEndX = 4.18f,
            rawEndY = 0.05f,
            walls = existingWalls,
            auxLines = emptyList()
        )
        assertEquals(4.0f, clamped.first, 0.01f)
        assertEquals(0.0f, clamped.second, 0.01f)
    }
}

