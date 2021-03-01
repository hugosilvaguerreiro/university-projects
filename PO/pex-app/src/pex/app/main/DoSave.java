/** @version  */
package pex.app.main;

import java.io.IOException;

//FIXME import used core classes
import pex.InterpreterHandler;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.Input;

/**
 * Save to file under current name (if unnamed, query for name).
 */
public class DoSave extends Command<InterpreterHandler> {
  /** Input field. */
  Input<String> _fileName;

  /**
   * @param receiver
   */
  public DoSave(InterpreterHandler receiver) {
    super(Label.SAVE, receiver);
    _fileName = _form.addStringInput(Message.newSaveAs());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    if(_receiver.getModifiedState()) {
      try {
        if(_receiver.getInterpreter().getFileName() == null) {
          _form.parse();
          _receiver.associateFile(_fileName.value());
        }
        _receiver.save();
      }catch(IOException exception) {exception.printStackTrace();}
    }
  }
}
