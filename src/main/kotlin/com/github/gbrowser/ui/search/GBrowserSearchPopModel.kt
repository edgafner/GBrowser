package com.github.gbrowser.ui.search


import javax.swing.AbstractListModel

class GBrowserSearchPopModel : AbstractListModel<GBrowserSearchPopUpItem>() {
  private var items: MutableSet<GBrowserSearchPopUpItem> = mutableSetOf()

  override fun getElementAt(index: Int): GBrowserSearchPopUpItem = items.elementAt(index)

  override fun getSize(): Int = minOf(15, items.size)

  fun setItems(aList: List<GBrowserSearchPopUpItem>?) {
    aList?.let {
      items = it.toMutableSet()
      fireContentsChanged(this, 0, items.size)
    }
  }
}
