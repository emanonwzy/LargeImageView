package org.wzy.largeimageview.LargeImage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.io.InputStream

/**
 * Created by zeyiwu on 26/08/2017.
 */
class LargeImageView : View, CellLoaderInterface {
    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    private var transX: Float = 0.0f
    private var transY: Float = 0.0f
    private var scale: Float = 1.0f
    private var loader: BitmapLoader? = null
    private var loaderThread: HandlerThread? = null
    private var loaderHandler: LoaderHandler? = null

    private val displayRect: Rect = Rect()
    private val bitmapRect: Rect = Rect()

    private val MSG_SET_IMAGE = 1
    private val MSG_LOAD_CELL = 2
    private val MSG_INIT_CELL = 3

    private var inputStream: InputStream? = null

    private class LoaderHandler(myLooper: Looper,
                                val img: LargeImageView) : Handler(myLooper) {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                img.MSG_SET_IMAGE -> {
                    img.loader?.setBitmap(img.inputStream)
                }

                img.MSG_LOAD_CELL -> {
                    img.loader?.loadCells(img.displayRect, img.scale, img.transX, img.transY)
                }

                img.MSG_INIT_CELL -> {
                    // center inside the image
                    if (img.loader != null) {
                        val bitmapWidth = img.loader!!.getWidth()
                        val bitmapHeight = img.loader!!.getHeight()

                        img.scale = Math.min(img.displayRect.width().toFloat() / bitmapWidth.toFloat(),
                                img.displayRect.height().toFloat() / bitmapHeight.toFloat())
                        img.transX = (bitmapWidth * img.scale - img.displayRect.width()) / 2
                        img.transY = (bitmapHeight * img.scale - img.displayRect.height()) / 2

                        img.loader?.loadCells(img.displayRect, img.scale, img.transX, img.transY)
                    }
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        displayRect.right = w
        displayRect.bottom = h
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.save()
        val scaleSampleSize = getSampleSize(scale)

        canvas?.translate(-transX, -transY)
        canvas?.scale(scale * scaleSampleSize, scale * scaleSampleSize)
        loader?.getCells()?.forEach {
            if (it.bitmap != null) {
                with(it.region) {
                    bitmapRect.left = left / scaleSampleSize
                    bitmapRect.right = right / scaleSampleSize
                    bitmapRect.top = top / scaleSampleSize
                    bitmapRect.bottom = bottom / scaleSampleSize
                }
                canvas?.drawBitmap(it.bitmap, null, bitmapRect, null)
            }
        }
        canvas?.restore()
    }

    fun setTransXY(x: Int, y: Int) {
        transX += x
        transY += y
        sendMessage(MSG_LOAD_CELL)
        invalidate()
    }

    fun setScale(scale: Float) {
        this.scale += scale
        if (this.scale < 0.2) {
            this.scale = 0.2f
        } else if (this.scale > 4) {
            this.scale = 4.0f
        }
        transX = 0.0f
        transY = 0.0f
        sendMessage(MSG_LOAD_CELL)
        invalidate()
    }

    fun getScale(): Float {
        return scale
    }

    fun setImage(inputStream: InputStream) {
        this.inputStream = inputStream
        clear()
        post {
            initLoader()
            sendMessage(MSG_SET_IMAGE)
            sendMessage(MSG_INIT_CELL)
        }
    }

    private fun initLoader() {
        loader = BitmapLoader(width, height)
        loader?.setLoaderInterface(this)

        loaderThread = HandlerThread("load_bitmap")
        loaderThread?.start()
        loaderHandler = LoaderHandler(loaderThread!!.looper, this)
    }

    private fun sendMessage(msg: Int) {
        if (loaderHandler != null) {
            val msg = Message.obtain(loaderHandler, msg)
            msg.sendToTarget()
        }
    }

    fun clear() {
        loader?.stop()
        loaderThread?.quit()
    }

    override fun cellLoaded(cell: Cell) {
        var left = cell.region.left * scale + transX
        var top = cell.region.top * scale + transY
        var right = cell.region.right * scale + transX
        var bottom = cell.region.bottom * scale + transY
        postInvalidate(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }
}
