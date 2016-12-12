# Encode/Decode instances for [Circe](http://circe.io)

`Encode` and `Decode` instances can be made available for an itemized ADT as follows.

```scala
import io.rbricks.itemized.circeinstances.verbatim._
```

This results in ADT names being encoded and decoded verbatim, in a case-sensitive manner.

```scala
@enum trait Planet {
  object Mars
  object Venus
}
```

`Planet.Mars` is encoded as "Mars", and "Mars" is decoded to `Planet.Mars`.

If you're using trait mixin to make a single-import custom implicit scope, you can use the provided traits:

```scala
trait MyInstances extends OtherInstances with io.rbricks.itemized.circeinstances.VerbatimCirceInstances
```

## Installation

```
libraryDependencies += "io.rbricks" %% "itemized-circe" % "0.2.0"
