import ingredients.caseenum._

import org.scalatest.{ Matchers, WordSpec }

class CaseEnumIndexSpec extends WordSpec with Matchers {
  sealed trait Planet extends IndexedCaseEnum { type Index = Int }
  object Planet {
    case object Mercury extends Planet { val index = 1 }
    case object Venus extends Planet { val index = 2 }
    case object Earth extends Planet { val index = 3 }
  }

  "CaseEnumIndexMacro" should {
    "construct a sensible CaseEnumIndex" in {
      val converter = CaseEnumIndex.caseEnumIndex[Planet]

      val pairs = List(
        Planet.Mercury -> 1,
        Planet.Venus -> 2,
        Planet.Earth -> 3)

      for ((co, index) <- pairs) {
        converter.caseToIndex(co).shouldBe(index)
        converter.caseFromIndex(index).shouldBe(Some(co))
      }
    }
  }

  "CaseEnumIndex" should {
    "provide the typeclass instance" in {
      trait FakeBinaryPickler[T] {
        def pickle(c: T)(picklerState: { def writeInt(int: Int) }): Unit
        def unpickle(unpicklerState: { def getInt(): Int }): Option[T]
      }

      implicit def fakeBinaryPickler[T <: IndexedCaseEnum { type Index = Int }](
        implicit instance: CaseEnumIndex[T]) = new FakeBinaryPickler[T] {

        def pickle(c: T)(picklerState: { def writeInt(int: Int) }): Unit = {
          picklerState.writeInt(instance.caseToIndex(c))
        }
        def unpickle(unpicklerState: { def getInt(): Int }): Option[T] = {
          instance.caseFromIndex(unpicklerState.getInt())
        }
      }

      object picklerState {
        var value: Int = 0
        def writeInt(int: Int): Unit = {
          value = int
        }
      }
      val binaryPickler = implicitly[FakeBinaryPickler[Planet]]
      binaryPickler.pickle(Planet.Venus)(picklerState)
      picklerState.value.shouldBe(2)

      object unpicklerState {
        def getInt(): Int = 3
      }
      binaryPickler.unpickle(unpicklerState).shouldBe(Some(Planet.Earth))
    }
  }
}
