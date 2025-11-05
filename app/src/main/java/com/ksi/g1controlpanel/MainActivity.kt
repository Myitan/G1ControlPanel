package com.ksi.g1controlpanel

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ksi.g1controlpanel.ui.theme.G1ControlPanelTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class MainActivity : ComponentActivity() {

    private val robotApi by lazy {
        RetrofitClient.api
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            G1ControlPanelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val videoStreamUrl = "udp://@:6666"

                    ControlScreen(robotApi, videoStreamUrl)
                }
            }
        }
    }
}

@Composable
fun ControlScreen(api: RobotApiService, videoUrl: String) {
    var statusText by remember { mutableStateOf("Status: Ready") }
    val scope = rememberCoroutineScope()
    var batteryLevel by remember { mutableStateOf("...") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.size(16.dp))
        VlcPlayer(
            videoUrl = videoUrl,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        statusText = "Executing..."
                        val request = ExecuteRequest(
                            programName = "run_demo_1",
                            parameters = listOf("paramA", "paramB")
                        )
                        val response = api.executeProgram(request)
                        statusText = "Status: ${response.message}"
                    } catch (e: Exception) {
                        e.printStackTrace()
                        statusText = "Status: FAILED (Check network)"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Run Custom Program 1", style = MaterialTheme.typography.titleMedium)
        }

        Button(
            onClick = {
                scope.launch {
                    try {
                        statusText = "Getting status..."
                        val response = api.getRobotStatus()

                        batteryLevel = "${response.battery_percent}%"
                        statusText = "Status: Updated (Charging: ${response.is_charging})"
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val errorType = e.javaClass.simpleName
                        val errorMessage = e.message ?: "No details"
                        statusText = "Status: FAILED - $errorType: $errorMessage"
                        batteryLevel = "N/A"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text("Get Battery Status", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = statusText, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Battery: $batteryLevel", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun VlcPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val libVLC = remember {
        val args = arrayListOf(
            "--network-caching=150", // (ms)
            "--live-caching=150",
            "--sout-mux-caching=150",
            "--no-audio",            // Nie wiem jak z audio wiec jest off
            "--vout=android-display"
        )
        LibVLC(context, args)
    }

    val mediaPlayer = remember { MediaPlayer(libVLC) }

    DisposableEffect(videoUrl) {
        // --- Setup ---
        val media = Media(libVLC, Uri.parse(videoUrl))
        media.setHWDecoderEnabled(true, false)
        media.addOption(":no-audio")

        mediaPlayer.media = media
        media.release()

        // --- Teardown ---
        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
            libVLC.release()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = {
                VLCVideoLayout(context).apply {
                    mediaPlayer.attachViews(this, null, false, false)
                    mediaPlayer.play()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}