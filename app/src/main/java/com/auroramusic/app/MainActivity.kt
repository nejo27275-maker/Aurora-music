package com.auroramusic.app

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.compose.AsyncImage
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch

private val AuroraPurple = Color(0xFFB8A7FF)
private val AuroraMint = Color(0xFF70E6D2)
private val AuroraBackground = Color(0xFF090A12)
private val AuroraSurface = Color(0xFF191A2A)

class MainActivity : ComponentActivity() {
    private var future: ListenableFuture<MediaController>? = null
    private var player: MediaController? = null
    private var currentTrackName by mutableStateOf("Escolha uma música do catálogo")
    private var currentArtist by mutableStateOf("")
    private var playing by mutableStateOf(false)
    private var clientId by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clientId = getSharedPreferences("aurora_settings", MODE_PRIVATE)
            .getString("jamendo_client_id", "").orEmpty()

        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        future = MediaController.Builder(this, token).buildAsync()
        future?.addListener({
            try {
                player = future?.get()
                player?.addListener(object : androidx.media3.common.Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        playing = isPlaying
                    }
                })
            } catch (_: Exception) {
                // A tela continua disponível; a mensagem será atualizada ao iniciar a reprodução.
            }
        }, mainExecutor)

        setContent {
            var searchText by remember { mutableStateOf("") }
            var clientIdInput by remember { mutableStateOf(clientId) }
            var showSettings by remember { mutableStateOf(clientId.isBlank()) }
            var tracks by remember { mutableStateOf<List<JamendoTrack>>(emptyList()) }
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()

            fun searchCatalog(query: String) {
                if (clientId.isBlank()) {
                    showSettings = true
                    errorMessage = "Ative o catálogo com seu Client ID gratuito do Jamendo."
                    return
                }
                scope.launch {
                    isLoading = true
                    errorMessage = ""
                    try {
                        tracks = JamendoCatalog.search(clientId, query)
                        if (tracks.isEmpty()) errorMessage = "Nenhuma faixa encontrada. Tente outro termo."
                    } catch (error: Exception) {
                        errorMessage = error.message ?: "Não foi possível carregar o catálogo. Verifique a internet."
                    } finally {
                        isLoading = false
                    }
                }
            }

            LaunchedEffect(clientId) {
                if (clientId.isNotBlank()) {
                    isLoading = true
                    errorMessage = ""
                    try {
                        tracks = JamendoCatalog.search(clientId)
                        if (tracks.isEmpty()) errorMessage = "O catálogo não retornou faixas."
                    } catch (error: Exception) {
                        errorMessage = error.message ?: "Não foi possível carregar o catálogo."
                    } finally {
                        isLoading = false
                    }
                }
            }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = AuroraPurple,
                    secondary = AuroraMint,
                    background = AuroraBackground,
                    surface = AuroraSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF211A43), AuroraBackground)))
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(Brush.linearGradient(listOf(AuroraPurple, AuroraMint))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = AuroraBackground)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("A U R O R A", color = AuroraPurple, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            Text("MUSIC", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        }
                        TextButton(onClick = {
                            clientIdInput = clientId
                            showSettings = !showSettings
                        }) {
                            Text(if (showSettings) "Fechar" else "Catálogo", color = AuroraMint)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Descubra sua próxima música.",
                        color = Color.White,
                        fontSize = 27.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "Streaming online • catálogo independente",
                        color = Color(0xFFB3B2C8),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    if (clientId.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("Buscar músicas ou artistas") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { searchCatalog(searchText) }),
                                shape = RoundedCornerShape(17.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AuroraPurple,
                                    unfocusedBorderColor = Color(0xFF4B4962),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            FilledIconButton(
                                onClick = { searchCatalog(searchText) },
                                modifier = Modifier.size(54.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = AuroraPurple)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Pesquisar", tint = AuroraBackground)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    if (showSettings) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = AuroraSurface)
                        ) {
                            Column(
                                Modifier.padding(17.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Ativar catálogo online", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    "O Jamendo oferece streaming de música independente. Para consultar a API, o app precisa de um Client ID gratuito.",
                                    color = Color(0xFFC4C3D4),
                                    fontSize = 13.sp
                                )
                                OutlinedTextField(
                                    value = clientIdInput,
                                    onValueChange = { clientIdInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Client ID do Jamendo") },
                                    placeholder = { Text("Cole seu Client ID aqui") },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AuroraMint,
                                        unfocusedBorderColor = Color(0xFF4B4962),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                Button(
                                    onClick = {
                                        val cleaned = clientIdInput.trim()
                                        if (cleaned.isBlank()) {
                                            errorMessage = "Cole o Client ID antes de salvar."
                                        } else {
                                            clientId = cleaned
                                            getSharedPreferences("aurora_settings", MODE_PRIVATE)
                                                .edit().putString("jamendo_client_id", cleaned).apply()
                                            showSettings = false
                                            searchText = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Salvar e carregar catálogo")
                                }
                                OutlinedButton(
                                    onClick = {
                                        runCatching {
                                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://devportal.jamendo.com/")))
                                        }.onFailure {
                                            errorMessage = "Não foi possível abrir o navegador."
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Obter Client ID gratuito")
                                }
                                Text(
                                    "Confira a licença de cada faixa antes de reutilizar ou publicar música. O catálogo não é o repertório completo do Spotify/YouTube Music.",
                                    color = Color(0xFF9293A8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (searchText.isBlank()) "Em destaque" else "Resultados",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = AuroraMint
                            )
                        }
                    }
                    Spacer(Modifier.height(9.dp))

                    if (errorMessage.isNotBlank()) {
                        Text(errorMessage, color = Color(0xFFFFB4AB), fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                    }

                    if (tracks.isEmpty() && !isLoading) {
                        Text(
                            if (clientId.isBlank()) "Ative o catálogo para encontrar e ouvir músicas online."
                            else "Pesquise por uma música ou artista para começar.",
                            color = Color(0xFFB3B2C8),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        items(tracks, key = { it.id }) { track ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { playTrack(track) },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = AuroraSurface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (track.image.startsWith("https://")) {
                                        AsyncImage(
                                            model = track.image,
                                            contentDescription = "Capa de ${track.name}",
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Brush.linearGradient(listOf(AuroraPurple, AuroraMint))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = AuroraBackground)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            track.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            track.artist,
                                            color = AuroraMint,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "${formatDuration(track.durationSeconds)} • Jamendo",
                                            color = Color(0xFF9293A8),
                                            fontSize = 11.sp
                                        )
                                    }
                                    IconButton(onClick = { playTrack(track) }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Reproduzir ${track.name}", tint = AuroraPurple)
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF25233A))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("TOCANDO AGORA", color = AuroraMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(currentTrackName, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (currentArtist.isNotBlank()) {
                                    Text(currentArtist, color = Color(0xFFB3B2C8), fontSize = 12.sp)
                                }
                            }
                            IconButton(
                                onClick = {
                                    val activePlayer = player
                                    if (activePlayer == null) {
                                        currentTrackName = "Preparando reprodutor..."
                                    } else {
                                        if (activePlayer.isPlaying) activePlayer.pause() else activePlayer.play()
                                        playing = activePlayer.isPlaying
                                    }
                                }
                            ) {
                                Icon(
                                    if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playing) "Pausar" else "Reproduzir",
                                    tint = AuroraPurple
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "AURORA MUSIC • MÚSICA INDEPENDENTE ONLINE",
                        color = Color(0xFF6D6F85),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    private fun playTrack(track: JamendoTrack) {
        val activePlayer = player
        if (activePlayer == null) {
            currentTrackName = "O reprodutor ainda está iniciando. Tente novamente."
            return
        }
        val mediaItem = MediaItem.Builder()
            .setUri(track.audio)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.name)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .build()
            )
            .build()
        activePlayer.setMediaItem(mediaItem)
        activePlayer.prepare()
        activePlayer.play()
        currentTrackName = track.name
        currentArtist = track.artist
        playing = true
    }

    override fun onDestroy() {
        future?.let { MediaController.releaseFuture(it) }
        player = null
        future = null
        super.onDestroy()
    }
}

private fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return "--:--"
    return "%d:%02d".format(seconds / 60, seconds % 60)
}
