package pex;

import java.lang.Exception;
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

/*Abstract class for describing the visitor */
public interface OperatorsVisitor {

  public Value<?> visitIntegerLiteral(IntegerLiteral integer);
  public Value<?> visitStringLiteral(StringLiteral string);
  public Value<?> visitIdentifier(Identifier indentifier);
  public Value<?> visitAdd(Add operatorAdd);
  public Value<?> visitAnd(And operatorAnd);
  public Value<?> visitCall(Call operatorCall);
  public Value<?> visitDiv(Div operatorDiv);
  public Value<?> visitEq(Eq operatorEq);
  public Value<?> visitGe(Ge operatorGe);
  public Value<?> visitGt(Gt operatorGt);
  public Value<?> visitIf(If operatorIf);
  public Value<?> visitLe(Le operatorLe);
  public Value<?> visitLt(Lt operatorLt);
  public Value<?> visitMod(Mod operatorMod);
  public Value<?> visitMul(Mul operatorMul);
  public Value<?> visitNe(Ne operatorNe);
  public Value<?> visitNeg(Neg operatorNeg);
  public Value<?> visitNot(Not operatorNot);
  public Value<?> visitOr(Or operatorOr);
  public Value<?> visitPrint(Print operatorPrint);
  public Value<?> visitProgram(Program operatorProgram);
  public Value<?> visitReadi(Readi operatorReadi);
  public Value<?> visitReads(Reads operatorReads);
  public Value<?> visitSequence(Sequence operatorSequence);
  public Value<?> visitSet(Set operatorSet);
  public Value<?> visitSub(Sub operatorSub);
  public Value<?> visitWhile(While operatorWhile);
}
