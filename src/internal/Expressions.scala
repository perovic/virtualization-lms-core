package scala.virtualization.lms
package internal

/**
 * The Expressions trait houses common AST nodes. It also manages a list of encountered Definitions which
 * allows for common sub-expression elimination (CSE).  
 * 
 * @since 0.1
 */
trait Expressions {

  abstract class Exp[+T:Manifest] { // constants/symbols (atomic)
    def Type : Manifest[_] = manifest
  }

  case class Const[T:Manifest](x: T) extends Exp[T]

  case class Sym[T:Manifest](val id: Int) extends Exp[T]

  case class Variable[+T:Manifest](e: Exp[T])

  case class External[A:Manifest](s: String, fmt_args: List[Exp[Any]] = List()) extends Exp[A]
      
  var nVars = 0
  def fresh[T:Manifest] = Sym[T] { nVars += 1; nVars -1 }

  abstract class Def[+T] // operations (composite)

  case class TP[T](sym: Sym[T], rhs: Def[T]) 

  var globalDefs: List[TP[_]] = Nil

  def findDefinition[T](s: Sym[T]): Option[TP[T]] =
    globalDefs.find(_.sym == s).asInstanceOf[Option[TP[T]]]

  def findDefinition[T](d: Def[T]): Option[TP[T]] =
    globalDefs.find(_.rhs == d).asInstanceOf[Option[TP[T]]]

  def findOrCreateDefinition[T:Manifest](d: Def[T]): TP[T] =
    findDefinition[T](d).getOrElse {
      createDefinition(fresh[T], d)
    }

  def createDefinition[T](s: Sym[T], d: Def[T]): TP[T] = {
    val f = TP(s, d)
    globalDefs = globalDefs:::List(f)
    f
  }

  implicit def toAtom[T:Manifest](d: Def[T]): Exp[T] = {
    findOrCreateDefinition(d).sym
  }

  object Def {
    def unapply[T](e: Exp[T]): Option[Def[T]] = e match { // really need to test for sym?
      case s @ Sym(_) =>
        findDefinition(s).map(_.rhs)
      case _ =>
        None
    }
  }

  def reset {
    nVars = 0
    globalDefs = Nil
  }

/*
  // dependencies
  def syms(e: Any): List[Sym[Any]] = e match {
    case s: Sym[Any] => List(s)
    case p: Product => p.productIterator.toList.flatMap(syms(_))
    case _ => Nil
  }

  def dep(e: Exp[Any]): List[Sym[Any]] = e match {
    case Def(d: Product) => syms(d)
    case _ => Nil
  }
*/
}