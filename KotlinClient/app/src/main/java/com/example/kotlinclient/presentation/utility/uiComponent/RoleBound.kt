package com.example.kotlinclient.presentation.utility.uiComponent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.example.kotlinclient.state_management.entity.UserRole
import com.example.kotlinclient.state_management.repository.UserSessionProvider
import org.koin.compose.koinInject

@Composable
fun Modifier.roleBound(allowedRoles: List<UserRole>): Modifier {
    val userSession: UserSessionProvider = koinInject()
    val user by userSession.user.collectAsState()
    val currentRole = if(user == null) UserRole.GUEST else UserRole.USER

    return if (allowedRoles.contains(currentRole)) {
        this
    } else {
        this.then(
            Modifier
                .alpha(0f)
                .clearAndSetSemantics { }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            // Initial pass перехватывает событие до того, как его обработают дети или другие модификаторы
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            // "Пожираем" событие, чтобы оно не пошло дальше
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
                .drawWithContent { }
        )
    }
}


//@Composable
//fun Modifier.adminOnly() = roleBound(listOf(UserRole.ADMIN))

//@Composable
//fun Modifier.userAndAdmin() = roleBound(listOf(UserRole.USER, UserRole.ADMIN))

@Composable
fun Modifier.Authorized() = roleBound(listOf(UserRole.USER))
