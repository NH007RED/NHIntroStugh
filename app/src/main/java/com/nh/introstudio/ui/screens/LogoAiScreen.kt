package com.nh.introstudio.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.introstudio.domain.AspectRatio
import com.nh.introstudio.domain.BgMode
import com.nh.introstudio.domain.EmblemType
import com.nh.introstudio.domain.InOutStyle
import com.nh.introstudio.domain.LayoutMode
import com.nh.introstudio.domain.LogoAiAnalyzer
import com.nh.introstudio.domain.LogoStyle
import com.nh.introstudio.domain.MascotType
import com.nh.introstudio.templates.TemplateCatalog
import com.nh.introstudio.ui.components.ChoiceChips
import com.nh.introstudio.ui.components.ColorRow
import com.nh.introstudio.ui.components.GlassCard
import com.nh.introstudio.ui.components.GlassIconButton
import com.nh.introstudio.ui.components.NeonButton
import com.nh.introstudio.ui.components.ScreenHeader
import com.nh.introstudio.ui.components.SectionLabel
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 1-Click Logo Animation AI Studio screen (v4) featuring 20 distinct AI Logo Animation
 * templates with Deep Unified IN/IDLE/OUT Fusion (sub-frame motion blur, unified surface
 * specular light sweep across logo & text, foreground light-wrap, and 5 seamless In/Out transitions).
 */
@Composable
fun LogoAiScreen(
    app: AppViewModel,
    onExportProject: (Long) -> Unit,
    onOpenEditor: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    val logoTemplates = TemplateCatalog.logoAiTemplates
    var cfg by remember { mutableStateOf(TemplateCatalog.defaultConfig(1)) }
    var logoFileName by remember { mutableStateOf<String?>(null) }
    var promptText by remember { mutableStateOf("") }
    var replayKey by remember { mutableIntStateOf(0) }
    var aiStatus by remember { mutableStateOf("1. Bird & Vortex Crest · Deep Unified In/Out Fusion") }

    val pickLogo = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            app.importLogoStandalone(uri, { displayName, updatedCfg ->
                logoFileName = displayName
                cfg = updatedCfg
                replayKey++
                aiStatus = "Custom logo imported ($displayName) · Unified light-wrap active"
            }, cfg)
        }
    }

    val selectedSpec = TemplateCatalog.byId(cfg.templateId)
    val effectiveLogoStyle = cfg.logoStyleOverride ?: selectedSpec.logoStyle
    val effectiveInOut = cfg.inOutStyleOverride ?: selectedSpec.inOutStyle
    val effectiveMascot = cfg.mascotOverride ?: selectedSpec.mascot
    val effectiveEmblem = cfg.emblemOverride ?: selectedSpec.emblem

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            "Logo Animation AI v4",
            subtitle = "Deep Unified In/Out Fusion · 20 Logo AI Choreographies"
        ) {
            GlassIconButton(Icons.Filled.Refresh, { replayKey++ }, size = 38.dp)
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeaturePill("Unified Light-Wrap")
                FeaturePill("Deep In/Out Motion Blur")
                FeaturePill("20 Unique Logo Reveals")
            }

            // Live Looping Preview Box
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.fillMaxWidth().heightIn(max = 220.dp), contentAlignment = Alignment.Center) {
                        LoopingPreview(cfg, replayKey, Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(aiStatus, color = NH.Green, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text("${cfg.durationSec.toInt()}s · ${cfg.aspect.label}", color = NH.Muted, fontSize = 12.sp)
                    }
                }
            }

            // STEP 01 — LOGO & BRAND TEXT
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    StepHeader("01", "Logo & Brand Text", "Unified specular light sweep fuses your logo and typography")
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cfg.brandName,
                        onValueChange = { cfg = cfg.copy(brandName = it) },
                        label = { Text("Line 1 — Brand Name (e.g. Logoipsum)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = cfg.tagline,
                        onValueChange = { cfg = cfg.copy(tagline = it) },
                        label = { Text("Line 2 — Tagline (e.g. University)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, NH.Purple.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .clickable { pickLogo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Image, null, tint = NH.Purple, modifier = Modifier.size(26.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (cfg.logoPath == null) "Tap to upload your logo (or use Vector Emblem)"
                                else "Custom Logo: ${logoFileName ?: "Active"} (Tap to replace)",
                                color = NH.Text,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (cfg.logoPath != null) {
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Remove solid logo background (Auto-key)", color = NH.Text, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Switch(
                                checked = cfg.removeLogoBg,
                                onCheckedChange = { v -> cfg = cfg.copy(removeLogoBg = v); replayKey++ },
                                colors = SwitchDefaults.colors(checkedTrackColor = NH.Purple, checkedThumbColor = Color.White)
                            )
                        }
                        NeonButton(
                            "Switch back to Vector Emblem",
                            { cfg = cfg.copy(logoPath = null); logoFileName = null; replayKey++ },
                            Modifier.fillMaxWidth().padding(top = 4.dp),
                            Icons.Filled.Delete,
                            outlined = true
                        )
                    }
                }
            }

            // STEP 02 — 20 LOGO ANIMATION TYPES + DEEP IN/OUT TRANSITION
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    StepHeader("02", "Animation & Deep In/Out Fusion", "20 logo choreographies + 5 broadcast In/Out camera transitions")
                    Spacer(Modifier.height(8.dp))
                    ChoiceChips(
                        options = logoTemplates,
                        selected = selectedSpec,
                        label = { "${it.id}. ${it.name}" },
                        onSelect = { spec ->
                            val def = TemplateCatalog.defaultConfig(spec.id)
                            cfg = cfg.copy(
                                templateId = spec.id,
                                showLogo = true,
                                layoutMode = LayoutMode.AUTO,
                                durationSec = def.durationSec,
                                logoStyleOverride = null,
                                textStyleOverride = null,
                                inOutStyleOverride = null,
                                mascotOverride = null,
                                emblemOverride = null,
                                useCustomColors = false,
                                colorPrimary = spec.c1,
                                colorSecondary = spec.c2,
                                colorText = spec.textColor,
                                bgSolidColor = spec.bg1,
                                musicSource = def.musicSource,
                                musicName = def.musicName
                            )
                            aiStatus = "${spec.id}. ${spec.name}: ${spec.inOutStyle.label}"
                            replayKey++
                        }
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(selectedSpec.description, color = NH.Muted, fontSize = 12.sp)

                    SectionLabel("Deep In/Out camera & dissolve physics")
                    ChoiceChips(
                        options = InOutStyle.values().toList(),
                        selected = effectiveInOut,
                        label = { it.label },
                        onSelect = { io -> cfg = cfg.copy(inOutStyleOverride = io); replayKey++ }
                    )

                    SectionLabel("Layout mode")
                    ChoiceChips(
                        options = listOf(LayoutMode.AUTO, LayoutMode.HORIZONTAL, LayoutMode.CENTERED),
                        selected = cfg.layoutMode,
                        label = { it.label },
                        onSelect = { m -> cfg = cfg.copy(layoutMode = m); replayKey++ }
                    )

                    if (effectiveLogoStyle == LogoStyle.MASCOT_VORTEX_MORPH) {
                        SectionLabel("Flying mascot (for Template 1 Bird/Mascot Morph)")
                        ChoiceChips(
                            options = MascotType.values().toList(),
                            selected = effectiveMascot,
                            label = { it.label },
                            onSelect = { m -> cfg = cfg.copy(mascotOverride = m); replayKey++ }
                        )
                    }

                    if (cfg.logoPath == null) {
                        SectionLabel("Vector emblem shape (10 styles)")
                        ChoiceChips(
                            options = EmblemType.values().toList(),
                            selected = effectiveEmblem,
                            label = { it.label },
                            onSelect = { e -> cfg = cfg.copy(emblemOverride = e); replayKey++ }
                        )
                    }

                    SectionLabel("Or describe with AI Motion Prompt")
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        label = { Text("e.g. 'bird crest', 'paint splash', '3d spin', 'netflix', 'prime'") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    NeonButton(
                        if (promptText.isBlank()) "AI Auto-Match Style" else "Apply AI Motion Prompt",
                        onClick = {
                            scope.launch {
                                val bmp = withContext(Dispatchers.IO) {
                                    cfg.logoPath?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }
                                }
                                val designed = withContext(Dispatchers.Default) {
                                    LogoAiAnalyzer.designFromPrompt(promptText, cfg, bmp)
                                }
                                cfg = designed
                                val matchedSpec = TemplateCatalog.byId(designed.templateId)
                                aiStatus = "AI matched: ${matchedSpec.id}. ${matchedSpec.name}"
                                replayKey++
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.AutoAwesome,
                        outlined = true
                    )
                }
            }

            // STEP 03 — BACKGROUND & OUTPUT
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    StepHeader("03", "Background & Output", "Template backdrop, solid color, transparent or 4K MP4")
                    Spacer(Modifier.height(8.dp))
                    ChoiceChips(
                        options = BgMode.values().toList(),
                        selected = cfg.bgMode,
                        label = { it.label },
                        onSelect = { m -> cfg = cfg.copy(bgMode = m); replayKey++ }
                    )
                    if (cfg.bgMode == BgMode.SOLID) {
                        Spacer(Modifier.height(8.dp))
                        ColorRow(
                            "Solid background color",
                            cfg.bgSolidColor,
                            onChange = { c -> cfg = cfg.copy(bgSolidColor = c); replayKey++ }
                        )
                    }
                    SectionLabel("Aspect ratio")
                    ChoiceChips(
                        options = AspectRatio.values().toList(),
                        selected = cfg.aspect,
                        label = { "${it.label} ${it.sub}" },
                        onSelect = { ar -> cfg = cfg.copy(aspect = ar); replayKey++ }
                    )
                    SectionLabel("Duration")
                    val durations = listOf(5f, 6f, 8f, 10f)
                    ChoiceChips(
                        options = durations,
                        selected = durations.minByOrNull { kotlin.math.abs(it - cfg.durationSec) },
                        label = { "${it.toInt()}s" },
                        onSelect = { d -> cfg = cfg.copy(durationSec = d); replayKey++ }
                    )
                }
            }

            NeonButton(
                "Animate & Export MP4",
                onClick = {
                    val projName = "${cfg.brandName.trim().ifEmpty { "Logoipsum" }} ${selectedSpec.name}".trim()
                    app.createLogoAiProject(projName, cfg) { pid -> onExportProject(pid) }
                },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.Movie
            )
            NeonButton(
                "Open in Full Timeline Editor",
                onClick = {
                    val projName = "${cfg.brandName.trim().ifEmpty { "Logoipsum" }} ${selectedSpec.name}".trim()
                    app.createLogoAiProject(projName, cfg) { pid -> onOpenEditor(pid) }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                icon = Icons.Filled.Tune,
                outlined = true
            )
        }
    }
}

@Composable
private fun StepHeader(number: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(NH.Purple.copy(alpha = 0.28f))
                .border(1.dp, NH.Purple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("$number — $title", color = NH.Text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = NH.Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FeaturePill(text: String) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CheckCircle, null, tint = NH.Green, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = NH.Text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
