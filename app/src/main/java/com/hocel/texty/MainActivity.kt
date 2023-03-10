package com.hocel.texty

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hocel.texty.navigation.NavGraph
import com.hocel.texty.ui.theme.BackgroundColor
import com.hocel.texty.ui.theme.TextyTheme
import com.hocel.texty.utils.loadInterstitial
import com.hocel.texty.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    lateinit var navController: NavHostController
    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextyTheme {
                val systemUiController = rememberSystemUiController()
                val systemUIColor = MaterialTheme.colors.BackgroundColor
                navController = rememberAnimatedNavController()
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = systemUIColor
                    )
                }
                NavGraph(
                    navController = navController,
                    mainViewModel = mainViewModel
                )
            }
        }
        loadInterstitial(this)
    }
}