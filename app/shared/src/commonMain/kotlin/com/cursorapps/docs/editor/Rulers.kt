package com.cursorapps.docs.editor

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

private val RulerLabelStyle = TextStyle(fontSize = 10.sp, color = DocsColors.TextSecondary)

/**
 * Ruler drawn above the page. Measurements start at the left margin, as in Docs,
 * so the inches to the left of it count back up.
 */
@Composable
internal fun HorizontalRuler(pageWidth: Dp, margin: Dp, inch: Dp, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier) {
        val width = pageWidth.toPx()
        val marginPx = margin.toPx()
        val inchPx = inch.toPx()
        val h = size.height
        val trackTop = h * 0.3f
        val trackHeight = h * 0.5f

        drawRect(Color.White, Offset(0f, trackTop), Size(width, trackHeight))
        drawRect(DocsColors.RulerMargin, Offset(0f, trackTop), Size(marginPx, trackHeight))
        drawRect(DocsColors.RulerMargin, Offset(width - marginPx, trackTop), Size(marginPx, trackHeight))

        drawTicks(
            measurer = measurer,
            origin = marginPx,
            start = 0f,
            end = width,
            inchPx = inchPx,
            labelCenter = trackTop + trackHeight / 2,
            baseline = trackTop + trackHeight,
            horizontal = true,
        )

        // Left indent (with first-line indent bar) and right indent markers.
        val markerWidth = 10f * density
        drawRect(DocsColors.RulerMarker, Offset(marginPx - markerWidth / 2, trackTop - 3 * density), Size(markerWidth, 3 * density))
        drawIndentMarker(marginPx, trackTop, markerWidth)
        drawIndentMarker(width - marginPx, trackTop, markerWidth)
    }
}

/**
 * Ruler drawn to the left of the page. [pageTop] is the page's y position inside the
 * viewport, so the ruler follows the page as it scrolls.
 */
@Composable
internal fun VerticalRuler(pageTop: Dp, pageHeight: Dp, margin: Dp, inch: Dp, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier) {
        val top = pageTop.toPx()
        val height = pageHeight.toPx()
        val marginPx = margin.toPx()
        val w = size.width
        val trackLeft = w * 0.2f
        val trackWidth = w * 0.6f

        drawRect(Color.White, Offset(trackLeft, top), Size(trackWidth, height))
        drawRect(DocsColors.RulerMargin, Offset(trackLeft, top), Size(trackWidth, marginPx))
        drawRect(DocsColors.RulerMargin, Offset(trackLeft, top + height - marginPx), Size(trackWidth, marginPx))

        drawTicks(
            measurer = measurer,
            origin = top + marginPx,
            start = top,
            end = top + height,
            inchPx = inch.toPx(),
            labelCenter = trackLeft + trackWidth / 2,
            baseline = trackLeft + trackWidth,
            horizontal = false,
        )
    }
}

/** Ticks every eighth of an inch, longer at halves, and a number at each whole inch. */
private fun DrawScope.drawTicks(
    measurer: TextMeasurer,
    origin: Float,
    start: Float,
    end: Float,
    inchPx: Float,
    labelCenter: Float,
    baseline: Float,
    horizontal: Boolean,
) {
    val step = inchPx / 8
    val first = ceil((start - origin) / step).toInt()
    val last = floor((end - origin) / step).toInt()
    for (i in first..last) {
        val pos = origin + i * step
        if (i % 8 == 0) {
            if (i == 0) continue
            val label = measurer.measure((abs(i) / 8).toString(), RulerLabelStyle)
            val center = if (horizontal) Offset(pos, labelCenter) else Offset(labelCenter, pos)
            val topLeft = Offset(center.x - label.size.width / 2f, center.y - label.size.height / 2f)
            if (horizontal) {
                drawText(label, topLeft = topLeft)
            } else {
                rotate(-90f, pivot = center) { drawText(label, topLeft = topLeft) }
            }
            continue
        }
        val length = (if (i % 4 == 0) 6f else 3f) * density
        val (from, to) = if (horizontal) {
            Offset(pos, baseline - length) to Offset(pos, baseline)
        } else {
            Offset(baseline - length, pos) to Offset(baseline, pos)
        }
        drawLine(DocsColors.RulerTick, from, to, strokeWidth = density)
    }
}

private fun DrawScope.drawIndentMarker(x: Float, top: Float, width: Float) {
    val path = Path().apply {
        moveTo(x - width / 2, top)
        lineTo(x + width / 2, top)
        lineTo(x, top + width * 0.6f)
        close()
    }
    drawPath(path, DocsColors.RulerMarker)
}
