package com.lionzxy.mobiusbleperfomance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lionzxy.mobiusbleperfomance.ui.screens.device.ComposableDeviceScreen
import com.lionzxy.mobiusbleperfomance.ui.screens.search.ComposableSearchScreen
import com.lionzxy.mobiusbleperfomance.ui.theme.MobiusBLEPerfomanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobiusBLEPerfomanceTheme {
                val navController = rememberNavController()
                NavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    startDestination = "SEARCHING"
                ) {
                    composable("SEARCHING") {
                        ComposableSearchScreen { address ->
                            navController.navigate("DEVICE?address=$address")
                        }
                    }
                    composable(
                        route = "DEVICE?address={address}",
                        arguments = listOf(
                            navArgument("address") {
                                type = NavType.StringType
                                nullable = true
                            }
                        )
                    ) { entry ->
                        val address = entry.arguments?.getString("address")!!
                        ComposableDeviceScreen(address)
                    }
                }
            }
        }
    }
}