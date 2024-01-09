package com.github.gbrowser.ui.toolwindow.gbrowser

import com.github.gbrowser.actions.GBrowserActionId
import com.github.gbrowser.actions.GBrowserDynamicGroupAction
import com.github.gbrowser.services.providers.CachingFavIconLoader
import com.github.gbrowser.services.GBrowserService
import com.github.gbrowser.settings.bookmarks.GBrowserBookmark
import com.github.gbrowser.ui.search.impl.GBrowserSearchTextField
import com.intellij.collaboration.ui.HorizontalListPanel
import com.intellij.icons.AllIcons
import com.intellij.ide.ui.UISettingsListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.openapi.util.Disposer
import com.intellij.ui.util.preferredHeight
import com.intellij.util.ui.InlineIconButton
import com.intellij.util.ui.JBUI
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.event.*
import javax.swing.BoxLayout
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.SwingUtilities

@Suppress("UnstableApiUsage")
class GBrowserToolWindowActionBar(private val delegate: GBrowserToolWindowActionBarDelegate) : ComponentAdapter(), Disposable {
  private val settings = GBrowserService.instance()
  private val favIconLoader: CachingFavIconLoader = service()
  private val rightActionGroup = DefaultActionGroup()
  private val leftActionGroup = DefaultActionGroup()
  private lateinit var leftToolActionBarComponent: ActionToolbar
  private lateinit var rightToolActionBarComponent: ActionToolbar
  val disposable: Disposable = Disposer.newDisposable(this)
  private val bookmarksComponent = HorizontalListPanel(2).apply {
    layout = BoxLayout(this, BoxLayout.X_AXIS)
    preferredHeight = 22
    border = JBUI.Borders.empty(2)
  }

  lateinit var search: GBrowserSearchTextField

  private val browserComponent by lazy {
    JPanel().apply { //layout = MigLayout(LC().gridGap("2", "0").insets("0").fillX().hideMode(3))
      layout = MigLayout(LC().fill().flowY().gridGap("4", "4") // Increased gap between components
                           .insets("2", "2", "2", "2").hideMode(3))
      border = JBUI.Borders.empty(2)

      leftToolActionBarComponent = createToolBarAction(leftActionGroup)
      add(leftToolActionBarComponent.component, CC().wrap().dockWest())

      search = GBrowserSearchTextField(delegate)
      add(search, CC().growX().minWidth("0"))

      rightToolActionBarComponent = createToolBarAction(rightActionGroup)
      add(rightToolActionBarComponent.component, CC().wrap().dockEast())
    }
  }


  init {

    leftActionGroup.addAll(GBrowserActionId.LEFT)
    rightActionGroup.add(GBrowserActionId.toAction(GBrowserActionId.GBROWSER_BOOKMARK_ADD_ID))
    val groupAll = GBrowserDynamicGroupAction(GBrowserActionId.allActions(), AllIcons.General.ArrowDown, "More Options")
    rightActionGroup.add(groupAll)
    setupBookmarks()
    ApplicationManager.getApplication().messageBus.connect().subscribe(UISettingsListener.TOPIC,
                                                                       UISettingsListener { updateUIForSettings() })

  }

  val mainToolBarComponent: JPanel = JPanel(BorderLayout()).apply {
    add(browserComponent, BorderLayout.NORTH)
    add(bookmarksComponent, BorderLayout.SOUTH)
  }


  private fun setupBookmarks() {
    initBookMarks(settings.bookmarks)
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


  private fun updateUIForSettings() {
    leftToolActionBarComponent.component.revalidate()
    leftToolActionBarComponent.component.repaint()
    rightToolActionBarComponent.component.revalidate()
    rightToolActionBarComponent.component.repaint()
    mainToolBarComponent.repaint()
  }

  override fun dispose() { // Disposal logic
    mainToolBarComponent.removeAll()
  }

  override fun componentShown(e: ComponentEvent?) {
    mainToolBarComponent.isVisible = true
  }

  override fun componentHidden(e: ComponentEvent?) {
    mainToolBarComponent.isVisible = false
  }

  private fun createToolBarAction(actionGroup: ActionGroup): ActionToolbar {
    val actionManager = ActionManager.getInstance()
    val actionToolbar = actionManager.createActionToolbar("ToolwindowToolbar", actionGroup, true)
    actionToolbar.layoutPolicy = ActionToolbar.NOWRAP_LAYOUT_POLICY
    actionToolbar.targetComponent = null
    return actionToolbar
  }
}
