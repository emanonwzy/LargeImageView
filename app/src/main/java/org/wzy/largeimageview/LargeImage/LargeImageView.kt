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
    private var scale: Float = 1.0f
    private val bitmapRect: Rect = Rect()

    fun setCells(cells: List<Cell>) {
        this.cells = cells
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (cells != null) {
            canvas?.save()
            canvas?.scale(scale, scale)
            canvas?.translate(-1 * transX.toFloat(), -1 * transY.toFloat())
            for ((bitmap, region) in cells!!) {
                if (bitmap != null) {
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

    fun setScale(scale: Float) {
        this.scale += scale
        if (this.scale < 0.2) {
            this.scale = 0.2f
        } else if (this.scale > 4) {
            this.scale = 4.0f
        }
        invalidate()
    }

    fun getDisplayRect(): Rect {
        val scaleWidth: Float = width / scale
        val scaleHeight: Float = height / scale
        return Rect(transX, transY, transX + scaleWidth.toInt(), transY + scaleHeight.toInt())
    }
}
