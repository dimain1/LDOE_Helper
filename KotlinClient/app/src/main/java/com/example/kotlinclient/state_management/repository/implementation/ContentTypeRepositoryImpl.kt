package com.example.kotlinclient.state_management.repository.implementation

import com.example.kotlinclient.local_cache.AppDatabase
import com.example.kotlinclient.local_cache.converters.toModel
import com.example.kotlinclient.state_management.entity.ContentType
import com.example.kotlinclient.state_management.repository.interfaces.ContentTypeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.reflect.typeOf

class ContentTypeRepositoryImpl(
    private val database: AppDatabase
): ContentTypeRepository {

    val contentTypeDao = database.ContentTypeDao()

    override fun getAllTypes(): Flow<List<ContentType>> {
        return contentTypeDao.getAllTypes().map { list -> list.map { type -> type.toModel() } }
    }

}