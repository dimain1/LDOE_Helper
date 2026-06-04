package com.example.kotlinclient.presentation.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kotlinclient.R
import com.example.kotlinclient.presentation.utility.uiComponent.LocalImage
import com.example.kotlinclient.presentation.utility.uiComponent.Authorized
import com.example.kotlinclient.state_management.viewModel.InfoAction
import com.example.kotlinclient.state_management.viewModel.InfoDialogType
import com.example.kotlinclient.state_management.viewModel.InfoUiState
import com.example.kotlinclient.ui.theme.Typography


// Список предметов(Сущностей игры)
@Composable
fun ItemList(
    uiState: InfoUiState,
    onAction: (InfoAction) -> Unit
) {

    val Items = uiState.gameContent

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()

    )
    {
        items(Items.size) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surface, shape = RoundedCornerShape(10))
                    .border(2.dp, colorScheme.outline, RoundedCornerShape(10))
                    .padding(all = 12.dp)
                    .clickable(onClick = { onAction(InfoAction.OpenDialog(InfoDialogType.View(Items[item]))) })
            ) {
                // Картинка предмета(Замениться на Image)
                Box(
                    modifier = Modifier
                        .border(1.dp, colorScheme.outline, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    LocalImage(Items[item].image, Modifier.size(48.dp), size=48)
                }
                Spacer(Modifier.width(16.dp))
                // Описание предмета
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                )

                {
                    Text(
                        text = Items[item].name,
                        style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = Items[item].description ?: "Описание отсутствует",
                        style = Typography.bodySmall,
                        color = colorScheme.secondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(8.dp))

                Image(
                    painter = painterResource(if (Items[item].pinned == false) R.drawable.pinned_off else R.drawable.pinned_on),
                    contentDescription = "Закреплено",
                    modifier = Modifier
                        .Authorized()
                        .size(32.dp)
                        .clickable(onClick = {
                            onAction(
                                InfoAction.UpdateContentPin(
                                    Items[item].id,
                                    !Items[item].pinned
                                )
                            )
                        }),
                    colorFilter = if (Items[item].pinned == false) null else ColorFilter.tint(
                        colorScheme.tertiary
                    )
                )

            }
            // Между последним элементом и краем экрана отступ не добавляем
            if (item != Items.size - 1) {
                Spacer(Modifier.height(16.dp))
            }

        }
        item {
            Spacer(Modifier.height(16.dp))
        }
    }


}
