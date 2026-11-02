package com.nh.introstudio.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun get(id: Long): ProjectEntity?

    @Insert
    suspend fun insert(p: ProjectEntity): Long

    @Update
    suspend fun update(p: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface FavoriteDao {
    @Query("SELECT templateId FROM favorites")
    fun observeAll(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(f: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE templateId = :id")
    suspend fun remove(id: Int)
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun get(id: Long): VideoEntity?

    @Insert
    suspend fun insert(v: VideoEntity): Long

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun delete(id: Long)
}
