package com.simonecompany.lavagna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.simonecompany.lavagna.ui.BoardScreen
import com.simonecompany.lavagna.ui.theme.LavagnaTheme
import com.simonecompany.lavagna.vm.BoardViewModel

class MainActivity : ComponentActivity() {

    private val vm: BoardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LavagnaTheme {
                BoardScreen(vm)
            }
        }
    }
}