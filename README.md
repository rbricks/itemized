# ![rbricks itemized](https://raw.githubusercontent.com/rbricks/rbricks.github.io/master/logo/itemized.png)

Part of [rbricks](http://rbricks.io), a collection of composable, small-footprint libraries for scala.

A convention and typeclass derivation for ADT-based safe enumerations.

[![Build Status](https://travis-ci.org/rbricks/itemized.svg?branch=master)](https://travis-ci.org/rbricks/itemized)

```scala
import io.rbricks.itemized.annotation.enum

@enum trait Planet {
  object Earth
  object Venus
  object Mercury
}
```

A typeclass to convert to and from `String` can be used as follows:

```scala
scala> import io.rbricks.itemized.ItemizedCodec

scala> ItemizedCodec[Planet].fromRep("Earth")
res0: Option[Planet] = Some(Earth)

scala> val planet: Planet = Planet.Earth

scala> import io.rbricks.itemized.ItemizedCodec.ops._

scala> planet.toRep
```

And pattern matching against the sealed hierarchy supports exhaustiveness checking, for added safety.

```scala
scala> (Planet.Earth : Planet) match {
     |   case Planet.Earth => "close"
     |   case Planet.Venus => "far"
     | }
<console>:19: warning: match may not be exhaustive.
It would fail on the following input: Mercury
       (Planet.Earth : Planet) match {
                     ^
res1: String = close
```

## Install

 Artifacts for Scala 2.11 and 2.12 are available on Maven Central.

Add the dependency to your `build.sbt`

```scala
libraryDependencies += "io.rbricks.itemized" %% "itemized" % "0.1.0"
```

To enable the macro paradise plugin (for the `@enum` annotation), also add

```scala
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
```

## Convention and marker trait

The `@enum` annotation builds enumerations that follow the library's convention for how ADT-based enums should be encoded. However, usage of the macro annotation can be avoided by manually writing out the ADT. Here's an example that serves as an informal definition of the convention.

```scala
sealed abstract trait Planet extends io.rbricks.itemized.Itemized
object Planet {
  case object Earth extends Planet
  case object Venus extends Planet
  case object Mercury extends Planet
}
```

This is equivalent to the following (which expands to the same encoding).

```scala
@enum trait Planet {
  object Earth
  object Venus
  object Mercury
}
```

Usage of the `@enum` macro annotations requires the macro paradise plugin to be enabled in your project. Refer to the [Install](#Install) section for how to set it up.

## To and from String

The `ItemizedCodec` typeclass provides operations to convert ADT-based enumerations to and from Strings.

Implemetors of encoding and decoding (serialization) protocols may use it as follows:

```scala
implicit def itemizedJsonEncoding[T <: Itemized](implicit instance: ItemizedCodec[T]) = new JsonEncoding[T] {
  def write(value: T): JsonObject = JsonString(instance.caseToString(value))
  def read(jsonObject: JsonObject) = ... instance.itemizedFromString(str).get
}
```

## Enumerations with an associated value

The `@indexedEnum` annotation builds enumerations that follow the library's convention for ADT-based enums with an associated value.

```
sealed abstract trait Planet extends io.rbricks.itemized.IndexedEnum {
  type Index = Int
}
object Planet {
  case object Earth extends Planet { val index = 1 }
  case object Venus extends Planet { val index = 2 }
  case object Mercury extends Planet { val index = 3 }
}
```

This is equivalent to the following.

```scala
import io.rbricks.itemized.annotation.indexedEnum

@indexedEnum trait Planet {
  type Index = Int
  object Earth   { 1 }
  object Venus   { 2 }
  object Mercury { 3 }
}
```

Usage of the `@indexedEnum` macro annotations requires the macro paradise plugin to be enabled in your project. Refer to the [Install](#Install) section for how to set it up.

## To and from the associated value ("index")

The `ItemizedIndex` typeclass provides operations to convert ADT-based enums to and from their associated values.

```scala
import io.rbricks.itemized.annotation.indexedEnum

@indexedEnum trait Planet {
  type Index = Int
  object Earth   { 1 }
  object Venus   { 2 }
  object Mercury { 3 }
}
```

Examples of usage follow.

```scala
scala> import io.rbricks.itemized.ItemizedIndex

scala> ItemizedIndex[Planet].fromIndex(2)
res0: Option[Planet] = Some(Venus)

scala> Planet.Mercury.index
res1: Int = 3

scala> import io.rbricks.itemized.ItemizedIndex.ops._

scala> val planet: Planet = Planet.Mercury

scala> planet.toIndex
res2: Planet#Index = 3
```

