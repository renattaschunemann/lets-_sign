package br.com.letssign

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.letssign.core.designsystem.theme.LetsSignTheme
import br.com.letssign.core.navigation.LetsSignNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LetsSignTheme {
                LetsSignNavHost()
            }
        }
    }
}
