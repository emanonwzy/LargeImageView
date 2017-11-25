package io.github.emanonwzy

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Created by zeyiwu on 21/10/2017.
 */

val LOG_TAG = "LargeImageView"

fun getSampleSize(scale: Float): Int {
    var result = 1
    while (result * scale < 1) {
        result *= 2
    }
    return result
}

fun screenPointToBitmapPoint(x: Float, scale: Float, transX: Float): Float {
    return (x - transX) / scale
}

fun bitmapPointToScreenPoint(x: Float, scale: Float, transX: Float): Float {
    return x * scale + transX
}

fun screenRectToBitmapRect(screenRect: Rect,
                           scale: Float,
                           transX: Float,
                           transY: Float) {
    with(screenRect) {
        left = screenPointToBitmapPoint(left.toFloat(), scale, transX).toInt()
        top = screenPointToBitmapPoint(top.toFloat(), scale, transY).toInt()
        right = screenPointToBitmapPoint(right.toFloat(), scale, transX).toInt()
        bottom = screenPointToBitmapPoint(bottom.toFloat(), scale,  transY).toInt()
    }
}

fun bitmapRectToScreenRect(bitmapRect: Rect, screenRect: Rect,
                           scale: Float,
                           transX: Float,
                           transY: Float) {
    with(bitmapRect) {
        screenRect.left = bitmapPointToScreenPoint(left.toFloat(), scale, transX).toInt()
        screenRect.top = bitmapPointToScreenPoint(top.toFloat(), scale, transY).toInt()
        screenRect.right = bitmapPointToScreenPoint(right.toFloat(), scale, transX).toInt()
        screenRect.bottom = bitmapPointToScreenPoint(bottom.toFloat(), scale, transY).toInt()
    }
}

fun bitmapRectToScreenRect(width: Int, height: Int,
                           screenRect: Rect,
                           scale: Float,
                           transX: Float, transY: Float) {
    with(screenRect) {
        left = bitmapPointToScreenPoint(0f, scale, transX).toInt()
        top = bitmapPointToScreenPoint(0f, scale, transY).toInt()
        right = bitmapPointToScreenPoint(width.toFloat(), scale, transX).toInt()
        bottom = bitmapPointToScreenPoint(height.toFloat(), scale, transY).toInt()
    }
}

fun dipToPx(ctx: Context, dp: Int): Float {
    val metrics = DisplayMetrics()
    val manager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    manager.defaultDisplay.getMetrics(metrics)

    return dp * metrics.density
}