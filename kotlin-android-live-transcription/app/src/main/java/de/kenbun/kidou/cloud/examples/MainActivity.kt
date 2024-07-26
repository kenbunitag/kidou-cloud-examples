package de.kenbun.kidou.cloud.examples

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import de.kenbun.kidou.cloud.examples.ui.theme.KotlinAndroidLiveTranscriptionTheme
import java.net.URI

class MainActivity : ComponentActivity() {

    companion object {
        @JvmStatic
        val KIDOU_CLOUD_ENDPOINT = URI.create("wss://cloud.kidou.ai/v1/api/transcribe")!!

        @JvmStatic
        val API_KEY = "" // <-- INSERT API KEY HERE (Receive free at https://cloud.kidou.ai)
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinAndroidLiveTranscriptionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    TranscriptionRecorder(activity = this)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun TranscriptionRecorder(activity: Activity) {
    var transcript by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var audioRecord: AudioRecord? = null

    // Ask for microphone permission if not already granted
    if (!isRecordAudioPermissionGranted(activity)) {
        askForAudioRecordingPermission(activity)
    }

    val kidouCloudWebSocket = WebSocketFactory().createSocket(MainActivity.KIDOU_CLOUD_ENDPOINT)
    kidouCloudWebSocket.addListener(object : WebSocketAdapter() {
        override fun onTextMessage(websocket: WebSocket?, text: String?) {
            if (text != null) transcript = text
        }
    })
    // Android forbids network on the main thread
    Thread {
        kidouCloudWebSocket.connect()
        // Send API key and sample rate to KIDOU CLOUD websocket
        kidouCloudWebSocket.sendText("""{ "api_key": "${MainActivity.API_KEY}", "sample_rate": 16000 }""")
    }.apply {
        start()
        join()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(modifier = Modifier.padding(vertical = Dp(15f)), text = transcript)
        ElevatedButton(modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(vertical = Dp(50f)), onClick = {
            if (isRecording) {
                audioRecord?.release()
                isRecording = false;
            } else {
                // Configure microphone
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    16000
                )
                audioRecord!!.startRecording()
                isRecording = true
                // Start audio processing in an other thread
                Thread {
                    val buffer = ByteArray(16000)
                    while (isRecording) {
                        // Read recorded audio
                        val bytesRead: Int = audioRecord!!.read(buffer, 0, buffer.size)
                        if (bytesRead > 0) {
                            // Send recorded audio to KIDOU CLOUD websocket
                            kidouCloudWebSocket.sendBinary(buffer.sliceArray(0..<bytesRead))
                        }
                    }
                }.start()
            }
        }) {
            Text(if (isRecording) "Stop recording" else "Start recording")
        }
    }
}

fun isRecordAudioPermissionGranted(activity: Activity) =
    ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

fun askForAudioRecordingPermission(activity: Activity) {
    //When permission is not granted by user, show them message why this permission is needed.
    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {
        Toast.makeText(activity, "Please grant permissions to record audio", Toast.LENGTH_LONG)
            .show()
        //Give user option to still opt-in the permissions
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
    } else {
        // Show user dialog to grant permission to record audio
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
    }
}