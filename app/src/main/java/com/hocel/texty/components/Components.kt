package com.hocel.texty.components

import android.content.Context
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.hocel.texty.data.models.ScannedText
import com.hocel.texty.ui.theme.TextColor
import com.hocel.texty.utils.AddOrRemoveAction
import com.hocel.texty.utils.deleteImageFromStorage
import com.hocel.texty.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TransparentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    indication: Indication = rememberRipple(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        elevation = 0.dp,
        shape = shape,
        color = Color.Transparent,
        contentColor = Color.Transparent,
        border = null,
        modifier = modifier
            .then(
                Modifier
                    .clip(shape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = indication,
                        onClick = onClick
                    )
            ),
    ) {
        CompositionLocalProvider(LocalContentAlpha provides 1f) {
            ProvideTextStyle(
                value = MaterialTheme.typography.button
            ) {
                Row(
                    Modifier
                        .defaultMinSize(
                            minWidth = ButtonDefaults.MinWidth,
                            minHeight = ButtonDefaults.MinHeight
                        )
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DeleteScannedTextSheetContent(
    context: Context,
    scannedText: ScannedText,
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState,
    mainViewModel: MainViewModel,
    onDeleteYes: () -> Unit,
    onDeleteCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Are you sure you want to delete?",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 24.dp),
            color = MaterialTheme.colors.TextColor
        )

        TransparentButton(
            shape = RoundedCornerShape(0),
            onClick = {
                mainViewModel.addOrRemoveScannedText(
                    context = context,
                    action = AddOrRemoveAction.REMOVE,
                    scannedText = scannedText,
                    onAddSuccess = {},
                    onRemoveSuccess = {
                        deleteImageFromStorage(scannedText)
                        onDeleteYes()
                        scope.launch {
                            modalBottomSheetState.hide()
                        }
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Yes",
                modifier = Modifier
                    .padding(vertical = 16.dp),
                color = MaterialTheme.colors.TextColor
            )
        }
        Divider(
            color = Color.Black.copy(alpha = 0.4f),
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
        )

        TransparentButton(
            shape = RoundedCornerShape(0),
            onClick = {
                onDeleteCancel()
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Cancel",
                modifier = Modifier
                    .padding(vertical = 16.dp),
                color = MaterialTheme.colors.TextColor
            )
        }
    }
}
