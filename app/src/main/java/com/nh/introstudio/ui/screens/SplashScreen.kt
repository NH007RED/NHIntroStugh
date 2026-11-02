package com.nh.introstudio.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.introstudio.ui.components.GradientBackground
import com.nh.introstudio.ui.components.NeonBrush
import com.nh.introstudio.ui.theme.NH
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        a.animateTo(1f, tween(1100))
        delay(700)
        onDone()
    }
    GradientBackground {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(a.value).scale(0.8f + 0.2f * a.value)) {
                Canvas(Modifier.size(110.dp)) {
                    val s = size.width
                    drawRoundRect(NeonBrush, topLeft = Offset(s * 0.08f, s * 0.14f), size = Size(s * 0.84f, s * 0.72f), cornerRadius = CornerRadius(s * 0.16f), style = Stroke(s * 0.06f))
                    val p = Path().apply {
                        moveTo(s * 0.42f, s * 0.34f); lineTo(s * 0.42f, s * 0.66f); lineTo(s * 0.70f, s * 0.5f); close()
                    }
                    drawPath(p, NeonBrush)
                }
                Spacer(Modifier.height(20.dp))
                Text("NH INTRO STUDIO", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                Spacer(Modifier.height(6.dp))
                Text("Cinematic intros, rendered on your phone", color = NH.Muted, fontSize = 13.sp)
            }
        }
    }
}
