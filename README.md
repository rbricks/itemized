# ![rbricks itemized](https://raw.githubusercontent.com/rbricks/rbricks.github.io/master/logo/itemized.png)

A small library (<300 loc) that provides macros and typeclasses for enums encoded as `sealed trait` hierarchies.

Part of [rbricks](http://rbricks.io), a collection of composable, small-footprint libraries for scala.

[![Build Status](https://travis-ci.org/rbricks/itemized.svg?branch=master)](https://travis-ci.org/rbricks/itemized) [![Maven Central](https://img.shields.io/maven-central/v/io.rbricks/itemized_2.12.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.rbricks%22%20a%3A%22itemized_2.12%22) [![Changelog](https://img.shields.io/badge/changelog-0.2.0-lightgrey.svg)](#changelog)

## Features

1. (Optional) _macro annotations_ for compact "enum" syntax.

  ```scala
  import io.rbricks.itemized.annotation.enum

  @enum trait Planet {
    object Earth
    object Venus
    object Mercury
  }
  ```

  This expands to:

  ```scala
  sealed trait Planet extends io.rbricks.itemized.Itemized
  object Planet {
    case object Earth extends Planet
    case object Venus extends Planet
    case object Mercury extends Planet
  }
  ```

2. _Typeclass instances_ to convert to and from `String` are automatically derived:

  ```scala
  scala> import io.rbricks.itemized.ItemizedCodec

  scala> ItemizedCodec[Planet].fromRep("Earth")
  res0: Option[Planet] = Some(Earth)

  scala> val planet: Planet = Planet.Earth

  scala> import io.rbricks.itemized.ItemizedCodec.ops._

  scala> planet.toRep
  ```

3. Pattern matching against the sealed hierarchy supports _exhaustiveness checking_, for added safety.

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

4. Support for serialization libraries, currently (click on links for usage/installation instructions):

  * [circe (JSON)](circe/README.md)

## Install

 Artifacts for Scala 2.11 and 2.12 are available on Maven Central.

Add the dependency to your `build.sbt`

```scala
libraryDependencies += "io.rbricks" %% "itemized" % "0.2.0"
```

To enable the macro paradise plugin (for the `@enum` annotation), also add

```scala
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
```

## Convention and marker trait

The `@enum` annotation builds enumerations that follow the library's convention for how ADT-based enums should be encoded. However, usage of the macro annotation can be avoided by manually writing out the ADT. Here's an example that serves as an informal definition of the convention.

```scala
sealed trait Planet extends io.rbricks.itemized.Itemized
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

Additionally, `ItemizedCodec[T]` exposes the `Map[String, T]` as `.stringMap`, for other use-cases (such as retrieving the set of admissible elements or strings).

## Enumerations with an associated value

The `@indexedEnum` annotation builds enumerations that follow the library's convention for ADT-based enums with an associated value.

```
sealed trait Planet extends io.rbricks.itemized.IndexedEnum {
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

Additionally, `ItemizedIndex[T]` exposes the `Map[T#Index, T]` as `.indexMap`, for other use-cases (such as retrieving the set of admissible elements or indices).

## Changelog

### 0.2.0

  * [circe](circe/README.md) encoders and decoders (@utaal, 6ac3e9a)
  * More info in macro errors (@esarbe, 0de55b3)
  * `ItemizedCodec.stringMap` and `ItemizedIndex.indexMap` retrieve the underlying string<->object or index<->object mappings (@esarbe, 25013c4)

