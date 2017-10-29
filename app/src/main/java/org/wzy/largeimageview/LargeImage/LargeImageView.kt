package org.wzy.largeimageview.LargeImage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.OverScroller
import java.io.InputStream

/**
 * Created by zeyiwu on 26/08/2017.
 */
class LargeImageView : View, CellLoaderInterface {
    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        simpleGestureDetector = GestureDetector(context, GestureListener())
        scroller = OverScroller(context)

        minTouchSize = dipToPx(context, 32)
    }

    private var transX: Float = 0.0f
    private var transY: Float = 0.0f
    private var scale: Float = 1.0f
    private var minScale: Float = 1.0f
    private val maxScale: Float = 4.0f
    private val minTouchSize: Float
    private var loader: BitmapLoader? = null
    private var loaderThread: HandlerThread? = null
    private var loaderHandler: LoaderHandler? = null
    private val scroller: OverScroller

    private val displayRect: Rect = Rect()
    private val cellDrawRect: Rect = Rect()

    private val MSG_SET_IMAGE = 1
    private val MSG_LOAD_CELL = 2

    private var touchSlop: Int = 0

    private var inputStream: InputStream? = null

    private val scaleGestureDetector: ScaleGestureDetector
    private val simpleGestureDetector: GestureDetector

    private class LoaderHandler(myLooper: Looper,
                                val img: LargeImageView) : Handler(myLooper) {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                img.MSG_SET_IMAGE -> {
                    img.loader?.setBitmap(img.inputStream, img.width, img.height)
                    img.updateInitFactor()
                }

                img.MSG_LOAD_CELL -> {
                    img.updateDisplayRect()
                    img.loader?.loadCells(img.displayRect, img.scale)
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
        if (loader != null) {
            val bitmapWidth = loader!!.getWidth()
            val bitmapHeight = loader!!.getHeight()

            scale = Math.min(width.toFloat() / bitmapWidth.toFloat(),
                    height.toFloat() / bitmapHeight.toFloat())
            minScale = scale
            transX = (width - bitmapWidth * scale) / 2
            transY = (height - bitmapHeight * scale) / 2

            updateDisplayRect()

            postInvalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val scaleSampleSize = getSampleSize(scale)

        canvas?.save()
        canvas?.translate(transX, transY)
        canvas?.scale(scale * scaleSampleSize, scale * scaleSampleSize)

        drawBackground(canvas, scaleSampleSize)
        drawCells(canvas, scaleSampleSize)

        canvas?.restore()
    }

    private fun drawBackground(canvas: Canvas?, scaleSampleSize: Int) {
//        getDrawingRect(backgroundDrawRect)
//        backgroundDrawRect.offset(-transX.toInt(), -transY.toInt())
//        getBitampRectFromDisplayRect(backgroundDrawRect, backgroundRect, scale)
//
//        if (loader != null && loader!!.isInitied()) {
//            if (backgroundRect.intersect(loader?.getInitCell()?.region) && !backgroundRect.isEmpty) {
//
//                val sampleSize:Int = loader?.getInitCell()?.inSampleSize!!
//
//                getDisplayRectFromBitmapRect(backgroundRect, backgroundDrawRect, scale)
//                backgroundDrawRect.offset(transX.toInt(), transY.toInt())
//
//                with(backgroundRect) {
//                    left /= sampleSize
//                    top /= sampleSize
//                    right /= sampleSize
//                    bottom /= sampleSize
//                }
//
//                canvas?.drawBitmap(loader?.getInitCell()?.bitmap,
//                        backgroundRect, backgroundDrawRect, null)
//            }
//        }
        val cell = loader?.getInitCell()
        if (cell?.bitmap != null) {
            with(cell!!.region) {
                cellDrawRect.left = left / scaleSampleSize
                cellDrawRect.right = right / scaleSampleSize
                cellDrawRect.top = top / scaleSampleSize
                cellDrawRect.bottom = bottom / scaleSampleSize
            }
            canvas?.drawBitmap(cell.bitmap, null, cellDrawRect, null)
        }
    }

    private fun drawCells(canvas: Canvas?, scaleSampleSize: Int) {
        loader?.getCells()?.forEach {
            if (it.bitmap != null) {
                with(it.region) {
                    cellDrawRect.left = left / scaleSampleSize
                    cellDrawRect.right = right / scaleSampleSize
                    cellDrawRect.top = top / scaleSampleSize
                    cellDrawRect.bottom = bottom / scaleSampleSize
                }
                canvas?.drawBitmap(it.bitmap, null, cellDrawRect, null)
            }
        }
    }

    private fun setTransXY(x: Float, y: Float): Boolean {
        val bitmapWidth = loader!!.getWidth() * scale
        val bitmapHeight = loader!!.getHeight() * scale
        var tempX = transX
        var tempY = transY

        if (width >= bitmapWidth) {
            tempX = (width - bitmapWidth) / 2
        } else {
            tempX += x
            val minTransX = width - bitmapWidth
            tempX = Math.min(0.0f, Math.max(tempX, minTransX))
        }

        if (height >= bitmapHeight) {
            tempY = (height - bitmapHeight) / 2
        } else {
            tempY += y
            val minTransY = height - bitmapHeight
            tempY = Math.min(0.0f, Math.max(tempY, minTransY))
        }

        if (tempX != transX || tempY != transY) {
            transX = tempX
            transY = tempY
            sendMessage(MSG_LOAD_CELL)
            invalidate()
            return true
        }
        return false
    }

    private fun setScale(newScale: Float, focusX: Float, focusY: Float): Boolean {
        var tempScale = scale
        tempScale *= newScale
        tempScale = Math.max(minScale, Math.min(tempScale, maxScale))

        if (tempScale != this.scale) {
            val (bitmapX, bitmapY) = screenPointToBitmapPoint(focusX, focusY,
                    scale, transX, transY)

            this.scale = tempScale
            val (newFocusX, newFocusY) = bitmapPointToScreenPoint(bitmapX, bitmapY,
                    tempScale, transX, transY)

            if (!setTransXY(focusX - newFocusX, focusY - newFocusY)) {
                sendMessage(MSG_LOAD_CELL)
                invalidate()
            }
            return true
        }
        return false
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
        }
    }

    private fun initLoader() {
        loader = BitmapLoader(width, height)
        loader?.setLoaderInterface(this)

        loaderThread = HandlerThread("load_bitmap")
        loaderThread?.start()
        loaderHandler = LoaderHandler(loaderThread!!.looper, this)
    }

    private fun sendMessage(what: Int) {
        if (loaderHandler != null) {
            val msg = Message.obtain(loaderHandler, what)
            msg.sendToTarget()
        }
    }

    fun clear() {
        loader?.stop()
        loaderThread?.quit()
    }

    override fun cellLoaded(cell: Cell) {
        val (left, top) = bitmapPointToScreenPoint(cell.region.left.toFloat(),
                cell.region.top.toFloat(),
                scale, transX, transY)

        val (right, bottom) = bitmapPointToScreenPoint(cell.region.right.toFloat(),
                cell.region.bottom.toFloat(),
                scale, transX, transY)
        postInvalidate(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var retVal = scaleGestureDetector.onTouchEvent(event)
        retVal = simpleGestureDetector.onTouchEvent(event) || retVal
        return retVal || super.onTouchEvent(event)
    }

    inner class ScaleListener: ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (java.lang.Float.isNaN(detector.scaleFactor)
                    || java.lang.Float.isInfinite(detector.scaleFactor)) return false

            if (hitTest(detector.focusX, detector.focusY)) {
                setScale(detector.scaleFactor, detector.focusX, detector.focusY)
                return true
            }
            return false
        }
    }

    inner class GestureListener: GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            scroller.forceFinished(true)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (hitTest(e.x, e.y)) {
                var doubalScale = 2f

                if (scale * doubalScale > maxScale && scale == maxScale) {
                    doubalScale = minScale / scale
                }
                setScale(doubalScale, e.x, e.y)
                return true
            }
            return false
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (hitTest(e1.x, e2.y)) {
                setTransXY(-distanceX, -distanceY)
                return true
            }
            return false
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (hitTest(e1.x, e1.y)) {
                flingStartX = e1.x.toInt()
                flingStartY = e1.y.toInt()
                return fling(flingStartX, flingStartY, -velocityX.toInt(), -velocityY.toInt())
            }
            return false
        }
    }

    private var flingStartX: Int = 0
    private var flingStartY: Int = 0

    private fun fling(
            startX: Int,
            startY: Int,
            velocityX: Int,
            velocityY: Int): Boolean {
        val (left, top) = bitmapPointToScreenPoint(0f, 0f, scale, transX, transY)
        val (right, bottom) = bitmapPointToScreenPoint(loader!!.getWidth().toFloat(),
                loader!!.getHeight().toFloat(), scale, transX, transY)

        scroller.forceFinished(true)

        scroller.fling(startX,
                startY,
                velocityX,
                velocityY,
                left.toInt(), right.toInt(),
                top.toInt(), bottom.toInt())
        ViewCompat.postInvalidateOnAnimation(this);
        return true
    }

    private fun hitTest(x: Float, y: Float): Boolean {
        return loader != null
                && loader!!.isInitied()
                && hitTest(x, y,
                loader!!.getWidth(),
                loader!!.getHeight(),
                scale, transX, transY,
                minTouchSize)
    }

    override fun computeScroll() {
        super.computeScroll()

        var needsInvalidate = false

        if (scroller.computeScrollOffset()) {
            val currX = scroller.currX
            val currY = scroller.currY
            setTransXY((flingStartX - currX).toFloat(), (flingStartY - currY).toFloat())
            flingStartX = currX
            flingStartY = currY
        }

        if (needsInvalidate) ViewCompat.postInvalidateOnAnimation(this)
    }
}
