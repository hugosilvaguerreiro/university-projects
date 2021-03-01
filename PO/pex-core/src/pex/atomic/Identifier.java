/*  */
package pex.atomic;

import pex.Expression;
import pex.Value;
import pex.OperatorsVisitor;
/**
 * Class for describing syntactic tree leaves for holding identifier values.
 */
public class Identifier extends Expression {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608281352L;

  /** Identifier name. */
  private String _name;

  /**
   * @param name
   */
  public Identifier(String name) {
    _name = name;
  }

  /**
   * @return the name
   */
  public String getName() {
    return _name;
  }


  /**
   * @param visitor
   * returns a value depending of the type of the visitor
   */
  public Value<?> accept(OperatorsVisitor visitor) {
    return visitor.visitIdentifier(this);
  }
}
