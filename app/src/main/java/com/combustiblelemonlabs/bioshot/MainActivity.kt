package com.combustiblelemonlabs.bioshot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.combustiblelemonlabs.bioshot.feature.navigation.BioShotNavGraph
import com.combustiblelemonlabs.bioshot.feature.viewmodel.BioShotViewModel
import com.combustiblelemonlabs.bioshot.ui.theme.BioShotTheme

class MainActivity : ComponentActivity() {
    private val viewModel: BioShotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val forceDark by viewModel.forceDark.collectAsState()
            val systemDark = isSystemInDarkTheme()
            BioShotTheme(darkTheme = forceDark || systemDark) {
                BioShotNavGraph(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
