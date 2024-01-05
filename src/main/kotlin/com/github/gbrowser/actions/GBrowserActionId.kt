package com.github.gbrowser.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import org.jetbrains.annotations.NonNls

object GBrowserActionId {

  const val GBROWSER_ADD_TAB_ID: @NonNls String = "GBrowserAddTabAction"
  private const val GBROWSER_BACKWARD_ID: @NonNls String = "GBrowserBackwardAction"
  private const val GBROWSER_BOOKMARK_ADD_ID: @NonNls String = "GBrowserBookmarkAddAction"
  private const val GBROWSER_BOOKMARK_MANAGER_ID: @NonNls String = "GBrowserBookmarkManagerAction"
  private const val GBROWSER_CLEAN_COOKIES_ID: @NonNls String = "GBrowserCookieDeleteAllAction"
  private const val GBROWSER_CLEAR_HISTORY_ID: @NonNls String = "GBrowserClearHistoryAction"
  private const val GBROWSER_CLOSE_TAB_ID: @NonNls String = "GBrowserCloseTabAction"
  private const val GBROWSER_DEVTOOLS_TOOL_WINDOW_ID: @NonNls String = "GBrowserDevToolsToolWindowAction"
  private const val GBROWSER_DEVTOOLS_WINDOW_ID: @NonNls String = "GBrowserDevToolsWindowAction"
  private const val GBROWSER_DUPLICATE_TAB_ID: @NonNls String = "GBrowserTabDuplicateAction"
  private const val GBROWSER_FIND_ID: @NonNls String = "GBrowserFindAction"
  private const val GBROWSER_FORWARD_ID: @NonNls String = "GBrowserForwardAction"
  private const val GBROWSER_HOME_ID: @NonNls String = "GBrowserHomeAction"
  private const val GBROWSER_OPEN_IN_ID: @NonNls String = "GBrowserOpenInAction"
  const val GBROWSER_PREFERENCES_ID: @NonNls String = "GBrowserPreferencesAction"
  private const val GBROWSER_REFRESH_ID: @NonNls String = "GBrowserRefreshAction"
  private const val GBROWSER_STOP_LOAD_ID: @NonNls String = "GBrowserStopLoadAction"
  const val GBROWSER_TOGGLE_TOOLBAR_ID: @NonNls String = "GBrowserToggleToolbarAction"
  private const val GBROWSER_ZOOM_IN_ID: @NonNls String = "GBrowserZoomInAction"
  private const val GBROWSER_ZOOM_OUT_ID: @NonNls String = "GBrowserZoomOutAction"
  private const val GBROWSER_ZOOM_RESET_ID: @NonNls String = "GBrowserZoomResetAction"

  fun toAction(id: String): AnAction = ActionManager.getInstance().getAction(id)

  val BOOKMARK: List<AnAction> = listOf(GBROWSER_BOOKMARK_ADD_ID, GBROWSER_BOOKMARK_MANAGER_ID).map {
    ActionManager.getInstance().getAction(it)
  }

  val ZOOM: List<AnAction> = listOf(GBROWSER_ZOOM_IN_ID, GBROWSER_ZOOM_OUT_ID, GBROWSER_ZOOM_RESET_ID).map {
    ActionManager.getInstance().getAction(it)
  }

  val TABS: List<AnAction> = listOf(GBROWSER_ADD_TAB_ID, GBROWSER_CLOSE_TAB_ID, GBROWSER_DUPLICATE_TAB_ID).map {
    ActionManager.getInstance().getAction(it)
  }

  val BROWSER: List<AnAction> =
    listOf(GBROWSER_HOME_ID, GBROWSER_FIND_ID, GBROWSER_OPEN_IN_ID, GBROWSER_REFRESH_ID, GBROWSER_STOP_LOAD_ID).map {
      ActionManager.getInstance().getAction(it)
    }

  val RIGHT: List<AnAction> = listOf(GBROWSER_DEVTOOLS_TOOL_WINDOW_ID, GBROWSER_BOOKMARK_ADD_ID).map {
    ActionManager.getInstance().getAction(it)
  }
  val LEFT: List<AnAction> = listOf(GBROWSER_BACKWARD_ID, GBROWSER_FORWARD_ID, GBROWSER_REFRESH_ID).map {
    ActionManager.getInstance().getAction(it)
  }

  val CLEAR_BROWSER_DATA: List<AnAction> = listOf(GBROWSER_CLEAN_COOKIES_ID, GBROWSER_CLEAR_HISTORY_ID).map {
    ActionManager.getInstance().getAction(it)
  }

  val DEVTOOLS_GROUP: List<AnAction> = listOf(GBROWSER_DEVTOOLS_TOOL_WINDOW_ID, GBROWSER_DEVTOOLS_WINDOW_ID).map {
    ActionManager.getInstance().getAction(it)
  }
}