package com.example.kotlinclient.presentation.info

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.state_management.viewModel.InfoAction
import com.example.kotlinclient.state_management.viewModel.InfoUiState
import com.example.kotlinclient.ui.theme.Typography

@Composable
fun InfoScreen(
    uiState: InfoUiState,
    onAction: (InfoAction) -> Unit,
    paddingValues: PaddingValues,
) {
    var textField: TextFieldState = rememberTextFieldState("")

    LaunchedEffect(textField) {
        snapshotFlow { textField.text }
            .collect { newText ->
                onAction(InfoAction.ChangeSearchQuery(newText.toString()))
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)

        //Основной контейнер инфо
        Column(
            modifier = Modifier
                .background(color = colorScheme.secondaryContainer)
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)

        ) {

            Spacer(Modifier.height(16.dp))
            //Поле поиска


            // Само базовое поле ввода
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        color = colorScheme.surface,
                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp)
                    )
                    .border(
                        2.dp,
                        colorScheme.outline,
                        shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка лупы
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .padding(start = 12.dp)
                        .size(16.dp),
                    //                    .clickable( onClick = {viewModel.search(text.text.toString())} )
                )

                BasicTextField(
                    state = textField,
                    textStyle = Typography.bodyLarge.copy(color = colorScheme.primary),
                    modifier = Modifier
                        .height(19.dp)
                        .padding(start = 8.dp)
                        .weight(1f),
                    //                    .focusRequester(focusRequester)
                    //                    .onFocusChanged({ focusState ->
                    //                        onFocusChanged(focusState.isFocused)
                    //                    }),
                    // декоратор ответственный за placeholder и изменение видимости крестика очистки
                    decorator = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart

                        ) {
                            if (textField.text.isEmpty()) {
                                Text(
                                    "Search entities..." +
                                            "",
                                    style = Typography.bodyLarge,
                                    color = colorScheme.secondary
                                )
                            }
                            innerTextField()
                        }
                    },
                    lineLimits = TextFieldLineLimits.SingleLine,
                )
                // Крестик очистки поля ввода
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Очистить",
                        modifier = Modifier
                            .size(16.dp)
                            .alpha(if (!textField.text.isEmpty()) 1f else 0f)
                            .clickable(onClick = {
                                textField.edit { replace(0, length, "") }
                                onAction(InfoAction.ClearQuery)
                            })
                    )
                }
            }



            Spacer(Modifier.height(16.dp))

            // Категории
            CategoryList(
                uiState.types,
                uiState.selectedType,
                { id -> onAction(InfoAction.SelectType(id)) })

            Spacer(Modifier.height(24.dp))

            ItemList(
                uiState,
                onAction = onAction
            )
        }


        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
    }
}