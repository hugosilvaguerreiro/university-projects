/*  */
package pex.exceptions;

import pex.ParserException;

/**
 * Exception for representing expression errors.
 */
public class BadNumberException extends ParserException {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608241029L;

  /** Original leaf expression. */
  String _description;

  /**
   * @param description
   */
  public BadNumberException(String description) {
    super(description);
    _description = description;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return _description;
  }

}
