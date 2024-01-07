package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.services.providers.CachingFavIconLoader
import com.github.gbrowser.settings.GBrowserService
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.ui.search.GBrowserSearchFieldDelegate
import com.github.gbrowser.ui.search.GBrowserSearchPopUpItemImpl
import com.github.gbrowser.ui.search.impl.GBrowserSearchField
import com.intellij.collaboration.ui.HorizontalListPanel
import com.intellij.ide.ui.UISettings
import com.intellij.ide.ui.UISettingsListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.util.preferredHeight
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.ui.InlineIconButton
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.*
import javax.swing.*

@Suppress("UnstableApiUsage")
class GBrowserToolWindowActionBar(private val delegate: GBrowserToolWindowActionBarDelegate) : ComponentAdapter(), Disposable {


  private val favIconLoader: CachingFavIconLoader = service()
  val settings = GBrowserService.instance()

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

  private val bookmarksComponent = HorizontalListPanel(7).apply {
    layout = BoxLayout(this, BoxLayout.X_AXIS)
    preferredHeight = 22


  }
  private val browserComponent = JPanel(null).apply {
    layout = BoxLayout(this, BoxLayout.LINE_AXIS)
  }

  val component: JPanel = panel {
    row {
      cell(browserComponent).align(AlignX.FILL).component
    }
    row {
      cell(bookmarksComponent).align(
        AlignX.LEFT).component.apply { // Override the maximum size to ensure it doesn't stretch to fill the space.
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
      }
    }
  }.apply {
    layout = BoxLayout(this, BoxLayout.Y_AXIS)
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
    actionsLeftComponent = actionToolbarLeft.component
    actionsLeftComponent?.addComponentListener(this as ComponentListener)

  }

  private fun setupBookmarks() {
    settings.addListener { state: GBrowserService.SettingsState ->
      bookmarksComponent.removeAll()
      state.let { currentState ->
        if (currentState.showBookMarksInToolbar) {
          bookmarksComponent.isVisible = true
          initBookMarks(currentState.bookmarks)
        } else {
          bookmarksComponent.isVisible = false
        }
      }
    }
  }

  private fun initBookMarks(bookmarks: MutableSet<GBrowserBookmark>) {
    val iconButtonSet = mutableMapOf<String, InlineIconButton>()
    bookmarks.forEach { bookMark ->
      val url = bookMark.url
      favIconLoader.loadFavIcon(url, targetSize = 18).thenAccept { icon ->
        icon?.let { iconBookMark ->
          iconButtonSet[url] = InlineIconButton(iconBookMark).apply {
            border = JBUI.Borders.empty(1, 5)
            actionListener = ActionListener { delegate.onToolBarIcon(url) }
            toolTipText = url
            addMouseListener(object : MouseAdapter() {
              override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                  createPopupMenu(bookMark, this@apply).show(e.component, e.x, e.y)
                }
              }
            })
          }
        }
      }
    }

    iconButtonSet.values.forEach {
      bookmarksComponent.add(it)
    }
    bookmarksComponent.revalidate()
    bookmarksComponent.repaint()
  }

  private fun createPopupMenu(bookmark: GBrowserBookmark, button: InlineIconButton): JBPopupMenu {
    val popupMenu = JBPopupMenu()
    val removeItem = JMenuItem("Remove Bookmark").apply {
      addActionListener { // Add logic here to remove the bookmark
        settings.removeBookmark(bookmark)
        bookmarksComponent.remove(button)
        bookmarksComponent.revalidate()
        bookmarksComponent.repaint()
      }
    }
    popupMenu.add(removeItem)
    return popupMenu
  }

  private fun setupRightActionBars() {
    val actionsRight = GBrowserToolBarSectionRightAction(browserComponent)
    val actionToolbarRight = createToolBarAction(actionsRight, browserComponent)
    browserComponent.add(actionToolbarRight.component)
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

      override fun onKeyReleased(text: String,
                                 popupItems: (List<GBrowserSearchPopUpItemImpl>, List<GBrowserSearchPopUpItemImpl>, List<GBrowserSearchPopUpItemImpl>) -> Unit) {
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
    initBookMarks(settings.bookmarks)

  }

  override fun dispose() { // Disposal logic
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
