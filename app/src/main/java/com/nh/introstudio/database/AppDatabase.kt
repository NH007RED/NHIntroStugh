package com.nh.introstudio.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProjectEntity::class, FavoriteEntity::class, VideoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projects(): ProjectDao
    abstract fun favorites(): FavoriteDao
    abstract fun videos(): VideoDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "nh_intro_studio.db")
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
        }
    }
}
