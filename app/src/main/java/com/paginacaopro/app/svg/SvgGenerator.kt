package com.paginacaopro.app.svg

import com.paginacaopro.app.model.JointRenderStyle
import com.paginacaopro.app.model.PieceType
import com.paginacaopro.app.model.TilingResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

enum class CadStyle(val displayName: String) {
    AUTOCAD_DARK("AutoCAD Model Space (Escuro)"),
    AUTOCAD_LIGHT("AutoCAD Prancha Plotagem (Claro)"),
    CONSTRUCTION("Blocos Coloridos Didáticos")
}

object SvgGenerator {

    /**
     * Gera o código XML completo do desenho SVG vetorial com estilo oficial de Cortes do AutoCAD.
     *
     * @param result Resultado do motor de paginação.
     * @param isSimplified Se true, gera versão simplificada apenas com elevação e cotas.
     * @param highDefinition Se true, ajusta a resolução de renderização para alta definição 8k.
     * @param cadStyle Estilo visual (AutoCAD Dark, AutoCAD Light, Construção).
     */
    fun generateSvg(
        result: TilingResult,
        isSimplified: Boolean = false,
        highDefinition: Boolean = true,
        cadStyle: CadStyle = CadStyle.AUTOCAD_DARK
    ): String {
        val wall = result.wall
        val block = result.block
        val bom = result.billOfMaterials

        val isCad = cadStyle == CadStyle.AUTOCAD_DARK || cadStyle == CadStyle.AUTOCAD_LIGHT
        val isDark = cadStyle == CadStyle.AUTOCAD_DARK

        // Escala: 1 unidade SVG = 1 cm real
        val wallW = wall.lengthCm
        val wallH = wall.heightCm

        val baldrameH = 25.0 // Altura da viga baldrame em cm
        val cintaH = 20.0    // Altura da cinta de respaldo superior em cm
        val earthH = 18.0    // Espessura da camada de terreno abaixo da fundação

        // Margens para cotas, cabeçalho, baldrame, cinta e quadro técnico
        val marginTop = if (isSimplified) (if (isCad) 80.0 else 60.0) else 170.0
        val marginBottom = if (isSimplified) (if (isCad) 120.0 else 80.0) else 250.0
        val marginLeft = if (isCad) 120.0 else 90.0 // Espaço para cotas verticais e símbolos de nível AutoCAD
        val marginRight = if (isSimplified) (if (isCad) 70.0 else 50.0) else 290.0

        val totalViewWidth = wallW + marginLeft + marginRight
        val totalViewHeight = wallH + marginTop + marginBottom + (if (isCad) baldrameH + cintaH + earthH else 0.0)

        // Dimensões nominais do viewport (em pixels para exportação e visualização limpa)
        val pixelWidth = if (highDefinition) 7680 else 3840
        val pixelHeight = (pixelWidth * (totalViewHeight / totalViewWidth)).toInt()

        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        val currentDateStr = dateFormat.format(Date())

        // Paleta de Cores do AutoCAD CTB
        val bgFill = when {
            isDark -> "#0D1117"
            cadStyle == CadStyle.AUTOCAD_LIGHT -> "#FFFFFF"
            else -> "#FFFFFF"
        }
        val blockFill = when {
            isDark -> "#161B22"
            cadStyle == CadStyle.AUTOCAD_LIGHT -> "#F8FAFC"
            else -> "#F8FAFC"
        }
        val blockStroke = when {
            isDark -> "#E2E8F0"
            cadStyle == CadStyle.AUTOCAD_LIGHT -> "#0F172A"
            else -> "#334155"
        }
        val concreteHatchColor = when {
            isDark -> "#FDE047"
            cadStyle == CadStyle.AUTOCAD_LIGHT -> "#475569"
            else -> "#CA8A04"
        }
        val concreteBorderColor = when {
            isDark -> "#FDE047" // Amarelo Layer Estrutura CAD
            cadStyle == CadStyle.AUTOCAD_LIGHT -> "#0F172A"
            else -> "#B45309"
        }
        val dimColor = when {
            isDark -> "#FF2D78" // Magenta clássico de cotas AutoCAD
            cadStyle == CadStyle.AUTOCAD_LIGHT -> "#0F172A"
            else -> "#0F172A"
        }
        val levelColor = when {
            isDark -> "#00FFFF" // Ciano clássico de níveis AutoCAD
            cadStyle == CadStyle.AUTOCAD_LIGHT -> "#0F172A"
            else -> "#0284C7"
        }
        val earthStrokeColor = if (isDark) "#475569" else "#94A3B8"

        // Letra identificadora do corte (ex: "A", "B", "C"...)
        val cutLetter = when {
            wall.wallName.contains("Parede ") -> wall.wallName.substringAfter("Parede ").take(1)
            wall.wallName.isNotEmpty() -> wall.wallName.take(1).uppercase()
            else -> "A"
        }

        sb.appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="no"?>""")
        sb.appendLine("""<svg xmlns="http://www.w3.org/2000/svg" """)
        sb.appendLine("""     xmlns:xlink="http://www.w3.org/1999/xlink" """)
        sb.appendLine("""     width="${pixelWidth}px" height="${pixelHeight}px" """)
        sb.appendLine("""     viewBox="0 0 ${format(totalViewWidth)} ${format(totalViewHeight)}">""")

        // Defs e Padrões de Hachura AutoCAD (AR-CONC e EARTH)
        sb.appendLine("""  <defs>""")
        sb.appendLine("""    <!-- Hachura de Concreto Armado AutoCAD (AR-CONC) -->""")
        sb.appendLine("""    <pattern id="cad-hatch-concrete" width="20" height="20" patternUnits="userSpaceOnUse">""")
        sb.appendLine("""      <polygon points="3,3 6,8 1,7" fill="$concreteHatchColor" />""")
        sb.appendLine("""      <polygon points="13,12 17,13 15,17" fill="$concreteHatchColor" />""")
        sb.appendLine("""      <circle cx="8" cy="4" r="0.8" fill="$concreteHatchColor" />""")
        sb.appendLine("""      <circle cx="16" cy="6" r="0.6" fill="$concreteHatchColor" />""")
        sb.appendLine("""      <circle cx="4" cy="14" r="0.7" fill="$concreteHatchColor" />""")
        sb.appendLine("""      <circle cx="10" cy="16" r="0.8" fill="$concreteHatchColor" />""")
        sb.appendLine("""    </pattern>""")
        sb.appendLine("""    <!-- Hachura de Solo/Terra AutoCAD (EARTH) -->""")
        sb.appendLine("""    <pattern id="cad-hatch-earth" width="24" height="24" patternUnits="userSpaceOnUse">""")
        sb.appendLine("""      <line x1="0" y1="24" x2="24" y2="0" stroke="$earthStrokeColor" stroke-width="0.8" />""")
        sb.appendLine("""      <line x1="0" y1="12" x2="12" y2="0" stroke="$earthStrokeColor" stroke-width="0.6" stroke-dasharray="2,2" />""")
        sb.appendLine("""      <line x1="12" y1="24" x2="24" y2="12" stroke="$earthStrokeColor" stroke-width="0.6" stroke-dasharray="2,2" />""")
        sb.appendLine("""    </pattern>""")

        sb.appendLine("""    <style type="text/css">""")
        sb.appendLine("""      .cad-title { font-family: 'Consolas', 'Courier New', monospace, sans-serif; font-size: 22px; font-weight: bold; fill: ${if (isDark) "#FDE047" else "#0F172A"}; }""")
        sb.appendLine("""      .cad-subtitle { font-family: 'Consolas', 'Courier New', monospace, sans-serif; font-size: 13px; fill: ${if (isDark) "#38BDF8" else "#334155"}; }""")
        sb.appendLine("""      .cad-header-info { font-family: 'Consolas', 'Courier New', monospace, sans-serif; font-size: 11px; fill: ${if (isDark) "#94A3B8" else "#475569"}; }""")
        sb.appendLine("""      .cad-block, .block-whole { fill: ${if (isDark) "#334155" else "#CBD5E1"}; stroke: ${if (isDark) "#94A3B8" else "#475569"}; stroke-width: 0.9; }""")
        sb.appendLine("""      .cad-block-tie { fill: ${if (isDark) "#9A3412" else "#FED7AA"}; stroke: ${if (isDark) "#FB923C" else "#EA580C"}; stroke-width: 1.3; }""")
        sb.appendLine("""      .cad-block-comp { fill: ${if (isDark) "#075985" else "#BAE6FD"}; stroke: ${if (isDark) "#38BDF8" else "#0284C7"}; stroke-width: 1.1; }""")
        sb.appendLine("""      .cad-block-t { fill: ${if (isDark) "#0369A1" else "#7DD3FC"}; stroke: ${if (isDark) "#38BDF8" else "#0284C7"}; stroke-width: 1.3; }""")
        sb.appendLine("""      .cad-block-half, .block-half { fill: ${if (isDark) "#065F46" else "#A7F3D0"}; stroke: ${if (isDark) "#34D399" else "#059669"}; stroke-width: 1.0; }""")
        sb.appendLine("""      .cad-block-adj { fill: ${if (isDark) "#3B0764" else "#F3E8FF"}; stroke: ${if (isDark) "#A855F7" else "#7E22CE"}; stroke-width: 1.0; }""")
        sb.appendLine("""      .cad-block-cut { fill: ${if (isDark) "#450A0A" else "#FEE2E2"}; stroke: ${if (isDark) "#EF4444" else "#DC2626"}; stroke-width: 1.1; stroke-dasharray: 2,1; }""")
        sb.appendLine("""      .cad-concrete-beam { fill: url(#cad-hatch-concrete); stroke: $concreteBorderColor; stroke-width: 1.8; }""")
        sb.appendLine("""      .cad-earth { fill: url(#cad-hatch-earth); stroke: $earthStrokeColor; stroke-width: 1.0; }""")
        sb.appendLine("""      .cad-dim-line, .dim-line { stroke: $dimColor; stroke-width: 0.9; }""")
        sb.appendLine("""      .cad-dim-tick { stroke: $dimColor; stroke-width: 1.8; }""")
        sb.appendLine("""      .cad-dim-text { font-family: 'Consolas', 'Courier New', monospace, sans-serif; font-size: 10px; font-weight: bold; fill: $dimColor; text-anchor: middle; }""")
        sb.appendLine("""      .cad-level-text { font-family: 'Consolas', 'Courier New', monospace, sans-serif; font-size: 10px; font-weight: bold; fill: $levelColor; }""")
        sb.appendLine("""      .cad-cut-line { stroke: ${if (isDark) "#EF4444" else "#DC2626"}; stroke-width: 2.2; stroke-dasharray: 10,4,2,4; }""")
        sb.appendLine("""      .cad-cut-circle { fill: ${if (isDark) "#1F2937" else "#FFFFFF"}; stroke: ${if (isDark) "#EF4444" else "#DC2626"}; stroke-width: 2.0; }""")
        sb.appendLine("""      .cad-cut-text { font-family: 'Consolas', 'Courier New', monospace, sans-serif; font-size: 13px; font-weight: bold; fill: ${if (isDark) "#EF4444" else "#DC2626"}; text-anchor: middle; }""")
        sb.appendLine("""      .cad-course-text, .course-text { font-family: 'Consolas', monospace; font-size: 9px; fill: ${if (isDark) "#64748B" else "#94A3B8"}; text-anchor: end; }""")
        sb.appendLine("""    </style>""")
        sb.appendLine("""  </defs>""")

        // Fundo Geral do Desenho
        sb.appendLine("""  <!-- Fundo Geral do Desenho -->""")
        sb.appendLine("""  <rect x="0" y="0" width="${format(totalViewWidth)}" height="${format(totalViewHeight)}" fill="$bgFill" />""")

        // Posição base da parede no SVG
        val wallSvgX = marginLeft
        val wallSvgY = marginTop + (if (isCad) cintaH else 0.0)

        // ==========================================
        // 1. CABEÇALHO TÉCNICO CAD
        // ==========================================
        if (!isSimplified) {
            sb.appendLine("""  <!-- Cabeçalho Técnico AutoCAD -->""")
            sb.appendLine("""  <g id="cad-header">""")
            val isOpeningFill = wall.wallName.contains("Vão", ignoreCase = true) || wall.projectName.contains("Vão", ignoreCase = true)
            val docTitle = if (isOpeningFill) "CORTE TÉCNICO AUTOCAD — FECHAMENTO E PAGINAÇÃO DE VÃO" else "CORTE $cutLetter-$cutLetter • DETALHAMENTO DE ALVENARIA ESTRUTURAL"
            val subTitleStr = "OBRA: ${escapeXml(wall.projectName)} • ELEMENTO: ${escapeXml(wall.wallName)} • ESCALA 1:25"

            val headerBg = if (isDark) "#161B22" else "#F8FAFC"
            val headerBorder = if (isDark) "#30363D" else "#CBD5E1"
            sb.appendLine("""    <rect x="30" y="25" width="${format(totalViewWidth - 60)}" height="95" fill="$headerBg" stroke="$headerBorder" stroke-width="1.2" rx="3" />""")
            sb.appendLine("""    <text x="45" y="55" class="cad-title">$docTitle</text>""")
            sb.appendLine("""    <text x="45" y="75" class="cad-subtitle">$subTitleStr</text>""")
            sb.appendLine("""    <line x1="45" y1="83" x2="${format(totalViewWidth - 45)}" y2="83" stroke="$headerBorder" stroke-width="1" />""")

            val info1 = "Dimensões: ${format(wall.lengthCm)} × ${format(wall.heightCm)} cm (Esp: ${format(wall.thicknessCm)} cm) | Bloco: ${escapeXml(block.name)} (${format(block.lengthCm)}×${format(block.heightCm)}×${format(block.thicknessCm)} cm)"
            val info2 = "Juntas: Horiz: ${format(result.jointConfig.horizontalCm)} cm / Vert: ${format(result.jointConfig.verticalCm)} cm | Amarração: ${result.bondType.displayName} | Emissão: $currentDateStr"
            sb.appendLine("""    <text x="45" y="100" class="cad-header-info">$info1</text>""")
            sb.appendLine("""    <text x="45" y="113" class="cad-header-info">$info2</text>""")
            sb.appendLine("""  </g>""")
        }

        // ==========================================
        // 2. ELEMENTOS ESTRUTURAIS DO CORTE AUTOCAD
        // ==========================================
        if (isCad) {
            sb.appendLine("""  <!-- Elementos Estruturais do Corte (Cinta de Respaldo e Baldrame) -->""")
            sb.appendLine("""  <g id="cad-structural-elements">""")

            // A) Cinta de Respaldo / Laje Superior
            val cintaY = wallSvgY - cintaH
            sb.appendLine("""    <!-- Cinta de Amarração / Respaldo Superior -->""")
            sb.appendLine("""    <rect x="${format(wallSvgX)}" y="${format(cintaY)}" width="${format(wallW)}" height="${format(cintaH)}" class="cad-concrete-beam" />""")
            sb.appendLine("""    <text x="${format(wallSvgX + wallW / 2.0)}" y="${format(cintaY + cintaH / 2.0 + 3.0)}" font-family="Consolas, monospace" font-size="9px" font-weight="bold" fill="$concreteBorderColor" text-anchor="middle">CINTA DE AMARRAÇÃO / LAJE (CONCRETO ARMADO 14×20 cm)</text>""")

            // B) Viga Baldrame / Fundação Inferior
            val baldrameY = wallSvgY + wallH
            sb.appendLine("""    <!-- Viga Baldrame Inferior -->""")
            sb.appendLine("""    <rect x="${format(wallSvgX)}" y="${format(baldrameY)}" width="${format(wallW)}" height="${format(baldrameH)}" class="cad-concrete-beam" />""")
            sb.appendLine("""    <text x="${format(wallSvgX + wallW / 2.0)}" y="${format(baldrameY + baldrameH / 2.0 + 3.0)}" font-family="Consolas, monospace" font-size="10px" font-weight="bold" fill="$concreteBorderColor" text-anchor="middle">VIGA BALDRAME VB-1 (CONCRETO ARMADO 14×25 cm)</text>""")

            // C) Terreno / Solo Compactado abaixo do Baldrame
            val earthY = baldrameY + baldrameH
            sb.appendLine("""    <!-- Solo Compactado / Fundação -->""")
            sb.appendLine("""    <rect x="${format(wallSvgX - 10.0)}" y="${format(earthY)}" width="${format(wallW + 20.0)}" height="${format(earthH)}" class="cad-earth" />""")
            sb.appendLine("""    <line x1="${format(wallSvgX - 15.0)}" y1="${format(earthY)}" x2="${format(wallSvgX + wallW + 15.0)}" y2="${format(earthY)}" stroke="$earthStrokeColor" stroke-width="1.5" />""")
            sb.appendLine("""  </g>""")
        }

        // ==========================================
        // 3. ELEVAÇÃO DA ALVENARIA (BLOCOS E FIADAS)
        // ==========================================
        sb.appendLine("""  <!-- Elevação da Alvenaria (Blocos) -->""")
        sb.appendLine("""  <g id="wall-elevation" transform="translate($wallSvgX, $wallSvgY)">""")

        // Argamassa de fundo da alvenaria
        sb.appendLine("""    <rect x="0" y="0" width="${format(wallW)}" height="${format(wallH)}" fill="${if (isDark) "#0A0D14" else "#E2E8F0"}" stroke="$blockStroke" stroke-width="1.2" />""")

        // Desenhar os blocos calculados fiada por fiada
        for (course in result.courses) {
            for (blk in course) {
                val blockSvgY = wallH - (blk.yCm + blk.heightCm)
                val cssClass = when {
                    !isCad -> when (blk.pieceType) {
                        PieceType.WHOLE -> "block-whole"
                        PieceType.HALF -> "block-half"
                        PieceType.CORNER_TIE -> "cad-block-tie"
                        PieceType.COMPENSATOR -> "cad-block-comp"
                        PieceType.T_JUNCTION -> "cad-block-t"
                        PieceType.ADJUSTMENT -> "cad-block-adj"
                        PieceType.CUT -> "cad-block-cut"
                        PieceType.SPECIAL -> "block-whole"
                        PieceType.LINTEL -> "cad-concrete-beam"
                    }
                    blk.pieceType == PieceType.CORNER_TIE -> "cad-block-tie"
                    blk.pieceType == PieceType.T_JUNCTION -> "cad-block-t"
                    blk.pieceType == PieceType.HALF -> "block-half"
                    blk.pieceType == PieceType.COMPENSATOR -> "cad-block-comp"
                    blk.pieceType == PieceType.ADJUSTMENT -> "cad-block-adj"
                    blk.pieceType == PieceType.CUT -> "cad-block-cut"
                    blk.pieceType == PieceType.LINTEL -> "cad-concrete-beam"
                    else -> "block-whole"
                }

                sb.append("""      <rect id="${blk.id}" x="${format(blk.xCm)}" y="${format(blockSvgY)}" width="${format(blk.lengthCm)}" height="${format(blk.heightCm)}" class="$cssClass">""")
                sb.append("""<title>${blk.id}: ${blk.pieceType.label} (${format(blk.lengthCm)}x${format(blk.heightCm)} cm) ${escapeXml(blk.notes)}</title>""")
                sb.appendLine("""</rect>""")
            }
        }

        // Desenhar Interseções de Saída em T / Encontro de Parede
        for (inter in result.intersections) {
            val interX = inter.xCm
            sb.appendLine("""    <!-- Saída em T / Interseção: ${escapeXml(inter.name)} -->""")
            sb.appendLine("""    <line x1="${format(interX)}" y1="0" x2="${format(interX)}" y2="${format(wallH)}" stroke="${if (isDark) "#38BDF8" else "#0284C7"}" stroke-width="1.8" stroke-dasharray="6,4" />""")
            sb.appendLine("""    <rect x="${format(interX - inter.thicknessCm / 2.0)}" y="-18" width="${format(inter.thicknessCm)}" height="14" fill="${if (isDark) "#0C4A6E" else "#E0F2FE"}" stroke="${if (isDark) "#38BDF8" else "#0284C7"}" stroke-width="0.8" rx="2" />""")
            sb.appendLine("""    <text x="${format(interX)}" y="-8" font-family="Consolas, monospace" font-size="8px" font-weight="bold" fill="${if (isDark) "#38BDF8" else "#0369A1"}" text-anchor="middle">T: ${format(inter.xCm)}cm</text>""")
        }

        // Desenhar Vãos (Portas e Janelas) — Verga e Contraverga já são blocos canaleta reais nas fiadas!
        for (op in result.openings) {
            val opSvgY = wallH - (op.yCm + op.heightCm)

            // Vão de Abertura
            sb.appendLine("""    <!-- Vão: ${escapeXml(op.name)} -->""")
            sb.appendLine("""    <g id="opening-${op.id}">""")
            sb.appendLine("""      <rect x="${format(op.xCm)}" y="${format(opSvgY)}" width="${format(op.widthCm)}" height="${format(op.heightCm)}" fill="${if (isDark) "#0A0F1D" else "#F1F5F9"}" stroke="#EF4444" stroke-width="1.5" stroke-dasharray="5,4" />""")
            sb.appendLine("""      <text x="${format(op.xCm + op.widthCm / 2.0)}" y="${format(opSvgY + op.heightCm / 2.0)}" font-family="Consolas, monospace" font-size="11px" font-weight="bold" fill="#EF4444" text-anchor="middle">${escapeXml(op.name)} (${format(op.widthCm)}×${format(op.heightCm)})</text>""")
            sb.appendLine("""    </g>""")
        }

        // Contorno da Alvenaria
        sb.appendLine("""    <rect x="0" y="0" width="${format(wallW)}" height="${format(wallH)}" fill="none" stroke="$blockStroke" stroke-width="1.8" />""")
        sb.appendLine("""  </g>""")

        // ==========================================
        // 4. SÍMBOLOS DE NÍVEL DE CORTE AUTOCAD
        // ==========================================
        if (isCad) {
            sb.appendLine("""  <!-- Símbolos de Nível de Corte AutoCAD -->""")
            sb.appendLine("""  <g id="cad-levels">""")
            val levelX = wallSvgX - 25.0

            // Nível Topo da Cinta
            drawCadLevelSymbol(sb, levelX, wallSvgY - cintaH, "+${format((wallH + cintaH) / 100.0)}", "TOPO CINTA", levelColor)
            // Nível Respaldo Alvenaria
            drawCadLevelSymbol(sb, levelX, wallSvgY, "+${format(wallH / 100.0)}", "RESPALDO", levelColor)

            // Nível Verga e Peitoril (se houver vãos)
            for (op in result.openings) {
                val opSvgVergaY = wallSvgY + wallH - op.topCm
                drawCadLevelSymbol(sb, levelX, opSvgVergaY, "+${format(op.topCm / 100.0)}", "VERGA", levelColor)
                if (op.yCm > 5.0) {
                    val opSvgPeitY = wallSvgY + wallH - op.yCm
                    drawCadLevelSymbol(sb, levelX, opSvgPeitY, "+${format(op.yCm / 100.0)}", "PEITORIL", levelColor)
                }
            }

            // Nível N.P.O. (Piso Osso / Topo do Baldrame)
            drawCadLevelSymbol(sb, levelX, wallSvgY + wallH, "±0.00", "N.P.O.", levelColor)
            // Nível Fundo Baldrame
            drawCadLevelSymbol(sb, levelX, wallSvgY + wallH + baldrameH, "-${format(baldrameH / 100.0)}", "FUNDO VB", levelColor)
            sb.appendLine("""  </g>""")
        }

        // ==========================================
        // 5. INDICAÇÃO DE LINHA DE CORTE VERTICAL CAD (A - A)
        // ==========================================
        if (isCad) {
            sb.appendLine("""  <!-- Linhas e Chamadas de Corte Vertical A-A -->""")
            sb.appendLine("""  <g id="cad-cut-indicators">""")
            val cutLineTop = wallSvgY - cintaH - 25.0
            val cutLineBottom = wallSvgY + wallH + baldrameH + earthH + 15.0

            // Linha de Corte Esquerda
            val cutLeftX = wallSvgX - 15.0
            sb.appendLine("""    <line x1="${format(cutLeftX)}" y1="${format(cutLineTop)}" x2="${format(cutLeftX)}" y2="${format(cutLineBottom)}" class="cad-cut-line" />""")
            drawCutCallout(sb, cutLeftX, cutLineTop, cutLetter, isPointingRight = true)
            drawCutCallout(sb, cutLeftX, cutLineBottom, cutLetter, isPointingRight = true)

            // Linha de Corte Direita
            val cutRightX = wallSvgX + wallW + 15.0
            sb.appendLine("""    <line x1="${format(cutRightX)}" y1="${format(cutLineTop)}" x2="${format(cutRightX)}" y2="${format(cutLineBottom)}" class="cad-cut-line" />""")
            drawCutCallout(sb, cutRightX, cutLineTop, cutLetter, isPointingRight = false)
            drawCutCallout(sb, cutRightX, cutLineBottom, cutLetter, isPointingRight = false)
            sb.appendLine("""  </g>""")
        }

        // ==========================================
        // 6. COTAS TÉCNICAS AUTOCAD
        // ==========================================
        sb.appendLine("""  <!-- Cotas Técnicas Estilo AutoCAD -->""")
        sb.appendLine("""  <g id="cad-dimensions">""")

        // Indicador lateral das fiadas (à esquerda)
        for ((idx, course) in result.courses.withIndex()) {
            val firstBlock = course.firstOrNull() ?: continue
            val courseSvgY = wallSvgY + wallH - (firstBlock.yCm + firstBlock.heightCm / 2.0)
            sb.appendLine("""    <text x="${format(wallSvgX - 8.0)}" y="${format(courseSvgY + 3.0)}" class="course-text">F${idx + 1}</text>""")
        }

        // Cota Vertical Total (à esquerda da parede)
        val dimLeftX = wallSvgX - 55.0
        drawVerticalDimension(sb, dimLeftX, wallSvgY + wallH, wallSvgY, "${format(wallH)} cm")

        // Cota Horizontal Total (abaixo do baldrame)
        val dimBottomBase = wallSvgY + wallH + (if (isCad) baldrameH + earthH + 20.0 else 35.0)
        drawHorizontalDimension(sb, wallSvgX, wallSvgX + wallW, dimBottomBase, "${format(wallW)} cm")

        // Cotas horizontais parciais para aberturas (se houver vãos)
        if (result.openings.isNotEmpty()) {
            val dimOpY = dimBottomBase + 25.0
            var lastX = wallSvgX
            for (op in result.openings.sortedBy { it.xCm }) {
                val opStartX = wallSvgX + op.xCm
                val opEndX = wallSvgX + op.rightCm

                // Trecho antes do vão
                if (opStartX > lastX + 1.0) {
                    val dist = op.xCm - (lastX - wallSvgX)
                    drawHorizontalDimension(sb, lastX, opStartX, dimOpY, "${format(dist)} cm")
                }
                // Vão
                drawHorizontalDimension(sb, opStartX, opEndX, dimOpY, "${format(op.widthCm)} cm")
                lastX = opEndX
            }
            // Trecho final após o último vão
            if (wallSvgX + wallW > lastX + 1.0) {
                val dist = wallW - (lastX - wallSvgX)
                drawHorizontalDimension(sb, lastX, wallSvgX + wallW, dimOpY, "${format(dist)} cm")
            }
        }
        sb.appendLine("""  </g>""")

        // ==========================================
        // 7. RÓTULO DE TÍTULO AUTOCAD NO RODAPÉ
        // ==========================================
        val titleY = totalViewHeight - 60.0
        val titleX = wallSvgX + wallW / 2.0
        sb.appendLine("""  <!-- Título do Desenho Estilo AutoCAD -->""")
        sb.appendLine("""  <g id="cad-drawing-title">""")
        sb.appendLine("""    <text x="${format(titleX)}" y="${format(titleY)}" class="cad-title" text-anchor="middle">CORTE $cutLetter-$cutLetter (LONGITUDINAL)</text>""")
        sb.appendLine("""    <line x1="${format(titleX - 160.0)}" y1="${format(titleY + 6.0)}" x2="${format(titleX + 160.0)}" y2="${format(titleY + 6.0)}" stroke="${if (isDark) "#FDE047" else "#0F172A"}" stroke-width="2.5" />""")
        sb.appendLine("""    <line x1="${format(titleX - 160.0)}" y1="${format(titleY + 10.0)}" x2="${format(titleX + 160.0)}" y2="${format(titleY + 10.0)}" stroke="${if (isDark) "#FDE047" else "#0F172A"}" stroke-width="1.0" />""")
        sb.appendLine("""    <text x="${format(titleX)}" y="${format(titleY + 24.0)}" class="cad-subtitle" text-anchor="middle">ELEVAÇÃO TÉCNICA E MODULAÇÃO DE ALVENARIA • ESCALA 1:25</text>""")
        sb.appendLine("""  </g>""")

        // ==========================================
        // 8. QUADRO TÉCNICO DE QUANTITATIVOS CAD
        // ==========================================
        if (!isSimplified) {
            val tableX = wallSvgX + wallW + 35.0
            val tableY = wallSvgY

            val extraRowsHeight = (bom.pieceBreakdown.size * 16.0)
            val tableHeight = 280.0 + extraRowsHeight
            val tableBg = if (isDark) "#161B22" else "#F8FAFC"
            val tableBorder = if (isDark) "#30363D" else "#CBD5E1"
            val textHeadColor = if (isDark) "#FDE047" else "#0F172A"
            val textCellColor = if (isDark) "#E2E8F0" else "#334155"

            sb.appendLine("""  <!-- Quadro Técnico de Quantitativos -->""")
            sb.appendLine("""  <g id="quantitativos-table" transform="translate($tableX, $tableY)">""")
            sb.appendLine("""    <rect x="0" y="0" width="250" height="${format(tableHeight)}" fill="$tableBg" stroke="$tableBorder" stroke-width="1.2" rx="3" />""")
            sb.appendLine("""    <text x="15" y="24" font-family="Consolas, monospace" font-size="12px" font-weight="bold" fill="$textHeadColor">QUADRO DE QUANTITATIVOS</text>""")
            sb.appendLine("""    <line x1="15" y1="32" x2="235" y2="32" stroke="$tableBorder" stroke-width="1" />""")

            val rows = mutableListOf(
                "Área Bruta da Parede" to "${format(bom.grossWallAreaM2)} m²",
                "Área dos Vãos" to "${format(bom.openingsAreaM2)} m²",
                "Área Líquida Alvenaria" to "${format(bom.netWallAreaM2)} m²",
                "Total de Fiadas" to "${bom.totalCourses}",
                "Total de Peças Parede" to "${bom.totalPiecesGeometrical} un",
                "Margem de Perdas" to "${format(bom.wastePercentage)} %",
                "COMPRA SUGERIDA (Blocos)" to "${bom.recommendedPurchaseBlocks} un",
                "COMPRA SUGERIDA (Meios)" to "${bom.recommendedPurchaseHalfBlocks} un"
            )

            var rowY = 50.0
            for ((label, value) in rows) {
                sb.appendLine("""    <text x="15" y="${format(rowY)}" font-family="Consolas, monospace" font-size="10px" fill="$textCellColor">$label:</text>""")
                sb.appendLine("""    <text x="235" y="${format(rowY)}" font-family="Consolas, monospace" font-size="10px" font-weight="bold" fill="$textCellColor" text-anchor="end">$value</text>""")
                rowY += 17.0
            }

            // Sub-tabela: Discriminação por Peça Comercial
            if (bom.pieceBreakdown.isNotEmpty()) {
                rowY += 6.0
                sb.appendLine("""    <line x1="15" y1="${format(rowY)}" x2="235" y2="${format(rowY)}" stroke="$tableBorder" stroke-width="1" />""")
                rowY += 16.0
                sb.appendLine("""    <text x="15" y="${format(rowY)}" font-family="Consolas, monospace" font-size="10px" font-weight="bold" fill="$textHeadColor">DISCRIMINAÇÃO POR PEÇA:</text>""")
                rowY += 17.0

                for (piece in bom.pieceBreakdown) {
                    val labelShort = "${format(piece.lengthCm)} cm (${piece.name.substringBefore(" (")})"
                    val valueShort = "${piece.count} un (Compra: ${piece.recommendedPurchase})"
                    sb.appendLine("""    <text x="15" y="${format(rowY)}" font-family="Consolas, monospace" font-size="9px" fill="$textCellColor">$labelShort</text>""")
                    sb.appendLine("""    <text x="235" y="${format(rowY)}" font-family="Consolas, monospace" font-size="9px" font-weight="bold" fill="$textHeadColor" text-anchor="end">$valueShort</text>""")
                    rowY += 16.0
                }
            }
            sb.appendLine("""  </g>""")
        }

        sb.appendLine("""</svg>""")
        return sb.toString()
    }

    /**
     * Desenha o símbolo de nível em corte arquitetônico oficial do AutoCAD.
     */
    private fun drawCadLevelSymbol(sb: StringBuilder, x: Double, y: Double, levelValue: String, label: String, color: String) {
        val r = 5.0
        sb.appendLine("""    <!-- Nível CAD: $label ($levelValue) -->""")
        sb.appendLine("""    <g>""")
        // Círculo de nível
        sb.appendLine("""      <circle cx="${format(x)}" cy="${format(y)}" r="${format(r)}" fill="none" stroke="$color" stroke-width="1.0" />""")
        // Quadrantes preenchidos alternados (superior direito e inferior esquerdo)
        sb.appendLine("""      <path d="M ${format(x)} ${format(y)} L ${format(x + r)} ${format(y)} A ${format(r)} ${format(r)} 0 0 0 ${format(x)} ${format(y - r)} Z" fill="$color" />""")
        sb.appendLine("""      <path d="M ${format(x)} ${format(y)} L ${format(x - r)} ${format(y)} A ${format(r)} ${format(r)} 0 0 0 ${format(x)} ${format(y + r)} Z" fill="$color" />""")
        // Cruz ortogonal
        sb.appendLine("""      <line x1="${format(x - r - 2)}" y1="${format(y)}" x2="${format(x + r + 2)}" y2="${format(y)}" stroke="$color" stroke-width="0.8" />""")
        sb.appendLine("""      <line x1="${format(x)}" y1="${format(y - r - 2)}" x2="${format(x)}" y2="${format(y + r + 2)}" stroke="$color" stroke-width="0.8" />""")
        // Linha horizontal apontando para a parede
        sb.appendLine("""      <line x1="${format(x + r)}" y1="${format(y)}" x2="${format(x + 22.0)}" y2="${format(y)}" stroke="$color" stroke-width="0.8" />""")
        // Textos
        sb.appendLine("""      <text x="${format(x - r - 3.0)}" y="${format(y - 2.0)}" class="cad-level-text" text-anchor="end">$levelValue</text>""")
        sb.appendLine("""      <text x="${format(x - r - 3.0)}" y="${format(y + 8.0)}" font-family="Consolas, monospace" font-size="8px" fill="$color" text-anchor="end">$label</text>""")
        sb.appendLine("""    </g>""")
    }

    /**
     * Desenha a chamada de corte com círculo e seta apontadora nas extremidades.
     */
    private fun drawCutCallout(sb: StringBuilder, x: Double, y: Double, letter: String, isPointingRight: Boolean) {
        val r = 8.0
        val arrowDir = if (isPointingRight) 1.0 else -1.0
        sb.appendLine("""    <circle cx="${format(x)}" cy="${format(y)}" r="${format(r)}" class="cad-cut-circle" />""")
        sb.appendLine("""    <text x="${format(x)}" y="${format(y + 4.5)}" class="cad-cut-text">$letter</text>""")
        // Seta de corte
        val arrowTipX = x + (r + 7.0) * arrowDir
        val arrowBaseX = x + (r + 1.0) * arrowDir
        sb.appendLine("""    <polygon points="${format(arrowTipX)},${format(y)} ${format(arrowBaseX)},${format(y - 5.0)} ${format(arrowBaseX)},${format(y + 5.0)}" fill="${if (letter.isNotEmpty()) "#EF4444" else "#DC2626"}" />""")
    }

    private fun drawHorizontalDimension(sb: StringBuilder, x1: Double, x2: Double, y: Double, text: String) {
        val midX = (x1 + x2) / 2.0
        sb.appendLine("""    <!-- Cota H AutoCAD: $text -->""")
        // Linha de cota
        sb.appendLine("""    <line x1="${format(x1 - 4.0)}" y1="${format(y)}" x2="${format(x2 + 4.0)}" y2="${format(y)}" class="dim-line" />""")
        // Architectural Ticks a 45°
        sb.appendLine("""    <line x1="${format(x1 - 3.5)}" y1="${format(y + 3.5)}" x2="${format(x1 + 3.5)}" y2="${format(y - 3.5)}" class="cad-dim-tick" />""")
        sb.appendLine("""    <line x1="${format(x2 - 3.5)}" y1="${format(y + 3.5)}" x2="${format(x2 + 3.5)}" y2="${format(y - 3.5)}" class="cad-dim-tick" />""")
        // Linhas de extensão passando além da cota
        sb.appendLine("""    <line x1="${format(x1)}" y1="${format(y - 6.0)}" x2="${format(x1)}" y2="${format(y + 6.0)}" class="dim-line" stroke-width="0.6" />""")
        sb.appendLine("""    <line x1="${format(x2)}" y1="${format(y - 6.0)}" x2="${format(x2)}" y2="${format(y + 6.0)}" class="dim-line" stroke-width="0.6" />""")
        sb.appendLine("""    <text x="${format(midX)}" y="${format(y - 4.5)}" class="cad-dim-text">$text</text>""")
    }

    private fun drawVerticalDimension(sb: StringBuilder, x: Double, yBottom: Double, yTop: Double, text: String) {
        val midY = (yBottom + yTop) / 2.0
        sb.appendLine("""    <!-- Cota V AutoCAD: $text -->""")
        // Linha de cota
        sb.appendLine("""    <line x1="${format(x)}" y1="${format(yBottom + 4.0)}" x2="${format(x)}" y2="${format(yTop - 4.0)}" class="dim-line" />""")
        // Architectural Ticks a 45°
        sb.appendLine("""    <line x1="${format(x - 3.5)}" y1="${format(yBottom + 3.5)}" x2="${format(x + 3.5)}" y2="${format(yBottom - 3.5)}" class="cad-dim-tick" />""")
        sb.appendLine("""    <line x1="${format(x - 3.5)}" y1="${format(yTop + 3.5)}" x2="${format(x + 3.5)}" y2="${format(yTop - 3.5)}" class="cad-dim-tick" />""")
        // Linhas de extensão
        sb.appendLine("""    <line x1="${format(x - 6.0)}" y1="${format(yBottom)}" x2="${format(x + 6.0)}" y2="${format(yBottom)}" class="dim-line" stroke-width="0.6" />""")
        sb.appendLine("""    <line x1="${format(x - 6.0)}" y1="${format(yTop)}" x2="${format(x + 6.0)}" y2="${format(yTop)}" class="dim-line" stroke-width="0.6" />""")
        sb.appendLine("""    <text x="${format(x - 8.0)}" y="${format(midY + 3.5)}" class="cad-dim-text" text-anchor="end">$text</text>""")
    }

    private fun format(v: Double) = String.format(Locale.US, "%.1f", v)

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
