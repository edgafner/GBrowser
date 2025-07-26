package com.github.gbrowser.settings.project

import com.github.gbrowser.GBrowserIcons
import com.github.gbrowser.i18n.GBrowserBundle
import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.settings.bookmarks.GBrowserBookmarksTableComponent
import com.github.gbrowser.settings.request_header.GBrowserRequestHeaderTableComponent
import com.github.gbrowser.settings.theme.GBrowserTheme
import com.github.gbrowser.util.GBrowserToolWindowUtil
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.UIUtil
import javax.swing.JTextField

class GBrowserProjectSettingsComponent(val project: Project) : SimpleToolWindowPanel(true, true), Disposable {
  private val settings = project.service<GBrowserService>()
  val settingsComponent: DialogPanel by lazy { createComponent() }
  val textField: JTextField by lazy { JTextField() }
  private val bookmarks: GBrowserBookmarksTableComponent by lazy { GBrowserBookmarksTableComponent(project) }
  private val responseHeaders: GBrowserRequestHeaderTableComponent by lazy { GBrowserRequestHeaderTableComponent(project) }


  private fun createComponent(): DialogPanel = panel {
    row {
      icon(GBrowserIcons.GBROWSER_LOGO_LARGER).applyToComponent {
        font = JBFont.label().biggerOn(3.0f).asBold()
      }.align(AlignX.CENTER)
      link("Support GBrowser and help keep it free") {
        BrowserUtil.browse("https://github.com/sponsors/edgafner")
      }
    }.topGap(TopGap.SMALL).bottomGap(BottomGap.SMALL)


    row(GBrowserBundle.message("default.url.field")) {
      cell(textField).columns(COLUMNS_MEDIUM).align(AlignX.FILL).bindText(settings::defaultUrl).validationOnApply {
        if (it.text.isBlank()) {
          ValidationInfo("Please enter a default URL.")
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
    group("Search Settings", true) {
      group("Configuration", false) {
        twoColumnsRow({
                        checkBox("Highlight URL host").bindSelected(settings::isHostHighlight) { value ->
                          settings.isHostHighlight = value
                        }
                      }, {
                        checkBox("Highlight suggestion query").bindSelected(settings::isSuggestionSearchHighlighted) { value ->
                          settings.isSuggestionSearchHighlighted = value
                        }
                      })
        twoColumnsRow({
                        checkBox("Hide URL protocol").bindSelected(settings::isProtocolHidden) { value ->
                          settings.isProtocolHidden = value
                        }
                      }, {
                        checkBox("Load favicon in popup").bindSelected(settings::isFavIconEnabled) { value ->
                          settings.isFavIconEnabled = value
                        }
                      })
        twoColumnsRow({
                        checkBox("Enable suggestion search").bindSelected(settings::isSuggestionSearchEnabled) { value ->
                          settings.isSuggestionSearchEnabled = value
                        }.comment("Suggestion search based on Google engine")
                      }, {
                        checkBox("Life Span in new tab").bindSelected(settings::navigateInNewTab) { value ->
                          settings.navigateInNewTab = value
                        }.comment("When disabled span links will pop up in a dialog")
                      })
      }
      group("Browser History", false) {
        row {
          val historyCheckBox = checkBox("History enables").bindSelected(settings::isHistoryEnabled) { value ->
            settings.isHistoryEnabled = value
          }.gap(RightGap.SMALL)
          spinner(0..60, 1).bindIntValue(settings::historyItemsToKeep) { value ->
            settings.historyItemsToKeep = value
          }.enabledIf(historyCheckBox.selected).gap(RightGap.SMALL)
          comment("Number of history items to persist").enabledIf(historyCheckBox.selected)
        }

      }
    }
  }

  private fun Panel.browserOptions() {
    group("Appearance", true) {
      buttonsGroup {
        row("Theme:") {
          GBrowserTheme.entries.forEach { theme ->
            radioButton(theme.displayName, theme).applyToComponent {
              addActionListener {
                if (isSelected) {
                  // Apply theme immediately when selected
                  com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
                    refreshAllBrowserThemes()
                  }
                }
              }
            }
          }
        }
      }.bind(settings::theme)
    }
    
    group("Developer Options", true) {
      row {
        val debugPortEnable = checkBox("Debug port").bindSelected(settings::isDebugEnabled) { value ->
          settings.isDebugEnabled = value
        }.gap(RightGap.SMALL)
        spinner(-1..9999, 1).bindIntValue(settings::debugPort) { value ->
          settings.debugPort = value
        }.enabledIf(debugPortEnable.selected).gap(RightGap.SMALL)
        icon(AllIcons.General.Warning).enabledIf(debugPortEnable.selected)
        comment("Required an IDE restart").enabledIf(debugPortEnable.selected)
      }
      row {
        comment("Port, which can be used for debugging JavaScript in JCEF components")
      }
      row {
        checkBox("Open DevTools in dialog").bindSelected(settings::isDevToolsInDialog) { value ->
          settings.isDevToolsInDialog = value
        }.comment("When unchecked, DevTools will open in a tool window")
      }
    }


  }

  private fun Panel.toolWindowOptions() {
    group("Tool Window Settings", true) {
      threeColumnsRow({
                        checkBox("Hide toolwindow label").bindSelected(settings::hideIdLabel) { value ->
                          settings.hideIdLabel = value
                        }
                      }, {
                        checkBox("Show tab icon").bindSelected(settings::isTabIconVisible) { value ->
                          settings.isTabIconVisible = value
                        }
                      }, {
                        checkBox("Show bookmarks toolbar").bindSelected(settings::showBookMarksInToolbar) { value ->
                          settings.showBookMarksInToolbar = value
                        }
                      })
      twoColumnsRow({
                      checkBox("Enable drag and drop tabs").bindSelected(settings::isDragAndDropEnabled) { value ->
                        settings.isDragAndDropEnabled = value
                      }.comment("Allow drag and drop for tabs")
                    }, {
                      checkBox("Reload previous tabs").bindSelected(settings::reloadTabOnStartup) { value ->
                        settings.reloadTabOnStartup = value
                      }.comment("Restore tabs when reopening a project")
                    })
    }
  }


  private fun Panel.bookmarksOptions() {
    collapsibleGroup("Bookmark Management", true) {
      row {
        cell(bookmarks.createScrollPane()).comment("Manage your bookmarks").align(Align.FILL).validationOnApply {

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
    collapsibleGroup("HTTP Response Headers", true) {
      row {
        cell(responseHeaders.createScrollPane()).label("Headers", LabelPosition.TOP).comment(
          "Manage your response headers. The overwritten column is used to overwrite the header if it already exists in the request."
        ).align(
          Align.FILL
        )
      }.resizableRow()
    }.apply {

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

    // Refresh all browser themes after settings are applied
    refreshAllBrowserThemes()
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

  private fun refreshAllBrowserThemes() {
    val browsers = GBrowserToolWindowUtil.getAllBrowsers(project)
    thisLogger().info("GBrowser: Refreshing theme for ${browsers.size} browsers")

    if (browsers.isEmpty()) {
      thisLogger().warn("GBrowser: No browsers found to refresh theme")
    }

    browsers.forEach { browser ->
      thisLogger().info("GBrowser: Refreshing theme for browser ${browser.id}")
      browser.refreshTheme()
      // Reload the page to ensure theme changes are applied
      browser.cefBrowser.reload()
    }
  }

  companion object {
    const val BOOKMARKS_OPTIONS_EXPANDED_KEY = "GBrowserToolDialog.bookmarks.expanded"
    const val BOOKMARKS_OPTIONS_EXPANDED_DEFAULT = false
  }


}
