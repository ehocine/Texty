package com.hocel.texty.views.home

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.hocel.texty.R
import com.hocel.texty.components.*
import com.hocel.texty.data.models.ScannedText
import com.hocel.texty.navigation.Screens
import com.hocel.texty.ui.theme.*
import com.hocel.texty.utils.LoadingState
import com.hocel.texty.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user by mainViewModel.userInfo.collectAsState()
    var imageChosen by remember { mutableStateOf(false) }
    var scannedTextToDelete by remember { mutableStateOf(ScannedText()) }
    val state by mainViewModel.gettingData.collectAsState()

    val deleteModalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val launcher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        mainViewModel.setLocalImageUri(uri)
        imageChosen = true
    }

    LaunchedEffect(key1 = imageChosen) {
        if (imageChosen) {
            navController.navigate(Screens.ScanScreen.route)
        }
    }

    ModalBottomSheetLayout(
        scrimColor = Color.Black.copy(alpha = 0.6f),
        sheetState = deleteModalBottomSheetState,
        sheetElevation = 8.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetBackgroundColor = MaterialTheme.colors.BottomSheetBackground,
        sheetContent = {
            DeleteScannedTextSheetContent(
                context = context,
                scannedText = scannedTextToDelete,
                scope = scope,
                modalBottomSheetState = deleteModalBottomSheetState,
                mainViewModel = mainViewModel,
                onDeleteYes = {},
                onDeleteCancel = {
                    scope.launch {
                        deleteModalBottomSheetState.hide()
                    }
                }
            )
        }
    ) {
        Scaffold(
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            text = "Scan image",
                            color = Color.White
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            tint = Color.White,
                            contentDescription = null
                        )
                    },
                    backgroundColor = MaterialTheme.colors.ButtonColor,
                    onClick = {
                        launcher.launch("image/*")
                    }
                )
            },
            bottomBar = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    // shows a traditional banner test ad
                    AndroidView(
                        factory = { context ->
                            AdView(context).apply {
                                setAdSize(AdSize.BANNER)
                                adUnitId = context.getString(R.string.ad_id_banner)
                                loadAd(AdRequest.Builder().build())
                            }
                        }
                    )
                }
            }
        ) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.BackgroundColor) {
                Column(Modifier.fillMaxSize()) {
                    TopBar(context = context, mainViewModel = mainViewModel)
                    Spacer(modifier = Modifier.height(8.dp))

                    when (state) {
                        LoadingState.LOADING -> LoadingList()
                        LoadingState.ERROR -> ErrorLoadingResults()
                        else -> {
                            if (user.listOfScannedText.isEmpty()) {
                                NoResults()
                            } else {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Here are your scanned texts",
                                        textAlign = TextAlign.Start,
                                        style = MaterialTheme.typography.subtitle1,
                                        color = MaterialTheme.colors.TextColor,
                                        modifier = Modifier.weight(9f)
                                    )
                                }
                                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                    items(user.listOfScannedText) { scannedTextEntity ->
                                        ScannedItem(
                                            scannedText = scannedTextEntity,
                                            onItemClicked = {
                                                mainViewModel.selectScannedText(it)
                                                navController.navigate(Screens.DetailsScreen.route)
                                            },
                                            enableDeleteAction = true,
                                            deleteScannedText = {
                                                scannedTextToDelete = it
                                                scope.launch {
                                                    deleteModalBottomSheetState.show()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
