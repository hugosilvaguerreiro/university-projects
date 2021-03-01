/** @version  */
package pex.app.main;

//FIXME import used core classes
import pex.InterpreterHandler;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.Input;

/**
 * Create new program.
 */
public class DoNewProgram extends Command<InterpreterHandler> {
  /** Input field. */
  Input<String> _programName;

  /**
   * @param receiver
   */
  public DoNewProgram(InterpreterHandler receiver) {
    super(Label.NEW_PROGRAM, receiver);
    _programName = _form.addStringInput(Message.requestProgramId());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    _form.parse();
    _receiver.newProgram(_programName.value());
  }
}
