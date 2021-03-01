/** @version  */
package pex.app.main;

import pex.InterpreterHandler;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.Input;

/**
 * Open a new interpreter.
 */
public class DoNew extends Command<InterpreterHandler> {
  /** Input field. */
  Input<Boolean> _shouldSave;

  /**
   * @param receiver
   */
  public DoNew(InterpreterHandler receiver) {
    super(Label.NEW, receiver);
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    _form.parse();
    _receiver.newInterpreter();
  }
}
