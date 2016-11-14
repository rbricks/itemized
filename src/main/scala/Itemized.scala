package io.rbricks.itemized

import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

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
trait ItemizedCodec[T <: Itemized] {
  def toRep(value: T): String

  /**
   * @return Some(T) if the string corresponds to one of the enumeration elements,
   *         None otherwise
   */
  def fromRep(str: String): Option[T]
}

// Companion object to provide typeclass instances for all Itemizeds
object ItemizedCodec {
  implicit def itemizedCodecFor[T <: Itemized]: ItemizedCodec[T] =
    macro ItemizedMacro.itemizedCodecMacro[T]

  // Object-oriented-forwarders ("syntax")
  object ops {
    implicit class ItemizedCodecOps[T <: Itemized](self: T)(implicit itemizedCodec: ItemizedCodec[T]) {
      def toRep = itemizedCodec.toRep(self)
    }
  }

  def apply[T <: Itemized](implicit itemizedCodec: ItemizedCodec[T]): ItemizedCodec[T] = itemizedCodec
}

// Macro implementation for ItemizedCodec typeclass instance
object ItemizedMacro {
  def itemizedCodecMacro[T <: Itemized : c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val typeName = tpe.typeSymbol
    if (!typeName.isAbstract) {
      val message = s"Cannot derive implementation for concrete object (${typeName}), please upcast to the abstract supertype (enum name)"
      c.info(c.enclosingPosition, message, true)
      throw new Exception(message)
    }
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
      new _root_.io.rbricks.itemized.ItemizedCodec[$typeName] {
        private[this] val map: Map[$typeName, String] = Map(..$mapComponents)
        private[this] val revMap = map.map(_ swap)
        def toRep(value: $typeName): String = map(value)
        def fromRep(str: String): Option[$typeName] = revMap.get(str)
      }
    """
  }
}

