package com.github.gib.gcef

import com.github.gib.SettingsChangedAction
import com.github.gib.services.GivServiceSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.handler.CefContextMenuHandlerAdapter
import java.awt.BorderLayout
import java.awt.Frame
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JDialog
import javax.swing.WindowConstants

class GBCefBrowser(url: String) : JBCefBrowser(url) {

    private var myDevtoolsFrame: JDialog? = null

    init {
        getJBCefClient().addContextMenuHandler(DefaultCefContextMenuHandler(), this.cefBrowser)
    }

    override fun openDevtools() {
        if (myDevtoolsFrame != null) {
            myDevtoolsFrame!!.toFront()
            return
        }
        val activeFrame = getActiveFrame() ?: return
        val bounds = activeFrame.graphicsConfiguration.bounds
        myDevtoolsFrame = JDialog(activeFrame)
        myDevtoolsFrame!!.title = "JCEF DevTools"
        myDevtoolsFrame!!.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        myDevtoolsFrame!!.setBounds(bounds.width / 4 + 100,
            bounds.height / 4 + 100,
            bounds.width / 2,
            bounds.height / 2)
        myDevtoolsFrame!!.layout = BorderLayout()
        val devTools = JBCefBrowser(getCefBrowser().devTools, getJBCefClient())
        myDevtoolsFrame!!.add(devTools.component, BorderLayout.CENTER)
        myDevtoolsFrame!!.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent) {
                myDevtoolsFrame = null
                Disposer.dispose(devTools)
            }
        })
        myDevtoolsFrame!!.isVisible = true


    }

    private fun getActiveFrame(): Window? {
        for (frame in Frame.getFrames()) {
            if (frame.isActive) return frame
        }
        return null
    }


    class DefaultCefContextMenuHandler : CefContextMenuHandlerAdapter() {
        override fun onBeforeContextMenu(browser: CefBrowser,
                                         frame: CefFrame,
                                         params: CefContextMenuParams,
                                         model: CefMenuModel) {

            model.addItem(28501, "Add to Bookmarks")
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
            return false
        }

        private fun addToBookmarks(browser: CefBrowser) {
            GivServiceSettings.instance().addFavorite(browser.url)
            val bus = ApplicationManager.getApplication().messageBus
            bus.syncPublisher(SettingsChangedAction.TOPIC).settingsChanged()
        }
    }


}
