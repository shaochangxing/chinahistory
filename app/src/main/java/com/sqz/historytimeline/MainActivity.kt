package com.sqz.historytimeline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sqz.historytimeline.ui.TimelineScreen
import com.sqz.historytimeline.ui.theme.HistoryTimelineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            HistoryTimelineTheme {
                TimelineScreen()
            }
        }
    }
}
