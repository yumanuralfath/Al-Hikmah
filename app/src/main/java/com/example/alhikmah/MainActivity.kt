package com.example.alhikmah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.alhikmah.ui.home.HomeScreen
import com.example.alhikmah.ui.theme.AlHikmahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlHikmahTheme {
                HomeScreen()
            }
        }
    }
}
