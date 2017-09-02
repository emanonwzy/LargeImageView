package org.wzy.largeimageview.LargeImage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.util.Log

/**
 * Created by zeyiwu on 01/09/2017.
 */

class BitmapLoader(val decoder: BitmapRegionDecoder,
                   val bitmapWidth: Int,
                   val bitmapHeight: Int,
                   val cellWidth: Int,
                   val cellHeight: Int) {

    private var cells: MutableList<Cell> = mutableListOf()
    var loaderInterface: CellLoaderInterface? = null

    init {
        val rows: Int = if (bitmapHeight % cellHeight == 0) bitmapHeight / cellHeight else (bitmapHeight / cellHeight) + 1
        val cols: Int = if (bitmapWidth % cellWidth == 0) bitmapWidth / cellWidth else (bitmapWidth / cellWidth) + 1
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val left = j * cellWidth
                val top = i * cellHeight
                val right = if ((left + cellWidth) > bitmapWidth) bitmapWidth else left + cellWidth
                val bottom = if ((top + cellHeight) > bitmapHeight) bitmapHeight else top + cellHeight
                cells.add(Cell(null, Rect(left, top, right, bottom)))
            }
        }
        Log.d("www", "cellRects size=" + cells.size)
    }

    fun updateDisplayRect(rect: Rect) {

        Log.d("www", "updateDisplayRect =" + rect)
        // validate rect
        val width = rect.width()
        val height = rect.height()
        // bigger than bitmap
        if (!rect.intersect(0, 0, bitmapWidth, bitmapHeight))
            return

        Log.d("www", "rect1=" + rect)
        if (rect.left == 0)
            rect.right = width
        else if (rect.right == bitmapWidth)
            rect.left = rect.right - width

        if (rect.top == 0)
            rect.bottom = height
        else if (rect.bottom == bitmapHeight)
            rect.top = rect.bottom - height

        rect.intersect(0, 0, bitmapWidth, bitmapHeight)
        Log.d("www", "validate rect=" + rect)

        // cache
        rect.inset(-1 * rect.width() / 4, -1 * rect.height() / 4)

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        cells.map {
            val isInterest = it.region.intersects(rect.left, rect.top, rect.right, rect.bottom)
            if (isInterest) {
                if (it.bitmap == null) {
                    it.bitmap = decoder.decodeRegion(it.region, options)
                    Log.d("www", "decode cell, rect=" + it.region + ", bitmap=(" + it.bitmap?.width + "," + it.bitmap?.height + ")")
                    loaderInterface?.cellLoaded(it)
                }
            } else {
                if (it.bitmap != null) {
                    val temp: Bitmap = it.bitmap!!
                    it.bitmap = null
                    temp.recycle()
                    Log.d("www", "recycle cell, rect=" + it.region)

                    loaderInterface?.cellRecycled(it)
                }
            }
        }
    }
}
