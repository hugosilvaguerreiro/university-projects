/*  */
package pex;

import java.io.Serializable;
import pex.atomic.StringLiteral;
/**
 * An expressions can be evaluated to produce a value.
 */
public abstract class Expression implements Serializable {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608281352L;


  public abstract Value<?> accept(OperatorsVisitor visitor);

}
