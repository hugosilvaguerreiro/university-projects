/*  */
package pex.app.evaluator;

/** Messages for menu interactions. */
@SuppressWarnings("nls")
public final class Message {

  /**
   * @return prompt for identifier
   */
  public static String requestId() {
    return "Identificador: ";
  }

  /**
   * @return prompt for new identifier
   */
  public static String requestNewId() {
    return "Novo identificador: ";
  }

  /**
   * @param name
   * @return return error message for unknown section
   */
  public static String noSuchIdentifier(String name) {
    return "O identificador '" + name + "' não existe.";
  }

  /**
   * @param name
   * @param dependent
   * @return error message for identifier removal
   */
  public static String identifierInUse(String name, String dependent) {
    return "O identificador '" + name + "' está em uso na definição de '" + dependent + "'.";
  }

  /** Prevent instantiation. */
  private Message() {
    // EMPTY
  }

  /**
   * @return prompt for position
   */
  public static String requestPosition() {
    return "Posição do programa: ";
  }

  /**
   * @return prompt for expression
   */
  public static String requestExpression() {
    return "Expressão: ";
  }

}
