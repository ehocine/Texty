package com.hocel.texty.views.details_screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.hocel.texty.R
import com.hocel.texty.components.DeleteScannedTextSheetContent
import com.hocel.texty.components.Title
import com.hocel.texty.ui.theme.BackgroundColor
import com.hocel.texty.ui.theme.BottomSheetBackground
import com.hocel.texty.ui.theme.ButtonColor
import com.hocel.texty.ui.theme.TextColor
import com.hocel.texty.utils.copyToClipboard
import com.hocel.texty.utils.showInterstitial
import com.hocel.texty.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DetailsScreen(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedScannedText by mainViewModel.selectedScannedText
    val deleteModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    ModalBottomSheetLayout(
        scrimColor = Color.Black.copy(alpha = 0.6f),
        sheetState = deleteModalBottomSheetState,
        sheetElevation = 8.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetBackgroundColor = MaterialTheme.colors.BottomSheetBackground,
        sheetContent = {
            DeleteScannedTextSheetContent(
                context = context,
                scannedText = selectedScannedText,
                scope = scope,
                modalBottomSheetState = deleteModalBottomSheetState,
                mainViewModel = mainViewModel,
                onDeleteYes = {
                    navController.navigateUp()
                },
                onDeleteCancel = {
                    scope.launch {
                        deleteModalBottomSheetState.hide()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Details", color = MaterialTheme.colors.TextColor) },
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                deleteModalBottomSheetState.show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "",
                                tint = MaterialTheme.colors.TextColor
                            )
                        }
                    },
                    backgroundColor = MaterialTheme.colors.BackgroundColor,
                    contentColor = MaterialTheme.colors.TextColor,
                    elevation = 0.dp,
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp, 24.dp)
                                .clickable {
                                    navController.navigateUp()
                                },
                            tint = MaterialTheme.colors.TextColor
                        )
                    }
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                showInterstitial(context)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colors.BackgroundColor)
                ) {
                    item {
                        Box(Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            SubcomposeAsyncImage(
                                modifier = Modifier
                                    .padding(10.dp, 0.dp, 10.dp, 0.dp)
                                    .height(250.dp)
                                    .fillMaxWidth()
                                    .clip(RectangleShape)
                                    .align(Alignment.TopCenter),
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(selectedScannedText.imageUri)
                                    .crossfade(true)
                                    .error(R.drawable.no_image)
                                    .build(),
                                contentDescription = "Image"
                            ) {
                                when (painter.state) {
                                    is AsyncImagePainter.State.Loading -> {
                                        Box(
                                            Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = MaterialTheme.colors.TextColor)
                                        }
                                    }
                                    else -> {
                                        SubcomposeAsyncImageContent(
                                            modifier = Modifier.clip(RectangleShape),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Title(title = "Scanned text")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = selectedScannedText.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp, 0.dp, 16.dp, 0.dp),
                            color = MaterialTheme.colors.TextColor,
                            style = MaterialTheme.typography.body2,
                            textAlign = TextAlign.Start
                        )
                    }
                }
                Button(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp, 16.dp, 16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = MaterialTheme.colors.ButtonColor,
                        contentColor = Color.White
                    ),
                    onClick = {
                        copyToClipboard(context, selectedScannedText.text)
                    }) {
                    Text(text = "Copy to clipboard")

                }
            }
        }
    }
}
