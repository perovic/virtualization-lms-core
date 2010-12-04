package scala.virtualization.lms
package internal

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym
import java.io.{File, FileWriter, PrintWriter}

trait ScalaCodegen extends GenericCodegen {
  val IR: Expressions
  import IR._

  override def kernelFileExt = "scala"

  override def toString = "scala"

  def emitSource[A,B](f: Exp[A] => Exp[B], className: String, stream: PrintWriter)(implicit mA: Manifest[A], mB: Manifest[B]): Unit = {

    val x = fresh[A]
    val y = f(x)

    val sA = mA.toString
    val sB = mB.toString

    stream.println("/*****************************************\n"+
                   "  Emitting Generated Code                  \n"+
                   "*******************************************/")
    stream.println("class "+className+" extends (("+sA+")=>("+sB+")) {")
    stream.println("def apply("+quote(x)+":"+sA+"): "+sB+" = {")
    
    emitBlock(y)(stream)
    stream.println(quote(getBlockResult(y)))
    
    stream.println("}")
    stream.println("}")
    stream.println("/*****************************************\n"+
                   "  End of Generated Code                  \n"+
                   "*******************************************/")

    stream.flush
  }

  override def emitKernelHeader(sym: Sym[_], vals: List[Sym[_]], vars: List[Sym[_]], resultIsVar: Boolean)(implicit stream: PrintWriter): Unit = {
    stream.println("package embedding." + this.toString + ".gen")
    stream.println("object kernel_" + quote(sym) + "{")
    stream.print("def apply(")
    stream.print(vals.map(p => quote(p) + ":" + remap(p.Type)).mkString(","))

    // variable name mangling
    if (vals.length > 0 && vars.length > 0){
      stream.print(", ")
    }
    if (vars.length > 0){
      stream.print(vars.map(v => quote(v) + ":" + "generated.Ref[" + remap(v.Type) +"]").mkString(","))
    }
    if (resultIsVar){
      stream.print("): " + "generated.Ref[" + remap(sym.Type) + "] = {")
    }
    else {
      stream.print("): " + remap(sym.Type) + " = {")
    }

    stream.println("")
  }

  override def emitKernelFooter(sym: Sym[_], vals: List[Sym[_]], vars: List[Sym[_]], resultIsVar: Boolean)(implicit stream: PrintWriter): Unit = {
    stream.println(quote(sym))
    stream.println("}}")
  }

  def emitValDef(sym: Sym[_], rhs: String)(implicit stream: PrintWriter): Unit = {
    stream.println("val " + quote(sym) + " = " + rhs)
  }
  def emitVarDef(sym: Sym[_], rhs: String)(implicit stream: PrintWriter): Unit = {
    stream.println("var " + quote(sym) + " = " + rhs)
  }
  def emitAssignment(lhs: String, rhs: String)(implicit stream: PrintWriter): Unit = {
    stream.println(lhs + " = " + rhs)
  }
}

trait ScalaNestedCodegen extends GenericNestedCodegen with ScalaCodegen {
  val IR: Expressions with Effects
  import IR._
  
  override def emitSource[A,B](f: Exp[A] => Exp[B], className: String, stream: PrintWriter)
      (implicit mA: Manifest[A], mB: Manifest[B]): Unit = {
    super.emitSource[A,B](x => reifyEffects(f(x)), className, stream)
  }

  override def quote(x: Exp[_]) = x match { // TODO: quirk!
    case Sym(-1) => "_"
    case _ => super.quote(x)
  }

}

// TODO: what is the point of these, I suggest to remove them
trait ScalaGenBase extends ScalaCodegen {
  import IR._

}

trait ScalaGenEffect extends ScalaNestedCodegen with ScalaGenBase {

}