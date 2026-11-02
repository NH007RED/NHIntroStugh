package com.nh.introstudio.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.introstudio.domain.AspectRatio
import com.nh.introstudio.domain.BUILTIN_MUSIC
import com.nh.introstudio.domain.BgMode
import com.nh.introstudio.domain.BrandConfig
import com.nh.introstudio.domain.EmblemType
import com.nh.introstudio.domain.FONT_OPTIONS
import com.nh.introstudio.domain.InOutStyle
import com.nh.introstudio.domain.LayoutMode
import com.nh.introstudio.domain.LogoStyle
import com.nh.introstudio.domain.MascotType
import com.nh.introstudio.domain.TextStyle
import com.nh.introstudio.templates.TemplateCatalog
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.utils.Fmt
import com.nh.introstudio.viewmodel.EditorViewModel

@Composable
private fun EditField(label: String, value: String, tag: String, vm: EditorViewModel, set: (BrandConfig, String) -> BrandConfig) {
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = { v -> text = v; vm.edit(tag) { set(it, v) } },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = NH.Text, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedTrackColor = NH.Purple, checkedThumbColor = Color.White))
    }
}

@Composable
fun TextPanel(vm: EditorViewModel, cfg: BrandConfig) {
    val spec = TemplateCatalog.byId(cfg.templateId)
    val effectiveTextStyle = cfg.textStyleOverride ?: spec.textStyle
    Column(Modifier.fillMaxWidth()) {
        EditField("Line 1 — Brand / Channel Name", cfg.brandName, "brand", vm) { c, v -> c.copy(brandName = v) }
        EditField("Line 2 — Tagline (e.g. University / ORIGINALS)", cfg.tagline, "tagline", vm) { c, v -> c.copy(tagline = v) }
        EditField("Line 3 — Optional Subtitle", cfg.subtitle, "subtitle", vm) { c, v -> c.copy(subtitle = v) }
        SectionLabel("Layout mode")
        ChoiceChips(LayoutMode.values().toList(), cfg.layoutMode, { it.label }, { m -> vm.edit("layout") { it.copy(layoutMode = m) }; vm.commit() })
        SectionLabel("Font")
        ChoiceChips(FONT_OPTIONS.indices.toList(), cfg.fontIndex, { FONT_OPTIONS[it].label }, { i -> vm.edit("font") { it.copy(fontIndex = i) } })
        SectionLabel("Text animation engine (31 styles)")
        ChoiceChips(TextStyle.values().toList(), effectiveTextStyle, { it.label }, { st -> vm.edit("textStyle") { it.copy(textStyleOverride = st) }; vm.commit() })
        Spacer(Modifier.height(8.dp))
        SliderRow("Font size", cfg.fontScale, 0.5f..2f, { v -> vm.edit("fontScale") { it.copy(fontScale = v) } }, vm::commit, String.format("%.0f%%", cfg.fontScale * 100))
        SliderRow("Position X", cfg.textX, 0.2f..0.8f, { v -> vm.edit("textX") { it.copy(textX = v) } }, vm::commit, String.format("%.0f%%", cfg.textX * 100))
        SliderRow("Position Y", cfg.textY, 0.2f..0.8f, { v -> vm.edit("textY") { it.copy(textY = v) } }, vm::commit, String.format("%.0f%%", cfg.textY * 100))
    }
}

@Composable
fun LogoPanel(vm: EditorViewModel, cfg: BrandConfig) {
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) vm.importLogo(uri) }
    val spec = TemplateCatalog.byId(cfg.templateId)
    val effectiveMascot = cfg.mascotOverride ?: spec.mascot
    val effectiveEmblem = cfg.emblemOverride ?: spec.emblem
    val effectiveLogoStyle = cfg.logoStyleOverride ?: spec.logoStyle
    val effectiveInOut = cfg.inOutStyleOverride ?: spec.inOutStyle
    Column(Modifier.fillMaxWidth()) {
        SwitchRow("Show logo / emblem reveal", cfg.showLogo) { v -> vm.edit("showLogo") { it.copy(showLogo = v) }; vm.commit() }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NeonButton(if (cfg.logoPath == null) "Import custom logo" else "Replace logo", { pick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, Modifier.weight(1f), Icons.Filled.Image)
            if (cfg.logoPath != null) NeonButton("Use Vector Emblem", { vm.edit("logo") { it.copy(logoPath = null) }; vm.commit() }, Modifier.weight(0.7f), Icons.Filled.Delete, outlined = true)
        }
        Text(
            if (cfg.logoPath == null) "Using vector emblem (${effectiveEmblem.label}) with unified light-wrap."
            else "Your custom logo is fused with ${effectiveLogoStyle.label} & ${effectiveInOut.label}.",
            color = NH.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)
        )
        if (cfg.logoPath != null) {
            SwitchRow("Auto-remove solid logo background", cfg.removeLogoBg) { v ->
                vm.edit("remBg") { it.copy(removeLogoBg = v) }; vm.commit()
            }
            NeonButton(
                "Extract brand colors from logo",
                { vm.extractColorsFromLogo() },
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                Icons.Filled.Palette,
                outlined = true
            )
        }
        SectionLabel("Deep In/Out camera & dissolve transition")
        ChoiceChips(
            InOutStyle.values().toList(),
            effectiveInOut,
            { it.label },
            { io -> vm.edit("inOut") { it.copy(inOutStyleOverride = io) }; vm.commit() }
        )
        SectionLabel("Logo entrance choreography (20 types)")
        ChoiceChips(
            LogoStyle.values().toList(),
            effectiveLogoStyle,
            { it.label },
            { st -> vm.edit("logoStyle") { it.copy(logoStyleOverride = st) }; vm.commit() }
        )
        if (effectiveLogoStyle == LogoStyle.MASCOT_VORTEX_MORPH) {
            SectionLabel("Flying mascot (for Type 1 Bird/Mascot Morph)")
            ChoiceChips(
                MascotType.values().toList(),
                effectiveMascot,
                { it.label },
                { m -> vm.edit("mascot") { it.copy(mascotOverride = m) }; vm.commit() }
            )
        }
        if (cfg.logoPath == null) {
            SectionLabel("Vector emblem shape (10 styles)")
            ChoiceChips(
                EmblemType.values().toList(),
                effectiveEmblem,
                { it.label },
                { e -> vm.edit("emblem") { it.copy(emblemOverride = e) }; vm.commit() }
            )
        }
        Spacer(Modifier.height(6.dp))
        SliderRow("Logo scale", cfg.logoScale, 0.4f..2.2f, { v -> vm.edit("logoScale") { it.copy(logoScale = v) } }, vm::commit, String.format("%.0f%%", cfg.logoScale * 100))
        SliderRow("Logo rotation", cfg.logoRotation, -180f..180f, { v -> vm.edit("logoRot") { it.copy(logoRotation = v) } }, vm::commit, String.format("%.0f°", cfg.logoRotation))
    }
}

@Composable
fun BackgroundPanel(vm: EditorViewModel, cfg: BrandConfig) {
    val pickImg = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) vm.importBackgroundImage(uri) }
    val pickVid = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) vm.importBackgroundVideo(uri) }
    Column(Modifier.fillMaxWidth()) {
        SectionLabel("Background mode (03 — Background)")
        ChoiceChips(
            BgMode.values().toList(),
            cfg.bgMode,
            { it.label },
            { m -> vm.edit("bgMode") { it.copy(bgMode = m) }; vm.commit() }
        )
        Text(cfg.bgMode.desc, color = NH.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        if (cfg.bgMode == BgMode.SOLID) {
            Spacer(Modifier.height(6.dp))
            ColorRow("Solid background color", cfg.bgSolidColor, { c -> vm.edit("bgSolid") { it.copy(bgSolidColor = c) } }, vm::commit)
        }
        Spacer(Modifier.height(10.dp))
        SectionLabel("Custom background media")
        val current = when {
            cfg.bgVideoPath != null -> "Background video is active."
            cfg.bgImagePath != null -> "Background image is active."
            else -> "Using ${cfg.bgMode.label}."
        }
        Text(current, color = NH.Muted, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NeonButton("Image", { pickImg.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, Modifier.weight(1f), Icons.Filled.Image)
            NeonButton("Video", { pickVid.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }, Modifier.weight(1f), Icons.Filled.Movie)
        }
        if (cfg.bgImagePath != null || cfg.bgVideoPath != null) {
            Spacer(Modifier.height(10.dp))
            NeonButton("Remove background media", { vm.edit("bg") { it.copy(bgImagePath = null, bgVideoPath = null) }; vm.commit() }, Modifier.fillMaxWidth(), Icons.Filled.Delete, outlined = true)
        }
    }
}

@Composable
fun ColorsPanel(vm: EditorViewModel, cfg: BrandConfig) {
    Column(Modifier.fillMaxWidth()) {
        SwitchRow("Use custom colors", cfg.useCustomColors) { v -> vm.edit("useColors") { it.copy(useCustomColors = v) }; vm.commit() }
        if (cfg.useCustomColors) {
            ColorRow("Primary accent color", cfg.colorPrimary, { c -> vm.edit("c1") { it.copy(colorPrimary = c) } }, vm::commit)
            ColorRow("Secondary effect color", cfg.colorSecondary, { c -> vm.edit("c2") { it.copy(colorSecondary = c) } }, vm::commit)
            ColorRow("Typography color", cfg.colorText, { c -> vm.edit("c3") { it.copy(colorText = c) } }, vm::commit)
        } else {
            Text("The template's studio palette is used.", color = NH.Muted, fontSize = 13.sp)
        }
    }
}

@Composable
fun AnimationPanel(vm: EditorViewModel, cfg: BrandConfig) {
    val spec = TemplateCatalog.byId(cfg.templateId)
    val effectiveInOut = cfg.inOutStyleOverride ?: spec.inOutStyle
    var prompt by remember(cfg.motionPrompt) { mutableStateOf(cfg.motionPrompt) }
    Column(Modifier.fillMaxWidth()) {
        SectionLabel("AI Motion Prompt (Logo AI & Streaming Text)")
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("e.g. 'netflix prism', 'prime smile', 'hotstar', 'paint splash'") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        NeonButton(
            if (prompt.isBlank()) "AI Auto-Match from Logo" else "Apply AI Motion Prompt",
            { vm.applyAiMotionPrompt(prompt) },
            Modifier.fillMaxWidth(),
            Icons.Filled.AutoAwesome,
            outlined = true
        )
        SectionLabel("All 40 Templates (1–20 Logo AI · 21–40 Text Intros)")
        ChoiceChips(
            TemplateCatalog.all,
            spec,
            { "${it.id}. ${it.name}" },
            { t ->
                val def = TemplateCatalog.defaultConfig(t.id)
                vm.edit("template") {
                    it.copy(
                        templateId = t.id,
                        showLogo = !t.isTextIntro,
                        layoutMode = if (t.isTextIntro) LayoutMode.TEXT_HERO else LayoutMode.AUTO,
                        durationSec = def.durationSec,
                        logoStyleOverride = null,
                        textStyleOverride = null,
                        inOutStyleOverride = null,
                        mascotOverride = null,
                        emblemOverride = null
                    )
                }
                vm.commit()
            }
        )
        SectionLabel("Deep In/Out camera & dissolve transition")
        ChoiceChips(InOutStyle.values().toList(), effectiveInOut, { it.label }, { io -> vm.edit("inOut") { it.copy(inOutStyleOverride = io) }; vm.commit() })
        SectionLabel("Layout mode")
        ChoiceChips(LayoutMode.values().toList(), cfg.layoutMode, { it.label }, { m -> vm.edit("layout") { it.copy(layoutMode = m) }; vm.commit() })
        SectionLabel("Aspect ratio")
        ChoiceChips(AspectRatio.values().toList(), cfg.aspect, { "${it.label} ${it.sub}" }, { a -> vm.setAspect(a); vm.commit() })
        Spacer(Modifier.height(6.dp))
        SliderRow("Animation speed", cfg.speed, 0.5f..2f, { v -> vm.edit("speed") { it.copy(speed = v) } }, vm::commit, String.format("%.2fx", cfg.speed))
        SliderRow("Intro duration", cfg.durationSec, 4f..15f, { v -> vm.edit("duration") { it.copy(durationSec = Math.round(v * 2f) / 2f) } }, vm::commit, String.format("%.1f s", cfg.durationSec))
        SwitchRow("Show corner watermark", cfg.watermarkEnabled) { v -> vm.edit("wm") { it.copy(watermarkEnabled = v) }; vm.commit() }
    }
}

@Composable
fun AudioPanel(vm: EditorViewModel, cfg: BrandConfig) {
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) vm.importAudio(uri) }
    val musicDur by vm.musicDuration.collectAsState()
    Column(Modifier.fillMaxWidth()) {
        SectionLabel("Background music")
        val selectedId = if (cfg.musicSource.startsWith("builtin:")) cfg.musicSource.removePrefix("builtin:") else null
        ChoiceChips(BUILTIN_MUSIC, BUILTIN_MUSIC.firstOrNull { it.id == selectedId }, { it.title }, { t -> vm.setBuiltinMusic(t.id); vm.commit() })
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NeonButton("Import audio", { pick.launch(arrayOf("audio/*")) }, Modifier.weight(1f), Icons.Filled.MusicNote)
            NeonButton("No music", { vm.clearMusic(); vm.commit() }, Modifier.weight(0.8f), Icons.Filled.Delete, outlined = true)
        }
        Text("Now: ${cfg.musicName}", color = NH.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
        SliderRow("Music volume", cfg.musicVolume, 0f..1f, { v -> vm.edit("mvol") { it.copy(musicVolume = v) } }, vm::commit, String.format("%.0f%%", cfg.musicVolume * 100))
        if (cfg.musicSource != "none" && musicDur > 0.5f) {
            val start = cfg.musicStartSec.coerceIn(0f, musicDur)
            val end = (if (cfg.musicEndSec > start) cfg.musicEndSec else musicDur).coerceIn(start, musicDur)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Trim music", color = NH.Text, fontSize = 14.sp)
                Text("${Fmt.time(start)} – ${Fmt.time(end)} of ${Fmt.time(musicDur)}", color = NH.Muted, fontSize = 13.sp)
            }
            RangeSlider(
                value = start..end,
                onValueChange = { r ->
                    vm.edit("trim") { it.copy(musicStartSec = r.start, musicEndSec = if (r.endInclusive >= musicDur - 0.05f) 0f else r.endInclusive) }
                },
                onValueChangeFinished = vm::commit,
                valueRange = 0f..musicDur,
                colors = SliderDefaults.colors(thumbColor = NH.Purple, activeTrackColor = NH.Purple, inactiveTrackColor = Color.White.copy(alpha = 0.15f))
            )
        }
        SectionLabel("Synchronized sound effects")
        SwitchRow("Built-in motion sound effects", cfg.sfxEnabled) { v -> vm.edit("sfx") { it.copy(sfxEnabled = v) }; vm.commit() }
        if (cfg.sfxEnabled) SliderRow("Effects volume", cfg.sfxVolume, 0f..1f, { v -> vm.edit("svol") { it.copy(sfxVolume = v) } }, vm::commit, String.format("%.0f%%", cfg.sfxVolume * 100))
    }
}
