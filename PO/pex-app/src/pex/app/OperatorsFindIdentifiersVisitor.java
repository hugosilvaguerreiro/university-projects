package pex.app;

import java.util.TreeMap;
import java.util.Map;
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
import pex.Value;
import pex.atomic.StringLiteral;
import pex.atomic.IntegerLiteral;
import pex.Interpreter;
import pex.app.OperatorsEvaluateVisitor;
import pex.Expression;

public class OperatorsFindIdentifiersVisitor implements OperatorsVisitor {

  /** variable to controll what Identifiers to show **/
  private boolean _showAll = true;
  /**contains the identifiers name and a boolean variable representing the inicialized state of the identifier **/
  private TreeMap<String, Boolean> _identifiersToShow;


  public OperatorsFindIdentifiersVisitor(Boolean showAll) {
    _showAll = showAll;
    _identifiersToShow = new TreeMap<String, Boolean>();
  }

  /**
  * @param integer
  *  @return null pointer
  */
  public Value<?> visitIntegerLiteral(IntegerLiteral integer) {
      return null;
  }

  /**
  * @param string
  *  @return null pointer
  */
  public Value<?> visitStringLiteral(StringLiteral string) {
      return null;
  }

  /**
  * @param identifier
  *  @return null pointer
  */
  public Value<?> visitIdentifier(Identifier identifier) {
      if(!_identifiersToShow.containsKey(identifier.getName()))
        _identifiersToShow.put(identifier.getName(), false);
      return null;
  }

  /**
  * @param operatorAdd
  *  @return null pointer
  */
  public Value<?> visitAdd(Add operatorAdd) {
    operatorAdd.first().accept(this);
    operatorAdd.second().accept(this);
    return null;
  }

  /**
  * @param operatorAnd
  *  @return null pointer
  */
  public Value<?> visitAnd(And operatorAnd) {
    operatorAnd.first().accept(this);
    operatorAnd.second().accept(this);
    return null;
  }

  /**
  * @param operatorCall
  *  @return null pointer
  */
  public Value<?> visitCall(Call operatorCall) {
    return operatorCall.argument().accept(this);
  }

  /**
  * @param operatorDiv
  *  @return null pointer
  */
  public Value<?> visitDiv(Div operatorDiv) {
    operatorDiv.first().accept(this);
    operatorDiv.second().accept(this);
    return null;
  }

  /**
  * @param operatorEq
  *  @return null pointer
  */
  public Value<?> visitEq(Eq operatorEq) {
    operatorEq.first().accept(this);
    operatorEq.second().accept(this);
    return null;
  }

  /**
  * @param operatorGe
  *  @return null pointer
  */
  public Value<?> visitGe(Ge operatorGe) {
    operatorGe.first().accept(this);
    operatorGe.second().accept(this);
    return null;
  }

  /**
  * @param operatorGt
  *  @return null pointer
  */
  public Value<?> visitGt(Gt operatorGt) {
    operatorGt.first().accept(this);
    operatorGt.second().accept(this);
    return null;
  }

  /**
  * @param operatorIf
  *  @return null pointer
  */
  public Value<?> visitIf(If operatorIf) {
    operatorIf.first().accept(this);
    operatorIf.second().accept(this);
    operatorIf.third().accept(this);
    return null;
  }

  /**
  * @param operatorLe
  *  @return null pointer
  */
  public Value<?> visitLe(Le operatorLe) {
    operatorLe.first().accept(this);
    operatorLe.second().accept(this);
    return null;
  }

  /**
  * @param operatorLt
  *  @return null pointer
  */
  public Value<?> visitLt(Lt operatorLt) {
    operatorLt.first().accept(this);
    operatorLt.second().accept(this);
    return null;
  }

  /**
  * @param operatorMod
  *  @return null pointer
  */
  public Value<?> visitMod(Mod operatorMod) {
    operatorMod.first().accept(this);
    operatorMod.second().accept(this);
    return null;
  }

  /**
  * @param operatorMul
  *  @return null pointer
  */
  public Value<?> visitMul(Mul operatorMul) {
    operatorMul.first().accept(this);
    operatorMul.second().accept(this);
    return null;
  }

  /**
  * @param operatorNe
  *  @return null pointer
  */
  public Value<?> visitNe(Ne operatorNe) {
    operatorNe.first().accept(this);
    operatorNe.second().accept(this);
    return null;
  }

  /**
  * @param operatorNeg
  *  @return null pointer
  */
  public Value<?> visitNeg(Neg operatorNeg) {
    operatorNeg.argument().accept(this);
    return null;
  }

  /**
  * @param operatorNot
  *  @return null pointer
  */
  public Value<?> visitNot(Not operatorNot) {
    operatorNot.argument().accept(this);
    return null;
  }

  /**
  * @param operatorOr
  *  @return null pointer
  */
  public Value<?> visitOr(Or operatorOr) {
    operatorOr.first().accept(this);
    operatorOr.second().accept(this);
    return null;
  }

  /**
  * @param operatorPrint
  *  @return null pointer
  */
  public Value<?> visitPrint(Print operatorPrint) {
    for(int i=0; i < operatorPrint.size(); i++) {
      operatorPrint.get(i).accept(this);
    }
    return null;
  }

  /**
  * @param operatorProgram
  *  @return StringLiteral containing the identifiers that were found
  */
  public Value<?> visitProgram(Program operatorProgram) {
    String output = "";
    for(int i=0; i < operatorProgram.size(); i++) {
      operatorProgram.get(i).accept(this);
    }
    for(Map.Entry<String,Boolean> identifier : _identifiersToShow.entrySet()) {
        if(_showAll) {
          output += identifier.getKey() + "\n";
        }
        else if (!identifier.getValue() && !_showAll) {
          output += identifier.getKey()+"\n";
        }
    }
    return new StringLiteral(output);
   }

   /**
   * @param operatorSequence
   *  @return null pointer
   */
  public Value<?> visitSequence(Sequence operatorSequence) {
    for(int i=0; i < operatorSequence.size(); i++) {
      operatorSequence.get(i).accept(this);
    }
    return null;
  }

  /**
  * @param operatorReadi
  *  @return null pointer
  */
  public Value<?> visitReadi(Readi operatorReadi) {
    return null;
  }

  /**
  * @param operatorReads
  *  @return null pointer
  */
  public Value<?> visitReads(Reads operatorReads) {
    return null;
  }

  /**
  * @param operatorSet
  *  @return null pointer
  */
  public Value<?> visitSet(Set operatorSet) {
    Identifier identifier = (Identifier) operatorSet.first();
    _identifiersToShow.put(identifier.getName(), true);
    return null;
  }

  /**
  * @param operatorSub
  *  @return null pointer
  */
  public Value<?> visitSub(Sub operatorSub) {
    operatorSub.first().accept(this);
    operatorSub.second().accept(this);
    return null;
  }

  /**
  * @param operatorWhile
  *  @return null pointer
  */
  public Value<?> visitWhile(While operatorWhile) {
    operatorWhile.condition().accept(this);
    operatorWhile.second().accept(this);
    return null;
  }
}
