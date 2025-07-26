package com.github.gbrowser.actions

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.actions.bookmark.GBrowserBookmarkGroupAction
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.Separator
import org.jetbrains.annotations.NonNls

object GBrowserActionId {

  const val GBROWSER_ADD_TAB_ID: @NonNls String = "GBrowserAddTabAction"
  private const val GBROWSER_BACKWARD_ID: @NonNls String = "GBrowserBackwardAction"
  const val GBROWSER_BOOKMARK_ADD_ID: @NonNls String = "GBrowserBookmarkAddAction"
  private const val GBROWSER_BOOKMARK_MANAGER_ID: @NonNls String = "GBrowserBookmarkManagerAction"
  private const val GBROWSER_CLEAN_COOKIES_ID: @NonNls String = "GBrowserCookieDeleteAllAction"
  private const val GBROWSER_CLEAR_HISTORY_ID: @NonNls String = "GBrowserClearHistoryAction"
  private const val GBROWSER_CLOSE_TAB_ID: @NonNls String = "GBrowserCloseTabAction"
  private const val GBROWSER_DEVTOOLS_ID: @NonNls String = "GBrowserDevToolsAction"
  private const val GBROWSER_DUPLICATE_TAB_ID: @NonNls String = "GBrowserTabDuplicateAction"
  private const val GBROWSER_FIND_ID: @NonNls String = "GBrowserFindAction"
  private const val GBROWSER_FORWARD_ID: @NonNls String = "GBrowserForwardAction"
  private const val GBROWSER_HOME_ID: @NonNls String = "GBrowserHomeAction"
  private const val GBROWSER_OPEN_IN_ID: @NonNls String = "GBrowserOpenInAction"
  private const val GBROWSER_PREFERENCES_ID: @NonNls String = "GBrowserPreferencesAction"
  private const val GBROWSER_REFRESH_ID: @NonNls String = "GBrowserRefreshAction"
  private const val GBROWSER_STOP_LOAD_ID: @NonNls String = "GBrowserStopLoadAction"
  private const val GBROWSER_TOGGLE_TOOLBAR_ID: @NonNls String = "GBrowserToggleToolbarAction"
  private const val GBROWSER_ZOOM_IN_ID: @NonNls String = "GBrowserZoomInAction"
  private const val GBROWSER_ZOOM_OUT_ID: @NonNls String = "GBrowserZoomOutAction"
  private const val GBROWSER_ZOOM_RESET_ID: @NonNls String = "GBrowserZoomResetAction"
  private const val GBROWSER_MOBILE_TOGGLE_ID: @NonNls String = "GBrowserMobileToggleAction"


  fun toAction(id: String): AnAction = ActionManager.getInstance().getAction(id)

  fun titleActions(): MutableList<AnAction> = mutableListOf(toAction(GBROWSER_ADD_TAB_ID), toAction(GBROWSER_TOGGLE_TOOLBAR_ID))

  fun allActions(): MutableList<AnAction> {
    val titleActions = mutableListOf<AnAction>()

    titleActions.addAll(BROWSER)
    titleActions.add(Separator.create())
    titleActions.add(Separator.create())
    val cleanGroup = GBrowserDynamicGroupAction(CLEAR_BROWSER_DATA, GBrowserIcons.COOKIES, "Clear Cookies and History")
    titleActions.add(cleanGroup)
    titleActions.add(Separator.create())
    titleActions.addAll(BOOKMARK)
    titleActions.add(GBrowserBookmarkGroupAction())
    titleActions.add(Separator.create())
    titleActions.addAll(TABS)
    titleActions.add(Separator.create())

    titleActions.addAll(ZOOM)
    titleActions.add(Separator.create())
    titleActions.add(toAction(GBROWSER_PREFERENCES_ID))
    return titleActions
  }

  private val BOOKMARK: List<AnAction> = listOf(GBROWSER_BOOKMARK_ADD_ID, GBROWSER_BOOKMARK_MANAGER_ID).map {
    ActionManager.getInstance().getAction(it)
  }

  private val ZOOM: List<AnAction> = listOf(GBROWSER_ZOOM_IN_ID, GBROWSER_ZOOM_OUT_ID, GBROWSER_ZOOM_RESET_ID).map {
    ActionManager.getInstance().getAction(it)
  }

  private val TABS: List<AnAction> = listOf(GBROWSER_ADD_TAB_ID, GBROWSER_CLOSE_TAB_ID, GBROWSER_DUPLICATE_TAB_ID).map {
    ActionManager.getInstance().getAction(it)
  }

  private val BROWSER: List<AnAction> =
    listOf(GBROWSER_HOME_ID, GBROWSER_FIND_ID, GBROWSER_DEVTOOLS_ID, GBROWSER_MOBILE_TOGGLE_ID, GBROWSER_OPEN_IN_ID, GBROWSER_REFRESH_ID, GBROWSER_STOP_LOAD_ID).map {
      ActionManager.getInstance().getAction(it)
    }

  val LEFT: List<AnAction> = listOf(GBROWSER_BACKWARD_ID, GBROWSER_FORWARD_ID, GBROWSER_HOME_ID, GBROWSER_REFRESH_ID).map {
    ActionManager.getInstance().getAction(it)
  }

  private val CLEAR_BROWSER_DATA: List<AnAction> = listOf(GBROWSER_CLEAN_COOKIES_ID, GBROWSER_CLEAR_HISTORY_ID).map {
    ActionManager.getInstance().getAction(it)
  }

}