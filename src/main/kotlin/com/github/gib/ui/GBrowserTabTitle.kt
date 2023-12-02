package com.github.gib.ui

import com.intellij.execution.ExecutionBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.jediterm.terminal.Terminal
import com.jediterm.terminal.model.TerminalApplicationTitleListener
import org.jetbrains.annotations.Nls
import java.util.concurrent.CopyOnWriteArrayList

interface GBrowserTabTitleTitleListener {
  /**
   * Can be called from any thread (not only from EDT)
   */
  fun onTitleChanged(terminalTitle: GBrowserTabTitle)
}

class GBrowserTabTitle {
  private val listeners = CopyOnWriteArrayList<GBrowserTabTitleTitleListener>()
  private var state = State()

  fun change(block: State.() -> Unit) {
    val newState = state.copy()
    newState.block()
    if (newState != state) {
      state = newState
      fireTitleChanged()
    }
  }

  val userDefinedTitle: @Nls String?
    get() = state.userDefinedTitle

  val applicationTitle: @Nls String?
    get() = state.applicationTitle

  val tag: @Nls String?
    get() = state.tag

  val defaultTitle: @Nls String?
    get() = state.defaultTitle

  fun addTitleListener(listener: GBrowserTabTitleTitleListener, parentDisposable: Disposable) {
    addTitleListener(listener)
    if (!Disposer.tryRegister(parentDisposable) { removeTitleListener(listener) }) {
      removeTitleListener(listener)
    }
  }

  private fun addTitleListener(GBrowserTabTitleTitleListener: GBrowserTabTitleTitleListener) {
    listeners.add(GBrowserTabTitleTitleListener)
  }

  private fun removeTitleListener(GBrowserTabTitleTitleListener: GBrowserTabTitleTitleListener) {
    listeners.remove(GBrowserTabTitleTitleListener)
  }

  fun buildTitle(): @Nls String {
    val title = userDefinedTitle ?: shortenApplicationTitle() ?: defaultTitle ?: ExecutionBundle.message("terminal.default.title")
    return if (tag != null) "$title ($tag)" else title
  }

  fun buildFullTitle(): @Nls String {
    return userDefinedTitle ?: applicationTitle ?: defaultTitle ?: ExecutionBundle.message("terminal.default.title")
  }

  private fun shortenApplicationTitle(): String? {
    return StringUtil.trimMiddle(applicationTitle ?: return null, 30)
  }

  private fun fireTitleChanged() {
    listeners.forEach {
      it.onTitleChanged(this)
    }
  }

  data class State(var userDefinedTitle: @Nls String? = null,
                   var applicationTitle: @Nls String? = null,
                   var tag: @Nls String? = null,
                   var defaultTitle: @Nls String? = null)
}

fun GBrowserTabTitle.bindApplicationTitle(terminal: Terminal, parentDisposable: Disposable) {
  val listener = TerminalApplicationTitleListener { newApplicationTitle ->
    if (AdvancedSettings.getBoolean("terminal.show.application.title")) {
      change {
        applicationTitle = newApplicationTitle
      }
    }
  }
  terminal.addApplicationTitleListener(listener)
  Disposer.register(parentDisposable) {
    terminal.removeApplicationTitleListener(listener)
  }
}
