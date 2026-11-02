package com.nh.introstudio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.introstudio.domain.TemplateSpec
import com.nh.introstudio.ui.theme.NH
import com.nh.introstudio.viewmodel.AppViewModel

@Composable
fun TemplateCard(
    spec: TemplateSpec,
    app: AppViewModel,
    favorite: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val thumb by produceState<ImageBitmap?>(null, spec.id) { value = app.templateThumb(spec.id) }
    GlassCard(modifier, onClick) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black)) {
                thumb?.let { Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                GlassIconButton(
                    if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, onFavorite,
                    Modifier.align(Alignment.TopEnd).padding(6.dp), size = 34.dp,
                    tint = if (favorite) NH.Red else Color.White
                )
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text("${spec.id}. ${spec.name}", color = NH.Text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(spec.category, color = NH.Muted, fontSize = 12.sp)
            }
        }
    }
}
