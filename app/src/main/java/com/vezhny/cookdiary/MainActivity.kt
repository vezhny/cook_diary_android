package com.vezhny.cookdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vezhny.cookdiary.ui.CookDiaryApp
import com.vezhny.cookdiary.ui.theme.CookDiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CookDiaryTheme {
                CookDiaryApp()
            }
        }
    }
}
