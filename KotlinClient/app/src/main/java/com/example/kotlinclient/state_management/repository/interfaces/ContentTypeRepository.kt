package com.example.kotlinclient.state_management.repository.interfaces

import com.example.kotlinclient.state_management.entity.ContentType
import kotlinx.coroutines.flow.Flow

interface ContentTypeRepository {

    fun getAllTypes(): Flow<List<ContentType>>


}