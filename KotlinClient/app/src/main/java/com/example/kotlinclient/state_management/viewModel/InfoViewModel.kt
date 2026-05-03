package com.example.kotlinclient.state_management.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.query
import com.example.kotlinclient.di.viewModelModule
import com.example.kotlinclient.state_management.entity.ContentType
import com.example.kotlinclient.state_management.entity.GameContent
import com.example.kotlinclient.state_management.repository.interfaces.ContentTypeRepository
import com.example.kotlinclient.state_management.repository.interfaces.GameContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InfoViewModel(
    val contentTypeRepository: ContentTypeRepository,
    val gameContentRepository: GameContentRepository
): ViewModel() {

    val types: StateFlow<List<ContentType>> = contentTypeRepository.getAllTypes().stateIn(viewModelScope,
        SharingStarted.Lazily, emptyList())

    private val _searchQuery: MutableStateFlow<String> = MutableStateFlow("")

    val searchQuery = _searchQuery.asStateFlow()

    private val _selected_type: MutableStateFlow<Long> = MutableStateFlow(0)

    val selected_type = _selected_type.asStateFlow()


    val gameContent: StateFlow<List<GameContent>> =
        combine(_searchQuery, selected_type)
        {
            query, typeId  ->  query to typeId
        }.flatMapLatest{
            (query, typeId) -> gameContentRepository.getFilteredContent(query, typeId)
        }

        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList())



    fun selectType(id: Long){
        _selected_type.value = id
    }

    fun changeSearchQuery(query: String){
        _searchQuery.value = query
    }

    fun clearQuery(){
        _searchQuery.value = ""
    }

    fun updateContentPin(id: Long, pinStatus: Boolean){
        viewModelScope.launch {
            if(pinStatus){
                gameContentRepository.pinContent(id)
            }
            else{
                gameContentRepository.unpinContent(id)
            }
        }
    }
}