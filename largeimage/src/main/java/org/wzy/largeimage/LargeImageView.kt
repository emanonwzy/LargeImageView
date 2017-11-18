package org.wzy.largeimage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.*
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.*
import android.widget.OverScroller
import java.io.File
import java.io.FileInputStream
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
        zoomer = Zoomer(context)

        minTouchSize = dipToPx(context, 48).toInt()
    }

    private var transX: Float = 0.0f
    private var transY: Float = 0.0f
    private var scale: Float = -1.0f
    private var minScale: Float = 1.0f
    private val maxScale: Float = 4.0f
    private var doubleTapStep: Float = 2.0f
    private val minTouchSize: Int
    private var loader: BitmapLoader? = null
    private var loaderThread: HandlerThread? = null
    private var loaderHandler: LoaderHandler? = null
    private val scroller: OverScroller

    private val displayRect: Rect = Rect()
    private val cellDrawRect: Rect = Rect()

    private val cellInvalidateRect: Rect = Rect()
    private val bitmapScreenRect: Rect = Rect()

    private val MSG_SET_IMAGE = 1
    private val MSG_LOAD_CELL = 2

    private var touchSlop: Int = 0

    private var inputStream: InputStream? = null

    private val scaleGestureDetector: ScaleGestureDetector
    private val simpleGestureDetector: GestureDetector
    private val zoomer: Zoomer

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

    private fun updateInitFactor() {
        if (loader != null) {
            if (scale == -1.0f) {
                val bitmapWidth = loader!!.getWidth()
                val bitmapHeight = loader!!.getHeight()

                scale = Math.min(width.toFloat() / bitmapWidth.toFloat(),
                        height.toFloat() / bitmapHeight.toFloat())
                minScale = scale
                transX = (width - bitmapWidth * scale) / 2
                transY = (height - bitmapHeight * scale) / 2
            } else {
                // restore from save state
                sendMessage(MSG_LOAD_CELL)
            }

            updateDisplayRect()

            postInvalidate()
        }
    }

    private fun updateDisplayRect() {
        getDrawingRect(displayRect)
        screenRectToBitmapRect(displayRect, scale, transX, transY)
        // 扩大显示范围，提前加载后面半屏的内容
        displayRect.inset(-displayRect.width() / 2, -displayRect.height() / 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val scaleSampleSize = getSampleSize(scale)

        canvas.save()
        canvas.translate(transX, transY)
        canvas.scale(scale * scaleSampleSize, scale * scaleSampleSize)

        drawBackground(canvas, scaleSampleSize)
        drawCells(canvas, scaleSampleSize)

        canvas.restore()
    }

    private fun drawBackground(canvas: Canvas, scaleSampleSize: Int) {
        val cell = loader?.getInitCell()
        if (cell?.bitmap != null) {
            with(cell!!.region) {
                cellDrawRect.left = left / scaleSampleSize
                cellDrawRect.right = right / scaleSampleSize
                cellDrawRect.top = top / scaleSampleSize
                cellDrawRect.bottom = bottom / scaleSampleSize
            }
            canvas.drawBitmap(cell.bitmap, null, cellDrawRect, null)
        }
    }

    private fun drawCells(canvas: Canvas, scaleSampleSize: Int) {
        loader?.getCells()?.forEach {
            if (it.bitmap != null) {
                with(it.region) {
                    cellDrawRect.left = left / scaleSampleSize
                    cellDrawRect.right = right / scaleSampleSize
                    cellDrawRect.top = top / scaleSampleSize
                    cellDrawRect.bottom = bottom / scaleSampleSize
                }
                canvas.drawBitmap(it.bitmap, null, cellDrawRect, null)
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

    private fun setNewScale(newScale: Float, focusX: Float, focusY: Float): Boolean {
        var tempScale = scale * newScale
        tempScale = Math.max(minScale, Math.min(tempScale, maxScale))

        return setScale(tempScale, focusX, focusY)
    }

    private fun setScale(tempScale: Float, focusX: Float, focusY: Float): Boolean {
        if (tempScale != this.scale) {
            val bitmapX = screenPointToBitmapPoint(focusX, scale, transX)
            val bitmapY = screenPointToBitmapPoint(focusY, scale, transY)

            this.scale = tempScale

            val newFocusX = bitmapPointToScreenPoint(bitmapX, tempScale, transX)
            val newFocusY = bitmapPointToScreenPoint(bitmapY, tempScale, transY)

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

    public fun setImageResource(inputStream: InputStream) {
        this.inputStream = inputStream
        clear()
        post {
            initLoader()
            sendMessage(MSG_SET_IMAGE)
        }
    }

    public fun setImageResource(file: File) {
        setImageResource(FileInputStream(file))
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

    private fun clear() {
        scroller.forceFinished(true)
        zoomer.forceFinished(true)

        loaderHandler?.removeCallbacksAndMessages(null)
        loader?.setLoaderInterface(null)
        loader?.stop()
        loaderThread?.quit()

        loader = null
        loaderThread = null
        transX = 0.0f
        transY = 0.0f
        scale = -1.0f
        minScale = 1.0f
    }

    override fun cellLoaded(cell: Cell) {

        bitmapRectToScreenRect(cell.region, cellInvalidateRect, scale, transX, transY)

        with(cellInvalidateRect) {
            postInvalidate(left, top, right, bottom)
        }
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
                zoomer.forceFinished(true)
                setNewScale(detector.scaleFactor, detector.focusX, detector.focusY)
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
                zoomer.forceFinished(true)

                zoomStartX = e.x
                zoomStartY = e.y
                zoomStartScale = scale
                if (scale * doubleTapStep > maxScale && scale == maxScale) {
                    setScale(minScale, zoomStartX, zoomStartY)
                } else {
                    zoomer.startZoom(doubleTapStep - 1)
                    ViewCompat.postInvalidateOnAnimation(this@LargeImageView)
                }
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
    private var zoomStartX: Float = 0f
    private var zoomStartY: Float = 0f
    private var zoomStartScale: Float = 0f

    private fun fling(
            startX: Int,
            startY: Int,
            velocityX: Int,
            velocityY: Int): Boolean {
        scroller.forceFinished(true)

        scroller.fling(startX,
                startY,
                velocityX,
                velocityY,
                bitmapScreenRect.left, bitmapScreenRect.right,
                bitmapScreenRect.top, bitmapScreenRect.bottom)
        ViewCompat.postInvalidateOnAnimation(this)
        return true
    }

    private fun updateBitmapScreenRect() {
        bitmapRectToScreenRect(loader!!.getWidth(),
                loader!!.getHeight(),
                bitmapScreenRect,
                scale, transX, transY)
        if (bitmapScreenRect.width() < minTouchSize) {
            bitmapScreenRect.left -= minTouchSize / 2
            bitmapScreenRect.right += minTouchSize / 2
        }
        if (bitmapScreenRect.height() < minTouchSize) {
            bitmapScreenRect.top -= minTouchSize / 2
            bitmapScreenRect.bottom += minTouchSize / 2
        }
    }

    private fun hitTest(x: Float, y: Float): Boolean {
        if (loader != null && loader!!.isInitied()) {
            updateBitmapScreenRect()
            return bitmapScreenRect.contains(x.toInt(), y.toInt())
        }
        return false
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
            needsInvalidate = true
        }

        if (zoomer.computeZoom()) {
            setScale(
                    Math.min((zoomer.getCurrZoom() + 1) * zoomStartScale, maxScale),
                    zoomStartX, zoomStartY)
            needsInvalidate = true
        }

        if (needsInvalidate) ViewCompat.postInvalidateOnAnimation(this)
    }

    /////////////////////////////////////////////////////////
    // implemented save state
    ////////////////////////////////////////////////////////

    override fun onSaveInstanceState(): Parcelable {
        val praceable = super.onSaveInstanceState()
        val ss = SavedState(praceable)
        ss.transX = transX
        ss.transY = transY
        ss.scale = scale
        ss.minScale = minScale
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        transX = state.transX
        transY = state.transY
        scale = state.scale
        minScale = state.minScale
    }

    class SavedState: BaseSavedState {
        var transX: Float = 0.0f
        var transY: Float = 0.0f
        var scale: Float = 1.0f
        var minScale: Float = 1.0f

        constructor(parcelable: Parcelable): super(parcelable)

        constructor(parcel: Parcel): super(parcel) {
            transX = parcel.readFloat()
            transY = parcel.readFloat()
            scale = parcel.readFloat()
            minScale = parcel.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(transX)
            out.writeFloat(transY)
            out.writeFloat(scale)
            out.writeFloat(minScale)
        }

        companion object {
            @JvmField val CREATOR: Parcelable.Creator<SavedState>
                    = object: Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
