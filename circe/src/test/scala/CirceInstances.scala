import io.circe._
import io.circe.syntax._
import io.rbricks.itemized.annotation.enum

import utest._

@enum trait Planet {
  object Mars
  object venus
}

object CirceInstancesTests extends TestSuite {
  val tests = this {
    import io.rbricks.itemized.circeinstances.verbatim._

    'encode {
      (Planet.Mars: Planet).asJson ==> Json.fromString("Mars")
      (Planet.venus: Planet).asJson ==> Json.fromString("venus")
    }

    'decode {
      * - { Json.fromString("Mars").as[Planet] ==> Right(Planet.Mars) }
      * - { val res = Json.fromString("mArs").as[Planet]; assertMatch(res) { case Left(_) => } }
      * - { val res = Json.fromString("MARS").as[Planet]; assertMatch(res) { case Left(_) => } }
      * - { val res = Json.fromString("mars").as[Planet]; assertMatch(res) { case Left(_) => } }
      * - { val res = Json.fromString("venus").as[Planet]; assertMatch(res) { case Right(Planet.venus) => }}
      * - { val res = Json.fromString("Venus").as[Planet]; assertMatch(res) { case Left(_) => } }
    }
  }
}
