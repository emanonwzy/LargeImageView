package org.wzy.largeimageview.LargeImage

import android.graphics.Rect

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