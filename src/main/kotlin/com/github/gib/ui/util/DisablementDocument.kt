// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.gib.ui.util

import com.github.gib.ui.util.Delegates.observableField
import com.intellij.util.EventDispatcher
import com.intellij.collaboration.ui.SimpleEventListener
import javax.swing.text.PlainDocument
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

class DisablementDocument : PlainDocument() {

  private val eventDispatcher = EventDispatcher.create(SimpleEventListener::class.java)

  var enabled by observableField(true, eventDispatcher)

  fun addAndInvokeEnabledStateListener(listener: () -> Unit) = SimpleEventListener.addAndInvokeListener(eventDispatcher, listener)
}


object Delegates {

  fun <T> observableField(initialValue: T, dispatcher: EventDispatcher<SimpleEventListener>): ObservableProperty<T> {
    return object : ObservableProperty<T>(initialValue) {
      override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = dispatcher.multicaster.eventOccurred()
    }
  }
}