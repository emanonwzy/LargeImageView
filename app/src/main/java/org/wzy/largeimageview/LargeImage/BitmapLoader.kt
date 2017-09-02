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
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val intestCells: List<Cell>? = getCellRects(rect)
        Log.d("www", "interest cell=" + intestCells?.size)
        intestCells?.map {
            if (it.bitmap == null) {
                it.bitmap = decoder.decodeRegion(it.region, options)
                Log.d("www", "decode cell, rect=" + it.region + ", bitmap=(" + it.bitmap?.width + "," + it.bitmap?.height + ")")
                loaderInterface?.cellLoaded(it)
            }
        }
    }

    /**
     * get divider rects
     */
    private fun getCellRects(rect: Rect) : List<Cell>? {
        // validate rect
        val width = rect.width()
        val height = rect.height()
        rect.intersect(0, 0, bitmapWidth, bitmapHeight)
        if (rect.left == 0)
            rect.right = width
        else if (rect.right == bitmapWidth)
            rect.left = rect.right - width
        if (rect.top == 0)
            rect.bottom = height
        else if (rect.bottom == bitmapHeight)
            rect.top = rect.bottom - height

        // cache
        rect.inset(-1 * rect.width() / 4, -1 * rect.height() / 4)
        Log.d("www", "getCellRects=" + rect)
        return cells.filter { it.region.intersects(rect.left, rect.top, rect.right, rect.bottom) }
    }
}
