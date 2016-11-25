package io.rbricks.itemized.annotation

import scala.reflect.macros._
import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly

/**
 * Macro annotation that transforms a trait with only bare,
 * empty objects as members to an ADT following the Itemized
 * convention (see the Itemized trait).
 *
 * e.g.
 * ```
 * @enum trait Planet {
 *   object Earth
 *   object Venus
 *   object Mercury
 * }
 * ```
 *
 * is transformed to
 *
 * ```
 * sealed trait Planet extends Itemized
 * object Planet {
 *   case object Earth extends Planet
 *   case object Venus extends Planet
 *   case object Mercury extends Planet
 * }
 * ```
 */
@compileTimeOnly("Enable macro paradise to expand macro annotations.")
class enum extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro EnumMacro.impl
}
   
object EnumMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
 
    def modifiedClass(classDecl: ClassDef) = {
      val (enumName, body) = try {
        val q"trait $enumName { ..$body }" = classDecl
        (enumName, body)
      } catch {
        case _: MatchError =>
          c.abort(c.enclosingPosition, "Annotation is only supported on objects")
      }
      val members = body.map {
        case q"object $memberName { ..$more }" =>
          q"case object $memberName extends $enumName"
        case _ =>
          c.abort(c.enclosingPosition, "Enum members should be plain objects")
      }
      c.Expr(q"""
        sealed trait $enumName extends _root_.io.rbricks.itemized.Itemized
        object ${enumName.toTermName} {
          ..$members
        }
      """)
    }

    annottees.map(_.tree) match {
      case (classDecl: ClassDef) :: Nil => modifiedClass(classDecl)
      case _ => c.abort(c.enclosingPosition, "Invalid annottee")
    }
  }
}

/**
 * Macro annotation that transforms a trait enclosing a single type
 * alias and a set of objects to an ADT following the IndexedItemized
 * convention (see the IndexedItemized trait).
 *
 * e.g.
 * ```
 * @indexedEnum trait Planet {
 *   type Index = Int
 *   object Earth   { 1 }
 *   object Venus   { 2 }
 *   object Mercury { 3 }
 * }
 * ```
 *
 * is transformed to
 *
 * ```
 * sealed trait Planet extends IndexedEnum {
 *   type Index = Int
 * }
 * object Planet {
 *   case object Earth extends Planet { val index = 1 }
 *   case object Venus extends Planet { val index = 2 }
 *   case object Mercury extends Planet { val index = 3 }
 * }
 * ```
 */
@compileTimeOnly("Enable macro paradise to expand macro annotations.")
class indexedEnum extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro IndexedEnumMacro.impl
}
 
object IndexedEnumMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
 
    def modifiedClass(classDecl: ClassDef) = {
      val (enumName, body) = try {
        val q"trait $enumName { ..$body }" = classDecl
        (enumName, body)
      } catch {
        case _: MatchError =>
          c.abort(c.enclosingPosition, "Annotation is only supported on traits")
      }
      val typeAliasTree :: memberTrees = body
      val members = memberTrees.map {
        case q"object $memberName { $statement }" =>
          q"""case object $memberName extends $enumName {
                val index = $statement
              }"""
        case _ =>
          c.abort(c.enclosingPosition, "Enum members should be plain objects")
      }
      val indexType = typeAliasTree match {
        case q"type Index = $ttype" => ttype
        case _ => c.abort(c.enclosingPosition, "Invalid type alias declaration")
      }
      c.Expr(q"""
        sealed trait $enumName extends _root_.io.rbricks.itemized.IndexedItemized {
          type Index = $indexType
        }
        object ${enumName.toTermName} {
          ..$members
        }
      """)
    }

    annottees.map(_.tree) match {
      case (classDecl: ClassDef) :: Nil => modifiedClass(classDecl)
      case _ => c.abort(c.enclosingPosition, "Invalid annottee. Expecting trait.")
    }
  }
}
