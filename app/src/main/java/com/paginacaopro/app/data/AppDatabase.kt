package com.paginacaopro.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.paginacaopro.app.model.BlockModel
import com.paginacaopro.app.model.OpeningModel
import com.paginacaopro.app.model.WallModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [BlockModel::class, WallModel::class, OpeningModel::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockDao(): BlockDao
    abstract fun wallDao(): WallDao
    abstract fun openingDao(): OpeningDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "paginacao_pro_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialBlocks(database.blockDao())
                    }
                }
            }
        }

        suspend fun populateInitialBlocks(blockDao: BlockDao) {
            val initialBlocks = listOf(
                // Blocos de Concreto Estrutural e Vedação
                BlockModel(
                    name = "Bloco de Concreto 14×19×39 cm",
                    manufacturer = "Padrão ABNT NBR 6136",
                    material = "Concreto",
                    lengthCm = 39.0,
                    heightCm = 19.0,
                    thicknessCm = 14.0,
                    halfBlockLengthCm = 19.0,
                    applicationType = "Estrutural / Vedação",
                    compatibleBonds = "Amarração alternada, Junta a prumo",
                    notes = "Módulo de 40x20 cm com junta de 1 cm. Modelo padrão de obra.",
                    isFavorite = true,
                    imageUri = null
                ),
                BlockModel(
                    name = "Bloco de Concreto 19×19×39 cm",
                    manufacturer = "Padrão ABNT NBR 6136",
                    material = "Concreto",
                    lengthCm = 39.0,
                    heightCm = 19.0,
                    thicknessCm = 19.0,
                    halfBlockLengthCm = 19.0,
                    applicationType = "Estrutural / Vedação",
                    compatibleBonds = "Amarração alternada",
                    notes = "Módulo 40x20 cm com parede de 19 cm de espessura.",
                    isFavorite = true,
                    imageUri = null
                ),
                BlockModel(
                    name = "Bloco de Concreto 9×19×39 cm",
                    manufacturer = "Padrão ABNT NBR 6136",
                    material = "Concreto",
                    lengthCm = 39.0,
                    heightCm = 19.0,
                    thicknessCm = 9.0,
                    halfBlockLengthCm = 19.0,
                    applicationType = "Vedação",
                    compatibleBonds = "Amarração alternada",
                    notes = "Bloco fino para divisórias internas.",
                    imageUri = null
                ),

                // Blocos Cerâmicos Modulares
                BlockModel(
                    name = "Bloco Cerâmico 14×19×29 cm",
                    manufacturer = "Padrão ABNT NBR 15270",
                    material = "Cerâmico",
                    lengthCm = 29.0,
                    heightCm = 19.0,
                    thicknessCm = 14.0,
                    halfBlockLengthCm = 14.0,
                    applicationType = "Vedação / Estrutural",
                    compatibleBonds = "Amarração alternada",
                    notes = "Módulo 30x20 cm com junta de 1 cm.",
                    isFavorite = true,
                    imageUri = null
                ),
                BlockModel(
                    name = "Bloco Cerâmico 14×19×39 cm",
                    manufacturer = "Padrão ABNT NBR 15270",
                    material = "Cerâmico",
                    lengthCm = 39.0,
                    heightCm = 19.0,
                    thicknessCm = 14.0,
                    halfBlockLengthCm = 19.0,
                    applicationType = "Estrutural / Vedação",
                    compatibleBonds = "Amarração alternada",
                    notes = "Cerâmica estrutural modular 40x20 cm.",
                    isFavorite = true,
                    imageUri = null
                ),
                BlockModel(
                    name = "Bloco Cerâmico 11.5×19×39 cm",
                    manufacturer = "Padrão ABNT NBR 15270",
                    material = "Cerâmico",
                    lengthCm = 39.0,
                    heightCm = 19.0,
                    thicknessCm = 11.5,
                    halfBlockLengthCm = 19.0,
                    applicationType = "Vedação / Divisória",
                    compatibleBonds = "Amarração alternada",
                    notes = "Cerâmico 39x19x11.5 cm com furos e ranhuras.",
                    isFavorite = true,
                    imageUri = null
                ),

                // Tijolos Cerâmicos / Baianos Tradicionais
                BlockModel(
                    name = "Tijolo Baiano 19×15×9 cm (6 furos)",
                    manufacturer = "Cerâmica Convencional",
                    material = "Baiano",
                    lengthCm = 19.0,
                    heightCm = 15.0,
                    thicknessCm = 9.0,
                    halfBlockLengthCm = 9.0,
                    applicationType = "Vedação Convencional",
                    compatibleBonds = "Amarração alternada",
                    notes = "Tijolo Baiano 19x15x9 cm vazado de alta procura.",
                    isFavorite = true,
                    imageUri = null
                ),
                BlockModel(
                    name = "Tijolo Baiano 9×19×19 cm (6 furos)",
                    manufacturer = "Cerâmica Convencional",
                    material = "Baiano",
                    lengthCm = 19.0,
                    heightCm = 19.0,
                    thicknessCm = 9.0,
                    halfBlockLengthCm = 9.0,
                    applicationType = "Vedação Convencional",
                    compatibleBonds = "Amarração alternada",
                    notes = "Tijolo 6 furos tradicional para fechamento.",
                    imageUri = null
                ),
                BlockModel(
                    name = "Tijolo Baiano 9×14×19 cm (8 furos)",
                    manufacturer = "Cerâmica Convencional",
                    material = "Baiano",
                    lengthCm = 19.0,
                    heightCm = 14.0,
                    thicknessCm = 9.0,
                    halfBlockLengthCm = 9.0,
                    applicationType = "Vedação Convencional",
                    compatibleBonds = "Amarração alternada",
                    notes = "Tijolo 8 furos comum.",
                    imageUri = null
                )
            )

            blockDao.insertBlocks(initialBlocks)
        }
    }
}
