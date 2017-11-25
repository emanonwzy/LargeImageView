package io.github.emanonwzy

/**
 * Created by zeyiwu on 01/09/2017.
 */
interface CellLoaderInterface {
    fun onCellInit(width: Int, height: Int)
    fun onCellLoaded(cell: Cell)
}