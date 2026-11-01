package com.paginacaopro.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.paginacaopro.app.model.WallModel
import kotlinx.coroutines.flow.Flow

@Dao
interface WallDao {
    @Query("SELECT * FROM walls ORDER BY updatedAt DESC")
    fun getAllWalls(): Flow<List<WallModel>>

    @Query("SELECT * FROM walls WHERE id = :id")
    suspend fun getWallById(id: Long): WallModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWall(wall: WallModel): Long

    @Update
    suspend fun updateWall(wall: WallModel)

    @Delete
    suspend fun deleteWall(wall: WallModel)
}
