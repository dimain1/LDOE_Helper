package com.example.kotlinclient.presentation.utility.uiComponent

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.ui.theme.Typography

@Composable
fun ValidatedBasicTextFieldInModal(
    state: TextFieldState,
    errorText: String? = null,
    onFocusLost: () -> Unit,
    onFocused: () -> Unit,
    placeholder: String,
    readOnly: Boolean = false,
    keyboardOption: KeyboardOptions = KeyboardOptions.Default,
    maxlines: Long = 2,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    val isPressed by interactionSource.collectIsPressedAsState()
    if (isPressed) {
        LaunchedEffect(Unit) {
            onClick()
        }
    }

    val isError = errorText != null

    var previousFocus by remember(state) { mutableStateOf(false) }

    val errorColor = if(isError) colorScheme.tertiary else colorScheme.outline

    Column() {

        Text(
            placeholder +
                    "", style = Typography.bodyLarge, color = colorScheme.primary
        )

        Spacer(Modifier.height(5.dp))

        BasicTextField(
            state = state,
            enabled = true,
            readOnly = readOnly,
            interactionSource = interactionSource,
            modifier = Modifier
                .heightIn(48.dp, 96.dp)
                .fillMaxWidth()
                .border(2.dp, errorColor, shape = RoundedCornerShape(10))
                .onFocusChanged {focusState ->
                    if(focusState.isFocused){
                        previousFocus = true
                        onFocused()
                    }
                    else if(previousFocus && !focusState.isFocused){
                        previousFocus = false
                        onFocusLost()
                    }
                }
            ,
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
            keyboardOptions = keyboardOption,
            cursorBrush = SolidColor(colorScheme.primary),
            textStyle = Typography.bodyLarge.copy(color = colorScheme.primary),
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 1, maxHeightInLines = maxlines.toInt())
        )

        Spacer(Modifier.height(5.dp))

        if(isError){
            Text(text= errorText, color=errorColor, style =  Typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
        }

    }
}