package com.example.ui.sections

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.config.AppConfig
import com.example.data.VaultSettings
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.SubtleGlassButton
import com.example.ui.components.Tilt3DContainer
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.RoseQuartz
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlinx.coroutines.launch

@Composable
fun SettingsVaultDeveloperSection(
    settings: VaultSettings,
    storageUsageBytes: Long,
    onUpdateSettings: ((VaultSettings) -> VaultSettings) -> Unit,
    onExportJson: suspend () -> String,
    onImportJson: (String) -> Unit,
    onResetProgress: () -> Unit,
    onClearAllData: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showClearConfirm by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = onExportJson()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray())
                    }
                    onShowMessage("SG DataVault backup exported")
                } catch (_: Exception) {
                    onShowMessage("Export failed")
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    if (raw != null) onImportJson(raw)
                } catch (_: Exception) {
                    onShowMessage("Could not read backup file")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("CUSTOMIZATION & DATAVAULT", style = MaterialTheme.typography.labelSmall, color = SoftLavender)

        // Settings Card
        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Experience Settings", style = MaterialTheme.typography.titleLarge, color = SilverMist)

                SettingSwitchRow("Background Music", settings.bgmEnabled) { v ->
                    onUpdateSettings { it.copy(bgmEnabled = v) }
                }
                SettingSwitchRow("Sound Effects", settings.sfxEnabled) { v ->
                    onUpdateSettings { it.copy(sfxEnabled = v) }
                }
                SettingSwitchRow("Floating Star Particles", settings.particlesEnabled) { v ->
                    onUpdateSettings { it.copy(particlesEnabled = v) }
                }
                SettingSwitchRow("Reduce Animations", settings.reduceAnimations) { v ->
                    onUpdateSettings { it.copy(reduceAnimations = v) }
                }
                SettingSwitchRow("Frost Midnight Theme Variation", settings.frostVariation) { v ->
                    onUpdateSettings { it.copy(frostVariation = v) }
                }
                SettingSwitchRow("Immersive Fullscreen Chrome", settings.immersiveFullscreen) { v ->
                    onUpdateSettings { it.copy(immersiveFullscreen = v) }
                }
                SettingSwitchRow("Random Surprise Popups", settings.surprisePopupsEnabled) { v ->
                    onUpdateSettings { it.copy(surprisePopupsEnabled = v) }
                }

                if (settings.surprisePopupsEnabled) {
                    Text(
                        "Popup Interval: ${settings.popupFrequencySeconds}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftLavender
                    )
                    Slider(
                        value = settings.popupFrequencySeconds.toFloat(),
                        onValueChange = { freq ->
                            onUpdateSettings { it.copy(popupFrequencySeconds = freq.toInt()) }
                        },
                        valueRange = 20f..120f
                    )
                }

                Text(
                    "Master Volume: ${(settings.volume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftLavender
                )
                Slider(
                    value = settings.volume,
                    onValueChange = { vol -> onUpdateSettings { it.copy(volume = vol) } }
                )
            }
        }

        // SG DataVault Card
        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), glowColor = AuroraMint) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("SG DATAVAULT • OFFLINE STORAGE", style = MaterialTheme.typography.labelSmall, color = AuroraMint)
                Text(
                    "SG DataVault — Developer ${AppConfig.DEVELOPER_NAME}",
                    style = MaterialTheme.typography.titleMedium,
                    color = SilverMist
                )
                val kb = (storageUsageBytes / 1024.0)
                Text(
                    text = "Local Storage Used: ${"%.1f".format(kb)} KB (Offline-first JSON + Media Vault)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverMist.copy(alpha = 0.78f)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SubtleGlassButton(
                        text = "Export JSON",
                        icon = Icons.Rounded.Download,
                        modifier = Modifier.weight(1f),
                        onClick = { exportLauncher.launch("sg_datavault_backup.json") }
                    )
                    SubtleGlassButton(
                        text = "Import JSON",
                        icon = Icons.Rounded.Upload,
                        modifier = Modifier.weight(1f),
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SubtleGlassButton(
                        text = "Reset Progress",
                        icon = Icons.Rounded.RestartAlt,
                        modifier = Modifier.weight(1f),
                        onClick = onResetProgress
                    )
                    GlowingPillButton(
                        text = "Clear Data",
                        icon = Icons.Rounded.DeleteForever,
                        accentColor = RoseQuartz,
                        testTag = "clear_vault_data_button",
                        modifier = Modifier.weight(1f),
                        onClick = { showClearConfirm = true }
                    )
                }
            }
        }

        // Developer Credits Card
        Tilt3DContainer(enabled = !settings.reduceAnimations) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth(), glowColor = StarlightGold) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            Icons.Rounded.Code,
                            contentDescription = null,
                            tint = StarlightGold
                        )
                        Text("DEVELOPER CREDITS", style = MaterialTheme.typography.labelSmall, color = StarlightGold)
                    }
                    Text(AppConfig.DEVELOPER_NAME, style = MaterialTheme.typography.headlineMedium, color = SilverMist)
                    Text(AppConfig.DEVELOPER_ROLE, style = MaterialTheme.typography.bodyMedium, color = SoftLavender)
                    Text("Instagram: ${AppConfig.DEVELOPER_HANDLE}", style = MaterialTheme.typography.labelLarge, color = SilverMist)

                    Spacer(modifier = Modifier.height(4.dp))
                    GlowingPillButton(
                        text = "Open Instagram (${AppConfig.DEVELOPER_HANDLE})",
                        icon = Icons.AutoMirrored.Rounded.OpenInNew,
                        accentColor = NebulaViolet,
                        testTag = "developer_instagram_button",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.DEVELOPER_INSTAGRAM_URL))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear SG DataVault?") },
            text = { Text("This will erase all saved progress, high scores, pet stats, and imported local photos.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    onClearAllData()
                }) {
                    Text("Delete Everything", color = RoseQuartz)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = SilverMist, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
