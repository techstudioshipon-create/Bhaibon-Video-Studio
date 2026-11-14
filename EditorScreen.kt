package com.bhaibon.videostudio.ui.editor

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.bhaibon.videostudio.utils.VideoProcessor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen() {
    val context = LocalContext.current
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Cut, 1: Reels, 2: Text, 3: Audio
    var isReelsMode by remember { mutableStateOf(false) }
    var captionText by remember { mutableStateOf("") }
    var trimRange by remember { mutableStateOf(0f..10f) }
    var isExporting by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            videoUri = it
            exoPlayer.setMediaItem(MediaItem.fromUri(it))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            audioUri = it
            Toast.makeText(context, "অডিও যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bhaibon Video Studio", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF181818)),
                actions = {
                    Button(
                        onClick = {
                            if (videoUri == null) {
                                Toast.makeText(context, "আগে একটি ভিডিও সিলেক্ট করুন", Toast.LENGTH_SHORT).show()
                            } else {
                                isExporting = true
                                VideoProcessor.exportVideo(
                                    context = context,
                                    videoUri = videoUri!!,
                                    startMs = (trimRange.start * 1000).toLong(),
                                    endMs = (trimRange.end * 1000).toLong(),
                                    isReels9x16 = isReelsMode,
                                    onSuccess = { path ->
                                        isExporting = false
                                        Toast.makeText(context, "সেভ হয়েছে: $path", Toast.LENGTH_LONG).show()
                                    },
                                    onError = { err ->
                                        isExporting = false
                                        Toast.makeText(context, "সমস্যা: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3EA6FF))
                    ) {
                        Text(if (isExporting) "রেন্ডার হচ্ছে..." else "Export", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = Color(0xFF1E1E1E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Program Monitor (Preview Screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (videoUri != null) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply { player = exoPlayer }
                        },
                        modifier = if (isReelsMode) Modifier.aspectRatio(9f / 16f) else Modifier.fillMaxSize()
                    )
                    if (captionText.isNotEmpty()) {
                        Text(
                            text = captionText,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp)
                                .background(Color(0x99000000), RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = { videoPicker.launch("video/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2D2D))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF3EA6FF))
                        Spacer(Modifier.width(8.dp))
                        Text("+ ভিডিও ইমপোর্ট করুন", color = Color.White)
                    }
                }
            }

            // 2. Multi-Track Timeline Preview (Premiere Pro Style)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f)
                    .background(Color(0xFF252525))
                    .padding(8.dp)
            ) {
                Text("TIMELINE (V1 / A1)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                // V1 Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(Color(0xFF3B5998), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF3EA6FF), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (videoUri != null) "V1: Video Clip (${trimRange.start.toInt()}s - ${trimRange.end.toInt()}s)" else "V1: Empty Video Track",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                // A1 Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (audioUri != null) "A1: Custom Music Track Added" else "A1: Original / Background Audio",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            // 3. Active Tool Controls Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.20f)
                    .background(Color(0xFF1A1A1A))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                when (selectedTab) {
                    0 -> Column {
                        Text("সহজ ভিডিও কাটিং (Trim Seconds): ${trimRange.start.toInt()}s - ${trimRange.end.toInt()}s", color = Color.White, fontSize = 13.sp)
                        RangeSlider(
                            value = trimRange,
                            onValueChange = { trimRange = it },
                            valueRange = 0f..60f
                        )
                    }
                    1 -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("রিলস / শর্টস মোড (9:16 Vertical): ", color = Color.White)
                        Switch(checked = isReelsMode, onCheckedChange = { isReelsMode = it })
                    }
                    2 -> OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        label = { Text("ভিডিওর ওপর টেক্সট / ক্যাপশন লিখুন", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    3 -> Button(onClick = { audioPicker.launch("audio/*") }) {
                        Icon(Icons.Default.MusicNote, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("ফোন থেকে মিউজিক / অডিও যুক্ত করুন")
                    }
                }
            }

            // 4. Bottom Premiere Tool Dock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFF121212)),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolButton("কাটিং", Icons.Default.ContentCut, selectedTab == 0) { selectedTab = 0 }
                ToolButton("রিলস", Icons.Default.Smartphone, selectedTab == 1) { selectedTab = 1 }
                ToolButton("টেক্সট", Icons.Default.Title, selectedTab == 2) { selectedTab = 2 }
                ToolButton("অডিও", Icons.Default.Audiotrack, selectedTab == 3) { selectedTab = 3 }
            }
        }
    }
}

@Composable
fun ToolButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = if (isSelected) Color(0xFF3EA6FF) else Color.Gray)
            Text(label, color = if (isSelected) Color(0xFF3EA6FF) else Color.Gray, fontSize = 11.sp)
        }
    }
}
