package pex.operators;

import pex.Value;
import pex.OperatorsVisitor;
import pex.Expression;
import pex.Value;

/**
 * Class for describing the negate operator
 */
public class Readi extends Expression {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608281352L;

  /**
   * @param visitor
   * @return A value depending of the type of the visitor
   */
  public Value<?> accept(OperatorsVisitor visitor) {
    return visitor.visitReadi(this);
  }
}
