# ![rbricks itemized](https://raw.githubusercontent.com/rbricks/rbricks.github.io/master/logo/itemized.png)

A convention and utility functions for ADT-based safe enumerations.

```scala
@enum trait Planet {
  object Earth
  object Venus
  object Mercury
}

scala> implicitly[ItemizedSerialization[Planet]].caseFromString("Earth")
res0: Option[Planet] = Some(Earth)

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

Add the dependency to your `build.sbt`

```scala
libraryDependencies += "io.rbricks.itemized" %% "itemized" % "..."
```

To enable the macro paradise plugin (for the @enum annotation), also add

```scala
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
```

## Convention and marker trait

The `@enum` annotation builds enumerations that follow the library's convention for how ADT-based enums should be encoded. However, usage of the macro annotation can be avoided by manually writing out the ADT. Here's an example that serves as an informal definition of the convention.

```scala
sealed abstract trait Planet
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

Usage of the @enum macro annotations requires the macro paradise plugin to be enabled in your project. Refer to the [Install](#Install) section for how to set it up.

## To and from String

The `ItemizedSerialization` typeclass provides operations to convert ADT-based enumerations to and from strings.

Implemetors of encoding and decoding (serialization) protocols may use it as follows:

```scala
implicit def caseEnumJsonEncoding[T <: Itemized](implicit instance: ItemizedSerialization[T]) = new JsonEncoding[T] {
  def write(value: T): JsonObject = JsonString(instance.caseToString(value))
  def read(jsonObject: JsonObject) = ... instance.caseFromString(str).get
}
```

## Enumerations with an associated value

The `@indexedEnum` annotation builds enumerations that follow the library's convention for ADT-based enums with an associated value.

```
sealed abstract trait Planet extends IndexedEnum {
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
@indexedEnum trait Planet {
  type Index = Int
  object Earth   { 1 }
  object Venus   { 2 }
  object Mercury { 3 }
}
```

Usage of the @indexedEnum macro annotations requires the macro paradise plugin to be enabled in your project. Refer to the [Install](#Install) section for how to set it up.

## To and from the associated value ("index")

The `ItemizedIndex` typeclass provides operations to convert ADT-based enums to and from their associated values.

```scala
@indexedEnum trait Planet {
  type Index = Int
  object Earth   { 1 }
  object Venus   { 2 }
  object Mercury { 3 }
}

scala> implicitly[ItemizedIndex[Planet]].caseFromIndex(2)
res0: Option[Planet] = Some(Venus)

scala> Planet.Mercury.index
res1: Int = 3

scala> implicitly[ItemizedIndex[Planet]].caseToIndex(Planet.Mercury)
res2: Int = 3
```

