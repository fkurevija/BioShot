package com.fkurevija.bioshot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.fkurevija.bioshot.feature.navigation.BioShotNavGraph
import com.fkurevija.bioshot.feature.viewmodel.BioShotViewModel
import com.fkurevija.bioshot.ui.theme.BioShotTheme

class MainActivity : ComponentActivity() {
    private val viewModel: BioShotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BioShotTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BioShotNavGraph(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
