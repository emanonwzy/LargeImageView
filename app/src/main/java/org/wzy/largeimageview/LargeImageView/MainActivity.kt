package org.wzy.largeimageview.LargeImageView

import android.os.*
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import org.wzy.largeimageview.LargeImage.LargeImageView
import org.wzy.largeimageview.R

/**
 * Created by zeyiwu on 26/08/2017.
 */
class MainActivity : AppCompatActivity() {

    var img: LargeImageView? = null
    var left: Button? = null
    var right: Button? = null
    var top: Button? = null
    var bottom: Button? = null
    var zoom_in: Button? = null
    var zoom_out: Button? = null

    inline fun <T: View> T.afterMeasured(crossinline f: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        img = findViewById(R.id.img) as LargeImageView
        left = findViewById(R.id.left) as Button
        right = findViewById(R.id.right) as Button
        top = findViewById(R.id.top) as Button
        bottom = findViewById(R.id.bottom) as Button
        zoom_in = findViewById(R.id.zoom_in) as Button
        zoom_out = findViewById(R.id.zoom_out) as Button

        img?.setImage(assets.open("111.jpg"))

        left?.setOnClickListener {
            img?.setTransXY(-100, 0)
        }

        right?.setOnClickListener {
            img?.setTransXY(100, 0)
        }

        top?.setOnClickListener {
            img?.setTransXY(0, 100)
        }

        bottom?.setOnClickListener {
            img?.setTransXY(0, -100)
        }

        zoom_in?.setOnClickListener {
            img?.setScale(0.2f)
        }

        zoom_out?.setOnClickListener {
            img?.setScale(-0.2f)
        }
    }
}