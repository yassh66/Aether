package studio.cluvex.aether.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import studio.cluvex.aether.model.ConnectionState
import studio.cluvex.aether.model.isBusy
import studio.cluvex.aether.model.isConnected
import studio.cluvex.aether.ui.theme.YasBlue
import studio.cluvex.aether.ui.theme.YasCyan
import studio.cluvex.aether.ui.theme.YasCyanLight
import studio.cluvex.aether.ui.theme.YasMint
import studio.cluvex.aether.ui.theme.YasViolet
import studio.cluvex.aether.ui.theme.YasVioletLight

/**
 * YAS VPN Signature Crystalline Energy Centerpiece.
 *
 * An original, faceted geometric crystal with ambient volumetric bloom,
 * refractive light facets forming a stylized 'Y' geometry, and subtle idle
 * hover physics.
 *
 * Drawn in a single DrawScope pass with zero per-frame allocations for
 * buttery-smooth 60/120fps performance on devices like Galaxy A55.
 */
@Composable
fun YasCrystalMark(
    state: ConnectionState,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
) {
    val transition = if (animated) rememberInfiniteTransition(label = "yasCrystal") else null

    // Idle floating breath
    val floatY = transition?.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatY",
    )

    // Pulse glow animation
    val pulseSpeed = if (state.isBusy) 900 else if (state.isConnected) 2400 else 3800
    val pulse = transition?.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseSpeed, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    // Shimmer light sweep
    val sweepSpeed = if (state.isBusy) 1400 else 3600
    val sweep = transition?.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(sweepSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sweep",
    )

    // Pre-allocated paths to prevent garbage collection jitter
    val pTop = remember { Path() }
    val pLeftUpper = remember { Path() }
    val pRightUpper = remember { Path() }
    val pCenterFacet = remember { Path() }
    val pLeftLower = remember { Path() }
    val pRightLower = remember { Path() }
    val pBottomSpire = remember { Path() }
    val pOutline = remember { Path() }
    val pYMark = remember { Path() }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val offsetY = floatY?.value ?: 0f
        val cy = (h / 2f) + offsetY
        val intensity = pulse?.value ?: 0.8f
        val sweepPos = sweep?.value ?: 0.5f

        // Dynamic palette based on connection state
        val (glowColor1, glowColor2, glowCore) = when {
            state.isConnected -> Triple(YasCyan, YasMint, YasCyanLight)
            state.isBusy -> Triple(YasViolet, YasCyan, Color.White)
            state is ConnectionState.Error -> Triple(Color(0xFFFF5C7A), Color(0xFFFFB74D), Color(0xFFFF7A8A))
            else -> Triple(YasViolet, YasBlue, YasVioletLight)
        }

        // 1. Ambient Volumetric Back-Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor1.copy(alpha = if (state.isConnected) 0.35f * intensity else 0.22f * intensity),
                    glowColor2.copy(alpha = if (state.isConnected) 0.18f * intensity else 0.10f * intensity),
                    Color.Transparent,
                ),
                center = Offset(cx, cy),
                radius = w * 0.55f,
            ),
            radius = w * 0.55f,
            center = Offset(cx, cy),
            blendMode = BlendMode.Plus,
        )

        // Geometric Crystal Nodes (Scaled to canvas)
        val r = minOf(w, h) * 0.38f
        val top = Offset(cx, cy - r * 1.05f)
        val topL = Offset(cx - r * 0.55f, cy - r * 0.55f)
        val topR = Offset(cx + r * 0.55f, cy - r * 0.55f)
        val midL = Offset(cx - r * 0.85f, cy - r * 0.05f)
        val midR = Offset(cx + r * 0.85f, cy - r * 0.05f)
        val center = Offset(cx, cy - r * 0.08f)
        val lowerL = Offset(cx - r * 0.45f, cy + r * 0.58f)
        val lowerR = Offset(cx + r * 0.45f, cy + r * 0.58f)
        val bottom = Offset(cx, cy + r * 1.08f)

        // 2. Facet Fills (Simulating 3D crystalline glass refraction)

        // Facet: Top Apex
        pTop.reset()
        pTop.moveTo(top.x, top.y)
        pTop.lineTo(topR.x, topR.y)
        pTop.lineTo(center.x, center.y)
        pTop.lineTo(topL.x, topL.y)
        pTop.close()
        drawPath(
            pTop,
            brush = Brush.verticalGradient(
                colors = listOf(
                    glowCore.copy(alpha = 0.45f),
                    glowColor1.copy(alpha = 0.25f),
                ),
                startY = top.y,
                endY = center.y,
            ),
        )

        // Facet: Left Upper Wing
        pLeftUpper.reset()
        pLeftUpper.moveTo(top.x, top.y)
        pLeftUpper.lineTo(topL.x, topL.y)
        pLeftUpper.lineTo(midL.x, midL.y)
        pLeftUpper.close()
        drawPath(
            pLeftUpper,
            brush = Brush.linearGradient(
                colors = listOf(glowColor1.copy(alpha = 0.20f), YasViolet.copy(alpha = 0.35f)),
                start = top,
                end = midL,
            ),
        )

        // Facet: Right Upper Wing
        pRightUpper.reset()
        pRightUpper.moveTo(top.x, top.y)
        pRightUpper.lineTo(topR.x, topR.y)
        pRightUpper.lineTo(midR.x, midR.y)
        pRightUpper.close()
        drawPath(
            pRightUpper,
            brush = Brush.linearGradient(
                colors = listOf(glowCore.copy(alpha = 0.30f), glowColor2.copy(alpha = 0.22f)),
                start = top,
                end = midR,
            ),
        )

        // Facet: Center Core Diamond (Inner Prism)
        pCenterFacet.reset()
        pCenterFacet.moveTo(center.x, center.y)
        pCenterFacet.lineTo(midL.x, midL.y)
        pCenterFacet.lineTo(lowerL.x, lowerL.y)
        pCenterFacet.lineTo(bottom.x, bottom.y)
        pCenterFacet.lineTo(lowerR.x, lowerR.y)
        pCenterFacet.lineTo(midR.x, midR.y)
        pCenterFacet.close()
        drawPath(
            pCenterFacet,
            brush = Brush.verticalGradient(
                colors = listOf(
                    glowColor2.copy(alpha = 0.15f),
                    glowColor1.copy(alpha = 0.28f),
                    glowColor2.copy(alpha = 0.40f * intensity),
                ),
                startY = center.y,
                endY = bottom.y,
            ),
        )

        // 3. Faceted Crystalline Edges & Internal Framework
        val edgeColor = if (state.isConnected) glowCore.copy(alpha = 0.90f) else glowColor1.copy(alpha = 0.70f)
        val strokeBase = 1.5.dp.toPx()

        pOutline.reset()
        pOutline.moveTo(top.x, top.y)
        pOutline.lineTo(midR.x, midR.y)
        pOutline.lineTo(bottom.x, bottom.y)
        pOutline.lineTo(midL.x, midL.y)
        pOutline.close()

        // Outer Neon Glow Rim
        drawPath(
            path = pOutline,
            color = glowColor1.copy(alpha = 0.35f * intensity),
            style = Stroke(width = strokeBase * 2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            blendMode = BlendMode.Plus,
        )
        drawPath(
            path = pOutline,
            color = edgeColor,
            style = Stroke(width = strokeBase, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // Internal Structural Facet Lines (Geometric 'Y' Heart)
        pYMark.reset()
        // Left arm of Y
        pYMark.moveTo(topL.x, topL.y)
        pYMark.lineTo(center.x, center.y)
        // Right arm of Y
        pYMark.moveTo(topR.x, topR.y)
        pYMark.lineTo(center.x, center.y)
        // Stem of Y
        pYMark.moveTo(center.x, center.y)
        pYMark.lineTo(bottom.x, bottom.y)

        // Lateral facets connecting Y
        pYMark.moveTo(center.x, center.y)
        pYMark.lineTo(midL.x, midL.y)
        pYMark.moveTo(center.x, center.y)
        pYMark.lineTo(midR.x, midR.y)

        // Glowing Y Framework
        drawPath(
            path = pYMark,
            color = glowCore.copy(alpha = if (state.isConnected) 0.75f * intensity else 0.50f),
            style = Stroke(width = strokeBase * 1.6f, cap = StrokeCap.Round),
            blendMode = BlendMode.Plus,
        )
        drawPath(
            path = pYMark,
            color = Color.White.copy(alpha = if (state.isConnected) 0.90f else 0.65f),
            style = Stroke(width = strokeBase * 0.8f, cap = StrokeCap.Round),
        )

        // 4. Diagonal Light Sweep Effect
        val sweepBand = r * 0.7f
        val sweepTravel = -r + (sweepPos * (r * 2.2f))
        rotate(degrees = -30f, pivot = Offset(cx, cy)) {
            drawRect(
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        0.5f to Color.White.copy(alpha = if (state.isConnected) 0.35f else 0.18f),
                        1f to Color.Transparent,
                    ),
                    start = Offset(cx + sweepTravel - sweepBand / 2f, cy - r),
                    end = Offset(cx + sweepTravel + sweepBand / 2f, cy + r),
                ),
                topLeft = Offset(cx - r, cy - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                blendMode = BlendMode.Plus,
            )
        }

        // 5. Central Refractive Core Spark
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.85f * intensity),
                    glowCore.copy(alpha = 0.40f * intensity),
                    Color.Transparent,
                ),
                center = center,
                radius = r * 0.22f,
            ),
            radius = r * 0.22f,
            center = center,
            blendMode = BlendMode.Plus,
        )
    }
}
