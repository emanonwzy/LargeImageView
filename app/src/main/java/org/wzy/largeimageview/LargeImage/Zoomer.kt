package org.wzy.largeimageview.LargeImage

import android.content.Context
import android.os.SystemClock
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator

/**
 * Created by zeyiwu on 30/10/2017.
 */
class Zoomer(ctx: Context) {
    private val interpolator: Interpolator = DecelerateInterpolator()
    private var animationDurationMills: Int = ctx.resources.getInteger(android.R.integer.config_shortAnimTime)
    private var finished: Boolean = true
    private var currentZoom: Float = 0f
    private var startRTC: Long = 0
    private var endZoom: Float = 0f

    fun forceFinished(finished: Boolean) {
        this.finished = finished
    }

    fun abortAnimation() {
        finished = true
        currentZoom = endZoom
    }

    /**
     * Starts a zoom from 1.0 to (1.0 + endZoom). That is, to zoom from 100% to 125%, endZoom should
     * by 0.25f
     *
     */
    fun startZoom(endZoom: Float) {
        this.startRTC = SystemClock.elapsedRealtime()
        this.endZoom = endZoom

        this.finished = false
        this.currentZoom = 1f
    }

    fun computeZoom(): Boolean {
        if (finished) {
            return false
        }

        val tRTC = SystemClock.elapsedRealtime() - startRTC
        if (tRTC >= animationDurationMills) {
            finished = true
            currentZoom = endZoom
            return false
        }

        val t = tRTC * 1f / animationDurationMills
        currentZoom = endZoom * interpolator.getInterpolation(t)
        return true
    }

    fun getCurrZoom() = currentZoom
}