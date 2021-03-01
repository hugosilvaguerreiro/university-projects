/*  */
package pex.exceptions;

import pex.ParserException;

/**
 * Exception for representing format errors.
 */
public class BadExpressionException extends ParserException {
  
  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608241029L;
  
  /** Original leaf expression. */
  String _description;

  /**
   * @param description 
   * @param cause 
   */
  public BadExpressionException(String description, Exception cause) {
    super(cause);
    _description = description;
  }
  
  /**
   * @return the description
   */
  public String getDescription() {
    return _description;
  }
  
}
