package com.nh.introstudio.data

import com.nh.introstudio.database.AppDatabase
import com.nh.introstudio.database.FavoriteEntity
import com.nh.introstudio.database.ProjectEntity
import com.nh.introstudio.database.VideoEntity
import com.nh.introstudio.domain.BrandConfig
import com.nh.introstudio.templates.TemplateCatalog
import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {
    val projects: Flow<List<ProjectEntity>> = db.projects().observeAll()
    val favorites: Flow<List<Int>> = db.favorites().observeAll()
    val videos: Flow<List<VideoEntity>> = db.videos().observeAll()

    suspend fun createProject(templateId: Int): Long {
        val spec = TemplateCatalog.byId(templateId)
        val now = System.currentTimeMillis()
        return db.projects().insert(
            ProjectEntity(
                name = "${spec.name} Intro",
                templateId = templateId,
                configJson = TemplateCatalog.defaultConfig(templateId).toJson(),
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun createProjectWithConfig(name: String, cfg: BrandConfig): Long {
        val spec = TemplateCatalog.byId(cfg.templateId)
        val now = System.currentTimeMillis()
        val finalName = name.ifBlank { "${spec.name} Logo AI" }
        return db.projects().insert(
            ProjectEntity(
                name = finalName,
                templateId = cfg.templateId,
                configJson = cfg.toJson(),
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun getProject(id: Long): ProjectEntity? = db.projects().get(id)

    suspend fun saveProject(id: Long, name: String, cfg: BrandConfig) {
        val old = db.projects().get(id) ?: return
        db.projects().update(old.copy(name = name, templateId = cfg.templateId, configJson = cfg.toJson(), updatedAt = System.currentTimeMillis()))
    }

    suspend fun renameProject(id: Long, name: String) {
        val old = db.projects().get(id) ?: return
        db.projects().update(old.copy(name = name.ifBlank { old.name }, updatedAt = System.currentTimeMillis()))
    }

    suspend fun duplicateProject(id: Long): Long? {
        val old = db.projects().get(id) ?: return null
        val now = System.currentTimeMillis()
        return db.projects().insert(old.copy(id = 0, name = old.name + " copy", createdAt = now, updatedAt = now))
    }

    suspend fun deleteProject(id: Long) = db.projects().delete(id)

    suspend fun setFavorite(templateId: Int, fav: Boolean) {
        if (fav) db.favorites().add(FavoriteEntity(templateId)) else db.favorites().remove(templateId)
    }

    suspend fun addVideo(v: VideoEntity): Long = db.videos().insert(v)
    suspend fun getVideo(id: Long): VideoEntity? = db.videos().get(id)
    suspend fun deleteVideo(id: Long) = db.videos().delete(id)
}
