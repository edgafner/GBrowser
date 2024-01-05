package com.github.gbrowser.settings.project

import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.settings.GBrowserSetting
import com.github.gbrowser.settings.bookmarks.GBrowserBookmarksTableComponent
import com.github.gbrowser.settings.dao.GBrowserHistoryDelete
import com.github.gbrowser.settings.request_header.GBrowserRequestHeaderTableComponent
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.UIUtil
import javax.swing.JTextField

class GBrowserProjectSettingsComponent : SimpleToolWindowPanel(true, true), Disposable {
  private val settings = GBrowserSetting.instance()
  val settingsComponent: DialogPanel by lazy { createComponent() }
  val textField: JTextField by lazy { JTextField() }
  private val bookmarks: GBrowserBookmarksTableComponent by lazy { GBrowserBookmarksTableComponent() }
  private val responseHeaders: GBrowserRequestHeaderTableComponent by lazy { GBrowserRequestHeaderTableComponent() }

  private val historyOptions = mutableListOf(GBrowserHistoryDelete(-1, "Delete never"), GBrowserHistoryDelete(0, "Delete on close IDE"),
                                             GBrowserHistoryDelete(24, "Delete after 1 day"),
                                             GBrowserHistoryDelete(24 * 7, "Delete after 7 days"),
                                             GBrowserHistoryDelete(24 * 14, "Delete after 14 days"),
                                             GBrowserHistoryDelete(24 * 30, "Delete after 30 days"),
                                             GBrowserHistoryDelete(24 * 60, "Delete after 60 days"))

  private fun createComponent(): DialogPanel = panel {
    separator()
    row(GBrowserBundle.message("default.url.field")) {
      cell(textField).columns(COLUMNS_MEDIUM).align(AlignX.FILL).bindText(settings::defaultUrl).validationOnApply {
        if (it.text.isBlank()) {
          ValidationInfo("Default URL cannot be empty")
        } else {
          null
        }
      }.component
    }
    searchGroup()
    browserOptions()
    toolWindowOptions()
    bookmarksOptions()
    responseHeaderOptions()

  }

  private fun Panel.searchGroup() {
    group("Search Configuration", true) {
      group("Search Engine", false) {
        twoColumnsRow({
                        checkBox("Enable suggestion search").bindSelected(settings::isSuggestionSearchEnabled) { value ->
                          settings.isSuggestionSearchEnabled = value
                        }
                      }, {
                        checkBox("Highlight URL host").bindSelected(settings::isHostHighlight) { value ->
                          settings.isHostHighlight = value

                        }
                      })
        twoColumnsRow({
                        checkBox("Hide URL protocol").bindSelected(settings::isProtocolHidden) { value ->
                          settings.isProtocolHidden = value
                        }
                      }, {
                        checkBox("Highlight suggestion query").bindSelected(settings::isSuggestionSearchHighlighted) { value ->
                          settings.isSuggestionSearchHighlighted = value
                        }
                      })
        twoColumnsRow({
                        checkBox("Load favicon in popup").bindSelected(settings::isFavIconEnabled) { value ->
                          settings.isFavIconEnabled = value
                        }
                      })
      }
      group("Browser History", false) {
        row {
          val historyCheckBox = checkBox("Auto cleanup").bindSelected(settings::isHistoryEnabled) { value ->
            settings.isHistoryEnabled = value
          }
          comboBox(historyOptions).bindItem(settings::historyDeleteOption) { value ->
            value?.let { settings.historyDeleteOption = it }
          }.enabledIf(historyCheckBox.selected)
        }

      }
    }
  }

  private fun Panel.browserOptions() {
    group("Debug Options", true) {
      row {
        val debugPortEnable = checkBox("Port").bindSelected(settings::isDebugEnabled) { value ->
          settings.isDebugEnabled = value
        }.gap(RightGap.SMALL)
        spinner(-1..9999, 1).bindIntValue(settings::debugPort) { value ->
          settings.debugPort = value
        }.enabledIf(debugPortEnable.selected).gap(RightGap.SMALL)
        icon(AllIcons.General.Warning).enabledIf(debugPortEnable.selected)
        comment("Required an IDE restart").enabledIf(debugPortEnable.selected)
      }
      row {
        comment("Port which can be used for debugging JavaScript in JCEF components")
      }
    }


  }

  private fun Panel.toolWindowOptions() {
    group("GBrowser Toolwindow Appearance", true) {
      twoColumnsRow({
                      checkBox("Hide ToolWindow title").bindSelected(settings::isToolWindowTitleVisible) { value ->
                        settings.isToolWindowTitleVisible = value
                      }
                    }, {
                      checkBox("Enable Tab reorder").bindSelected(settings::isDnDEnabled) { value ->
                        settings.isDnDEnabled = value
                      }
                    })
      twoColumnsRow({
                      checkBox("Show Tab icon").bindSelected(settings::isTabIconVisible) { value ->
                        settings.isTabIconVisible = value
                      }
                    }, {
                      checkBox("Show bookmarks Icons").bindSelected(settings::showBookMarksInToolbar) { value ->
                        settings.showBookMarksInToolbar = value
                      }
                    })
    }
  }


  private fun Panel.bookmarksOptions() {
    collapsibleGroup("Bookmarks", true) {
      row {
        cell(bookmarks.createScrollPane()).comment("Add and Remove bookmarks").align(Align.FILL).validationOnApply {

          return@validationOnApply bookmarks.validate()
        }
      }.resizableRow()
    }.apply {
      border = JBEmptyBorder(UIUtil.getRegularPanelInsets())
      expanded = PropertiesComponent.getInstance().getBoolean(BOOKMARKS_OPTIONS_EXPANDED_KEY, BOOKMARKS_OPTIONS_EXPANDED_DEFAULT)
      addExpandedListener {
        PropertiesComponent.getInstance().setValue(BOOKMARKS_OPTIONS_EXPANDED_KEY, it, BOOKMARKS_OPTIONS_EXPANDED_DEFAULT)
      }
    }.topGap(TopGap.NONE)

  }

  private fun Panel.responseHeaderOptions() {
    collapsibleGroup("Response Headers", true) {
      row {
        cell(responseHeaders.createScrollPane()).label("Headers", LabelPosition.TOP).comment(
          "Add and Remove headers. The overwrite column is used to overwrite the header if it already exists in the request.").align(
          Align.FILL)
      }.resizableRow()
    }.apply {

      // Border is required to have more space - otherwise there could be issues with focus ring.
      // `getRegularPanelInsets()` is used to simplify border calculation for dialogs where this panel is used.
      border = JBEmptyBorder(UIUtil.getRegularPanelInsets())

    }.topGap(TopGap.NONE)
  }

  fun isModified(): Boolean {
    return settingsComponent.isModified() || bookmarks.isModified() || responseHeaders.isModified()
  }

  fun apply() {
    settingsComponent.apply()
    bookmarks.apply()
    responseHeaders.apply()

    //val bus = ApplicationManager.getApplication().messageBus
    //bus.syncPublisher(GBrowserSettingsChangedAction.TOPIC).settingsChanged(settings)
    //settings.addListener(object : GBrowserSetting.Listener {
    //  override fun onSettingsChange(settings: GBrowserSetting) {
    //    bus.syncPublisher(GBrowserSettingsChangedAction.TOPIC).settingsChanged(settings)
    //  }
    //})
    //
    //settings.addListener { state: GBrowserSetting.SettingsState ->
    //  update(state)
    //}
  }

  fun reset() {
    settingsComponent.reset()
    bookmarks.reset()
    responseHeaders.reset()

  }

  override fun dispose() {
    bookmarks.dispose()
    responseHeaders.dispose()

  }

  companion object {
    const val BOOKMARKS_OPTIONS_EXPANDED_KEY = "GBrowserToolDialog.bookmarks.expanded"
    const val BOOKMARKS_OPTIONS_EXPANDED_DEFAULT = false
  }


}
