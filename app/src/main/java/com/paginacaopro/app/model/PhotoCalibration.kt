package com.paginacaopro.app.model

import kotlin.math.sqrt

/**
 * Dados de calibração métrica a partir de uma fotografia.
 */
data class PhotoCalibration(
    val imageUri: String,
    val point1X: Float,
    val point1Y: Float,
    val point2X: Float,
    val point2Y: Float,
    val realDistanceCm: Double
) {
    val pixelDistance: Double
        get() {
            val dx = (point2X - point1X).toDouble()
            val dy = (point2Y - point1Y).toDouble()
            return sqrt(dx * dx + dy * dy)
        }

    val pixelsPerCm: Double
        get() = if (realDistanceCm > 0.0) pixelDistance / realDistanceCm else 0.0

    val cmPerPixel: Double
        get() = if (pixelDistance > 0.0) realDistanceCm / pixelDistance else 0.0

    fun calculateRealDistance(pAx: Float, pAy: Float, pBx: Float, pBy: Float): Double {
        val dx = (pBx - pAx).toDouble()
        val dy = (pBy - pAy).toDouble()
        val pxDist = sqrt(dx * dx + dy * dy)
        return pxDist * cmPerPixel
    }
}
