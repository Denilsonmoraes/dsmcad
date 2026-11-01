package com.paginacaopro.app

import com.paginacaopro.app.engine.TilingEngine
import com.paginacaopro.app.model.BlockModel
import com.paginacaopro.app.model.BondType
import com.paginacaopro.app.model.JointConfig
import com.paginacaopro.app.model.OpeningModel
import com.paginacaopro.app.model.OpeningType
import com.paginacaopro.app.model.WallModel
import com.paginacaopro.app.svg.SvgGenerator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SvgGeneratorTest {

    // ========================================================
    // CENÁRIO 5 — EXPORTAÇÃO SVG
    // ========================================================
    @Test
    fun testScenario5_SvgExport() {
        val wall = WallModel(
            projectName = "Edifício Horizonte",
            wallName = "Parede Fachada Norte",
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
        val door = OpeningModel(
            id = 1,
            name = "Porta Principal",
            type = OpeningType.DOOR,
            xCm = 100.0,
            yCm = 0.0,
            widthCm = 90.0,
            heightCm = 210.0
        )

        val tilingResult = TilingEngine.calculate(
            wall = wall,
            block = block,
            jointConfig = JointConfig(1.0, 1.0),
            bondType = BondType.RUNNING_HALF,
            openings = listOf(door)
        )

        val svgContent = SvgGenerator.generateSvg(tilingResult, isSimplified = false, highDefinition = true)

        // 1. Sintaxe XML e tags de abertura/fechamento
        assertTrue("Deve iniciar com cabeçalho XML", svgContent.startsWith("<?xml version=\"1.0\""))
        assertTrue("Deve conter a tag raiz <svg", svgContent.contains("<svg xmlns=\"http://www.w3.org/2000/svg\""))
        assertTrue("Deve finalizar com a tag </svg>", svgContent.trimEnd().endsWith("</svg>"))

        // 2. Existência de viewBox com coordenadas geométricas
        assertTrue("Deve possuir atributo viewBox", svgContent.contains("viewBox=\"0 0 "))

        // 3. Presença dos blocos individuais (<rect com classes de estilo)
        assertTrue("Deve conter blocos inteiros com classe block-whole", svgContent.contains("class=\"block-whole\""))
        assertTrue("Deve conter meios blocos com classe block-half", svgContent.contains("class=\"block-half\""))
        assertTrue("Deve conter retângulos com IDs de fiada", svgContent.contains("id=\"F1_"))

        // 4. Presença das cotas e identificadores
        assertTrue("Deve conter cotas com dim-line", svgContent.contains("class=\"dim-line\""))
        assertTrue("Deve conter texto de cota com valor da parede (400.0 cm)", svgContent.contains("400.0 cm"))
        assertTrue("Deve conter identificador de fiada F1", svgContent.contains("class=\"course-text\">F1<"))

        // 5. Presença do quadro técnico de quantitativos e metadados
        assertTrue("Deve conter quadro de quantitativos", svgContent.contains("QUADRO DE QUANTITATIVOS"))
        assertTrue("Deve conter nome da obra", svgContent.contains("Edifício Horizonte"))
        assertTrue("Deve conter nome da parede", svgContent.contains("Parede Fachada Norte"))
        assertTrue("Deve conter área líquida e blocos", svgContent.contains("COMPRA SUGERIDA"))

        // 6. Ausência de coordenadas NaN ou Infinity
        assertFalse("Não deve conter NaN", svgContent.contains("NaN"))
        assertFalse("Não deve conter Infinity", svgContent.contains("Infinity"))
    }

    @Test
    fun testOpeningFillSvg_ScenarioFromUserPhoto() {
        val openingWall = WallModel(
            projectName = "Fechamento de Vão por Imagem",
            wallName = "Vão de Janela 120×120 cm",
            floor = "Pavimento Superior",
            environment = "Vão Existente em Obra",
            lengthCm = 120.0,
            heightCm = 120.0,
            thicknessCm = 14.0,
            horizontalJointCm = 1.0,
            verticalJointCm = 1.0,
            wastePercentage = 5.0
        )
        val block = BlockModel(
            name = "Bloco de Concreto 14×19×39 cm",
            material = "Concreto",
            lengthCm = 39.0,
            heightCm = 19.0,
            thicknessCm = 14.0,
            halfBlockLengthCm = 19.0
        )
        val result = TilingEngine.calculate(
            wall = openingWall,
            block = block,
            jointConfig = JointConfig(1.0, 1.0),
            bondType = BondType.RUNNING_HALF,
            openings = emptyList()
        )
        val svg = SvgGenerator.generateSvg(result, isSimplified = false, highDefinition = true)
        
        try {
            java.io.File("../exemplo_paginacao_fechamento_vao_120x120_8k.svg").writeText(svg)
        } catch (_: Exception) {}
        try {
            java.io.File("exemplo_paginacao_fechamento_vao_120x120_8k.svg").writeText(svg)
        } catch (_: Exception) {}

        assertTrue("Deve conter título de fechamento de vão", svg.contains("FECHAMENTO E PAGINAÇÃO DE VÃO"))
        assertTrue("Deve conter medidas reais X e Y", svg.contains("120.0 cm"))
        assertFalse("Não deve conter NaN", svg.contains("NaN"))
    }
}
