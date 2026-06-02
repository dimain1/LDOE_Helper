package com.example.kotlinclient.presentation.utility.uiComponent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.kotlinclient.state_management.entity.UserRole
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import org.koin.compose.koinInject

@Composable
fun RoleGuard(
    allowedRoles: List<UserRole>,
    content: @Composable () -> Unit
) {
    val userSession: UserSessionProvider = koinInject()
    val user by userSession.user.collectAsState()
    val currentRole = if(user == null) UserRole.GUEST else UserRole.USER

    if (allowedRoles.contains(currentRole)) {
        content()
    }
}

//// Удобные сокращения
//@Composable
//fun AdminOnly(content: @Composable () -> Unit) = RoleGuard(listOf(UserRole.ADMIN), content)
//
@Composable
fun AuthOnly(content: @Composable () -> Unit) = RoleGuard(listOf(UserRole.USER), content)


