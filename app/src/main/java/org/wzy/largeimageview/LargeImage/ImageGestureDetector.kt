package org.wzy.largeimageview.LargeImage

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.VelocityTracker
import android.view.ViewConfiguration

/**
 * Created by zeyiwu on 22/10/2017.
 */
class ImageGestureDetector(ctx: Context) {
    private val INVALID_POINTER_ID = -1
    private var activePointerId = INVALID_POINTER_ID
    private var activePointerIndex = 0

    private var gestureListener: OnGestureListener? = null
    private val scaleDetector: ScaleGestureDetector

    private var lastTouchX: Float = 0.0f
    private var lastTouchY: Float = 0.0f
    private val touchSlop: Int
    private val minimumVelocity: Int

    private var isDragging: Boolean = false
    private var velocityTracker: VelocityTracker? = null

    init {
        val configuration = ViewConfiguration.get(ctx)
        touchSlop = configuration.scaledTouchSlop
        minimumVelocity = configuration.scaledMinimumFlingVelocity

        scaleDetector = ScaleGestureDetector(ctx, ScaleGestureListener())
    }

    inner class ScaleGestureListener : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (java.lang.Float.isNaN(detector.scaleFactor) || java.lang.Float.isInfinite(detector.scaleFactor)) return false
            gestureListener?.onScale(detector.scaleFactor,
                    detector.focusX, detector.focusY)
            return true
        }
    }

    private fun getActiveX(ev: MotionEvent): Float {
        try {
            return ev.getX(activePointerIndex)
        } catch (e: Exception) {
            return ev.x
        }
    }

    private fun getActiveY(ev: MotionEvent): Float {
        try {
            return ev.getY(activePointerIndex)
        } catch (e: Exception) {
            return ev.y
        }
    }

    private fun getPointerIndex(action: Int): Int {
        return (action and MotionEvent.ACTION_POINTER_INDEX_MASK) shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
    }

    fun isScaling() = scaleDetector.isInProgress

    fun isDragging() = isDragging

    fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                lastTouchX = getActiveX(event)
                lastTouchY = getActiveY(event)
                isDragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                val x = getActiveX(event)
                val y = getActiveY(event)
                val dx = x - lastTouchX
                val dy = y - lastTouchY
                if (!isDragging) {
                    isDragging = Math.sqrt(((dx * dx) + (dy * dy)).toDouble()) >= touchSlop
                }

                if (isDragging) {
                    gestureListener?.onDrag(dx, dy)
                    lastTouchX = x
                    lastTouchY = y
                    velocityTracker?.addMovement(event)
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
                velocityTracker?.recycle()
                velocityTracker = null
            }

            MotionEvent.ACTION_UP -> {
                activePointerId = INVALID_POINTER_ID
                if (isDragging) {
                    lastTouchX = getActiveX(event)
                    lastTouchY = getActiveY(event)

                    if (velocityTracker != null) {
                        velocityTracker!!.addMovement(event)
                        velocityTracker!!.computeCurrentVelocity(1000)

                        val vX = velocityTracker!!.xVelocity
                        val vY = velocityTracker!!.yVelocity
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= minimumVelocity) {
                            gestureListener?.onFling(lastTouchX, lastTouchY, -vX, -vY)
                        }
                    }
                }

                velocityTracker?.recycle()
                velocityTracker = null
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = getPointerIndex(event.action)
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    activePointerId = event.getPointerId(newPointerIndex)
                    lastTouchX = event.getX(newPointerIndex)
                    lastTouchY = event.getY(newPointerIndex)
                }
            }
        }
        val pointerId = if (activePointerId != INVALID_POINTER_ID) activePointerId else 0
        activePointerIndex = event.findPointerIndex(pointerId)

        return true
    }

    fun setListener(listener: OnGestureListener) {
        this.gestureListener = listener
    }
}

interface OnGestureListener {
    fun onDrag(dx: Float, dy: Float)
    fun onFling(startX: Float, startY: Float, velocityX: Float, velocityY: Float)
    fun onScale(scaleFactor: Float, focusX: Float, focusY: Float)
}