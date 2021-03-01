/*  */
package pex.exceptions;

import pex.ParserException;

/**
 * Class for representing a syntax error problem.
 */
public class ExtraneousDataAtEndOfInputException extends ParserException {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608241029L;

  /** Extraneous character. */
  private int _c; 

  /**
   * @param c
   */
  public ExtraneousDataAtEndOfInputException(int c) {
    _c = c;
  }

  /**
   * @param e
   */
  public ExtraneousDataAtEndOfInputException(ExtraneousDataAtEndOfInputException e) {
    super(e);
  }

  /**
   * @return the character
   */
  public int getC() {
    return _c;
  }

}
