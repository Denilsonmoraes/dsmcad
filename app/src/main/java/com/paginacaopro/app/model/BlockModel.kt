package com.paginacaopro.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa um bloco ou tijolo comercial ou personalizado.
 * Dimensões nominais e reais em centímetros (cm).
 */
@Entity(tableName = "blocks")
data class BlockModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val manufacturer: String = "",
    val material: String, // Cerâmico, Concreto, Solo-Cimento, etc.
    val lengthCm: Double, // Comprimento nominal/real da face
    val heightCm: Double, // Altura nominal/real da face
    val thicknessCm: Double, // Largura/espessura da parede
    val unit: String = "cm",
    val applicationType: String = "Vedação", // Vedação, Estrutural
    val compatibleBonds: String = "Amarração alternada, Junta a prumo",
    val halfBlockLengthCm: Double = 0.0, // Se 0, calcula automaticamente como (lengthCm - junta)/2
    val isModular: Boolean = true,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val isActive: Boolean = true,
    val isCustom: Boolean = false,
    val imageUri: String? = null
) {
    /**
     * Comprimento efetivo da meia peça para amarração.
     * Se não especificado explicitamente, padrão é aproximadamente metade da dimensão modular.
     */
    fun getEffectiveHalfLength(jointVerticalCm: Double): Double {
        return if (halfBlockLengthCm > 0.0) {
            halfBlockLengthCm
        } else {
            // Exemplo padrão: para 39cm com junta 1cm -> módulo 40cm -> meio bloco = 20 - 1 = 19cm
            val modularHalf = (lengthCm + jointVerticalCm) / 2.0
            (modularHalf - jointVerticalCm).coerceAtLeast(1.0)
        }
    }
}
