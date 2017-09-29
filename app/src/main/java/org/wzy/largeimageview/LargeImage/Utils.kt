package org.wzy.largeimageview.LargeImage

/**
 * Created by zeyiwu on 21/10/2017.
 */

fun getSampleSize(scale: Float) : Int {
    var result = 1
    while (result * scale < 1) {
        result *= 2
    }
    return result
}