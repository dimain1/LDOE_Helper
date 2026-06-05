package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.api_client.ApiService
import com.example.kotlinclient.api_client.dto.ContentTypeDto
import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.local_cache.entity.ContentTypeEntity
import com.example.kotlinclient.state_management.entity.ContentType
import com.example.kotlinclient.state_management.repository.interfaces.ContentTypeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContentTypeRepositoryImpl(
    private val database: AppDatabase,
    private val api: ApiService
) : ContentTypeRepository {

    private val contentTypeDao = database.ContentTypeDao()

    override fun getAllTypes(): Flow<List<ContentType>> =
        contentTypeDao.getAllTypes().map { list -> list.map { it.toModel() } }

    override suspend fun createType(name: String): ContentType {
        val dto = api.createContentType(ContentTypeDto(id = 0L, name = name))
        val entity = ContentTypeEntity(id = dto.id, name = dto.name)
        contentTypeDao.upsertType(entity)
        return entity.toModel()
    }

    override suspend fun deleteType(id: Long) {
        api.deleteContentType(id)
        contentTypeDao.deleteType(id)
    }
}