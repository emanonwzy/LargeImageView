package org.wzy.largeimageview.LargeImage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.util.Log
import java.io.InputStream
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by zeyiwu on 01/09/2017.
 */

class BitmapLoader(val cellWidth: Int,
                   val cellHeight: Int) {

    private var cells: CopyOnWriteArrayList<Cell>? = null
    private var loaderInterface: CellLoaderInterface? = null

    private var width: Int = 0
    private var height: Int = 0
    private var decoder: BitmapRegionDecoder? = null

    fun setBitmap(input: InputStream?) {
        if (input != null) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(input, null, options)

            width = options.outWidth
            height = options.outHeight

            decoder = BitmapRegionDecoder.newInstance(input, false)
        }
    }

    fun setLoaderInterface(loader: CellLoaderInterface) {
        loaderInterface = loader
    }

    fun loadCells(displayRect: Rect, scale: Float, transX: Float, transY: Float) {
        if (decoder == null) return

        val displayWidth = displayRect.width() / scale
        val displayHeight = displayRect.height() / scale
        val deltaX = transX / scale
        val deltaY = transY /scale

        var rect = Rect( (displayRect.left + deltaX).toInt(),
                (displayRect.top + deltaY).toInt(),
                (displayRect.left + deltaX + displayWidth).toInt(),
                (displayRect.top + deltaY + displayHeight).toInt() )
        rect.inset((-displayWidth / 2).toInt(), (- displayHeight / 2).toInt())

        if (!rect.intersect(0, 0, width, height))
            return

        val sampleSize = getSampleSize(scale)
        val scaleCellWidth = cellWidth * sampleSize
        val scaleCellHeight = cellHeight * sampleSize

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inSampleSize = sampleSize

        //todo 从 displayRect 起始处开始
        val rows: Int = if (height % scaleCellHeight == 0) height / scaleCellHeight else (height / scaleCellHeight) + 1
        val cols: Int = if (width % scaleCellWidth == 0) width / scaleCellWidth else (width / scaleCellWidth) + 1
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val left = j * scaleCellWidth
                val top = i * scaleCellHeight
                val right = if ((left + scaleCellWidth) > width) width else left + scaleCellWidth
                val bottom = if ((top + scaleCellHeight) > height) height else top + scaleCellHeight

                if (rect.intersects(left, top, right, bottom)) {
                    val cellRegion = Rect(left, top, right, bottom)
                    var cell = cells?.find {
                        it.region == cellRegion && it.inSampleSize == sampleSize
                    }
                    if (cell == null) {
                        cell = Cell(decoder?.decodeRegion(cellRegion, options), cellRegion, sampleSize)

                        if (cells == null) cells = CopyOnWriteArrayList()
                        cells?.add(cell)

                        Log.d("www", "decode cell, rect=" + cellRegion + ", bitmap=(" + cell.bitmap?.width + "," + cell.bitmap?.height + ")")
                    }
                    loaderInterface?.cellLoaded(cell)
                }
            }
        }

        val recycleCells = cells?.filter {
            !Rect.intersects(it.region, rect) || it.inSampleSize != sampleSize
        }
        if (recycleCells != null) {
            cells?.removeAll(recycleCells)
            recycleCells.forEach {
                it.bitmap?.recycle()
                it.bitmap = null
            }
        }
    }

    fun getCells(): CopyOnWriteArrayList<Cell>? {
        return cells
    }

    fun stop() {
        cells?.forEach {
            it.bitmap?.recycle()
            it.bitmap = null
        }
        cells?.clear()
        decoder?.recycle()
        decoder = null
    }

    fun getWidth() = width
    fun getHeight() = height
}
