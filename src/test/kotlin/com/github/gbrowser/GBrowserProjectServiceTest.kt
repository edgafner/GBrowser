package com.github.gbrowser

import com.github.gbrowser.services.DateAsStringSerializer
import com.github.gbrowser.services.GBrowserProjectService
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserTab
import com.intellij.configurationStore.serialize
import com.intellij.openapi.components.service
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.jupiter.api.Assertions
import java.util.*


class GBrowserProjectServiceTest : BasePlatformTestCase() {


    override fun setUp() {
        super.setUp()
    }

    override fun getName(): String {
        return "GBrowserProjectServiceTest"
    }


    @Suppress("unused")
    fun tabSerialization() {
        val project = project
        val projectSettings = project.service<GBrowserProjectService>()

        val date = Date()
        val dateAsString = DateAsStringSerializer.dateFormat.format(date)

        projectSettings.addTab(GBrowserTab(url = "https://dorkag.com/dorkag", name = "Dorkag", createdAt = date))
        val state = projectSettings.getState()
        val element = serialize(state)!!
        val xml = JDOMUtil.write(element)

        Assertions.assertEquals(
            """<state><![CDATA[{
  "tabs": [
    {
      "url": "https://dorkag.com/dorkag",
      "name": "Dorkag",
      "createdAt": "$dateAsString"
    }
  ]
}]]></state>""".trimIndent(), xml
        )
    }


    @Suppress("unused")
    fun multipleTabsSerialization() {
        val project = project
        val projectSettings = project.service<GBrowserProjectService>()

        val date = Date()
        val dateAsString = DateAsStringSerializer.dateFormat.format(date)

        projectSettings.addTabs(
            listOf(
                GBrowserTab(url = "https://google.com", name = "Google", createdAt = date),
                GBrowserTab(url = "https://dorkag.com/dorkag", name = "Dorkag", createdAt = date)
            )
        )
        val state = projectSettings.getState()
        val element = serialize(state)!!
        val xml = JDOMUtil.write(element)

        Assertions.assertEquals(
            """<state><![CDATA[{
  "tabs": [
    {
      "url": "https://google.com",
      "name": "Google",
      "createdAt": "$dateAsString"
    },
    {
      "url": "https://dorkag.com/dorkag",
      "name": "Dorkag",
      "createdAt": "$dateAsString"
    }
  ]
}]]></state>""".trimIndent(), xml
        )
    }
}