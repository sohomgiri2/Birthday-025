package com.example.ui.sections

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.UserPhotoRecord
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.SubtleGlassButton
import com.example.ui.components.Tilt3DContainer
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlinx.coroutines.delay
import java.io.File

private enum class GalleryMode(val label: String) {
    GRID("Wall"),
    SLIDESHOW("Slideshow"),
    CARDS_3D("3D Cards"),
    RANDOM("Random")
}

@Composable
fun GallerySection(
    photos: List<UserPhotoRecord>,
    reduceAnimations: Boolean,
    onAddPhoto: (android.net.Uri, String) -> Unit,
    onRevealPhotoSecret: (String) -> Unit
) {
    var mode by remember { mutableStateOf(GalleryMode.GRID) }
    var lightboxPhoto by remember { mutableStateOf<UserPhotoRecord?>(null) }
    var pendingCaptionUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var captionDraft by remember { mutableStateOf("") }
    var slideshowIndex by remember { mutableIntStateOf(0) }
    var randomIndex by remember { mutableIntStateOf(0) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingCaptionUri = uri
            captionDraft = ""
        }
    }

    LaunchedEffect(mode, photos.size) {
        if (mode == GalleryMode.SLIDESHOW && photos.isNotEmpty()) {
            while (true) {
                delay(3200)
                slideshowIndex = (slideshowIndex + 1) % photos.size
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("MEMORY GALLERY", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
                Text("${photos.size} Loaded Photos", style = MaterialTheme.typography.titleLarge, color = SilverMist)
            }
            GlowingPillButton(
                text = "Add Photo",
                icon = Icons.Rounded.AddPhotoAlternate,
                testTag = "add_photo_button",
                onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // View mode switcher
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(GalleryMode.entries) { item ->
                val icon = when (item) {
                    GalleryMode.GRID -> Icons.Rounded.GridView
                    GalleryMode.SLIDESHOW -> Icons.Rounded.Slideshow
                    GalleryMode.CARDS_3D -> Icons.Rounded.ViewCarousel
                    GalleryMode.RANDOM -> Icons.Rounded.Shuffle
                }
                FilterChip(
                    selected = mode == item,
                    onClick = {
                        mode = item
                        if (item == GalleryMode.RANDOM && photos.isNotEmpty()) {
                            randomIndex = photos.indices.random()
                        }
                    },
                    label = { Text(item.label) },
                    leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NebulaViolet.copy(alpha = 0.35f),
                        selectedLabelColor = SilverMist,
                        selectedLeadingIconColor = SoftLavender,
                        labelColor = SilverMist.copy(alpha = 0.7f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (photos.isEmpty()) {
            // Graceful fallback when no assets/photos/photo1..300 or user-added photos exist yet
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Tilt3DContainer(enabled = !reduceAnimations) {
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_memory_fallback),
                                contentDescription = "Memory frame placeholder",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                                    .clip(RoundedCornerShape(18.dp))
                            )
                            Text(
                                text = "Your Gallery Is Ready",
                                style = MaterialTheme.typography.titleLarge,
                                color = SilverMist
                            )
                            Text(
                                text = "Drop numbered photos (photo1.png, photo2.jpg, ... up to photo300) into app/src/main/assets/photos/ or tap Add Photo above to save memories offline inside SG DataVault.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SilverMist.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            when (mode) {
                GalleryMode.GRID -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(148.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(photos, key = { it.id }) { photo ->
                            LiquidGlassCard(
                                onClick = { lightboxPhoto = photo },
                                contentPadding = 8.dp
                            ) {
                                Column {
                                    PhotoMediaView(
                                        photo = photo,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = photo.caption,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SilverMist,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                GalleryMode.SLIDESHOW -> {
                    val active = photos[slideshowIndex % photos.size]
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedContent(targetState = active, label = "slideshow") { item ->
                            Tilt3DContainer(enabled = !reduceAnimations) {
                                LiquidGlassCard(
                                    onClick = { lightboxPhoto = item },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        PhotoMediaView(
                                            photo = item,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(290.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(item.caption, style = MaterialTheme.typography.titleMedium, color = SilverMist)
                                        Text("Tap photo to zoom & inspect secret note", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
                                    }
                                }
                            }
                        }
                    }
                }

                GalleryMode.CARDS_3D -> {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(photos, key = { it.id }) { photo ->
                            Tilt3DContainer(enabled = !reduceAnimations) {
                                LiquidGlassCard(
                                    onClick = { lightboxPhoto = photo },
                                    modifier = Modifier
                                        .size(width = 250.dp, height = 340.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        PhotoMediaView(
                                            photo = photo,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(photo.caption, style = MaterialTheme.typography.titleMedium, color = SilverMist)
                                        Text("Drag card to tilt in 3D", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
                                    }
                                }
                            }
                        }
                    }
                }

                GalleryMode.RANDOM -> {
                    val chosen = photos[randomIndex % photos.size]
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LiquidGlassCard(
                            onClick = { lightboxPhoto = chosen },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                PhotoMediaView(
                                    photo = chosen,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(270.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(chosen.caption, style = MaterialTheme.typography.titleLarge, color = SilverMist)
                                Spacer(modifier = Modifier.height(8.dp))
                                SubtleGlassButton(
                                    text = "Shuffle Random Memory",
                                    icon = Icons.Rounded.Shuffle,
                                    onClick = { randomIndex = photos.indices.random() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Caption prompt when user adds a photo
    if (pendingCaptionUri != null) {
        AlertDialog(
            onDismissRequest = { pendingCaptionUri = null },
            title = { Text("Add Memory Caption") },
            text = {
                OutlinedTextField(
                    value = captionDraft,
                    onValueChange = { captionDraft = it },
                    placeholder = { Text("e.g., Sunset walk, Birthday laugh...") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingCaptionUri?.let { uri -> onAddPhoto(uri, captionDraft) }
                    pendingCaptionUri = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCaptionUri = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Fullscreen Zoomable Lightbox with Hidden Message Reveal
    lightboxPhoto?.let { photo ->
        var scale by remember { mutableFloatStateOf(1f) }
        Dialog(
            onDismissRequest = { lightboxPhoto = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBlack.copy(alpha = 0.96f))
                    .padding(16.dp)
            ) {
                PhotoMediaView(
                    photo = photo,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 3.5f)
                            }
                        }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
                IconButton(
                    onClick = { lightboxPhoto = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.White.copy(alpha = 0.14f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close lightbox", tint = SilverMist)
                }

                LiquidGlassCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(photo.caption, style = MaterialTheme.typography.titleMedium, color = SilverMist)
                            Text("Pinch to zoom", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
                        }
                        GlowingPillButton(
                            text = "Hidden Note",
                            icon = Icons.Rounded.AutoAwesome,
                            accentColor = StarlightGold,
                            onClick = { onRevealPhotoSecret(photo.secretNote) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoMediaView(
    photo: UserPhotoRecord,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val model: Any = when {
        photo.isAsset && photo.assetPath != null -> photo.assetPath
        photo.localFilePath != null -> File(photo.localFilePath)
        else -> R.drawable.img_memory_fallback
    }
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(model)
            .crossfade(true)
            .error(R.drawable.img_memory_fallback)
            .build(),
        contentDescription = photo.caption,
        contentScale = contentScale,
        modifier = modifier
    )
}
