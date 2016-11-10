package io.rbricks.itemized

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

/**
 * Marker trait for ADTs representing enumerations
 *
 * The convention requires the following structure for the enumeration:
 * ```
 * sealed trait EnumName extends Itemized
 * object EnumName {
 *   case object Element1 extends EnumName
 *   case object Element2 extends EnumName
 * }
 * ```
 */
trait Itemized extends Product with Serializable

/**
 * Typeclass with conversions to and from strings for ADTs representing enumerations
 */
trait ItemizedSerialization[T <: Itemized] {
  def caseToString(value: T): String
  /**
   * @return Some(T) if the string corresponds to one of the enumeration elements,
   *         None otherwise
   */
  def caseFromString(str: String): Option[T]
}

// Companion object to provide typeclass instances for all Itemizeds
object ItemizedSerialization {
  implicit def caseEnumSerialization[T <: Itemized]: ItemizedSerialization[T] =
    macro ItemizedMacro.caseEnumSerializationMacro[T]
}

// Macro implementation for ItemizedSerialization typeclass instance
object ItemizedMacro {
  def caseEnumSerializationMacro[T <: Itemized : c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val typeName = tpe.typeSymbol
    val companion = tpe.typeSymbol.companion
    val enumElements = tpe.typeSymbol.companion.typeSignature.decls.collect {
      case x: ModuleSymbol => x
    } toList

    val mapComponents = enumElements.map { x =>
      val name = x.name
      val decoded = name.decodedName.toString
      q"($companion.$name, $decoded)"
    }

    q"""
      new _root_.io.rbricks.itemized.ItemizedSerialization[$typeName] {
        private[this] val map: Map[$typeName, String] = Map(..$mapComponents)
        private[this] val revMap = map.map(_ swap)
        def caseToString(value: $typeName): String = map(value)
        def caseFromString(str: String): Option[$typeName] = revMap.get(str)
      }
    """
  }
}

