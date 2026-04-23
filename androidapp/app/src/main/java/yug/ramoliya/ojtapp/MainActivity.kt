package yug.ramoliya.ojtapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import yug.ramoliya.ojtapp.ui.StudentApp
import yug.ramoliya.ojtapp.ui.StudentAppViewModel
import yug.ramoliya.ojtapp.ui.theme.OjtappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Force dark icons on the light status bar
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true
        setContent {
            val app = LocalContext.current.applicationContext as Application
            val vm: StudentAppViewModel = viewModel(factory = StudentAppViewModel.factory(app))
            OjtappTheme {
                StudentApp(vm)
            }
        }
    }
}
