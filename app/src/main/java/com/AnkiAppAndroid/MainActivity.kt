package com.AnkiAppAndroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.AnkiAppAndroid.ui.navigation.AppNavHost
import com.AnkiAppAndroid.ui.theme.AnkiAppAndroidTheme
import com.AnkiAppAndroid.ui.viewmodel.BaralhoViewModel
import com.AnkiAppAndroid.ui.viewmodel.LocalViewModel

class MainActivity : ComponentActivity() {

    private val baralhoViewModel: BaralhoViewModel by viewModels()
    private val localViewModel: LocalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnkiAppAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        baralhoViewModel = baralhoViewModel,
                        localViewModel = localViewModel
                    )
                }
            }
        }
    }
}