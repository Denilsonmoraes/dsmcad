package com.paginacaopro.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.paginacaopro.app.model.BlockModel
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockDao {
    @Query("SELECT * FROM blocks WHERE isActive = 1 ORDER BY isFavorite DESC, name ASC")
    fun getAllActiveBlocks(): Flow<List<BlockModel>>

    @Query("SELECT * FROM blocks WHERE id = :id")
    suspend fun getBlockById(id: Long): BlockModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: BlockModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<BlockModel>)

    @Update
    suspend fun updateBlock(block: BlockModel)

    @Delete
    suspend fun deleteBlock(block: BlockModel)

    @Query("SELECT COUNT(*) FROM blocks")
    suspend fun getCount(): Int
}
