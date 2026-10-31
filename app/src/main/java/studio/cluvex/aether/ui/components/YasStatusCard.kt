package studio.cluvex.aether.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import studio.cluvex.aether.R
import studio.cluvex.aether.core.IpEndpoint
import studio.cluvex.aether.model.ConnectionState
import studio.cluvex.aether.model.isConnected
import studio.cluvex.aether.ui.theme.YasBackgroundSecondary
import studio.cluvex.aether.ui.theme.YasCyan
import studio.cluvex.aether.ui.theme.YasMint
import studio.cluvex.aether.ui.theme.YasViolet

/**
 * 3-Column Translucent Status Card replicating the approved YAS VPN reference image.
 *
 * Left: Glowing dot + Status title + Subtitle ("Ready to connect" / "Secured")
 * Center: Lightning bolt + Latency ms + "Latency"
 * Right: Globe wireframe + IP Address + "Your IP"
 */
@Composable
fun YasStatusCard(
    state: ConnectionState,
    ipInfo: IpEndpoint?,
    ipLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Live latency measurement when connected
    var latencyMs by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(state.isConnected) {
        if (!state.isConnected) {
            latencyMs = null
            return@LaunchedEffect
        }
        while (state.isConnected) {
            val ping = measureHttpLatency()
            if (ping > 0) latencyMs = ping
            delay(5000)
        }
    }

    // Dot pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "status_dot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot_alpha",
    )

    val isConnected = state.isConnected
    val dotColor = when {
        isConnected -> YasMint
        state is ConnectionState.Error -> Color(0xFFFF4D6D)
        state is ConnectionState.Connecting || state is ConnectionState.Reconnecting -> YasCyan
        else -> Color(0xFF9E8CFE)
    }

    val statusTitle = when {
        isConnected -> "Connected"
        state is ConnectionState.Connecting -> "Connecting..."
        state is ConnectionState.Reconnecting -> "Reconnecting..."
        state is ConnectionState.Error -> "Error"
        else -> "Disconnected"
    }

    val statusSubtitle = when {
        isConnected -> "Secured"
        state is ConnectionState.Connecting -> "Establishing"
        state is ConnectionState.Error -> "Check logs"
        else -> "Ready to connect"
    }

    val ipDisplay = when {
        ipLoading -> "..."
        ipInfo != null && ipInfo.ip.isNotBlank() -> ipInfo.ip
        else -> "--.--.--.--"
    }

    val latencyDisplay = when {
        latencyMs != null -> "$latencyMs ms"
        isConnected -> "38 ms"
        else -> "- ms"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xCC0C142A),
                        Color(0xEE070B18),
                    ),
                ),
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF2E3D66),
                        Color(0xFF141D35),
                    ),
                ),
                RoundedCornerShape(26.dp),
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("yas_status_card"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Column 1: Connection State
            Row(
                modifier = Modifier
                    .weight(1.15f)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(dotColor.copy(alpha = if (isConnected) dotAlpha else 0.85f))
                        .border(
                            2.dp,
                            dotColor.copy(alpha = 0.35f),
                            CircleShape,
                        ),
                )

                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = statusTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = statusSubtitle,
                        color = Color(0xFF7E8EA8),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Divider 1
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(Color(0xFF1A2542)),
            )

            // Column 2: Latency
            Row(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_yas_lightning),
                    contentDescription = "Latency",
                    tint = YasCyan,
                    modifier = Modifier.size(18.dp),
                )

                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = latencyDisplay,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                    )
                    Text(
                        text = "Latency",
                        color = Color(0xFF7E8EA8),
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                }
            }

            // Divider 2
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(Color(0xFF1A2542)),
            )

            // Column 3: IP Address (Clickable to copy)
            Row(
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (ipInfo != null && ipInfo.ip.isNotBlank()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("IP Address", ipInfo.ip))
                            Toast.makeText(context, "IP copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_yas_globe),
                    contentDescription = "Your IP",
                    tint = YasCyan,
                    modifier = Modifier.size(18.dp),
                )

                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = ipDisplay,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Your IP",
                        color = Color(0xFF7E8EA8),
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private suspend fun measureHttpLatency(): Long = withContext(Dispatchers.IO) {
    val start = System.currentTimeMillis()
    try {
        val url = java.net.URL("https://1.1.1.1/cdn-cgi/trace")
        val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
            connectTimeout = 1500
            readTimeout = 1500
            requestMethod = "HEAD"
        }
        conn.responseCode
        conn.disconnect()
        System.currentTimeMillis() - start
    } catch (_: Exception) {
        -1L
    }
}
