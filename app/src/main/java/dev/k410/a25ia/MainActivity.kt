package dev.k410.a25ia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.k410.a25ia.ui.A25IaApp
import dev.k410.a25ia.ui.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as A25IaApplication).container
        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModel.Factory(container),
            )
            A25IaApp(viewModel)
        }
    }
}
