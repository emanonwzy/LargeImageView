package org.wzy.largeimageview.LargeImageView

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import org.wzy.largeimage.LargeImageView
import org.wzy.largeimageview.R

/**
 * Created by zeyiwu on 26/08/2017.
 */
class MainActivity : AppCompatActivity() {

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
        val img = findViewById<LargeImageView>(R.id.img)
        img.setImageResource(assets.open("b.jpg"))


        findViewById<Button>(R.id.pic1).setOnClickListener {
            img.setImageResource(assets.open("a.jpg"))
        }
        findViewById<Button>(R.id.pic2).setOnClickListener {
            img.setImageResource(assets.open("b.jpg"))
        }
        findViewById<Button>(R.id.pic3).setOnClickListener {
            img.setImageResource(assets.open("c.jpg"))
        }
    }
}