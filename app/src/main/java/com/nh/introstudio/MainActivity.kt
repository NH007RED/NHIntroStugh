package com.nh.introstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nh.introstudio.ui.navigation.AppRoot
import com.nh.introstudio.ui.theme.NHTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            NHTheme { AppRoot() }
        }
    }
}
