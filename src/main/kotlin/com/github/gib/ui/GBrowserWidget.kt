package com.github.gib.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.ComponentContainer
import com.intellij.openapi.util.Disposer
import com.intellij.terminal.TerminalTitle
import com.intellij.util.concurrency.annotations.RequiresEdt
import com.jediterm.core.util.TermSize
import javax.swing.JComponent

interface GBrowserWidget : ComponentContainer {
  val terminalTitle: TerminalTitle

  /**
   * Terminal size in characters according to an underlying UI component;
   * null, if unavailable, e.g. the component is not shown or not laid out yet
   */
  val termSize: TermSize?


  fun hasFocus(): Boolean

  fun requestFocus()

  /**
   * Adds a custom notification component to the top of the terminal.
   */
  fun addNotification(notificationComponent: JComponent, disposable: Disposable)


  @RequiresEdt(generateAssertion = false)
  fun addTerminationCallback(onTerminated: Runnable, parentDisposable: Disposable)
}

fun GBrowserWidget.setNewParentDisposable(newParentDisposable: Disposable) {
  Disposer.register(newParentDisposable, this)
  //val jediTermWidget = JBTerminalWidget.asJediTermWidget(this)
  //if (jediTermWidget != null) {
  //  Disposer.register(newParentDisposable, jediTermWidget)
  //}
}
