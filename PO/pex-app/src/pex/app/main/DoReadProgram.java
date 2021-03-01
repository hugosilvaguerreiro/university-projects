/** @version  */
package pex.app.main;

import pex.InterpreterHandler;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.Input;
import pex.app.BadFileException;
import pex.ParserException;

/**
 * Read existing program.
 */
public class DoReadProgram extends Command<InterpreterHandler> {
  /** Input field. */
  Input<String> _filename;

  /**
   * @param receiver
   */
  public DoReadProgram(InterpreterHandler receiver) {
    super(Label.READ_PROGRAM, receiver);
    _filename = _form.addStringInput(Message.programFileName());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() throws BadFileException {
    _form.parse();
    try {
      _receiver.readProgram(_filename.value());
    } catch (ParserException badfile) {
        badfile.printStackTrace();
        throw new BadFileException(_filename.value());
    }
  }

}
