package scala.lms
package epfl
package test4

import common._
import test1._
import test2._
import test3._

import org.scala_lang.virtualized.virtualize
import org.scala_lang.virtualized.SourceContext

@virtualize
trait BasicProg extends LiftPrimitives { this: PrimitiveOps with Functions with Equal with IfThenElse =>

  implicit def intToRepDouble(i:Int):Rep[Double] = new IntOpsCls(unit(i)).toDouble

  def f(b: Rep[Boolean]) = {
    if (b) 1 else 2
  }
}

@virtualize
trait BasicProg2 extends LiftPrimitives { this: PrimitiveOps with Functions with Equal with IfThenElse =>
  def f(n: Rep[Double]) = {
    if (n == 0) n+1 else n
  }
}


class TestBasic extends FileDiffSuite {
  
  val prefix = home + "test-out/epfl/test4-"

  def testBasic1 = {
    withOutFile(prefix+"basic1") {
      object BasicProgExp extends BasicProg with PrimitiveOpsExpOpt with EqualExp with IfThenElseExp with FunctionsExternalDef1
      import BasicProgExp._

      val p = new ScalaGenPrimitiveOps with ScalaGenEqual with ScalaGenIfThenElse with ScalaGenFunctionsExternal { val IR: BasicProgExp.type = BasicProgExp }
      p.emitSource(f, "Basic", new java.io.PrintWriter(System.out))
    }
    assertFileEqualsCheck(prefix+"basic1")
  }

  def testBasic2 = {
    withOutFile(prefix+"basic2") {
      object BasicProgExp extends BasicProg2
        with PrimitiveOpsExpOpt with EqualExp with IfThenElseExp
        with FunctionsExternalDef1
      import BasicProgExp._

      val p = new ScalaGenPrimitiveOps with ScalaGenEqual with
        ScalaGenIfThenElse with ScalaGenFunctionsExternal { val IR: BasicProgExp.type = BasicProgExp }
      p.emitSource(f, "Basic2", new java.io.PrintWriter(System.out))
    }
    assertFileEqualsCheck(prefix+"basic2")
  }
}
