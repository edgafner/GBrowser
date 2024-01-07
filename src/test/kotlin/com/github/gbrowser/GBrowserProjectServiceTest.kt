package com.github.gbrowser

import com.github.gbrowser.services.DateAsStringSerializer
import com.github.gbrowser.settings.GBrowserProjectService
import com.github.gbrowser.ui.toolwindow.gbrowser.GBrowserTab
import com.intellij.configurationStore.serialize
import com.intellij.openapi.components.service
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.junit5.RunInEdt
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.rules.ProjectModelExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.*


@TestApplication
@RunInEdt(writeIntent = true)
class GBrowserProjectServiceTest {
  @JvmField
  @RegisterExtension
  val projectModel: ProjectModelExtension = ProjectModelExtension()

  @Test
  fun `tab serialization`() = runTest {
    val project = projectModel.project
    val projectSettings = project.service<GBrowserProjectService>()

    val date = Date()
    val dateAsString = DateAsStringSerializer.dateFormat.format(date)

    projectSettings.addTab(GBrowserTab(url = "https://dorkag.com/dorkag", name = "Dorkag", createdAt = date))
    val state = projectSettings.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
  "tabs": [
    {
      "url": "https://dorkag.com/dorkag",
      "name": "Dorkag",
      "createdAt": "$dateAsString"
    }
  ]
}]]></state>""".trimIndent(), xml)
  }


  @Test
  fun `multiple tabs serialization`() = runTest {
    val project = projectModel.project
    val projectSettings = project.service<GBrowserProjectService>()

    val date = Date()
    val dateAsString = DateAsStringSerializer.dateFormat.format(date)

    projectSettings.addTabs(listOf(GBrowserTab(url = "https://google.com", name = "Google", createdAt = date),
                                   GBrowserTab(url = "https://dorkag.com/dorkag", name = "Dorkag", createdAt = date)))
    val state = projectSettings.getState()
    val element = serialize(state)!!
    val xml = JDOMUtil.write(element)

    Assertions.assertEquals("""<state><![CDATA[{
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
}]]></state>""".trimIndent(), xml)
  }
}