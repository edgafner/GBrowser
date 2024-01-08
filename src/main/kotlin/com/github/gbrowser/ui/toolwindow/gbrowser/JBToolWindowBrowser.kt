package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.services.providers.CachingFavIconLoader
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.settings.dao.GBrowserHistory
import com.github.gbrowser.ui.search.GBrowserSearchPopUpItem
import com.github.gbrowser.util.GBrowserUtil.suggestQuery
import com.intellij.icons.AllIcons.Actions
import com.intellij.icons.AllIcons.General
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

fun getHistoryItemsWidthValue(searchValue: String,
                              history: MutableSet<GBrowserHistory>,
                              favIconLoader: CachingFavIconLoader,
                              isFaviconEnabled: Boolean = true): List<GBrowserSearchPopUpItem> {
  val displayItems = mutableListOf<GBrowserSearchPopUpItem>()
  val defaultIcon = General.Web
  val displayCount = 10

  history.forEach { entry ->
    val url = entry.url
    val isMatch = url.contains(searchValue, ignoreCase = true)
    val isMinCount = displayItems.size <= displayCount

    if (isMatch && isMinCount) {
      val item = GBrowserSearchPopUpItem(searchValue, defaultIcon, null, false, entry.name, url)
      displayItems.add(item)
      if (isFaviconEnabled) {
        favIconLoader.loadFavIcon(url).thenAccept {
          item.icon = it ?: defaultIcon
        }
      }
    }
  }

  return displayItems
}

fun getHBookmarkItemsWidthValue(searchValue: String, bookmarks: MutableSet<GBrowserBookmark>): List<GBrowserSearchPopUpItem> {
  val displayItems = mutableListOf<GBrowserSearchPopUpItem>()

  bookmarks.forEach { item ->
    val url = item.url
    if (url.contains(searchValue, ignoreCase = true)) {
      val name = item.name
      val icon = GBrowserIcons.BOOKMARK_ADD
      displayItems.add(GBrowserSearchPopUpItem(searchValue, icon, null, false, name, url))
    }
  }

  return displayItems
}


fun getSuggestionItems(text: String): List<GBrowserSearchPopUpItem> {
  val displayItems = mutableListOf<GBrowserSearchPopUpItem>()
  val suggest = suggestQuery(text)

  if (suggest.isNotEmpty()) {
    try {
      val jsonElement = Json.parseToJsonElement(suggest)
      val jsonList = jsonElement.jsonArray

      if (jsonList.size >= 2) {
        val suggestionsArray = jsonList[1].jsonArray
        for (suggestion in suggestionsArray) {
          val name = suggestion.jsonPrimitive.content.replace("\"", "")
          val query = name.replace(" ", "+")
          val url = "https://www.google.com/search?q=$query"
          val icon = Actions.Search
          val item = GBrowserSearchPopUpItem(text, icon, null, false, name, url)
          displayItems.add(item)
        }
      }
    } catch (e: Exception) { // Handle exception if needed
    }
  }

  return displayItems
}

