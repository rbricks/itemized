package ingredients.caseenum.annotations

import scala.reflect.macros._
import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly

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
        sealed abstract trait $enumName extends _root_.ingredients.caseenum.CaseEnum
        object ${enumName.toTermName} {
          ..$members
        }
      """)
    }

    annottees.map(_.tree) match {
      //case (classDecl: ClassDef) :: Nil => modifiedClass(classDecl, None)
      case (classDecl: ClassDef) :: Nil => modifiedClass(classDecl)
      case _ => c.abort(c.enclosingPosition, "Invalid annottee")
    }
  }
}
