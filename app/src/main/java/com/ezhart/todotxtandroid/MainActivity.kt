package com.ezhart.todotxtandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        (this.application as TodotxtAndroidApplication).dropboxService.onResume()
    }
}




