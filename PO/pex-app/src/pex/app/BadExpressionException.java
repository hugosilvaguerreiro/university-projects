/* SANE COPY */
package pex.app;

import pt.tecnico.po.ui.DialogException;

/**
 * Exception for representing format errors.
 */
@SuppressWarnings("nls")
public class BadExpressionException extends DialogException {
  
  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608241029L;
  
  /** Original leaf expression. */
  String _description;

  /**
   * @param description
   */
  public BadExpressionException(String description) {
    _description = description;
  }
  
  /**
   * @return the description
   */
  public String getDescription() {
    return _description;
  }

  /** @see pt.tecnico.po.ui.DialogException#getMessage() */
  @Override
  public String getMessage() {
    return "Problemas na expressão: " + _description;
  }
  
}
