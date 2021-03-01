/** @version  */
package pex.app.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import pex.app.main.Message;
import pex.InterpreterHandler;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.Input;

/**
 * Open existing interpreter.
 */
public class DoOpen extends Command<InterpreterHandler> {
  Input<String> _fileName;
  /**
   * @param receiver
   */
  public DoOpen(InterpreterHandler receiver) {
    super(Label.OPEN, receiver);
    _fileName = _form.addStringInput(Message.openFile());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    _form.parse();
    try {
      _receiver.open(_fileName.value());
    }catch(IOException exception) { _display.popup(Message.fileNotFound());}
     catch(ClassNotFoundException classException){ classException.printStackTrace();}
  }
}
