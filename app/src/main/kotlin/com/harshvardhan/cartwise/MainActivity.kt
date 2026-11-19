package com.harshvardhan.cartwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.harshvardhan.cartwise.ui.CartwiseNavHost
import com.harshvardhan.cartwise.ui.theme.CartwiseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CartwiseTheme {
                CartwiseNavHost()
            }
        }
    }
}
