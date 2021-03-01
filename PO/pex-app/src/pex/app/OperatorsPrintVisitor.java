package pex.app;

import pex.OperatorsVisitor;
import pex.operators.Add;
import pex.operators.And;
import pex.operators.Call;
import pex.operators.Div;
import pex.operators.Eq;
import pex.operators.Ge;
import pex.operators.Gt;
import pex.operators.If;
import pex.operators.Le;
import pex.operators.Lt;
import pex.operators.Mod;
import pex.operators.Mul;
import pex.operators.Ne;
import pex.operators.Neg;
import pex.operators.Not;
import pex.operators.Or;
import pex.operators.Print;
import pex.operators.Program;
import pex.operators.Readi;
import pex.operators.Reads;
import pex.operators.Sequence;
import pex.operators.Set;
import pex.operators.Sub;
import pex.operators.While;
import pex.atomic.Identifier;
import pex.atomic.StringLiteral;
import pex.atomic.IntegerLiteral;

public class OperatorsPrintVisitor implements OperatorsVisitor {


  /**
  * @param integer
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitIntegerLiteral(IntegerLiteral integer) {
    String output = integer.getValue().toString();
    return new StringLiteral(output);
  }

  /**
  * @param string
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitStringLiteral(StringLiteral string) {
    String output = '"'+string.getValue()+'"';
    return new StringLiteral(output);
  }

  /**
  * @param identifier
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitIdentifier(Identifier indentifier) {
    String output = indentifier.getName();
    return new StringLiteral(output);
  }

  /**
  * @param operatorAdd
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitAdd(Add operatorAdd) {
    String output = "(add ";
    output += operatorAdd.first().accept(this).getValue();
    output += " ";
    output += operatorAdd.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorAnd
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitAnd(And operatorAnd) {
    String output = "(and ";
    output += operatorAnd.first().accept(this).getValue();
    output += " ";
    output+= operatorAnd.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorCall
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitCall(Call operatorCall) {
    String output = "(call ";
    output += operatorCall.argument().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorDiv
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitDiv(Div operatorDiv) {
    String output = "(div ";
    output += operatorDiv.first().accept(this).getValue();
    output += " ";
    output += operatorDiv.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorEq
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitEq(Eq operatorEq) {
    String output = "(eq ";
    output += operatorEq.first().accept(this).getValue();
    output += " ";
    output += operatorEq.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorGe
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitGe(Ge operatorGe) {
    String output = "(ge ";
    output += operatorGe.first().accept(this).getValue();
    output += " ";
    output += operatorGe.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorGt
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitGt(Gt operatorGt) {
    String output = "(gt ";
    output += operatorGt.first().accept(this).getValue();
    output += " ";
    output += operatorGt.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorIf
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitIf(If operatorIf) {
    String output = "(if ";
    output += operatorIf.first().accept(this).getValue();
    output += " ";
    output += operatorIf.second().accept(this).getValue();
    output += " ";
    output += operatorIf.third().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorLe
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitLe(Le operatorLe) {
    String output = "(le ";
    output += operatorLe.first().accept(this).getValue();
    output += " ";
    output += operatorLe.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }


  /**
  * @param operatorLt
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitLt(Lt operatorLt) {
    String output = "(lt ";
    output += operatorLt.first().accept(this).getValue();
    output += " ";
    output += operatorLt.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorMod
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitMod(Mod operatorMod) {
    String output = "(mod ";
    output += operatorMod.first().accept(this).getValue();
    output += " ";
    output += operatorMod.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorMul
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitMul(Mul operatorMul) {
    String output = "(mul ";
    output += operatorMul.first().accept(this).getValue();
    output += " ";
    output += operatorMul.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorNe
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitNe(Ne operatorNe) {
    String output = "(ne ";
    output += operatorNe.first().accept(this).getValue();
    output += " ";
    output += operatorNe.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorNeg
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitNeg(Neg operatorNeg) {
    String output = "(neg ";
    output += operatorNeg.argument().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorNot
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitNot(Not operatorNot) {
    String output = "(not ";
    output += operatorNot.argument().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorOr
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitOr(Or operatorOr) {
    String output = "(or ";
    output += operatorOr.first().accept(this).getValue();
    output += " ";
    output += operatorOr.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorSequence
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitSequence(Sequence operatorSequence) {
    String output = "(seq ";
    for(int i = 0; i < operatorSequence.size(); i++)
      output += " " +operatorSequence.get(i).accept(this).getValue();
    output +=")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorPrint
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitPrint(Print operatorPrint) {
      String output = "(print ";
      for(int i = 0; i < operatorPrint.size(); i++)
        output += " " + operatorPrint.get(i).accept(this).getValue();
      output += ")";
      return new StringLiteral(output);
  }

  /**
  * @param operatorProgram
  * @return StringLiteral containing the textual representation of this Expression
  */

  public StringLiteral visitProgram(Program operatorProgram) {
    String output = "";
    for(int i = 0; i < operatorProgram.size(); i++) {
      output += operatorProgram.get(i).accept(this).getValue();
      output += "\n";
    }
    return new StringLiteral(output);
  }

  /**
  * @param operatorReadi
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitReadi(Readi operatorReadi) {
    return new StringLiteral("(readi)");
  }

  /**
  * @param operatorReads
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitReads(Reads operatorReads) {
    return new StringLiteral("(reads)");
  }

  /**
  * @param operatorSet
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitSet(Set operatorSet) {
    String output = "(set ";
    output += operatorSet.first().accept(this).getValue();
    output += " ";
    output += operatorSet.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorSub
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitSub(Sub operatorSub) {
    String output = "(sub ";
    output += operatorSub.first().accept(this).getValue();
    output += " ";
    output += operatorSub.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }

  /**
  * @param operatorWhile
  * @return StringLiteral containing the textual representation of this Expression
  */
  public StringLiteral visitWhile(While operatorWhile) {
    String output = "(while ";
    output += operatorWhile.condition().accept(this).getValue();
    output += " ";
    output += operatorWhile.second().accept(this).getValue();
    output += ")";
    return new StringLiteral(output);
  }
}
