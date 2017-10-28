package org.wzy.largeimageview.LargeImageView

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewTreeObserver
import org.wzy.largeimageview.LargeImage.LargeImageView
import org.wzy.largeimageview.R

/**
 * Created by zeyiwu on 26/08/2017.
 */
class MainActivity : AppCompatActivity() {

    var img: LargeImageView? = null

    inline fun <T: View> T.afterMeasured(crossinline f: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    } else {
                        viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }
                    f()
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        img = findViewById(R.id.img) as LargeImageView

        img?.setImage(assets.open("aaa.jpg"))
    }
}