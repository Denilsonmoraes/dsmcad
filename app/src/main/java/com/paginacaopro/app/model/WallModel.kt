package com.paginacaopro.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WallShape {
    RECTANGULAR,
    L_SHAPED,
    U_SHAPED,
    POLYGONAL,
    SLOPED
}

/**
 * Representa os dados gerais e dimensões de uma parede.
 * Todas as medidas em centímetros (cm).
 */
@Entity(tableName = "walls")
data class WallModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectName: String = "Obra Padrão",
    val wallName: String = "Parede 1",
    val floor: String = "Térreo",
    val environment: String = "Geral",
    val notes: String = "",
    val lengthCm: Double = 400.0,
    val heightCm: Double = 280.0,
    val thicknessCm: Double = 14.0,
    val shape: WallShape = WallShape.RECTANGULAR,
    val selectedBlockId: Long = 1,
    val horizontalJointCm: Double = 1.0,
    val verticalJointCm: Double = 1.0,
    val bondTypeName: String = "RUNNING_HALF",
    val wastePercentage: Double = 5.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
