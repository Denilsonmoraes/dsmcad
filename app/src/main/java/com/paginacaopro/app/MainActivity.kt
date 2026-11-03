package com.paginacaopro.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paginacaopro.app.ui.screens.CatalogScreen
import com.paginacaopro.app.ui.screens.DimensionInputScreen
import com.paginacaopro.app.ui.screens.FloorPlanScreen
import com.paginacaopro.app.ui.screens.FreePlanStudioScreen
import com.paginacaopro.app.ui.screens.HomeScreen
import com.paginacaopro.app.ui.screens.MyTilingsScreen
import com.paginacaopro.app.ui.screens.PhotoCalibrationScreen
import com.paginacaopro.app.ui.screens.SettingsScreen
import com.paginacaopro.app.ui.screens.SimuladorScreen
import com.paginacaopro.app.ui.screens.TechnicalHelpScreen
import com.paginacaopro.app.ui.screens.TilingViewerScreen
import com.paginacaopro.app.ui.theme.PaginacaoProTheme
import com.paginacaopro.app.ui.viewmodel.PaginacaoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PaginacaoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableTrueImmersiveFullscreen()
        setContent {
            PaginacaoProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableTrueImmersiveFullscreen()
        }
    }

    fun enableTrueImmersiveFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

@Composable
fun AppNavigation(viewModel: PaginacaoViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "simulador") {
        // Simulador completo (todas as funções do simulador HTML) — tela inicial do app.
        // Voltar no simulador abre o menu nativo.
        composable("simulador") {
            SimuladorScreen(
                onExit = {
                    navController.navigate("home") {
                        popUpTo("simulador") { inclusive = false }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToFreePlanStudio = { navController.navigate("simulador") },
                onNavigateToNewTiling = { navController.navigate("dimension_input") },
                onNavigateToPhotoTiling = { navController.navigate("photo_calibration") },
                onNavigateToFloorPlan = { navController.navigate("floor_plan") },
                onNavigateToCatalog = { navController.navigate("catalog") },
                onNavigateToMyTilings = { navController.navigate("my_tilings") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToTechnicalHelp = { navController.navigate("technical_help") }
            )
        }

        composable("free_plan_studio") {
            FreePlanStudioScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToElevationViewer = { navController.navigate("tiling_viewer") }
            )
        }

        composable("dimension_input") {
            DimensionInputScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToViewer = { navController.navigate("tiling_viewer") }
            )
        }

        composable("floor_plan") {
            FloorPlanScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToViewer = { navController.navigate("tiling_viewer") }
            )
        }

        composable("tiling_viewer") {
            TilingViewerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("catalog") {
            CatalogScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("photo_calibration") {
            PhotoCalibrationScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToViewer = { navController.navigate("tiling_viewer") }
            )
        }

        composable("my_tilings") {
            MyTilingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenWall = { navController.navigate("tiling_viewer") }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("technical_help") {
            TechnicalHelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
