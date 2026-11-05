package com.tsutsen.platformplayer.activities

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tsutsen.platformplayer.core.data.repository.ChannelRepository
import com.tsutsen.platformplayer.core.model.ContentItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Large subtitles for the second screen, shown at the bottom. */
@Composable
internal fun BoxScope.RearSubtitles(text: String) {
    if (text.isBlank()) return
    Text(
        text = text,
        color = Color.White,
        fontSize = 30.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

/** Channel name, subscribers and upload age for a few seconds when a video starts. */
@Composable
internal fun BoxScope.RearChannelInfo(
    video: ContentItem?,
    channelRepository: ChannelRepository,
) {
    var visible by remember { mutableStateOf(false) }
    var line by remember { mutableStateOf("") }
    val url = video?.url
    LaunchedEffect(url) {
        if (video == null) {
            visible = false
            return@LaunchedEffect
        }
        val name = video.author?.name.orEmpty()
        var subs = video.author?.subscriberCount
        val channelUrl = video.author?.url
        if (subs == null && !channelUrl.isNullOrEmpty()) {
            subs = runCatching { withContext(Dispatchers.IO) { channelRepository.getChannel(channelUrl).subscribers } }.getOrNull()
        }
        val parts = mutableListOf<String>()
        if (name.isNotBlank()) parts += name
        if (subs != null && subs > 0) parts += "${compact(subs)} subscribers"
        video.publishedAt?.takeIf { it > 0 }?.let { t ->
            val ms = if (t < 100_000_000_000L) t * 1000 else t
            parts += "uploaded " + DateUtils.getRelativeTimeSpanString(ms, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        }
        if (parts.isEmpty()) return@LaunchedEffect
        line = parts.joinToString(" · ")
        visible = true
        delay(6_000)
        visible = false
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(line, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun compact(n: Long): String =
    when {
        n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
        n >= 1_000 -> "%.1fK".format(n / 1_000.0)
        else -> n.toString()
    }
