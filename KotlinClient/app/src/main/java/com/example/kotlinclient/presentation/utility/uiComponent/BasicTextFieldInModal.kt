package com.example.kotlinclient.presentation.utility.uiComponent

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.ui.theme.Typography

@Composable
fun BasicTextFieldInModal(
    state: TextFieldState,
    placeholder: String,
    readOnly: Boolean = false,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    val isPressed by interactionSource.collectIsPressedAsState()
    if (isPressed) {
        LaunchedEffect(Unit) {
            onClick()
        }
    }

    BasicTextField(
        state = state,
        enabled = true,
        readOnly = readOnly,
        interactionSource = interactionSource,
        modifier = Modifier
            .heightIn(48.dp, 96.dp)
            .fillMaxWidth()
            .border(2.dp, colorScheme.outline, shape = RoundedCornerShape(10)),
        decorator = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 4.dp)
            ) {
                if (state.text.isEmpty()) {
                    Text(
                        placeholder +
                                "", style = Typography.bodyLarge, color = colorScheme.secondary
                    )
                }
                innerTextField()
            }
        },
        cursorBrush = SolidColor(colorScheme.primary),
        textStyle = Typography.bodyLarge.copy(color = colorScheme.primary)
    )
}