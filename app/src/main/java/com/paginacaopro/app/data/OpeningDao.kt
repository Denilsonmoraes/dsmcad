package com.paginacaopro.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.paginacaopro.app.model.OpeningModel
import kotlinx.coroutines.flow.Flow

@Dao
interface OpeningDao {
    @Query("SELECT * FROM openings WHERE wallId = :wallId ORDER BY xCm ASC")
    fun getOpeningsByWallId(wallId: Long): Flow<List<OpeningModel>>

    @Query("SELECT * FROM openings WHERE wallId = :wallId ORDER BY xCm ASC")
    suspend fun getOpeningsListByWallId(wallId: Long): List<OpeningModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpening(opening: OpeningModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpenings(openings: List<OpeningModel>)

    @Update
    suspend fun updateOpening(opening: OpeningModel)

    @Delete
    suspend fun deleteOpening(opening: OpeningModel)

    @Query("DELETE FROM openings WHERE wallId = :wallId")
    suspend fun deleteOpeningsByWallId(wallId: Long)
}
