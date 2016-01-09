import ingredients.caseenum.annotations.enum

import org.scalatest.{ Matchers, WordSpec }

class CaseEnumMacroSpec extends WordSpec with Matchers {
  @enum trait Planet {
    object Mercury
    object Venus
    object Earth
  }

  "@enum annotation" should {
    "produce a valid CaseEnum-style ADT" in {
      Planet.Earth shouldBe a[Product]
      Planet.Earth shouldBe a[Serializable]
      Planet.Earth shouldBe a[Planet]
      Planet.Mercury should not be a [Planet.Earth.type]
      Planet.Earth shouldBe Planet.Earth
    }
  }

}
