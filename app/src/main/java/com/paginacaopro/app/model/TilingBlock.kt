package com.paginacaopro.app.model

enum class PieceType(val label: String, val colorHex: String) {
    WHOLE("Bloco Inteiro", "#E2E8F0"),           // Bege/Cinza claro neutro
    HALF("Meio Bloco", "#93C5FD"),              // Azul suave para meios blocos
    CORNER_TIE("Amarração de Canto", "#FDE047"), // Amarelo/Dourado para bloco de canto (34cm / 29cm)
    COMPENSATOR("Compensador", "#C7D2FE"),      // Lilás para compensador 29/9cm
    T_JUNCTION("Amarração em T", "#FBCFE8"),     // Rosa claro para travamento de saída em T
    ADJUSTMENT("Pastilha 5cm", "#A7F3D0"),       // Verde suave para pastilha de amarração 5cm
    CUT("Peça Cortada", "#FED7AA"),             // Laranja suave para identificar cortes
    SPECIAL("Peça Especial", "#DDD6FE"),        // Roxo suave para peças especiais
    LINTEL("Verga / Canaleta", "#FEF08A")       // Amarelo suave para canaletas/vergas
}

/**
 * Representa uma peça individual calculada e posicionada na parede.
 * Coordenadas xCm e yCm representam o canto inferior esquerdo da peça.
 */
data class TilingBlock(
    val id: String,
    val courseIndex: Int,          // Índice da fiada (0 = primeira fiada na base)
    val xCm: Double,               // Posição horizontal inferior esquerda
    val yCm: Double,               // Posição vertical inferior esquerda
    val lengthCm: Double,          // Comprimento da peça
    val heightCm: Double,          // Altura da peça
    val thicknessCm: Double,       // Espessura
    val pieceType: PieceType,
    val originalBlockLengthCm: Double,
    val relatedOpeningId: Long? = null,
    val notes: String = "",
    val countsInQuantity: Boolean = true
) {
    val rightCm: Double get() = xCm + lengthCm
    val topCm: Double get() = yCm + heightCm

    /**
     * Informa se a peça é cortada em relação ao bloco original comercial.
     */
    val isCut: Boolean get() = pieceType == PieceType.CUT

    /**
     * Porcentagem remanescente em relação ao comprimento original.
     */
    val percentageOfOriginal: Double
        get() = (lengthCm / originalBlockLengthCm * 100.0).coerceIn(0.0, 100.0)
}
