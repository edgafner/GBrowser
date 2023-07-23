package com.github.gib

import com.github.gib.services.GivServiceSettings
import com.github.gib.settings.FavoritesWeb
import com.intellij.configurationStore.serialize
import com.intellij.openapi.util.JDOMUtil
import io.kotest.core.spec.style.BehaviorSpec
import org.junit.Assert.assertEquals

class GIBPersistFavoritesTest : BehaviorSpec() {

  init {
    given("test serialize") {
      val service = GivServiceSettings()

      service.addFavorite(
        FavoritesWeb(webUrl = "https://www.ynet.co.il/home/0,7340,L-8,00.html")
      )
      val state = service.getState()
      val element = serialize(state)!!
      val xml = JDOMUtil.write(element)
      then("validate xml") {
        assertEquals("""<State>
  <option name="favorites">
    <list>
      <FavoritesWeb>
        <option name="webUrl" value="https://www.ynet.co.il/home/0,7340,L-8,00.html" />
      </FavoritesWeb>
    </list>
  </option>
</State>
    """.trimIndent(), xml)
      }
    }

  }

}
