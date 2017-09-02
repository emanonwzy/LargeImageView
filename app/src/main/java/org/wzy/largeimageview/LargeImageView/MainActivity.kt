package org.wzy.largeimageview.LargeImageView

import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import kotlinx.android.synthetic.main.activity_main.*
import org.wzy.largeimageview.LargeImage.BitmapLoader
import org.wzy.largeimageview.LargeImage.Cell
import org.wzy.largeimageview.LargeImage.CellLoaderInterface
import org.wzy.largeimageview.R

/**
 * Created by zeyiwu on 26/08/2017.
 */
class MainActivity : AppCompatActivity(), CellLoaderInterface {

    val cellHeight: Int = 512
    val cellWidth: Int = 512
    val cells: MutableList<Cell> = mutableListOf()
    private var myHandler: MyHandler? = null
    private val mainHandler: Handler = Handler()

    private class MyHandler(myLooper: Looper,
                            val fileName: String,
                            val cellHeight: Int,
                            val cellWidth: Int,
                            val activity: MainActivity) : Handler(myLooper) {
        var loader: BitmapLoader? = null

        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                1 -> {
                    loader?.updateDisplayRect(msg.obj as Rect)
                }

                2 -> {
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeStream(activity.assets.open(fileName))
                    Log.d("www", "bitmap w=" + options.outWidth + ", h=" + options.outHeight)

                    val decoder = BitmapRegionDecoder.newInstance(activity.assets.open(fileName), true)

                    loader = BitmapLoader(decoder, 30000, 926, cellWidth, cellHeight)
                    loader?.loaderInterface = activity
                }
            }
        }
    }

    override fun cellLoaded(cell: Cell) {
        mainHandler.post {
            cells.add(cell)
            img.setCells(cells)
        }
    }

    override fun cellRecycled(cell: Cell) {
        mainHandler.post {
            cells.remove(cell)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val thread: HandlerThread = HandlerThread("update")
        thread.start()
        myHandler = MyHandler(thread.looper,
                "aaa.jpg",
                cellHeight,
                cellWidth,
                this)
        val msg = Message.obtain(myHandler, 2)
        msg.sendToTarget()

        img.afterMeasured {
            updateImg(img.getDisplayRect())
        }

        left.setOnClickListener {
            img.setTransXY(-100, 0)
            updateImg(img.getDisplayRect())
        }

        right.setOnClickListener {
            img.setTransXY(100, 0)
            updateImg(img.getDisplayRect())
        }

        top.setOnClickListener {
            img.setTransXY(0, 100)
            updateImg(img.getDisplayRect())
        }

        bottom.setOnClickListener {
            img.setTransXY(0, -100)
            updateImg(img.getDisplayRect())
        }

        zoom_in.setOnClickListener {
            img.setScale(0.2f)
            updateImg(img.getDisplayRect())
        }

        zoom_out.setOnClickListener {
            img.setScale(-0.2f)
            updateImg(img.getDisplayRect())
        }
    }

    private fun updateImg(rect: Rect) {
        val msg = Message.obtain(myHandler, 1)
        msg.obj = rect
        msg.sendToTarget()
    }

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
}