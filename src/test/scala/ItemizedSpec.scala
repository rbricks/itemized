import io.rbricks.itemized._

import org.scalatest.{ Matchers, WordSpec }

class ItemizedSpec extends WordSpec with Matchers {
  sealed trait Planet extends Itemized
  object Planet {
    case object Mercury extends Planet
    case object Venus extends Planet
    case object Earth extends Planet
  }

  "ItemizedMacro" should {
    "construct a sensible ItemizedSerialization" in {
      val serialization = ItemizedSerialization.caseEnumSerialization[Planet]

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

      implicit def fakeJsonSerializer[T <: Itemized](implicit instance: ItemizedSerialization[T]) = new FakeJsonSerializer[T] {
        def toString(value: T): String = instance.caseToString(value)
        def fromString(str: String): Option[T] = instance.caseFromString(str)
      }

      implicitly[FakeJsonSerializer[Planet]].fromString("Mercury").shouldBe(Some(Planet.Mercury))
    }
  }
}
