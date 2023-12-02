package com.github.gbrowser.ui.toolwindow.model

import com.github.gbrowser.ui.toolwindow.base.GBrowserTabViewModel
import com.intellij.util.ui.JBImageIcon
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import javax.swing.Icon

sealed interface GBrowserToolWindowTabViewModel : GBrowserTabViewModel {
  suspend fun destroy()

  class NewBrowserTab internal constructor(url: String) : GBrowserToolWindowTabViewModel {

    override val displayName: String
      get() = UUID.randomUUID().toString()

    override val icon: Icon by lazy {
      JBImageIcon(ImageIO.read(URL("https://www.google.com/s2/favicons?domain=${url}")))
    }


    private val _focusRequests = Channel<Unit>(1)
    internal val focusRequests: Flow<Unit> = _focusRequests.receiveAsFlow()

    fun requestFocus() {
      _focusRequests.trySend(Unit)
    }

    override suspend fun destroy() = Unit

  }
}