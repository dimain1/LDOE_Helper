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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// region InfoScreen

data class InfoUiState(
    val types: List<ContentType> = emptyList(),
    val searchQuery: String = "",
    val selectedType: Long = 0,
    val gameContent: List<GameContent> = emptyList()
)

sealed interface InfoAction{
    data class SelectType(val id: Long): InfoAction
    data class ChangeSearchQuery(val query: String): InfoAction
    data object ClearQuery : InfoAction
    data class UpdateContentPin(val id: Long, val pinStatus: Boolean) : InfoAction
}

// endregion

class InfoViewModel(
    val contentTypeRepository: ContentTypeRepository,
    val gameContentRepository: GameContentRepository
): ViewModel() {

    // region flows

    private val _uiState = MutableStateFlow(InfoUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow(0L)

    // endregion

    init{

        contentTypeRepository.getAllTypes().onEach { types ->
            _uiState.update { it.copy(types=types) }
        }.launchIn(viewModelScope)

        combine(_searchQuery, _selectedType)
        {
            query, typeId ->
            query to typeId
        }.debounce(300)
            .flatMapLatest { (query, typeId)  ->
                gameContentRepository.getFilteredContent(query,typeId)
            }
            .onEach {
                content ->
                _uiState.update { it.copy(
                    gameContent=content,
                    searchQuery = _searchQuery.value,
                    selectedType = _selectedType.value
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: InfoAction){
        when(action){
            is InfoAction.ChangeSearchQuery -> changeSearchQuery(action.query)
            is InfoAction.ClearQuery -> clearQuery()
            is InfoAction.SelectType -> selectType(action.id)
            is InfoAction.UpdateContentPin -> updateContentPin(action.id,action.pinStatus)
        }
    }

    // region onAction function

    private fun selectType(id: Long){
        _selectedType.value = id
    }

    private fun changeSearchQuery(query: String){
        _searchQuery.value = query
    }

    private fun clearQuery(){
        _searchQuery.value = ""
    }

    private fun updateContentPin(id: Long, pinStatus: Boolean){
        viewModelScope.launch {
            if(pinStatus){
                gameContentRepository.pinContent(id)
            }
            else{
                gameContentRepository.unpinContent(id)
            }
        }
    }

    // endregion
}