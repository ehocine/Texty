package com.hocel.texty.components

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hocel.texty.data.models.ScannedText
import com.hocel.texty.ui.theme.BackgroundColor
import com.hocel.texty.ui.theme.TextColor
import com.hocel.texty.utils.AddOrRemoveAction
import com.hocel.texty.utils.RecognitionLanguageModel
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

@Composable
fun DropDownModelOptions(
    optionsList: List<RecognitionLanguageModel>,
    onOptionSelected: (RecognitionLanguageModel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(RecognitionLanguageModel.Latin) }
    val angle: Float by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )
    var parentSize by remember { mutableStateOf(IntSize.Zero) }
    Row(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .onGloballyPositioned {
                parentSize = it.size
            }
            .background(MaterialTheme.colors.BackgroundColor)
            .height(50.dp)
            .clickable { expanded = !expanded }
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(
                    alpha = ContentAlpha.disabled
                ),
                shape = MaterialTheme.shapes.small
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(weight = 8f)
                .padding(start = 10.dp),
        ) {
            Text(
                text = selectedOption.toString(),
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colors.TextColor,
            )
        }
        IconButton(
            modifier = Modifier
                .alpha(ContentAlpha.medium)
                .rotate(degrees = angle)
                .weight(weight = 1.5f),
            onClick = { expanded = !expanded }
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Drop Down Arrow"
            )
        }
        DropdownMenu(
            modifier = Modifier
                .width(with(LocalDensity.current) { parentSize.width.toDp() }),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            optionsList.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        selectedOption = option
                        onOptionSelected(option)
                    }
                ) {
                    Text(text = option.toString(), color = MaterialTheme.colors.TextColor)
                }
            }
        }
    }
}
