package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WatchedMovieEntity::class, CustomWheelListEntity::class, SavedDecisionEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchedMovieDao(): WatchedMovieDao
    abstract fun customWheelListDao(): CustomWheelListDao
    abstract fun savedDecisionDao(): SavedDecisionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE watched_movies ADD COLUMN personalNote TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE watched_movies ADD COLUMN wantToRewatch INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE watched_movies ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE watched_movies ADD COLUMN posterPath TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS custom_wheel_lists (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        emoji TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        movieIdsCsv TEXT NOT NULL DEFAULT '',
                        customMoviesJson TEXT NOT NULL DEFAULT '[]',
                        accentColorHex INTEGER NOT NULL DEFAULT 4287324150,
                        isPreset INTEGER NOT NULL DEFAULT 0,
                        updatedAtMillis INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS saved_decisions (
                        id TEXT NOT NULL PRIMARY KEY,
                        movieId TEXT NOT NULL,
                        movieTitle TEXT NOT NULL,
                        movieOriginalTitle TEXT NOT NULL DEFAULT '',
                        movieYear INTEGER NOT NULL,
                        movieDurationMins INTEGER NOT NULL,
                        movieImdb REAL NOT NULL,
                        movieMoodTag TEXT NOT NULL DEFAULT '',
                        moviePlatformsCsv TEXT NOT NULL DEFAULT '',
                        movieGenre TEXT NOT NULL,
                        movieLogline TEXT NOT NULL DEFAULT '',
                        movieWhyWatch TEXT NOT NULL DEFAULT '',
                        moviePosterPath TEXT NOT NULL DEFAULT '',
                        moviePosterUrl TEXT NOT NULL DEFAULT '',
                        movieColorHex INTEGER NOT NULL DEFAULT 4287324150,
                        dateText TEXT NOT NULL,
                        minutesSaved INTEGER NOT NULL DEFAULT 45,
                        mode TEXT NOT NULL,
                        selectedAtMillis INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ne_izlesek_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
