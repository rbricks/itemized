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
    "construct a sensible ItemizedCodec" in {
      val itemizedCodec = ItemizedCodec.itemizedCodecFor[Planet]

      val pairs = List(
        Planet.Mercury -> "Mercury",
        Planet.Venus -> "Venus",
        Planet.Earth -> "Earth")

      for ((co, str) <- pairs) {
        itemizedCodec.toRep(co).shouldBe(str)
        itemizedCodec.fromRep(str).shouldBe(Some(co))
      }
    }

  }

  "ItemizedCodec" should {
    "provide the typeclass instance" in {
      trait FakeJsonSerializer[T] {
        def toString(value: T): String
        def fromString(str: String): Option[T]
      }

      implicit def fakeJsonSerializer[T <: Itemized](implicit instance: ItemizedCodec[T]) = new FakeJsonSerializer[T] {
        def toString(value: T): String = instance.toRep(value)
        def fromString(str: String): Option[T] = instance.fromRep(str)
      }

      implicitly[FakeJsonSerializer[Planet]].fromString("Mercury").shouldBe(Some(Planet.Mercury))
    }
  }
}
