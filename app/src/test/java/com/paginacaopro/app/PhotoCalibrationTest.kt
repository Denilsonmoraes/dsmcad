package com.paginacaopro.app

import com.paginacaopro.app.engine.TilingEngine
import com.paginacaopro.app.model.BlockModel
import com.paginacaopro.app.model.BondType
import com.paginacaopro.app.model.JointConfig
import com.paginacaopro.app.model.PhotoCalibration
import com.paginacaopro.app.model.WallModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoCalibrationTest {

    // ========================================================
    // CENÁRIO 6 — FOTOGRAFIA E CALIBRAÇÃO MÉTRICA
    // ========================================================
    @Test
    fun testScenario6_PhotoCalibrationAndTransmission() {
        // Exemplo: uma trena de 200 cm fotografada ocupa 800 pixels na imagem (de x=100 a x=900)
        val calibration = PhotoCalibration(
            imageUri = "content://media/external/images/media/42",
            point1X = 100.0f,
            point1Y = 500.0f,
            point2X = 900.0f,
            point2Y = 500.0f,
            realDistanceCm = 200.0
        )

        // 1. Verificação da distância em pixels
        assertEquals("Distância em pixels deve ser 800", 800.0, calibration.pixelDistance, 0.01)

        // 2. Verificação da escala
        // 800 pixels / 200 cm = 4.0 pixels por cm -> 0.25 cm por pixel (2.5 mm por pixel)
        assertEquals("Deve ter 4.0 pixels por cm", 4.0, calibration.pixelsPerCm, 0.01)
        assertEquals("Deve ter 0.25 cm por pixel", 0.25, calibration.cmPerPixel, 0.01)

        // 3. Medição de uma parede na foto usando a calibração
        // Se a parede na foto vai de x=50 a x=1650 (1600 pixels) e altura de 1120 pixels
        val measuredLengthCm = calibration.calculateRealDistance(50f, 500f, 1650f, 500f)
        val measuredHeightCm = calibration.calculateRealDistance(50f, 0f, 50f, 1120f)

        assertEquals("Comprimento medido deve ser 400.0 cm", 400.0, measuredLengthCm, 0.01)
        assertEquals("Altura medida deve ser 280.0 cm", 280.0, measuredHeightCm, 0.01)

        // 4. Transmissão das dimensões confirmadas para o motor geométrico
        val wallFromPhoto = WallModel(
            wallName = "Parede Calibrada por Foto",
            lengthCm = measuredLengthCm,
            heightCm = measuredHeightCm
        )

        val block = BlockModel(
            name = "Bloco 14x19x39",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 14.0
        )

        val result = TilingEngine.calculate(
            wall = wallFromPhoto,
            block = block,
            jointConfig = JointConfig(1.0, 1.0),
            bondType = BondType.RUNNING_HALF,
            openings = emptyList()
        )

        assertTrue("A paginação gerada a partir da calibração fotográfica deve ser válida", result.isValid)
        assertEquals(14, result.courses.size)
        assertEquals(11.2, result.billOfMaterials.grossWallAreaM2, 0.01)
    }
}
