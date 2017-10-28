package org.wzy.largeimageview.LargeImage

import android.graphics.RectF

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

fun hitTestInBitmap(x: Float, y: Float, width: Int, height: Int): Boolean {
    return x > 0 && x < width && y > 0 && y < height
}

fun getBitmapDisplayRect(width: Int, height: Int, scale: Float,
                         transX: Float, transY: Float, rectF: RectF) {
    rectF.left = transX
    rectF.top = transY
    rectF.right = width * scale + transX
    rectF.bottom = height * scale + transY
}