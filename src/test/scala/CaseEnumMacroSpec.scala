import ingredients.caseenum.annotations.{enum, indexedEnum}

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

class IndexedCaseEnumMacroSpec extends WordSpec with Matchers {
  @indexedEnum trait Planet {
    type Index = Int
    object Mercury { 1 }
    object Venus   { 2 }
    object Earth   { 3 }
  }

  "@indexedEnum annotation" should {
    "produce a valid IndexedCaseEnum-style ADT" in {
      val typecheck: Int = 3: Planet#Index
      Planet.Earth shouldBe a[Product]
      Planet.Earth shouldBe a[Serializable]
      Planet.Earth shouldBe a[Planet]
      Planet.Mercury should not be a [Planet.Earth.type]
      Planet.Earth shouldBe Planet.Earth
      Planet.Earth.index shouldBe 3
    }
  }

}
