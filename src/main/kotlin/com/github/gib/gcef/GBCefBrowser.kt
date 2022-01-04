package com.github.gib.gcef

import com.github.gib.SettingsChangedAction
import com.github.gib.services.GivServiceSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JDialog
import javax.swing.SwingUtilities
import javax.swing.WindowConstants


class GBCefBrowser(url: String?) :
    JBCefBrowser(createBuilder().setOffScreenRendering(false).setEnableOpenDevToolsMenuItem(true).setUrl(url)) {

    private var myDevtoolsFrame: JDialog? = null
    override fun openDevtools() {
        if (myDevtoolsFrame != null) {
            myDevtoolsFrame!!.toFront()
            return
        }
        val comp: Component = component
        val ancestor =
            (SwingUtilities.getWindowAncestor(
                comp))
                ?: return
        val bounds = ancestor.graphicsConfiguration.bounds
        myDevtoolsFrame = JDialog(ancestor)
        myDevtoolsFrame!!.title = "JCEF DevTools"
        myDevtoolsFrame!!.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        myDevtoolsFrame!!.setBounds(bounds.width / 4 + 100,
            bounds.height / 4 + 100,
            bounds.width / 2,
            bounds.height / 2)
        myDevtoolsFrame!!.layout = BorderLayout()
        val devTools =
            createBuilder().setCefBrowser(cefBrowser.devTools).setClient(jbCefClient).createBrowser()
        myDevtoolsFrame!!.add(devTools.component, BorderLayout.CENTER)
        myDevtoolsFrame!!.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) {
                myDevtoolsFrame = null

            }
        })
        myDevtoolsFrame!!.isVisible = true
    }

    override fun createDefaultContextMenuHandler(): DefaultCefContextMenuHandler {
        return object : DefaultCefContextMenuHandler(true) {
            override fun onBeforeContextMenu(browser: CefBrowser,
                                             frame: CefFrame,
                                             params: CefContextMenuParams,
                                             model: CefMenuModel) {
                model.addItem(28501, "Add to Bookmarks")
                super.onBeforeContextMenu(browser, frame, params, model)
            }

            override fun onContextMenuCommand(browser: CefBrowser,
                                              frame: CefFrame,
                                              params: CefContextMenuParams,
                                              commandId: Int,
                                              eventFlags: Int): Boolean {
                if (commandId == 28501) {
                    addToBookmarks(browser)
                    return true
                }
                return super.onContextMenuCommand(browser, frame, params, commandId, eventFlags)
            }

            private fun addToBookmarks(browser: CefBrowser) {
                GivServiceSettings.instance().addFavorite(browser.url)
                val bus = ApplicationManager.getApplication().messageBus
                bus.syncPublisher(SettingsChangedAction.TOPIC).settingsChanged()
            }
        }
    }


}
