package pex.operators;

import pex.Value;
import pex.OperatorsVisitor;
import pex.Expression;
import pex.Value;

/**
 * Class for describing the Div (/) operator
 */
public class Div extends BinaryExpression {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608281352L;

  /**
   * @param first
   * @param second
   */
  public Div(Expression first, Expression second) {
    super(first, second);
  }

  /**
   * @param visitor
   * @return A value depending of the type of the visitor
   */
  public Value<?> accept(OperatorsVisitor visitor) {
    return visitor.visitDiv(this);
  }
}