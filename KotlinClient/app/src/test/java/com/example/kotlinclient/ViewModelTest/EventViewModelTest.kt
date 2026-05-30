package com.example.kotlinclient.ViewModelTest

import com.example.kotlinclient.state_management.entity.User
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import com.example.kotlinclient.state_management.repository.interfaces.EventRepository
import com.example.kotlinclient.state_management.viewModel.EventFormAction
import com.example.kotlinclient.state_management.viewModel.EventFormValidation
import com.example.kotlinclient.state_management.viewModel.EventViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EventViewModelTest() {

    private lateinit var eventRepository: EventRepository
    private lateinit var userSession: UserSessionProvider
    private lateinit var viewModel : EventViewModel

    @Before
    fun setup(){
        eventRepository = FakeEventRepository()
        userSession = FakeUserSession(User(4, "dmitry", "dima@mail.ru"))
        viewModel = EventViewModel(eventRepository, userSession)
    }

    @Test
    fun endTimeCompareCurrentTimeTest(){
        viewModel.onFormAction(EventFormAction.SelectTemplate(null))
        viewModel.formUiState.value.startTime.edit { replace(0, length, "30.05.2026 - 15:48") }
        viewModel.formUiState.value.endTime.edit { replace(0, length, "30.05.2026 - 15:49") }

        viewModel.onFormValidation(EventFormValidation.ValidateEndTime)

        assertEquals("Событие не может закончится в прошлом", viewModel.formUiState.value.errors.endTimeError )

    }

}