package com.paginacaopro.app.model

enum class JointRenderStyle {
    SOLID_MORTAR,      // Faixa preenchida representando a argamassa
    SIMPLE_LINE,       // Linha simples de junta
    PROPORTIONAL_GAP   // Espaçamento proporcional
}

/**
 * Configuração independente das juntas de argamassa.
 */
data class JointConfig(
    val horizontalCm: Double = 1.0,
    val verticalCm: Double = 1.0,
    val renderStyle: JointRenderStyle = JointRenderStyle.SOLID_MORTAR
) {
    fun isValid(): Boolean {
        return horizontalCm >= 0.0 && verticalCm >= 0.0 &&
                horizontalCm <= 5.0 && verticalCm <= 5.0
    }
}
