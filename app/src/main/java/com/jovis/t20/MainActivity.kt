package com.jovis.t20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.jovis.t20.ui.SheetsViewModel
import com.jovis.t20.ui.T20App
import com.jovis.t20.ui.theme.T20Theme

class MainActivity : ComponentActivity() {

    private val viewModel: SheetsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            T20Theme {
                T20App(viewModel)
            }
        }
    }
}
