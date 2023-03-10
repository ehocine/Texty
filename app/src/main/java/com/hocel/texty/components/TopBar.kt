package com.hocel.texty.components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hocel.texty.ui.theme.TextColor
import com.hocel.texty.viewmodels.MainViewModel

@Composable
fun TopBar(
    context: Context,
    mainViewModel: MainViewModel
) {
    LaunchedEffect(key1 = true) {
        mainViewModel.getUserInfo(context = context)
    }

    val user by mainViewModel.userInfo.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Hey ${user.name}",
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.TextColor
            )
        }
    }
}