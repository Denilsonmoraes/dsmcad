package com.paginacaopro.app

import com.paginacaopro.app.engine.TilingEngine
import com.paginacaopro.app.model.BlockModel
import com.paginacaopro.app.model.BondType
import com.paginacaopro.app.model.JointConfig
import com.paginacaopro.app.model.OpeningModel
import com.paginacaopro.app.model.OpeningType
import com.paginacaopro.app.model.WallModel
import com.paginacaopro.app.svg.SvgGenerator
import org.junit.Test
import java.io.File

class ExportSampleSvgTest {

    @Test
    fun exportRealSampleSvg() {
        val wall = WallModel(
            projectName = "Residencial Bela Vista - Torre 1",
            wallName = "Parede Sala/Varanda (Elevação 01)",
            floor = "1º Pavimento",
            environment = "Sala de Estar",
            lengthCm = 400.0,
            heightCm = 280.0,
            thicknessCm = 14.0,
            wastePercentage = 5.0
        )

        val block = BlockModel(
            name = "Bloco de Concreto 14×19×39 cm",
            manufacturer = "Padrão NBR 6136",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 14.0,
            halfBlockLengthCm = 19.0
        )

        val door = OpeningModel(
            id = 1,
            name = "Porta Balcão P1",
            type = OpeningType.DOOR,
            xCm = 100.0,
            yCm = 0.0,
            widthCm = 90.0,
            heightCm = 210.0,
            hasLintel = true
        )

        val result = TilingEngine.calculate(
            wall = wall,
            block = block,
            jointConfig = JointConfig(horizontalCm = 1.0, verticalCm = 1.0),
            bondType = BondType.RUNNING_HALF,
            openings = listOf(door)
        )

        val svgCode = SvgGenerator.generateSvg(result, isSimplified = false, highDefinition = true)

        val targetFile = File("../exemplo_paginacao_400x280_porta_8k.svg")
        targetFile.writeText(svgCode, Charsets.UTF_8)
    }
}
