package com.auroramusic.app

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : ComponentActivity() {
    private var future: ListenableFuture<MediaController>? = null
    private var player: MediaController? = null
    private var trackName by mutableStateOf("Nenhuma música selecionada")
    private var playing by mutableStateOf(false)

    private val audioPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) { }
            trackName = uri.lastPathSegment?.substringAfterLast('/') ?: "Áudio selecionado"
            player?.setMediaItem(MediaItem.fromUri(uri))
            player?.prepare()
            player?.play()
            playing = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        future = MediaController.Builder(this, token).buildAsync()
        future?.addListener({
            try {
                player = future?.get()
                player?.addListener(object : androidx.media3.common.Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) { playing = isPlaying }
                })
            } catch (_: Exception) { }
        }, mainExecutor)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFFB8A7FF),
                secondary = Color(0xFF70E6D2),
                background = Color(0xFF090A12),
                surface = Color(0xFF191A2A)
            )) {
                Column(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color(0xFF28204B), Color(0xFF090A12)))
                    ).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text("A U R O R A", color = Color(0xFFB8A7FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Sua música.\nSeu universo.", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                    Text("BETA 0.1 • REPRODUTOR LOCAL", color = Color(0xFF70E6D2), fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF191A2A))
                    ) {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("TOCANDO AGORA", color = Color(0xFFB8A7FF), fontWeight = FontWeight.Bold)
                            Text(trackName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Text("Selecione um arquivo de áudio salvo no aparelho.", color = Color.LightGray)
                            Button(onClick = { audioPicker.launch(arrayOf("audio/*")) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Escolher música")
                            }
                            Button(onClick = {
                                player?.let { p ->
                                    if (p.isPlaying) p.pause() else p.play()
                                    playing = p.isPlaying
                                }
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (playing) "Pausar" else "Reproduzir")
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text("O áudio Bluetooth usa o roteamento normal do Android.", color = Color(0xFF9293A8), fontSize = 12.sp)
                    Text("AURORA MUSIC • EM DESENVOLVIMENTO", color = Color(0xFF6D6F85), fontSize = 10.sp)
                }
            }
        }
    }

    override fun onDestroy() {
        future?.let { MediaController.releaseFuture(it) }
        player = null
        future = null
        super.onDestroy()
    }
}