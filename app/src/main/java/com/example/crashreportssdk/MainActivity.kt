package com.example.crashreportssdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.android_sdk.CrashReporter
import com.example.crashreportssdk.ui.theme.CrashReportsSdkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CrashReporter.init(this, "http://10.0.2.2:5000")

        enableEdgeToEdge()
        setContent {
            CrashReportsSdkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CrashDemoScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CrashDemoScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { throw RuntimeException("Test crash from button") }) {
            Text("Test Crash")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCrashDemo() {
    CrashReportsSdkTheme {
        CrashDemoScreen()
    }
}
