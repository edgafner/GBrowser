package com.github.gbrowser.uitl

import com.intellij.openapi.components.ComponentManager


suspend inline fun <reified T : Any> ComponentManager.serviceAsync(): T {
  return (this as ComponentManagerEx).getServiceAsync(T::class.java)
}

interface ComponentManagerEx {
  suspend fun <T : Any> getServiceAsync(keyClass: Class<T>): T {
    throw AbstractMethodError()
  }

}