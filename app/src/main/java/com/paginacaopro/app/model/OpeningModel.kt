package com.paginacaopro.app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class OpeningType {
    DOOR,       // Porta (geralmente Y=0)
    WINDOW,     // Janela (com peitoril Y > 0)
    PASSAGE,    // Passagem / Vão livre
    CUSTOM      // Abertura personalizada
}

/**
 * Representa um vão (porta, janela ou passagem) na parede.
 * Coordenadas (xCm, yCm) com origem (0,0) no canto inferior esquerdo da parede.
 */
@Entity(
    tableName = "openings",
    foreignKeys = [
        ForeignKey(
            entity = WallModel::class,
            parentColumns = ["id"],
            childColumns = ["wallId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wallId")]
)
data class OpeningModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wallId: Long = 0,
    val name: String = "Vão",
    val type: OpeningType = OpeningType.DOOR,
    val xCm: Double = 100.0,
    val yCm: Double = 0.0,
    val widthCm: Double = 90.0,
    val heightCm: Double = 220.0,
    val hasLintel: Boolean = true,       // Verga
    val hasSillBeam: Boolean = false     // Contraverga (para janelas)
) {
    val rightCm: Double get() = xCm + widthCm
    val topCm: Double get() = yCm + heightCm

    /**
     * Valida se a abertura está totalmente dentro dos limites da parede.
     */
    fun isValidWithin(wallLengthCm: Double, wallHeightCm: Double): Boolean {
        return widthCm > 0 && heightCm > 0 &&
                xCm >= 0 && yCm >= 0 &&
                rightCm <= wallLengthCm &&
                topCm <= wallHeightCm
    }
}
