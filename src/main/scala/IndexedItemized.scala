package io.rbricks.itemized

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

/**
 * Marker trait for ADTs representing enumerations with an associated value ("index")
 *
 * The convention requires the following structure for the enumeration:
 * ```
 * sealed trait EnumName extends Itemized { type Index = IndexType }
 * object EnumName {
 *   case object Element1 extends EnumName { val index = element1Index }
 *   case object Element2 extends EnumName { val index = element2Index }
 * }
 * ```
 */
trait IndexedItemized extends Itemized {
  type Index
  val index: Index
}

trait ItemizedIndex[T <: IndexedItemized] {
  def caseToIndex(c: T): T#Index

  def caseFromIndex(v: T#Index): Option[T]
}

object ItemizedIndex {
  implicit def caseEnumIndex[T <: IndexedItemized]: ItemizedIndex[T] = macro ItemizedIndexMacro.caseEnumIndexMacro[T]
}

object ItemizedIndexMacro {
  def caseEnumIndexMacro[T <: IndexedItemized : c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val typeName = tpe.typeSymbol
    val companion = tpe.typeSymbol.companion
    val enumElements = tpe.typeSymbol.companion.typeSignature.decls.collect {
      case x: ModuleSymbol => x
    } toList

    val items = enumElements.map { x =>
      val name = x.name
      q"$companion.$name"
    }

    q"""
      new _root_.io.rbricks.itemized.ItemizedIndex[$typeName] {
        private[this] val revMap: Map[$typeName#Index, $typeName] =
          List(..$items).map(c => (c.index, c)).toMap
        def caseToIndex(c: $typeName): $typeName#Index = c.index
        def caseFromIndex(str: $typeName#Index): Option[$typeName] = revMap.get(str)
      }
    """
  }
}

