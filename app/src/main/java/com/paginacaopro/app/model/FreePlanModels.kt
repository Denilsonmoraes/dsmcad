package com.paginacaopro.app.model

import java.util.UUID
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Modos de operação do Estúdio de Planta Baixa:
 * - VISUAL_DRAWING: Modo 1 — Desenho livre sem exigir medidas iniciais.
 * - EXECUTIVE_ENGINEERING: Modo 2 — Cotagem real, portas/janelas e paginação executiva.
 */
enum class PlanEditorMode(val title: String, val subtitle: String) {
    VISUAL_DRAWING("Modo 1: Desenho Visual", "Desenhe paredes e linhas livremente sem medidas"),
    EXECUTIVE_ENGINEERING("Modo 2: Execução & Cotas", "Informe medidas reais, vãos e gere paginação")
}

/**
 * Ferramentas disponíveis na área de desenho da planta baixa.
 */
enum class DrawingTool(val label: String, val description: String) {
    WALL("Parede", "Desenhar paredes externas e internas (90° com conexão magnética)"),
    AUX_LINE_90("Linha 90°", "Desenhar linhas auxiliares exclusivamente horizontais ou verticais"),
    SELECT_MOVE("Selecionar / Mover", "Selecionar parede, informar medida real ou mover elemento"),
    DOOR("Porta", "Toque em uma parede para posicionar uma porta"),
    WINDOW("Janela", "Toque em uma parede para posicionar uma janela"),
    ERASER("Apagar", "Toque em uma parede, linha auxiliar ou abertura para remover")
}

/**
 * Regras de recálculo geométrico ao alterar o comprimento real de uma parede no Módulo de Cotagem.
 */
enum class DimensionAdjustRule(val label: String, val description: String) {
    ADJUST_CONNECTED(
        "Ajustar conectadas",
        "Altera o comprimento selecionado transladando as paredes conectadas"
    ),
    KEEP_FIXED_WALLS(
        "Manter paredes fixas",
        "Preserva paredes já travadas/fixadas e sinaliza caso haja conflito"
    ),
    AUTO_ADJUST_ROOM(
        "Ajustar ambiente",
        "Ajusta automaticamente a parede oposta do cômodo para manter o fechamento ortogonal"
    )
}

/**
 * Espessuras nominais configuráveis para a Ferramenta Parede (Seção 2.B).
 */
enum class NominalWallThickness(val thicknessCm: Double, val label: String, val usageDescription: String) {
    THICK_9(9.0, "9 cm", "Alvenaria com bloco de 9 cm de largura"),
    THICK_11_5(11.5, "11,5 cm", "Alvenaria com bloco de 11,5 cm de largura"),
    THICK_14(14.0, "14 cm", "Alvenaria com bloco de 14 cm de largura"),
    THICK_19(19.0, "19 cm", "Alvenaria com bloco de 19 cm de largura");

    companion object {
        fun fromCm(cm: Double): NominalWallThickness {
            return entries.minByOrNull { abs(it.thicknessCm - cm) } ?: THICK_9
        }
    }
}

/**
 * Categoria técnica de cada peça no cadastro de blocos da obra (Seção 3).
 */
enum class BlockPieceCategory(val label: String) {
    STANDARD("Bloco Principal"),
    HALF("Meio Bloco"),
    CORNER_TIE("Bloco de Amarração / Canto"),
    COMPENSATOR("Bloco Compensador"),
    PASTILHA("Pastilha / Ajuste Modular"),
    SPECIAL_LENGTH("Bloco Especial (44 / 54 cm)"),
    CANALETA("Canaleta (Verga / Cinta)")
}

/**
 * Peça cadastrada e selecionável na biblioteca da obra (Seção 3.1 a 3.4 e 8.C).
 * Distingue comprimento nominal/real, altura, largura, código (B01, B02, C01...),
 * família do produto, dimensão modular (com junta) e disponibilidade no canteiro.
 */
data class CatalogBlockPiece(
    val id: String = UUID.randomUUID().toString(),
    val code: String,                     // Ex: "B01", "B02", "C01"
    val name: String,                     // Ex: "Bloco 39×19×14 cm"
    val familyName: String,               // Ex: "Família Concreto/Cerâmico 14 cm"
    val lengthCm: Double,                 // Comprimento da peça (cm)
    val heightCm: Double,                 // Altura da peça (cm)
    val thicknessCm: Double,              // Largura / Espessura (cm): 9.0, 11.5, 14.0, 19.0
    val category: BlockPieceCategory,
    val isAvailableAtSite: Boolean = true // Disponível na obra para paginação automática
) {
    val isCanaleta: Boolean get() = category == BlockPieceCategory.CANALETA

    /**
     * Dimensão modular longitudinal considerando a junta vertical especificada.
     * Ex: 39 cm + 1 cm de junta = 40 cm de módulo.
     */
    fun modularLengthCm(verticalJointCm: Double): Double = lengthCm + verticalJointCm

    /**
     * Dimensão modular vertical considerando a junta horizontal especificada.
     * Ex: 19 cm + 1 cm de junta = 20 cm de módulo.
     */
    fun modularHeightCm(horizontalJointCm: Double): Double = heightCm + horizontalJointCm

    fun dimensionsFormatted(): String {
        val l = formatDim(lengthCm)
        val h = formatDim(heightCm)
        val t = formatDim(thicknessCm)
        return "$l × $h × $t cm"
    }

    private fun formatDim(v: Double): String {
        return if (abs(v - v.roundToInt()) < 0.05) {
            v.roundToInt().toString()
        } else {
            v.toString().replace('.', ',')
        }
    }
}

/**
 * Linha auxiliar desenhada com a Ferramenta Linha (exclusivamente em ângulos de 90°: horizontal ou vertical).
 */
data class AuxLine90(
    val id: String = UUID.randomUUID().toString(),
    val startX: Float, // Coordenadas em metros no canvas
    val startY: Float,
    val endX: Float,
    val endY: Float
) {
    val isHorizontal: Boolean get() = abs(endY - startY) <= abs(endX - startX)
    val lengthMeters: Float get() = hypot(endX - startX, endY - startY)
}

/**
 * Tipo de porta configurável no Módulo de Portas e Janelas (Seção 7).
 */
enum class DoorStyle(val label: String) {
    SINGLE_SWING("Porta de Abrir (1 Folha)"),
    DOUBLE_SWING("Porta Dupla"),
    SLIDING("Porta de Correr"),
    PASSAGE("Vão Livre / Passagem")
}

/**
 * Abertura (Porta ou Janela) vinculada a uma parede desenhada na planta baixa.
 */
data class FreePlanOpening(
    val id: Long = System.nanoTime(),
    val type: OpeningType,               // DOOR ou WINDOW
    val label: String,                   // Ex: "P1", "J1"
    val widthCm: Double,                 // Largura (cm)
    val heightCm: Double,                // Altura (cm)
    val sillHeightCm: Double = 0.0,      // Peitoril (0 para porta, >0 para janela)
    val distanceFromStartCm: Double,     // Distância a partir da extremidade inicial da parede (cm)
    val referenceEnd: String = "Início", // "Início" ou "Fim"
    val doorStyle: DoorStyle = DoorStyle.SINGLE_SWING,
    val useCanaletaLintel: Boolean = true,     // Verga com canaleta compatível
    val useCanaletaSill: Boolean = true,       // Contraverga com canaleta (para janelas)
    val lintelBearingCm: Double = 20.0         // Apoio lateral mínimo da verga (cm)
) {
    fun toOpeningModel(wallId: Long = 0): OpeningModel {
        return OpeningModel(
            id = id,
            wallId = wallId,
            name = "$label (${if (type == OpeningType.DOOR) doorStyle.label else "Janela"})",
            type = type,
            xCm = distanceFromStartCm,
            yCm = if (type == OpeningType.DOOR) 0.0 else sillHeightCm,
            widthCm = widthCm,
            heightCm = heightCm,
            hasLintel = useCanaletaLintel,
            hasSillBeam = type == OpeningType.WINDOW && useCanaletaSill
        )
    }
}

/**
 * Parede desenhada livremente pelo usuário na planta baixa (Seções 2.B, 5 e 6).
 */
data class FreePlanWall(
    val id: String = UUID.randomUUID().toString(),
    val label: String,                   // "A", "B", "C"...
    val customName: String = "",         // Ex: "Parede Externa Norte", "Divisória Sala/Quarto"
    val startX: Float,                   // Coordenadas 2D no canvas (em metros)
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val thicknessCm: Double = 9.0,       // Espessura inicial padrão = 9 cm (opções: 9, 11.5, 14, 19)
    val heightCm: Double = 280.0,        // Pé-direito / altura da parede (cm)
    val realLengthCm: Double? = null,    // Medida real informada no Módulo de Cotagem (null enquanto apenas visual)
    val isFixedLocked: Boolean = false,  // Se true, mantém esta parede fixa durante recálculos geométricos
    val isExternal: Boolean = true,      // Parede de contorno externo vs. divisória interna
    val includeTopCintaCanaleta: Boolean = false, // Fiada superior em canaleta para cinta de amarração
    val openings: List<FreePlanOpening> = emptyList()
) {
    val isHorizontal: Boolean get() = abs(endY - startY) <= abs(endX - startX)

    val visualLengthMeters: Double
        get() = hypot((endX - startX).toDouble(), (endY - startY).toDouble())

    /**
     * Comprimento efetivo para paginação: usa a medida real cotada se informada,
     * ou a escala visual convertida para centímetros, sempre arredondada para o módulo ABNT de 20 cm.
     */
    val effectiveLengthCm: Double
        get() {
            val raw = realLengthCm ?: (visualLengthMeters * 100.0)
            return (kotlin.math.round(raw / 20.0) * 20.0).coerceAtLeast(80.0)
        }

    val isCotada: Boolean get() = realLengthCm != null

    val displayName: String
        get() = if (customName.isNotBlank()) "Parede $label ($customName)" else "Parede $label"
}

/**
 * Tipo de encontro entre paredes detectado na topologia da planta (Seção 10.2 e 10.3).
 */
enum class JunctionKind(val code: String, val label: String) {
    CORNER_L("L", "Encontro em L (Canto 90°)"),
    JUNCTION_T("T", "Encontro em T"),
    CROSS_X("+", "Encontro em Cruz")
}

/**
 * Encontro geométrico detectado automaticamente entre 2, 3 ou 4 trechos de parede.
 */
data class DetectedWallJunction(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val kind: JunctionKind,
    val connectedWallIds: List<String>,
    val isExternalCorner: Boolean = true
)

/**
 * Ambiente (cômodo fechado) reconhecido automaticamente a partir das paredes (Seção 10.1).
 */
data class DetectedRoom(
    val id: String,
    val defaultIndex: Int,
    val suggestedType: String,           // Ex: "Sala", "Quarto", "Banheiro", "Cozinha"
    val customName: String? = null,      // Nome editado pelo usuário
    val centerX: Float,
    val centerY: Float,
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float,
    val areaM2: Double,
    val boundingWallLabels: List<String>
) {
    val displayTitle: String
        get() = customName ?: "Ambiente $defaultIndex: $suggestedType"
}

/**
 * Análise técnica de fechamento geométrico, cortes e desperdício (Seção 4 - Regra de sobra máxima <= 1 cm).
 * Distingue rigorosamente Folga de Fechamento, Corte de Ajuste, Desperdício e Junta de Argamassa.
 */
data class ClosingAnalysis(
    val wallLabel: String,
    val wallLengthCm: Double,
    val closingGapCm: Double,            // Folga de fechamento (diferença entre comprimento e combinação modular)
    val adjustmentCutCm: Double,         // Corte de ajuste (dimensão da peça cortada quando necessário)
    val estimatedWasteCm: Double,        // Desperdício (material descartado após o corte)
    val mortarJointCm: Double,           // Junta de argamassa prevista entre peças
    val hasExactCommercialFit: Boolean,  // true se fechou com folga <= 1 cm apenas com peças cadastradas sem corte
    val statusTitle: String,
    val technicalExplanation: String,
    val recommendedAlternatives: List<String> = emptyList(),
    val pieceCodeSummary: Map<String, Int> = emptyMap(), // Código (B01, B02, C01) -> Quantidade total na parede
    val canaletaCount: Int = 0
)

/**
 * Resultado executivo completo da paginação de uma parede da Planta Livre.
 */
data class FreePlanWallPagination(
    val wall: FreePlanWall,
    val tilingResult: TilingResult,
    val closingAnalysis: ClosingAnalysis,
    val blockCodeMap: Map<Double, CatalogBlockPiece>, // Comprimento -> Peça do catálogo (B01, B02...)
    val canaletaPiece: CatalogBlockPiece?,            // Canaleta associada (C01...)
    val junctionsAtWall: List<DetectedWallJunction>
)

/**
 * Snapshot imutável para suportar Desfazer (Undo) e Refazer (Redo) (Seção 5 - Etapa 3).
 */
data class FreePlanSnapshot(
    val walls: List<FreePlanWall>,
    val auxLines: List<AuxLine90>,
    val roomCustomNames: Map<String, String>
)
