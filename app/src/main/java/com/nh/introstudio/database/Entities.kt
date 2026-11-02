package com.nh.introstudio.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val templateId: Int,
    val configJson: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "favorites")
data class FavoriteEntity(@PrimaryKey val templateId: Int)

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val uri: String,
    val width: Int,
    val height: Int,
    val fps: Int,
    val durationSec: Float,
    val sizeBytes: Long,
    val templateId: Int,
    val createdAt: Long
)
