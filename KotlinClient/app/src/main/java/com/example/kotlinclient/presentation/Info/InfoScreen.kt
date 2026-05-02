package com.example.kotlinclient.presentation.Info

import android.R
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.state_management.entity.ContentType
import com.example.kotlinclient.state_management.entity.GameContent
import com.example.kotlinclient.ui.theme.Typography
import kotlinx.coroutines.flow.debounce
import kotlin.text.clear

@Composable
fun InfoScreen(
    paddingValues: PaddingValues,
    categoryList: List<ContentType>,
    selectedCategory: Long,
    onCategoryClick: (Long) -> Unit,
    gameContent: List<GameContent>,
    onSearchChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onPinClick: (Long, Boolean) -> Unit

){
    var textField: TextFieldState = rememberTextFieldState("")

    LaunchedEffect(textField) {
        snapshotFlow { textField.text }
            .debounce(500)
            .collect { newText ->
                onSearchChange(newText.toString())
            }
    }

    Column(modifier= Modifier
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
                    .background(color = colorScheme.surface, shape = RoundedCornerShape(8.dp,8.dp,8.dp,8.dp))
                    .border(2.dp, colorScheme.outline, shape = RoundedCornerShape(8.dp,8.dp,8.dp,8.dp) ),
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
                    textStyle = Typography.bodyLarge.copy(color= colorScheme.primary),
                    modifier = Modifier
                        .height(19.dp)
                        .padding(start = 8.dp)
                        .weight(1f)
    //                    .focusRequester(focusRequester)
    //                    .onFocusChanged({ focusState ->
    //                        onFocusChanged(focusState.isFocused)
    //                    }),
                            ,
                    // декоратор ответственный за placeholder и изменение видимости крестика очистки
                    decorator = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart

                        ) {
                            if (textField.text.isEmpty()) {
                                Text("Search entities..." +
                                        "", style=Typography.bodyLarge, color= colorScheme.secondary)
                            }
                            innerTextField()
                        }
                    },
                    lineLimits = TextFieldLineLimits.SingleLine,
                )
                // Крестик очистки поля ввода
                Box(
                    modifier = Modifier.fillMaxHeight().width(40.dp),
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
                                onClearClick()
                            })
                    )
                }
            }



            Spacer(Modifier.height(16.dp))

            // Категории
            CategoryList(categoryList, selectedCategory, onCategoryClick)

            Spacer(Modifier.height(24.dp))

            ItemList(gameContent, onPinClick)


        }


        HorizontalDivider(thickness = 1.dp, color = colorScheme.outline)
    }
}