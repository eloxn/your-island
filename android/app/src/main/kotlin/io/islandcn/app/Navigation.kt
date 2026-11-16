package io.islandcn.app

import androidx.compose.runtime.Composable
import io.islandcn.app.ui.main.MainScreen
import io.islandcn.app.ui.main.MainScreenViewModel

@Composable
fun MainNavigation(viewModel: MainScreenViewModel) {
    MainScreen(viewModel = viewModel)
}
