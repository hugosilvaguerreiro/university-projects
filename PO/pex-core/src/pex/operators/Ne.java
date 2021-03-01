package pex.operators;

import pex.Value;
import pex.OperatorsVisitor;
import pex.Expression;
import pex.Value;

/**
 * Class for describing the not equals (!=) operator
 */
public class Ne extends BinaryExpression {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608281352L;

  /**
   * @param first
   * @param second
   */
  public Ne(Expression first, Expression second) {
    super(first, second);
  }

  /**
   * @param visitor
   * @return A value depending of the type of the visitor
   */
  public Value<?> accept(OperatorsVisitor visitor) {
    return visitor.visitNe(this);
  }
}