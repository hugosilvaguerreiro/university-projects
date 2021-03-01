/*  */
package pex.atomic;

import pex.Value;
import pex.OperatorsVisitor;

/**
 * Class for describing syntactic tree leaves for holding string values.
 */
public class StringLiteral extends Value<String> {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608281352L;

  /**
   * @param value
   */
  public StringLiteral(String value) {
    super(value);
  }

  /**
   * @param visitor
   * returns a value depending of the type of the visitor
   */
  public Value<?> accept(OperatorsVisitor visitor) {
    return visitor.visitStringLiteral(this);
  }
}
