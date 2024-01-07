package com.github.gbrowser.ui.search.impl


import com.github.gbrowser.ui.search.GBrowserSearchPopUpItem
import com.github.gbrowser.ui.search.GBrowserSearchPopUpItemImpl
import com.github.gbrowser.ui.search.GBrowserSearchPopUpItemSeparator
import javax.swing.AbstractListModel

class GBrowserSearchPopModel : AbstractListModel<GBrowserSearchPopUpItem>() {
  private var items: MutableSet<GBrowserSearchPopUpItem> = mutableSetOf()

  override fun getElementAt(index: Int): GBrowserSearchPopUpItem = items.elementAt(index)

  override fun getSize(): Int = minOf(15, items.size)

  fun setItems(historyList: List<GBrowserSearchPopUpItemImpl>,
               bookmarks: List<GBrowserSearchPopUpItemImpl>,
               suggested: List<GBrowserSearchPopUpItemImpl>) {
    items.clear() // Clear the existing items

    // Add all items and separators to the set
    items.addAll(historyList)
    if (historyList.isNotEmpty()) {
      items.add(GBrowserSearchPopUpItemSeparator())
    }
    items.addAll(bookmarks)
    if (bookmarks.isNotEmpty()) {
      items.add(GBrowserSearchPopUpItemSeparator())
    }
    items.addAll(suggested)

    fireContentsChanged(this, 0, size -1)
  }

}
