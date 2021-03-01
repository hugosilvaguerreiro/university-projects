/** @version  */
package pex.app.evaluator;

import pex.operators.Program;
import pex.Interpreter;
import pex.app.OperatorsFindIdentifiersVisitor;


/**
 * Show uninitialized identifiers.
 */
public class DoShowUninitializedIdentifiers extends ProgramCommand {

  /**
   * @param interpreter
   * @param receiver
   */
  public DoShowUninitializedIdentifiers(Interpreter interpreter, Program receiver) {
    super(Label.SHOW_UNINITIALIZED_IDENTIFIERS, interpreter, receiver);
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    _display.popup(_receiver.accept(new OperatorsFindIdentifiersVisitor(false)));
  }

}
