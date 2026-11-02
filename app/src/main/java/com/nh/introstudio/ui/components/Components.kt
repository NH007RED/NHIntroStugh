package com.nh.introstudio.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.introstudio.domain.AspectRatio
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.utils.Fmt

val NeonBrush = Brush.linearGradient(listOf(NH.Purple, NH.Red))

/** Full screen premium dark background: black to deep purple with soft neon glows. */
@Composable
fun GradientBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NH.DeepPurple, NH.Black, NH.Black)))
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(listOf(NH.Purple.copy(alpha = 0.30f), Color.Transparent), center = Offset(size.width * 0.15f, size.height * 0.05f), radius = size.width * 0.9f),
                    radius = size.width * 0.9f, center = Offset(size.width * 0.15f, size.height * 0.05f)
                )
                drawCircle(
                    Brush.radialGradient(listOf(NH.Red.copy(alpha = 0.18f), Color.Transparent), center = Offset(size.width * 0.95f, size.height * 0.95f), radius = size.width * 0.8f),
                    radius = size.width * 0.8f, center = Offset(size.width * 0.95f, size.height * 0.95f)
                )
            },
        content = content
    )
}

/** Liquid-glass card: translucent gradient fill with a bright rim. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    corner: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(corner)
    var m = modifier
        .clip(shape)
        .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.04f))))
        .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.05f))), shape)
    if (onClick != null) m = m.clickable(onClick = onClick)
    Box(m, content = content)
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    outlined: Boolean = false
) {
    val shape = RoundedCornerShape(18.dp)
    val base = modifier
        .height(52.dp)
        .clip(shape)
        .let {
            if (outlined) it.border(1.5.dp, NeonBrush, shape).background(Color.White.copy(alpha = 0.05f))
            else it.background(if (enabled) NeonBrush else Brush.linearGradient(listOf(Color(0xFF3A2D52), Color(0xFF3A2D52))))
        }
        .clickable(enabled = enabled, onClick = onClick)
    Row(base.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        if (icon != null) {
            Icon(icon, null, tint = if (enabled) Color.White else NH.Muted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = if (enabled) Color.White else NH.Muted, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun GlassIconButton(icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, size: Dp = 44.dp, tint: Color = Color.White) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (enabled) 0.10f else 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = if (enabled) tint else NH.Muted.copy(alpha = 0.5f), modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Row(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            GlassIconButton(Icons.Filled.ArrowBack, onBack)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = NH.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, color = NH.Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        actions()
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), color = NH.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp, modifier = modifier.padding(top = 14.dp, bottom = 6.dp))
}

@Composable
fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    onFinished: () -> Unit = {},
    valueText: String = String.format("%.2f", value),
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = NH.Text, fontSize = 14.sp)
            Text(valueText, color = NH.Muted, fontSize = 13.sp)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            onValueChangeFinished = onFinished,
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = NH.Purple, activeTrackColor = NH.Purple, inactiveTrackColor = Color.White.copy(alpha = 0.15f))
        )
    }
}

@Composable
fun <T> ChoiceChips(options: List<T>, selected: T?, label: (T) -> String, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (o in options) {
            FilterChip(
                selected = o == selected,
                onClick = { onSelect(o) },
                label = { Text(label(o), fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NH.Purple.copy(alpha = 0.45f),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White.copy(alpha = 0.06f),
                    labelColor = NH.Text
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true, selected = o == selected,
                    borderColor = Color.White.copy(alpha = 0.18f), selectedBorderColor = NH.Purple
                )
            )
        }
    }
}

private val SWATCHES = listOf(
    0xFFB026FF, 0xFFFF2D55, 0xFFFFC857, 0xFF00E5FF, 0xFF4DA3FF, 0xFF34D399,
    0xFFFF6B00, 0xFFFF5FD2, 0xFFFFFFFF, 0xFFC9D6E8, 0xFF7C4DFF, 0xFF00FF66
).map { it.toInt() }

@Composable
fun ColorRow(label: String, color: Int, onChange: (Int) -> Unit, onFinished: () -> Unit = {}, modifier: Modifier = Modifier) {
    var hex by remember(color) { mutableStateOf(Fmt.hex(color)) }
    Column(modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, color = NH.Text, fontSize = 14.sp)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            for (s in SWATCHES) {
                Box(
                    Modifier.size(34.dp).clip(CircleShape).background(Color(s))
                        .border(if (s == color) 3.dp else 1.dp, if (s == color) Color.White else Color.White.copy(alpha = 0.25f), CircleShape)
                        .clickable { onChange(s); onFinished() }
                )
            }
            OutlinedTextField(
                value = hex,
                onValueChange = { v ->
                    hex = v
                    Fmt.parseHex(v)?.let { onChange(it); onFinished() }
                },
                singleLine = true,
                modifier = Modifier.width(120.dp),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** Renders an already drawn frame with the correct aspect ratio. */
@Composable
fun FramePreview(image: ImageBitmap?, aspect: AspectRatio, modifier: Modifier = Modifier, corner: Dp = 20.dp) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier
            .aspectRatio(aspect.rw.toFloat() / aspect.rh)
            .clip(shape)
            .background(Color.Black)
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            Image(image, null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds, filterQuality = FilterQuality.Medium)
        } else {
            Text("Rendering preview…", color = NH.Muted, fontSize = 13.sp)
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier, action: @Composable () -> Unit = {}) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(title, color = NH.Text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(message, color = NH.Muted, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        action()
    }
}

@Composable
fun TextInputDialog(title: String, initial: String, confirmLabel: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onConfirm(text); onDismiss() }) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = NH.Surface
    )
}

@Composable
fun ConfirmDialog(title: String, message: String, confirmLabel: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = { onConfirm(); onDismiss() }) { Text(confirmLabel, color = NH.Red) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = NH.Surface
    )
}
