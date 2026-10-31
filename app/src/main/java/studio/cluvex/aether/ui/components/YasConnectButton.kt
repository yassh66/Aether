package studio.cluvex.aether.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.cluvex.aether.model.ConnectionState
import studio.cluvex.aether.model.isBusy
import studio.cluvex.aether.model.isConnected
import studio.cluvex.aether.ui.theme.YasCyan
import studio.cluvex.aether.ui.theme.YasMint
import studio.cluvex.aether.ui.theme.YasViolet

/**
 * Interactive Power Button for YAS VPN.
 *
 * Aligned over the reference hero button location with live feedback,
 * tactile press animations, rotating sweep for connecting states,
 * and luminous mint aura when connected.
 */
@Composable
fun YasConnectButton(
    state: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(120),
        label = "press_scale",
    )

    val isConnected = state.isConnected
    val isBusy = state.isBusy
    val isError = state is ConnectionState.Error

    val infiniteTransition = rememberInfiniteTransition(label = "power_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin_angle",
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    val targetAuraColor = when {
        isConnected -> YasMint
        isError -> Color(0xFFFF3366)
        isBusy -> YasCyan
        else -> Color.Transparent
    }
    val auraColor by animateColorAsState(targetAuraColor, tween(300), label = "aura_color")

    val buttonLabel = when {
        isConnected -> "CONNECTED"
        state is ConnectionState.Connecting -> "CONNECTING..."
        state is ConnectionState.Reconnecting -> "RECONNECTING..."
        isError -> "RETRY"
        else -> "TAP TO CONNECT"
    }

    val iconColor = when {
        isConnected -> YasMint
        isError -> Color(0xFFFF3366)
        isBusy -> YasCyan
        else -> Color.White
    }

    Box(
        modifier = modifier
            .size(140.dp)
            .scale(pressScale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .semantics {
                role = Role.Button
                contentDescription = buttonLabel
            }
            .testTag("yas_power_button"),
        contentAlignment = Alignment.Center,
    ) {
        // Active aura ring and state indicator canvas
        Canvas(modifier = Modifier.size(136.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 4.dp.toPx()

            // If connected or connecting, draw active glow rings
            if (isConnected) {
                // Neon mint active glow
                drawCircle(
                    color = YasMint.copy(alpha = 0.25f * pulseAlpha),
                    radius = radius + 6.dp.toPx(),
                )
                drawCircle(
                    color = YasMint,
                    radius = radius,
                    style = Stroke(width = 3.dp.toPx()),
                )
            } else if (isBusy) {
                // Rotating cyan sweep ring
                rotate(spinAngle, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color.Transparent,
                                YasCyan.copy(alpha = 0.2f),
                                YasCyan,
                                YasViolet,
                            ),
                            center = center,
                        ),
                        startAngle = 0f,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
            } else if (isError) {
                drawCircle(
                    color = Color(0xFFFF3366).copy(alpha = 0.7f),
                    radius = radius,
                    style = Stroke(width = 3.dp.toPx()),
                )
            }
        }

        // Active State Icon & Label Overlay (renders dynamically when state != Disconnected)
        if (isConnected || isBusy || isError) {
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                // Semi-translucent dark dome overlay to present active state
                Canvas(modifier = Modifier.size(118.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xE6081026),
                                Color(0xF0050814),
                            ),
                        ),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = buttonLabel,
                        color = iconColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                }
            }
        }
    }
}
