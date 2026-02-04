package com.example.crashreportssdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.android_sdk.CrashReporter
import com.example.crashreportssdk.ui.theme.CrashReportsSdkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            CrashReportsSdkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CrashDemoScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CrashDemoScreen(modifier: Modifier = Modifier) {
    var showSnackBar by remember { mutableStateOf(false) }

    if (showSnackBar) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            showSnackBar = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Crash Reporter SDK Demo",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Demonstration of fatal and non-fatal crash reporting",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    CrashReporter.logException(
                        RuntimeException("Demo non-fatal crash from SDK")
                    )
                    showSnackBar = true
                }
            ) {
                Text("Send Non-Fatal Crash")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                onClick = {
                    throw RuntimeException("Demo fatal crash from SDK")
                }
            ) {
                Text("Trigger Fatal Crash")
            }
        }

        if (showSnackBar) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Non-fatal crash sent successfully")
            }
        }
    }
}
