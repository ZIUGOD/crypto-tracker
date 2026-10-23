package com.example.crypto_tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import kotlin.random.Random
import com.example.crypto_tracker.config.AppConfig
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/**
 * Glassmorphism design system.
 *
 * - [GlassBackground] paints a textured silk-and-grain scene and registers it as the blur source.
 * - [glass] / [GlassCard] frost whatever is behind them with a real backdrop blur (Android 12+),
 *   add a white tint, a diagonal specular sheen and a luminous gradient edge.
 * - Below Android 12 the blur is unavailable, so a denser tint is used to keep text legible.
 */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

/** Single source of truth for glass design tokens (radius, border, blur, opacity, spacing). */
object GlassDefaults {
    // Corner radii
    val RadiusCard = 24.dp
    val RadiusInner = 14.dp
    val RadiusPill = 36.dp
    val CardShape: Shape = RoundedCornerShape(RadiusCard)
    val InnerShape: Shape = RoundedCornerShape(RadiusInner)
    val PillShape: Shape = RoundedCornerShape(RadiusPill)

    // Border / blur
    val BorderWidth = 1.dp
    val BlurRadius = 24.dp
    const val NoiseFactor = 0.03f

    // Tint opacity of the surface token over the blurred backdrop: [normal, strong]
    val TintDark = 0.58f to 0.74f
    val TintLight = 0.62f to 0.78f
    // Opacity when no backdrop blur is available (Android < 12 or no source): [normal, strong]
    val FallbackOpacity = 0.92f to 0.96f

    // Spacing
    val ScreenPadding = 16.dp
    val CardPadding = 16.dp
    val ListSpacing = 10.dp
}

/** True when the app theme (not the system one) is dark, so the in-app theme override is respected. */
@Composable
private fun isDarkGlass(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/** Frosted-glass surface: backdrop blur + tint + sheen + luminous border, clipped to [shape]. */
@Composable
fun Modifier.glass(
    shape: Shape = GlassDefaults.CardShape,
    strong: Boolean = false,
    highlight: Color? = null
): Modifier {
    val hazeState = LocalHazeState.current
    val dark = isDarkGlass()

    // Tint comes from the theme surface token with enough opacity to keep text at AA contrast.
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outline
    val tintPair = if (dark) GlassDefaults.TintDark else GlassDefaults.TintLight
    val tint = surface.copy(alpha = if (strong) tintPair.second else tintPair.first)
    val fallbackPair = GlassDefaults.FallbackOpacity
    val fallback = surface.copy(alpha = if (strong) fallbackPair.second else fallbackPair.first)

    // Styles and brushes are remembered: this modifier runs for every card on every recomposition.
    val background = MaterialTheme.colorScheme.background
    val style = remember(background, tint, fallback) {
        HazeStyle(
            backgroundColor = background,
            tint = HazeTint(tint),
            blurRadius = GlassDefaults.BlurRadius,
            noiseFactor = GlassDefaults.NoiseFactor,
            fallbackTint = HazeTint(fallback)
        )
    }

    // Restrained specular highlight on the top-left corner only.
    val sheen = remember(dark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (dark) 0.07f else 0.40f),
                Color.Transparent
            ),
            end = Offset(400f, 400f)
        )
    }
    // Subtle border: bright hint at the top-left fading into the theme border token.
    val edge = remember(dark, highlight, outline) {
        Brush.linearGradient(
            colors = listOf(
                highlight ?: Color.White.copy(alpha = if (dark) 0.28f else 0.90f),
                outline,
                outline.copy(alpha = 0.6f)
            )
        )
    }

    return this
        .clip(shape)
        .then(
            if (hazeState != null && AppConfig.Design.GLASS_BACKDROP_BLUR_ENABLED) Modifier.hazeEffect(state = hazeState, style = style) {
                inputScale = HazeInputScale.Fixed(AppConfig.Design.GLASS_BLUR_RESOLUTION_SCALE)
            }
            else Modifier.background(fallback)
        )
        .background(sheen)
        .border(width = GlassDefaults.BorderWidth, brush = edge, shape = shape)
}

/** Glass card. Pass [onClick] to make it tappable. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = GlassDefaults.CardShape,
    strong: Boolean = false,
    highlight: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .glass(shape = shape, strong = strong, highlight = highlight)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content
    )
}

/** Colour used for subtle inner panels sitting on top of glass. */
@Composable
fun glassInnerPanelColor(): Color =
    MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDarkGlass()) 0.07f else 0.05f)

/**
 * App-wide backdrop: a textured "silk" surface - flowing ribbons with creased highlights, a fine dot
 * grid, film grain and a soft vignette. The scene is the blur source, so every [glass] surface placed
 * inside [content] frosts it. The texture is generated once per size and cached.
 */
@Composable
fun GlassBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val hazeState = rememberHazeState()
    val dark = isDarkGlass()

    // Everything derives from theme tokens: green (primary), cyan (tertiary), gold (secondary).
    val scheme = MaterialTheme.colorScheme
    val baseTop = scheme.background
    val baseBottom = scheme.surfaceVariant
    val ribbonA = scheme.primary
    val ribbonB = scheme.tertiary
    val ribbonC = scheme.secondary
    val ribbonAlpha = if (dark) 0.22f else 0.20f
    val ink = scheme.onSurface

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .hazeSource(hazeState)
                .background(Brush.verticalGradient(listOf(baseTop, baseBottom)))
                .drawWithCache {
                    if (!AppConfig.Design.BACKGROUND_TEXTURE_ENABLED) return@drawWithCache onDrawBehind { }
                    val w = size.width
                    val h = size.height

                    // Silk ribbon: two cubic edges offset vertically, closed into a band.
                    fun ribbon(y0: Float, bend: Float, thickness: Float): Path = Path().apply {
                        moveTo(-0.15f * w, y0)
                        cubicTo(0.25f * w, y0 - bend, 0.65f * w, y0 + bend, 1.15f * w, y0 - bend * 0.3f)
                        lineTo(1.15f * w, y0 - bend * 0.3f + thickness)
                        cubicTo(0.65f * w, y0 + bend + thickness * 1.4f, 0.25f * w, y0 - bend + thickness * 0.6f, -0.15f * w, y0 + thickness)
                        close()
                    }
                    fun edgeLine(y0: Float, bend: Float): Path = Path().apply {
                        moveTo(-0.15f * w, y0)
                        cubicTo(0.25f * w, y0 - bend, 0.65f * w, y0 + bend, 1.15f * w, y0 - bend * 0.3f)
                    }

                    data class Band(val y: Float, val bend: Float, val thick: Float, val color: Color)
                    val bands = listOf(
                        Band(h * 0.10f, h * 0.10f, h * 0.20f, ribbonA),
                        Band(h * 0.38f, -h * 0.12f, h * 0.24f, ribbonB),
                        Band(h * 0.66f, h * 0.11f, h * 0.22f, ribbonC),
                        Band(h * 0.88f, -h * 0.08f, h * 0.16f, ribbonA)
                    ).map { b ->
                        Triple(
                            ribbon(b.y, b.bend, b.thick),
                            edgeLine(b.y, b.bend),
                            Brush.linearGradient(
                                colors = listOf(
                                    b.color.copy(alpha = 0f),
                                    b.color.copy(alpha = ribbonAlpha),
                                    b.color.copy(alpha = ribbonAlpha * 0.15f)
                                ),
                                start = Offset(0f, b.y),
                                end = Offset(w, b.y + b.thick)
                            )
                        ) to b.color
                    }

                    // Dot grid
                    val step = 22.dp.toPx()
                    val dots = ArrayList<Offset>()
                    var gy = step / 2
                    while (gy < h) {
                        var gx = step / 2
                        while (gx < w) {
                            dots.add(Offset(gx, gy))
                            gx += step
                        }
                        gy += step
                    }

                    // Film grain
                    val rnd = Random(7)
                    val grain = List(((w * h) / 380f).toInt().coerceIn(1500, 4500)) {
                        Offset(rnd.nextFloat() * w, rnd.nextFloat() * h)
                    }

                    val vignette = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = if (dark) 0.45f else 0.10f)),
                        center = Offset(w / 2f, h / 2f),
                        radius = maxOf(w, h) * 0.85f
                    )

                    onDrawBehind {
                        bands.forEach { (parts, color) ->
                            val (fill, line, brush) = parts
                            drawPath(fill, brush)
                            drawPath(
                                path = line,
                                color = color.copy(alpha = if (dark) 0.35f else 0.45f),
                                style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        drawPoints(
                            points = dots,
                            pointMode = PointMode.Points,
                            color = ink.copy(alpha = if (dark) 0.12f else 0.14f),
                            strokeWidth = 1.6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawPoints(
                            points = grain,
                            pointMode = PointMode.Points,
                            color = ink.copy(alpha = if (dark) 0.07f else 0.08f),
                            strokeWidth = 1.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawRect(brush = vignette)
                    }
                }
        )
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            content()
        }
    }
}
