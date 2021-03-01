package pex.operators;

import pex.Value;
import pex.OperatorsVisitor;
import pex.Expression;
import pex.Value;

/**
 * Class for describing the while operator
 */
public class While extends Expression {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608281352L;

  /** condition of the while cicle. */
  Expression _condition;

  /** second operand. */
  Expression _second;

  /**
   * @param condition
   * @param second
   */
  public While(Expression condition, Expression second) {
    _condition = condition;
    _second = second;
  }

  /**
   * @return Expression
   */
  public Expression condition() {
    return _condition;
  }

  /**
   * @return Expression
   */
  public Expression second() {
    return _second;
  }

  /**
   * @param visitor
   * @return A value depending of the type of the visitor
   */
  public Value<?> accept(OperatorsVisitor visitor) {
    return visitor.visitWhile(this);
  }
}
