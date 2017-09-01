package org.wzy.largeimageview.LargeImageView

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.*
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.wzy.largeimageview.LargeImage.Cell
import org.wzy.largeimageview.R

/**
 * Created by zeyiwu on 26/08/2017.
 */
class MainActivity : AppCompatActivity() {

    val cellHeight: Int = 512
    val cellWidth: Int = 256

    private class MyHandler(myLooper: Looper,
                            val cellHeight: Int,
                            val cellWidth: Int,
                            val activity: MainActivity) : Handler(myLooper) {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                1 -> {
                    val decoder = BitmapRegionDecoder.newInstance(activity.assets.open("111.jpg"), true)
                    val rect: Rect = msg.obj as Rect
                    val rows: Int = rect.height() / cellHeight + 1
                    val cols: Int = rect.width() / cellWidth + 1
                    for (i in 0..rows) {
                        for (j in 0..cols) {
                            val cell: Cell = activity.decodeBitmap(Rect(i * cellWidth, j * cellHeight,
                                    i * cellWidth + cellWidth, j * cellHeight + cellHeight), decoder)
                            activity.runOnUiThread { activity.addCell(cell)  }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val thread: HandlerThread = HandlerThread("update")
        thread.start()
        val handler: MyHandler = MyHandler(thread.looper,
                cellHeight,
                cellWidth,
                this)
        val msg : Message = Message.obtain(handler, 1)
        msg.obj = Rect(0, 0, 1024, 1700)
        msg.sendToTarget()
    }

    private fun decodeBitmap(rect: Rect, decoder: BitmapRegionDecoder) : Cell {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        return Cell(decoder.decodeRegion(rect, options), rect)
    }

    private fun addCell(cel: Cell) {
        img.addCell(cel)
    }
}