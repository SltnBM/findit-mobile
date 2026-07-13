package com.sultan.findit

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sultan.findit.data.util.RootDetector
import com.sultan.findit.ui.navigation.AppNavigation
import com.sultan.findit.ui.theme.FindItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        super.onCreate(savedInstanceState)

        val isRooted = RootDetector.isDeviceRooted()

        setContent {
            FindItTheme {
                val showWarning = remember { mutableStateOf(isRooted) }

                if (showWarning.value) {
                    AlertDialog(
                        onDismissRequest = { showWarning.value = false },
                        title = { Text("Perangkat Tidak Aman") },
                        text = { Text("Aplikasi mendeteksi bahwa perangkat Anda telah di-root. Aplikasi tetap dapat berjalan, namun tingkat keamanan data lokal Anda berada dalam risiko.") },
                        confirmButton = {
                            TextButton(onClick = { showWarning.value = false }) {
                                Text("Mengerti")
                            }
                        }
                    )
                }

                AppNavigation()
            }
        }
    }
}