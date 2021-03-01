package pex.app;

import pt.tecnico.po.ui.Form;
import pt.tecnico.po.ui.Display;
import pt.tecnico.po.ui.Input;
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
import pex.Interpreter;
import pex.Value;
import pex.operators.Readi;
import pex.operators.Reads;
import pex.operators.Sequence;
import pex.operators.Set;
import pex.operators.Sub;
import pex.operators.While;
import pex.atomic.Identifier;
import pex.atomic.StringLiteral;
import pex.atomic.IntegerLiteral;
import java.lang.NullPointerException;

public class OperatorsEvaluateVisitor implements OperatorsVisitor  {

  /**the interpreter to change the list of identifiers**/
  private Interpreter _interpreter;


  public OperatorsEvaluateVisitor(Interpreter interpreter) {
    _interpreter = interpreter;
  }

  /**
  * @param integer
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitIntegerLiteral(IntegerLiteral integer) {
    return integer;
  }

  /**
  * @param string
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitStringLiteral(StringLiteral string) {
    return string;
  }

  /**
  * @param identifier
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitIdentifier(Identifier identifier) {
    if(_interpreter.containsIdentifier(identifier.getName())) {
      if(_interpreter.getIdentifierValue(identifier.getName()) != null)
        return _interpreter.getIdentifierValue(identifier.getName());
      else
        return new IntegerLiteral(0);
    }
    else {
      _interpreter.putIdentifier(identifier.getName());
      return new IntegerLiteral(0);
    }
  }

  /**
  * @param operatorAdd
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitAdd(Add operatorAdd) {
      int output = 0;
      IntegerLiteral first = (IntegerLiteral) operatorAdd.first().accept(this);
      IntegerLiteral second = (IntegerLiteral) operatorAdd.second().accept(this);
      output += first.getValue() + second.getValue();
      return new IntegerLiteral(output);


  }

  /**
  * @param operatorSub
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitSub(Sub operatorSub) {
    int output = 0;
    IntegerLiteral first = (IntegerLiteral) operatorSub.first().accept(this);
    IntegerLiteral second = (IntegerLiteral) operatorSub.second().accept(this);
    output += first.getValue() - second.getValue();
    return new IntegerLiteral(output);
  }


  /**
  * @param operatorDiv
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitDiv(Div operatorDiv) {
    int output = 0;
    IntegerLiteral first = (IntegerLiteral) operatorDiv.first().accept(this);
    IntegerLiteral second = (IntegerLiteral) operatorDiv.second().accept(this);
    output += first.getValue() / second.getValue();
    return new IntegerLiteral(output);
  }

  /**
  * @param operatorMod
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitMod(Mod operatorMod) {
    int output = 0;
    IntegerLiteral first = (IntegerLiteral) operatorMod.first().accept(this);
    IntegerLiteral second = (IntegerLiteral) operatorMod.second().accept(this);
    output += first.getValue() % second.getValue();
    return new IntegerLiteral(output);
  }

  /**
  * @param operatorMul
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitMul(Mul operatorMul)  {
    int output = 0;
    IntegerLiteral first = (IntegerLiteral) operatorMul.first().accept(this);
    IntegerLiteral second = (IntegerLiteral) operatorMul.second().accept(this);
    output += first.getValue() * second.getValue();
    return new IntegerLiteral(output);
  }

  /**
  * @param operatorCall
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitCall(Call operatorCall)  {
    try {
      StringLiteral programName = (StringLiteral) operatorCall.argument();
      Program p = _interpreter.getProgram(programName.getValue());
      return p.accept(this);
    }catch (NullPointerException exception) {
      exception.printStackTrace();
      return new IntegerLiteral(0);
    }
  }


  /**
  * @param operatorGe
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitGe(Ge operatorGe) {
      IntegerLiteral first = (IntegerLiteral) operatorGe.first().accept(this);
      IntegerLiteral second = (IntegerLiteral) operatorGe.second().accept(this);
      if(first.getValue() >= second.getValue())
        return new IntegerLiteral(1);
      else
        return new IntegerLiteral(0);
  }

  /**
  * @param operatorGt
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitGt(Gt operatorGt) {
      IntegerLiteral first = (IntegerLiteral)operatorGt.first().accept(this);
      IntegerLiteral second = (IntegerLiteral)operatorGt.second().accept(this);
      if(first.getValue() > second.getValue())
        return new IntegerLiteral(1);
      else
        return new IntegerLiteral(0);
  }

  /**
  * @param operatorIf
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitIf(If operatorIf) {
      IntegerLiteral condition = (IntegerLiteral) operatorIf.first().accept(this);
      if(condition.getValue() == 0)
        return operatorIf.third().accept(this);

      else
        return operatorIf.second().accept(this);
  }

  /**
  * @param operatorAnd
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitAnd(And operatorAnd)  {
    IntegerLiteral firstCondition = (IntegerLiteral) operatorAnd.first().accept(this);
    if(firstCondition.getValue() != 0) {
      IntegerLiteral secondCondition = (IntegerLiteral) operatorAnd.second().accept(this);
      if(secondCondition.getValue() != 0)
        return new IntegerLiteral(1);
      else
        return new IntegerLiteral(0);
    }
    else
      return new IntegerLiteral(0);
  }

  /**
  * @param operatorOr
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitOr(Or operatorOr) {
    IntegerLiteral firstCondition = (IntegerLiteral) operatorOr.first().accept(this);
    IntegerLiteral secondCondition = (IntegerLiteral) operatorOr.second().accept(this);
    if(firstCondition.getValue() == 0) {
      if(secondCondition.getValue() != 0)
        return new IntegerLiteral(1);
      else
        return new IntegerLiteral(0);
    }
    else
      return new IntegerLiteral(1);
  }

  /**
  * @param operatorLe
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitLe(Le operatorLe) {
      IntegerLiteral first = (IntegerLiteral) operatorLe.first().accept(this);
      IntegerLiteral second = (IntegerLiteral) operatorLe.second().accept(this);
      if(first.getValue() <= second.getValue())
        return new IntegerLiteral(1);
      else
        return new IntegerLiteral(0);
  }


  /**
  * @param operatorLt
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitLt(Lt operatorLt) {
      IntegerLiteral first = (IntegerLiteral)operatorLt.first().accept(this);
      IntegerLiteral second = (IntegerLiteral)operatorLt.second().accept(this);
      if(first.getValue() < second.getValue())
        return new IntegerLiteral(1);
      else
        return new IntegerLiteral(0);
  }

  /**
  * @param operatorEq
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitEq(Eq operatorEq) {
        if(operatorEq.first().accept(this).getValue() == operatorEq.second().accept(this).getValue())
          return new IntegerLiteral(1);
        else
          return new IntegerLiteral(0);
  }

  /**
  * @param operatorNe
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitNe(Ne operatorNe) {
      if(operatorNe.first().accept(this).getValue() != operatorNe.second().accept(this).getValue())
        return new IntegerLiteral(1);
      else
        return new IntegerLiteral(0);
  }

  /**
  * @param operatorNeg
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitNeg(Neg operatorNeg) {
      IntegerLiteral argument = (IntegerLiteral)operatorNeg.argument().accept(this);
      return new IntegerLiteral(argument.getValue()*(-1));
  }

  /**
  * @param operatorNot
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitNot(Not operatorNot) {
    IntegerLiteral argument =(IntegerLiteral) operatorNot.argument().accept(this);
     if(argument.getValue() == 0)
      return new IntegerLiteral(1);
    else
      return new IntegerLiteral(0);
  }

  /**
  * @param operatorSequence
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitSequence(Sequence operatorSequence) {
    if(operatorSequence.size() == 0)
      return new IntegerLiteral(0);

    for(int i = 0; i < operatorSequence.size()-1; i++) {
      operatorSequence.get(i).accept(this);
    }
    return operatorSequence.get(operatorSequence.size()-1).accept(this);
  }

  /**
  * @param operatorPrint
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitPrint(Print operatorPrint) {
      Display display = new Display();
      Value<?> output = null;
      for(int i = 0; i < operatorPrint.size(); i++) {
        output = operatorPrint.get(i).accept(this);
        display.add(output.toString());
      }
      display.display();
      return output;
  }

  /**
  * @param operatorProgram
  * @return Value containing the evaluation of the expression
  */

  public Value<?> visitProgram(Program operatorProgram) {
    if(operatorProgram.size() == 0)
      return new IntegerLiteral(0);

    for(int i = 0; i < operatorProgram.size()-1; i++) {
      operatorProgram.get(i).accept(this);
    }
    return operatorProgram.get(operatorProgram.size()-1).accept(this);
  }

  /**
  * @param operatorReadi
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitReadi(Readi operatorReadi) {
    Form form = new Form();
    form.addIntegerInput(null);
    form.parse();
    Integer input = (Integer) form.entry(0).value();
    return new IntegerLiteral(input);
  }

  /**
  * @param operatorReads
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitReads(Reads operatorReads) {
    Form form = new Form();
    form.addStringInput(null);
    form.parse();
    String input = (String) form.entry(0).value();
    return new StringLiteral(input);
  }

  /**
  * @param operatorSet
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitSet(Set operatorSet) {
      Identifier Identifier = (Identifier) operatorSet.first();
      Value<?> identifierValue = operatorSet.second().accept(this);

      _interpreter.putIdentifier(Identifier.getName(), identifierValue);
      return identifierValue;
  }

  /**
  * @param operatorWhile
  * @return Value containing the evaluation of the expression
  */
  public Value<?> visitWhile(While operatorWhile) {
      while((Integer) operatorWhile.condition().accept(this).getValue() != 0) {
        operatorWhile.second().accept(this);
      }
      return (IntegerLiteral) operatorWhile.condition().accept(this);
  }
}
