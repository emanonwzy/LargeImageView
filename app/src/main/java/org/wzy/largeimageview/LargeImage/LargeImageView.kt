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
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import java.io.InputStream

/**
 * Created by zeyiwu on 26/08/2017.
 */
class LargeImageView : View, CellLoaderInterface, OnGestureListener, View.OnTouchListener {
    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        gestureDetector = ImageGestureDetector(context)
        gestureDetector.setListener(this)
        setOnTouchListener(this)
    }

    private var transX: Float = 0.0f
    private var transY: Float = 0.0f
    private var scale: Float = 1.0f
    private var minScale: Float = 1.0f
    private val maxScale: Float = 4.0f
    private var loader: BitmapLoader? = null
    private var loaderThread: HandlerThread? = null
    private var loaderHandler: LoaderHandler? = null

    private val displayRect: Rect = Rect()
    private val bitmapRect: Rect = Rect()

    private val MSG_SET_IMAGE = 1
    private val MSG_LOAD_CELL = 2
    private val MSG_INIT_CELL = 3

    private var touchSlop: Int = 0

    private var inputStream: InputStream? = null

    private val gestureDetector: ImageGestureDetector

    private class LoaderHandler(myLooper: Looper,
                                val img: LargeImageView) : Handler(myLooper) {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                img.MSG_SET_IMAGE -> {
                    img.loader?.setBitmap(img.inputStream)
                }

                img.MSG_LOAD_CELL -> {
                    img.updateDisplayRect()
                    img.loader?.loadCells(img.displayRect, img.scale)
                }

                img.MSG_INIT_CELL -> {
                    // center inside the image
                    if (img.loader != null) {
                        img.updateInitFactor()

                        img.updateDisplayRect()
                        img.loader!!.loadCells(img.displayRect, img.scale)
                    }
                }
            }
        }
    }

    private fun updateDisplayRect() {
        getDrawingRect(displayRect)
        displayRect.offset(-transX.toInt(), -transY.toInt())

        val displayWidth = displayRect.width() / scale
        val displayHeight = displayRect.height() / scale
        with(displayRect) {
            left = (left / scale - displayWidth / 2).toInt()
            top = (top / scale - displayHeight / 2).toInt()
            right = (right / scale + displayWidth / 2).toInt()
            bottom = (bottom / scale + displayHeight / 2).toInt()
        }
    }

    private fun updateInitFactor() {
        val bitmapWidth = loader!!.getWidth()
        val bitmapHeight = loader!!.getHeight()

        scale = Math.min(width.toFloat() / bitmapWidth.toFloat(),
                height.toFloat() / bitmapHeight.toFloat())
        minScale = scale
        transX = (width - bitmapWidth * scale) / 2
        transY = (height - bitmapHeight * scale) / 2
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.save()
        val scaleSampleSize = getSampleSize(scale)

        canvas?.translate(transX, transY)
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

    private fun setTransXY(x: Float, y: Float) {
        if (loader != null && loader!!.isInitied()) {
            getDrawingRect(displayRect)

            val bitmapWidth = loader!!.getWidth() * scale
            val bitmapHeight = loader!!.getHeight() * scale
            var tempX = transX
            var tempY = transY

            if (displayRect.width() >= bitmapWidth && displayRect.height() >= bitmapHeight) {
                tempX = (width - bitmapWidth) / 2
                tempY = (height - bitmapHeight) / 2
            } else {
                tempX += x
                tempY += y

                val minTransX = displayRect.width() - bitmapWidth
                val minTransY = displayRect.height() - bitmapHeight
                tempX = Math.min(0.0f, Math.max(tempX, minTransX))
                tempY = Math.min(0.0f, Math.max(tempY, minTransY))
            }

            if (tempX != transX || tempY != transY) {
                transX = tempX
                transY = tempY
                sendMessage(MSG_LOAD_CELL)
                invalidate()
            }
        }
    }

    private fun setScale(scale: Float) {
        if (loader != null && loader!!.isInitied()) {
            var tempScale = this.scale

            tempScale *= scale
            tempScale = Math.max(minScale, Math.min(tempScale, maxScale))

            if (tempScale != this.scale) {
                this.scale = tempScale
                sendMessage(MSG_LOAD_CELL)
                invalidate()
            }
        }
    }

    private fun getScale(): Float {
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
        var left = (cell.region.left) * scale + transX
        var top = (cell.region.top) * scale + transY
        var right = (cell.region.right) * scale + transX
        var bottom = (cell.region.bottom) * scale + + transY
        Log.d("www1", "$left, $top, $right, $bottom, cell=${cell.region}, ${cell.inSampleSize}, $scale")
        postInvalidate(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    override fun onDrag(dx: Float, dy: Float) {
        setTransXY(dx, dy)
    }

    override fun onFling(startX: Float, startY: Float, velocityX: Float, velocityY: Float) {

    }

    override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float) {
        setScale(scaleFactor)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return if (event != null) gestureDetector.onTouchEvent(event!!) else false
    }
}
