package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.settings.GBrowserSetting
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.ui.search.GBrowserSearchField
import com.github.gbrowser.ui.search.GBrowserSearchFieldDelegate
import com.github.gbrowser.ui.search.GBrowserSearchPopUpItem
import com.github.gbrowser.util.GBrowserUtil
import com.intellij.collaboration.ui.HorizontalListPanel
import com.intellij.ide.ui.UISettings
import com.intellij.ide.ui.UISettingsListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.ui.InlineIconButton
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionListener
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

@Suppress("UnstableApiUsage")
class GBrowserToolWindowActionBar(private val delegate: GBrowserToolWindowActionBarDelegate, toolWindow: ToolWindow) : ComponentAdapter(),
                                                                                                                       Disposable {

  companion object {
    const val ACTIONS_TOOLBAR_LEFT = "toolBarActionsLeft"
    const val ACTIONS_TOOLBAR_RIGHT = "toolBarActionsRight"
  }

  private var tabCount: Int = toolWindow.contentManager.contentCount

  private val componentBorder: JBEmptyBorder by lazy {
    if (UISettings.getInstance().compactMode) {
      JBUI.Borders.empty(2, 0)
    } else {
      JBUI.Borders.empty(3, 0)
    }
  }
  private val searchInsets: JBInsets by lazy {
    if (UISettings.getInstance().compactMode) {
      JBUI.insets(2, 0)
    } else {
      JBUI.insets(5, 0)
    }
  }
  private val searchConstraints: GridBagConstraints by lazy {
    GridBagConstraints().apply {
      insets = searchInsets
      fill = GridBagConstraints.HORIZONTAL
      weightx = 1.0
      weighty = 1.0
    }
  }

  private val bookmarksComponent = HorizontalListPanel(5)
  private val browserComponent = JPanel().apply {
    layout = BoxLayout(this, BoxLayout.LINE_AXIS)
  }

  val component: JPanel = panel {
    row {
      cell(browserComponent).align(AlignX.FILL).component
    }
    row {
      cell(bookmarksComponent).align(AlignX.FILL).component
    }

  }

  var search: GBrowserSearchField? = null
  private var actionsLeftComponent: JComponent? = null
  private var actionsRightComponent: JComponent? = null
  private var settingsConnection: MessageBusConnection? = null

  init {
    setupLeftActionBars()
    setupSearchField()
    setupRightActionBars()
    setupBookmarks()
    registerSettingsListener()


    val messageBus = ApplicationManager.getApplication().messageBus
    this.settingsConnection = messageBus.connect()
    settingsConnection!!.subscribe(UISettingsListener.TOPIC, UISettingsListener { updateUIForSettings() })

  }

  private fun setupLeftActionBars() {
    val actionsLeft = GBrowserToolBarSectionLeftAction(browserComponent)
    val actionToolbarLeft = createToolBarAction(actionsLeft, browserComponent)

    browserComponent.add(actionToolbarLeft.component)
    replaceRegisteredAction("toolBarActionsLeft" + this.tabCount, actionsLeft)
    actionsLeftComponent = actionToolbarLeft.component
    actionsLeftComponent?.addComponentListener(this as ComponentListener)

  }

  private fun setupBookmarks() {
    val settings = GBrowserSetting.instance()
    bookmarksComponent.removeAll()
    initBookMarks(settings.bookmarks)

    settings.addListener { state: GBrowserSetting.SettingsState ->
      state.let { currentState ->
        bookmarksComponent.removeAll()
        initBookMarks(currentState.bookmarks)
      }
    }
  }

  private fun initBookMarks(bookmarks: MutableSet<GBrowserBookmark>) {

    bookmarks.forEach { bookMark ->
      GBrowserUtil.loadFavIconBGTSmall(bookMark.url) { icon ->
        icon?.let { iconBookMark ->
          bookmarksComponent.add(InlineIconButton(iconBookMark).apply {
            border = JBUI.Borders.empty(1)
            actionListener = ActionListener { delegate.onSearchEnter(bookMark.url) }
          })
        }
      }
    }
  }

  private fun setupRightActionBars() {
    val actionsRight = GBrowserToolBarSectionRightAction(browserComponent)
    val actionToolbarRight = createToolBarAction(actionsRight, browserComponent)
    browserComponent.add(actionToolbarRight.component)

    replaceRegisteredAction("toolBarActionsRight" + this.tabCount, actionsRight)
    this.actionsRightComponent = actionToolbarRight.component
    actionsRightComponent?.addComponentListener(this as ComponentListener)
  }

  private fun setupSearchField() {
    this.search = GBrowserSearchField(object : GBrowserSearchFieldDelegate {
      override fun onEnter(url: String) {
        delegate.onSearchEnter(url)
      }

      override fun onFocus() {
        delegate.onSearchFocus()
      }

      override fun onFocusLost() {
        delegate.onSearchFocusLost()
      }

      override fun onKeyReleased(text: String, popupItems: (List<GBrowserSearchPopUpItem>?) -> Unit) {
        delegate.onKeyReleased(text, popupItems)
      }
    })
    browserComponent.layout = GridBagLayout()
    browserComponent.border = componentBorder
    browserComponent.layout = GridBagLayout()

    search?.let {
      browserComponent.add(it.component, searchConstraints)
    }
  }

  private fun registerSettingsListener() {
    settingsConnection = ApplicationManager.getApplication().messageBus.connect().apply {
      subscribe(UISettingsListener.TOPIC, UISettingsListener {
        updateUIForSettings()
      })
    }
  }

  private fun updateUIForSettings() {
    this.searchConstraints.insets = searchInsets
    this.search?.let {
      browserComponent.remove(it.component)
      browserComponent.add(it.component, searchConstraints, 1)

    }

    browserComponent.border = componentBorder

    bookmarksComponent.removeAll()
    initBookMarks(GBrowserSetting.instance().bookmarks)

  }

  override fun dispose() { // Disposal logic
    unregisterAction("$ACTIONS_TOOLBAR_LEFT$tabCount")
    unregisterAction("$ACTIONS_TOOLBAR_RIGHT$tabCount")
    settingsConnection?.disconnect()
    actionsLeftComponent?.removeComponentListener(this)
    search?.dispose()
    component.removeAll()
  }

  override fun componentShown(e: ComponentEvent?) {
    search?.component?.isVisible = true
    component.border = componentBorder
  }

  override fun componentHidden(e: ComponentEvent?) {
    search?.component?.isVisible = false
    component.border = JBUI.Borders.empty()
  }
}
