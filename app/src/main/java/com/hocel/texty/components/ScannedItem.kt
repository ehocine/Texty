package com.hocel.texty.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.hocel.texty.R
import com.hocel.texty.data.models.ScannedText
import com.hocel.texty.ui.theme.CardColor
import com.hocel.texty.ui.theme.DividerColor
import com.hocel.texty.ui.theme.RedColor
import com.hocel.texty.ui.theme.TextColor
import com.hocel.texty.utils.convertTimeStampToDate
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@Composable
fun ScannedItem(
    scannedText: ScannedText,
    showImage: Boolean = true,
    onItemClicked: (scannedText: ScannedText) -> Unit,
    enableDeleteAction: Boolean = false,
    deleteScannedText: (scannedText: ScannedText) -> Unit
) {
    val delete = SwipeAction(
        onSwipe = {
            deleteScannedText(scannedText)
        },
        icon = {
            Icon(
                modifier = Modifier.padding(16.dp),
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete icon",
                tint = Color.White
            )
        },
        background = RedColor
    )
    SwipeableActionsBox(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp)),
        swipeThreshold = 120.dp,
        endActions = if (enableDeleteAction) listOf(delete) else listOf()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = {
                    onItemClicked(scannedText)
                }),
            elevation = 0.dp,
            backgroundColor = MaterialTheme.colors.CardColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showImage) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .height(100.dp)
                            .width(75.dp)
                            .weight(0.18f, fill = false)
                            .clip(RoundedCornerShape(12.dp)),
                        alignment = Alignment.CenterStart,
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(scannedText.imageUri)
                            .crossfade(true)
                            .error(R.drawable.no_image)
                            .placeholder(R.drawable.no_image)
                            .build(),
                        contentDescription = "Image"
                    ) {
                        val state = painter.state
                        if (state is AsyncImagePainter.State.Loading) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colors.TextColor)
                            }
                        } else {
                            SubcomposeAsyncImageContent(
                                modifier = Modifier.clip(RectangleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(5.dp))

                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 12.dp)
                        .align(Alignment.CenterVertically)
                        .weight(0.6f)
                ) {
                    Column {
                        Text(
                            text = "Text content",
                            color = MaterialTheme.colors.TextColor,
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = scannedText.text,
                            color = MaterialTheme.colors.TextColor,
                            style = MaterialTheme.typography.subtitle2,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .wrapContentSize(Alignment.BottomStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            tint = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.7f) else Color.Black.copy(
                                alpha = 0.7f
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = convertTimeStampToDate(scannedText.scannedTime),
                            modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                            color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.7f) else Color.Black.copy(
                                alpha = 0.7f
                            ),
                            style = MaterialTheme.typography.subtitle2
                        )
                    }
                }
            }
        }
    }
}

