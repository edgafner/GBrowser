package com.github.gbrowser

import com.github.gbrowser.services.GBrowserSettings
import com.github.gbrowser.settings.GBrowserBookmarks
import com.intellij.configurationStore.serialize
import com.intellij.openapi.util.JDOMUtil
import io.kotest.core.spec.style.BehaviorSpec
import org.junit.Assert.assertEquals

class GBrowserPersistBookmarksTest : BehaviorSpec() {

  init {
    given("test bookmarks serialize") {
      val service = GBrowserSettings()

      service.addToBookmarks(GBrowserBookmarks(webUrl = "https://www.ynet.co.il/home/0,7340,L-8,00.html"))
      val state = service.getState()
      val element = serialize(state)!!
      val xml = JDOMUtil.write(element)
      then("validate xml") {
        assertEquals("""<State>
  <option name="bookmarks">
    <list>
      <GBrowserBookmarks>
        <option name="webUrl" value="https://www.ynet.co.il/home/0,7340,L-8,00.html" />
      </GBrowserBookmarks>
    </list>
  </option>
</State>
    """.trimIndent(), xml)
      }
    }

    given("test list bookmarks serialize") {
      val service = GBrowserSettings()

      service.addToBookmarks(mutableListOf(GBrowserBookmarks(webUrl = "https://www.ynet.co.il/home/0,7340,L-8,00.html"),
                                           GBrowserBookmarks(webUrl = "https://dorkag.com/dorkag")))
      val state = service.getState()
      val element = serialize(state)!!
      val xml = JDOMUtil.write(element)
      then("validate xml") {
        assertEquals("""<State>
  <option name="bookmarks">
    <list>
      <GBrowserBookmarks>
        <option name="webUrl" value="https://www.ynet.co.il/home/0,7340,L-8,00.html" />
      </GBrowserBookmarks>
      <GBrowserBookmarks>
        <option name="webUrl" value="https://dorkag.com/dorkag" />
      </GBrowserBookmarks>
    </list>
  </option>
</State>
    """.trimIndent(), xml)
      }
    }

    given("test list bookmarks no duplicate serialize") {
      val service = GBrowserSettings()

      service.addToBookmarks(mutableListOf(GBrowserBookmarks(webUrl = "https://www.ynet.co.il/home/0,7340,L-8,00.html"),
                                           GBrowserBookmarks(webUrl = "https://www.ynet.co.il/home/0,7340,L-8,00.html")))
      val state = service.getState()
      val element = serialize(state)!!
      val xml = JDOMUtil.write(element)
      then("validate xml") {
        assertEquals("""<State>
  <option name="bookmarks">
    <list>
      <GBrowserBookmarks>
        <option name="webUrl" value="https://www.ynet.co.il/home/0,7340,L-8,00.html" />
      </GBrowserBookmarks>
    </list>
  </option>
</State>
    """.trimIndent(), xml)
      }
    }

    given("test quick access serialize") {
      val service = GBrowserSettings()

      service.addToQuickAccessBookmarks(GBrowserBookmarks(webUrl = "https://www.ynet.co.il/home/0,7340,L-8,00.html"))
      val state = service.getState()
      val element = serialize(state)!!
      val xml = JDOMUtil.write(element)
      then("validate xml") {
        assertEquals("""<State>
  <option name="quickAccessBookmarks">
    <list>
      <GBrowserBookmarks>
        <option name="webUrl" value="https://www.ynet.co.il/home/0,7340,L-8,00.html" />
      </GBrowserBookmarks>
    </list>
  </option>
</State>
    """.trimIndent(), xml)
      }
    }

    given("test list quick access serialize") {
      val service = GBrowserSettings()

      service.addToQuickAccessBookmarks(mutableListOf(GBrowserBookmarks(webUrl = "https://www.ynet.co.il/home/0,7340,L-8,00.html"),
                                                      GBrowserBookmarks(webUrl = "https://dorkag.com/dorkag")))
      val state = service.getState()
      val element = serialize(state)!!
      val xml = JDOMUtil.write(element)
      then("validate xml") {
        assertEquals("""<State>
  <option name="quickAccessBookmarks">
    <list>
      <GBrowserBookmarks>
        <option name="webUrl" value="https://www.ynet.co.il/home/0,7340,L-8,00.html" />
      </GBrowserBookmarks>
      <GBrowserBookmarks>
        <option name="webUrl" value="https://dorkag.com/dorkag" />
      </GBrowserBookmarks>
    </list>
  </option>
</State>
    """.trimIndent(), xml)
      }
    }

  }

}
