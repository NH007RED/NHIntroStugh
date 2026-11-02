package com.nh.introstudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.introstudio.domain.AspectRatio
import com.nh.introstudio.domain.FONT_OPTIONS
import com.nh.introstudio.domain.InOutStyle
import com.nh.introstudio.templates.TemplateCatalog
import com.nh.introstudio.ui.components.ChoiceChips
import com.nh.introstudio.ui.components.ColorRow
import com.nh.introstudio.ui.components.GlassCard
import com.nh.introstudio.ui.components.GlassIconButton
import com.nh.introstudio.ui.components.NeonButton
import com.nh.introstudio.ui.components.ScreenHeader
import com.nh.introstudio.ui.components.SectionLabel
import com.nh.introstudio.ui.components.SliderRow
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.viewmodel.AppViewModel

/**
 * Dedicated Streaming & Cinema Text Intro Studio (Templates 21..40):
 * Create Netflix, Amazon Prime Video, Disney+ Hotstar, Marvel, Apple TV+, HBO, IMAX, Dolby,
 * Universal, Columbia, JioCinema, Zee5, A24, Hulu, Paramount, Warner, Peacock, YouTube Originals,
 * Cyberpunk, and Molten Titanium style text intros in 1 click!
 */
@Composable
fun TextIntroScreen(
    app: AppViewModel,
    onExportProject: (Long) -> Unit,
    onOpenEditor: (Long) -> Unit
) {
    val textTemplates = TemplateCatalog.textIntroTemplates
    var cfg by remember { mutableStateOf(TemplateCatalog.defaultConfig(21)) }
    var replayKey by remember { mutableIntStateOf(0) }
    val selectedSpec = TemplateCatalog.byId(cfg.templateId)
    val effectiveInOut = cfg.inOutStyleOverride ?: selectedSpec.inOutStyle

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            "Streaming Text Intros",
            subtitle = "20 Netflix · Prime · Hotstar · Cinema Text Templates"
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
                TextIntroPill("Netflix Prism")
                TextIntroPill("Prime Smile Arc")
                TextIntroPill("Hotstar Shooting Star")
                TextIntroPill("Marvel · HBO · IMAX")
            }

            // Live Looping Preview
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.fillMaxWidth().heightIn(max = 225.dp), contentAlignment = Alignment.Center) {
                        LoopingPreview(cfg, replayKey, Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${selectedSpec.id - 20}. ${selectedSpec.name}",
                            color = NH.Green,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${cfg.durationSec.toInt()}s · ${cfg.aspect.label}", color = NH.Muted, fontSize = 12.sp)
                    }
                }
            }

            // STEP 01 — CHOOSE STREAMING / CINEMA TEXT TEMPLATE (21..40)
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("01 — Choose Streaming Text Style (20 Templates)", color = NH.Text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    ChoiceChips(
                        options = textTemplates,
                        selected = selectedSpec,
                        label = { "${it.id - 20}. ${it.name}" },
                        onSelect = { spec ->
                            val def = TemplateCatalog.defaultConfig(spec.id)
                            cfg = cfg.copy(
                                templateId = spec.id,
                                textStyleOverride = spec.textStyle,
                                inOutStyleOverride = spec.inOutStyle,
                                useCustomColors = false,
                                colorPrimary = spec.c1,
                                colorSecondary = spec.c2,
                                colorText = spec.textColor,
                                bgSolidColor = spec.bg1,
                                musicSource = def.musicSource,
                                musicName = def.musicName
                            )
                            replayKey++
                        }
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(selectedSpec.description, color = NH.Muted, fontSize = 12.sp)
                }
            }

            // STEP 02 — YOUR BRAND / CHANNEL TITLE
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("02 — Enter Your Brand / Channel Title", color = NH.Text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cfg.brandName,
                        onValueChange = { cfg = cfg.copy(brandName = it) },
                        label = { Text("Main Title (e.g. NETSTREAM, PRIME, HOTSTAR, NH STUDIO)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = cfg.tagline,
                        onValueChange = { cfg = cfg.copy(tagline = it) },
                        label = { Text("Top / Bottom Label (e.g. ORIGINALS, SPECIALS, STUDIOS)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SectionLabel("Font style")
                    ChoiceChips(
                        options = FONT_OPTIONS.indices.toList(),
                        selected = cfg.fontIndex,
                        label = { FONT_OPTIONS[it].label },
                        onSelect = { idx -> cfg = cfg.copy(fontIndex = idx); replayKey++ }
                    )
                    Spacer(Modifier.height(6.dp))
                    SliderRow(
                        "Title size",
                        cfg.fontScale,
                        0.6f..1.8f,
                        onChange = { v -> cfg = cfg.copy(fontScale = v) },
                        valueText = String.format("%.0f%%", cfg.fontScale * 100)
                    )
                }
            }

            // STEP 03 — DEEP IN/OUT CAMERA TRANSITION, COLORS & EXPORT
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("03 — Deep In/Out Transition & Colors", color = NH.Text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    SectionLabel("Cinematic In/Out camera physics")
                    ChoiceChips(
                        options = InOutStyle.values().toList(),
                        selected = effectiveInOut,
                        label = { it.label },
                        onSelect = { io -> cfg = cfg.copy(inOutStyleOverride = io); replayKey++ }
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Custom brand colors", color = NH.Text, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Switch(
                            checked = cfg.useCustomColors,
                            onCheckedChange = { v -> cfg = cfg.copy(useCustomColors = v); replayKey++ },
                            colors = SwitchDefaults.colors(checkedTrackColor = NH.Purple, checkedThumbColor = Color.White)
                        )
                    }
                    if (cfg.useCustomColors) {
                        ColorRow("Primary beam / ribbon color", cfg.colorPrimary, onChange = { c -> cfg = cfg.copy(colorPrimary = c); replayKey++ })
                        ColorRow("Secondary glow / arc color", cfg.colorSecondary, onChange = { c -> cfg = cfg.copy(colorSecondary = c); replayKey++ })
                        ColorRow("Title text color", cfg.colorText, onChange = { c -> cfg = cfg.copy(colorText = c); replayKey++ })
                    }
                    SectionLabel("Aspect ratio")
                    ChoiceChips(
                        options = AspectRatio.values().toList(),
                        selected = cfg.aspect,
                        label = { "${it.label} ${it.sub}" },
                        onSelect = { ar -> cfg = cfg.copy(aspect = ar); replayKey++ }
                    )
                    SectionLabel("Duration")
                    val durations = listOf(4f, 5f, 6f, 8f)
                    ChoiceChips(
                        options = durations,
                        selected = durations.minByOrNull { kotlin.math.abs(it - cfg.durationSec) },
                        label = { "${it.toInt()}s" },
                        onSelect = { d -> cfg = cfg.copy(durationSec = d); replayKey++ }
                    )
                }
            }

            NeonButton(
                "Render & Export Text Intro MP4",
                onClick = {
                    val projName = "${cfg.brandName.trim().ifEmpty { "Stream" }} ${selectedSpec.name}".trim()
                    app.createLogoAiProject(projName, cfg) { pid -> onExportProject(pid) }
                },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Filled.Movie
            )
            NeonButton(
                "Open in Full Timeline Editor",
                onClick = {
                    val projName = "${cfg.brandName.trim().ifEmpty { "Stream" }} ${selectedSpec.name}".trim()
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
private fun TextIntroPill(text: String) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CheckCircle, null, tint = NH.Red, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = NH.Text, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
