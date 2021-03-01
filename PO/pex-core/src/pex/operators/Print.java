package pex.operators;

import pex.Value;
import pex.OperatorsVisitor;
import java.util.Collections;
import java.util.List;
import pex.Expression;
import pex.Value;

/**
 * Class for describing the print operator
 */
public class Print extends Sequence {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608281352L;


  /**
   * Default constructor (empty print).
   */
  public Print() {
    super();
  }

  /**
   * Constructor for single-expression print.
   *
   * @param expression
   */
  public Print(Expression expression) {
    super(expression);
  }

  /**
   * @param expressions
   */
  public Print(List<Expression> expressions) {
    super(expressions);
  }

  /**
   * @param visitor
   * @return A value depending of the type of the visitor
   */
  public Value<?> accept(OperatorsVisitor visitor) {
    return visitor.visitPrint(this);
  }
}
