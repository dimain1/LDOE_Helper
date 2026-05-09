package com.example.kotlinclient.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import com.example.kotlinclient.state_management.entity.User
import com.example.kotlinclient.state_management.viewModel.SettingsUiState

@Composable
fun EditProfileModal(showModal: Boolean, user: User?, onDismiss: () -> Unit, onApproveClick: (String, String) -> Unit ){
    if (showModal){
        val userName = rememberTextFieldState(user?.login ?: "")
        val email= rememberTextFieldState(user?.email ?: "")

        AlertDialog(
            onDismissRequest = {onDismiss()},
            title = { Text(text ="Edit Profile")},
            text = {
                Column() {
                    TextField(value = userName.text.toString(), onValueChange = { text: String -> userName.edit { replace(0, length, text) } })
                    TextField(value = email.text.toString(), onValueChange = { text: String -> email.edit { replace(0, length, text) } })
                }
            },
            confirmButton = {
                Button(onClick= {
                    onApproveClick(userName.text.toString(), email.text.toString())
                    onDismiss()
                }){
                    Text("Approve")
                }
            },
            dismissButton = {
                Button(onClick = {onDismiss()}){
                    Text("Deny")
                }
            }

        )
    }
}