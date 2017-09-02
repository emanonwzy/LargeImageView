package org.wzy.largeimageview.LargeImage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

/**
 * Created by zeyiwu on 26/08/2017.
 */
class LargeImageView : View {
    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    private var cells: List<Cell>? = null
    private var transX: Int = 0
    private var transY: Int = 0
    private val bitmapRect: Rect = Rect()
    private val drawRect: Rect = Rect()

    fun setCells(cells: List<Cell>) {
        this.cells = cells
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        getDrawingRect(drawRect)
        if (cells != null) {
            canvas?.save()
            canvas?.translate(-1 * transX.toFloat(), -1 * transY.toFloat())
            drawRect.offset(transX, transY)
            for ((bitmap, region) in cells!!) {
                if (bitmap != null && drawRect.intersects(region.left, region.top, region.right, region.bottom)) {
                    bitmapRect.left = 0
                    bitmapRect.top = 0
                    bitmapRect.right = bitmap.width
                    bitmapRect.bottom = bitmap.height

                    canvas?.drawBitmap(bitmap, bitmapRect, region, null)
                }
            }
            canvas?.restore()
        }
    }

    fun setTransXY(x: Int, y: Int) {
        transX += x
        transY += y
        invalidate()
    }

    fun getDisplayRect(): Rect {
        return Rect(transX, transY, transX + width, transY + height)
    }
}
