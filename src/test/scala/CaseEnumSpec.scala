import ingredients.caseenum._

import org.scalatest.{ Matchers, WordSpec }

class CaseEnumSpec extends WordSpec with Matchers {
  sealed trait Planet extends CaseEnum
  object Planet {
    case object Mercury extends Planet
    case object Venus extends Planet
    case object Earth extends Planet
  }

  "CaseEnumMacro" should {
    "construct a sensible CaseEnumSerialization" in {
      val serialization = CaseEnumSerialization.caseEnumSerialization[Planet]

      val pairs = List(
        Planet.Mercury -> "Mercury",
        Planet.Venus -> "Venus",
        Planet.Earth -> "Earth")

      for ((co, str) <- pairs) {
        serialization.caseToString(co).shouldBe(str)
        serialization.caseFromString(str).shouldBe(Some(co))
      }
    }
  }

  "SerializationSupport" should {
    "provide the typeclass instance" in {
      trait FakeJsonSerializer[T] {
        def toString(value: T): String
        def fromString(str: String): Option[T]
      }

      implicit def fakeJsonSerializer[T <: CaseEnum](implicit instance: CaseEnumSerialization[T]) = new FakeJsonSerializer[T] {
        def toString(value: T): String = instance.caseToString(value)
        def fromString(str: String): Option[T] = instance.caseFromString(str)
      }

      implicitly[FakeJsonSerializer[Planet]].fromString("Mercury").shouldBe(Some(Planet.Mercury))
    }
  }
}
