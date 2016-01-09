package ingredients.caseenum

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

trait CaseEnum

trait CaseEnumSerialization[T <: CaseEnum] {
  def caseToString(value: T): String
  def caseFromString(str: String): Option[T]
}

object CaseEnumSerialization {
  implicit def caseEnumSerialization[T <: CaseEnum]: CaseEnumSerialization[T] =
    macro CaseEnumMacro.caseEnumSerializationMacro[T]
}

object CaseEnumMacro {
  def caseEnumSerializationMacro[T <: CaseEnum : c.WeakTypeTag](c: Context): c.Tree = {
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
      new _root_.ingredients.caseenum.CaseEnumSerialization[$typeName] {
        private[this] val map: Map[$typeName, String] = Map(..$mapComponents)
        private[this] val revMap = map.map(_ swap)
        def caseToString(value: $typeName): String = map(value)
        def caseFromString(str: String): Option[$typeName] = revMap.get(str)
      }
    """
  }
}

