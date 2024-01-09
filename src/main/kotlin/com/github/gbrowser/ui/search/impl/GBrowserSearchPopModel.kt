package com.github.gbrowser.ui.search.impl

import com.github.gbrowser.ui.search.GBrowserSearchPopUpItem
import javax.swing.AbstractListModel

class GBrowserSearchPopModel : AbstractListModel<GBrowserSearchPopUpItem>() {
  private var items: MutableSet<GBrowserSearchPopUpItem> = mutableSetOf()

  override fun getElementAt(index: Int): GBrowserSearchPopUpItem = items.elementAt(index)

  override fun getSize(): Int = minOf(10, items.size)

  fun setItems(popUpItem: List<GBrowserSearchPopUpItem>) {
    items.clear() // Clear the existing items

    // Add all items and separators to the set
    items.addAll(popUpItem)

    fireContentsChanged(this, 0, size - 1)
  }

}
