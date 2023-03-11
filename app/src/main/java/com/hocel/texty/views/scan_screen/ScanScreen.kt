package com.hocel.texty.views.scan_screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.hocel.texty.R
import com.hocel.texty.components.Title
import com.hocel.texty.data.models.ScannedText
import com.hocel.texty.ui.theme.BackgroundColor
import com.hocel.texty.ui.theme.ButtonColor
import com.hocel.texty.ui.theme.TextColor
import com.hocel.texty.utils.*
import com.hocel.texty.viewmodels.MainViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScanScreen(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val scanningState by mainViewModel.scanningStatus.collectAsState()
    val textLanguage by mainViewModel.textLanguage
    var text by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        mainViewModel.processImage {
            val scannedText = ScannedText(
                text = it,
                imageUri = mainViewModel.localImageUri.value.toString(),
                scannedTime = System.currentTimeMillis(),
                textLanguage = mainViewModel.textLanguage.value
            )
            text = it
            mainViewModel.addOrRemoveScannedText(
                context = context,
                action = AddOrRemoveAction.ADD,
                scannedText = scannedText,
                onAddSuccess = {
                    uploadScannedTextImage(
                        fileUri = mainViewModel.localImageUri.value,
                        scannedText = scannedText
                    )
                },
                onRemoveSuccess = {}
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text scan", color = MaterialTheme.colors.TextColor) },
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
                                .height(250.dp)
                                .padding(10.dp, 0.dp, 10.dp, 0.dp)
                                .fillMaxWidth()
                                .clip(RectangleShape)
                                .align(Alignment.TopCenter),
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(mainViewModel.localImageUri.value)
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
                    when (scanningState) {
                        ScanningStatus.LOADED -> {
                            showInterstitial(context)
                            Column(Modifier.fillMaxSize()) {
                                Text(
                                    text = "Text language: $textLanguage",
                                    modifier = Modifier
                                        .padding(16.dp, 0.dp, 0.dp, 0.dp),
                                    color = MaterialTheme.colors.TextColor,
                                    style = MaterialTheme.typography.subtitle2,
                                    fontWeight = FontWeight.W600,
                                    textAlign = TextAlign.Start
                                )
                                Spacer(modifier = Modifier.padding(16.dp))
                                Text(
                                    text = text.ifBlank { "Couldn't find any text" },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp, 0.dp, 16.dp, 0.dp),
                                    color = MaterialTheme.colors.TextColor,
                                    style = MaterialTheme.typography.body2,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                        ScanningStatus.ERROR -> {
                            Text(
                                text = "Something went wrong!",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp, 0.dp, 16.dp, 0.dp),
                                color = MaterialTheme.colors.TextColor,
                                style = MaterialTheme.typography.body2,
                                textAlign = TextAlign.Start
                            )
                        }
                        else -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.padding(24.dp))
                                Text(
                                    text = "Scanning image...",
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    color = MaterialTheme.colors.TextColor,
                                    style = MaterialTheme.typography.body2,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.padding(16.dp))
                                CircularProgressIndicator(color = MaterialTheme.colors.TextColor)
                            }
                        }
                    }
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
                enabled = scanningState == ScanningStatus.LOADED,
                onClick = {
                    copyToClipboard(context, text)
                }) {
                Text(text = "Copy to clipboard")
            }
        }
    }
}