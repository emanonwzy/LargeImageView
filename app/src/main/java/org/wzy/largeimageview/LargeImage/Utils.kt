package org.wzy.largeimageview.LargeImage

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Created by zeyiwu on 21/10/2017.
 */

fun getSampleSize(scale: Float): Int {
    var result = 1
    while (result * scale < 1) {
        result *= 2
    }
    return result
}

/**
 * get screen bitmap point from screen point
 *
 * @param x screen point x
 * @param y screen point y
 * @param scale scale factor
 * @param transX screen trans x
 * @param transY screen trans y
 * @return point on bitmap
 */
fun screenPointToBitmapPoint(x: Float, y: Float, scale: Float,
                             transX: Float, transY: Float): Pair<Float, Float> {
    return Pair((x - transX) / scale, (y - transY) / scale)
}

fun bitmapPointToScreenPoint(x: Float, y: Float, scale: Float,
                             transX: Float, transY: Float): Pair<Float, Float> {
    return Pair(x * scale + transX, y * scale + transY)
}

fun getBitampRectFromDisplayRect(displayRect: Rect, bitmapRectF: Rect, scale: Float) {
    with(displayRect) {
        bitmapRectF.left = (left.toFloat() / scale).toInt()
        bitmapRectF.top = (top.toFloat() / scale).toInt()
        bitmapRectF.right = (right.toFloat() / scale).toInt()
        bitmapRectF.bottom = (bottom.toFloat() / scale).toInt()
    }
}

fun getDisplayRectFromBitmapRect(bitmapRect: Rect, displayRect: Rect, scale: Float) {
    with(bitmapRect) {
        displayRect.left = (left.toFloat() * scale).toInt()
        displayRect.top = (top.toFloat() * scale).toInt()
        displayRect.right = (right.toFloat() * scale).toInt()
        displayRect.bottom = (bottom.toFloat() * scale).toInt()
    }
}

fun hitTest(screenX: Float, screenY: Float, bitmapWidth: Int, bitmapHeight: Int,
            scale: Float, transX: Float, transY: Float,
            minTouchSize: Float): Boolean {
    var (left, top) = bitmapPointToScreenPoint(0f, 0f, scale, transX, transY)
    var (right, bottom) = bitmapPointToScreenPoint(bitmapWidth.toFloat(), bitmapHeight.toFloat(),
            scale, transX, transY)

    if (right - left < minTouchSize) {
        left -= minTouchSize / 2
        right += minTouchSize / 2
    }

    if (bottom - top < minTouchSize) {
        top -= minTouchSize / 2
        bottom += minTouchSize / 2
    }

    return screenX > left && screenX < right && screenY > top && screenY < bottom
}

fun dipToPx(ctx: Context, dp: Int): Float {
    val metrics = DisplayMetrics()
    val manager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    manager.defaultDisplay.getMetrics(metrics)

    return dp * metrics.density
}