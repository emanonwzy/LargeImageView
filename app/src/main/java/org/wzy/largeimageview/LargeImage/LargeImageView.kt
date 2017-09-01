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

    private var cells: MutableList<Cell>? = null
    private var transX: Float = 0f
    private var transY: Float = 0f

    fun setCells(temp: List<Cell>) {
        if (this.cells == null) {
            this.cells = mutableListOf()
        }
        this.cells!!.addAll(temp)
        invalidate()
    }

    fun addCell(cell: Cell) {
        if (this.cells == null) {
            this.cells = mutableListOf()
        }
        this.cells!!.add(cell)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (cells != null) {
            canvas?.save()
            canvas?.translate(-1 * transX, -1 * transY)
            val rect: Rect = Rect()
            for ((bitmap, region) in cells!!) {
                rect.left = 0
                rect.top = 0
                rect.right = bitmap.width
                rect.bottom = bitmap.height

                canvas?.drawBitmap(bitmap, rect, region, null)
            }
            canvas?.restore()
        }
    }
}
