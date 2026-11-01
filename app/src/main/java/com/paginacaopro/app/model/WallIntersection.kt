package com.paginacaopro.app.model

import java.util.UUID

/**
 * Representa uma saída, encontro de parede ou interseção em T / L.
 * Exemplo: Uma parede que chega a 2,00m e tem uma saída para outro cômodo.
 */
data class WallIntersection(
    val id: String = UUID.randomUUID().toString(),
    val xCm: Double, // Posição horizontal na parede onde ocorre o encontro (cm)
    val thicknessCm: Double = 14.0, // Espessura da parede perpendicular que sai (cm)
    val name: String = "Saída em T", // "Saída em T", "Encontro em L", etc.
    val side: String = "Direita" // "Direita", "Esquerda", "Ambos"
)
