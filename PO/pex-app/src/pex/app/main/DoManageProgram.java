/** @version  */
package pex.app.main;

import pex.InterpreterHandler;
import pex.app.evaluator.EvaluatorMenu;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.Input;
import pt.tecnico.po.ui.Menu;

/**
 * Open menu for managing programs.
 */
public class DoManageProgram extends Command<InterpreterHandler> {
  /** Input field. */
  Input<String> _program;

  /**
   * @param receiver
   */
  public DoManageProgram(InterpreterHandler receiver) {
    super(Label.MANAGE_PROGRAM, receiver);
    _program = _form.addStringInput(Message.requestProgramId());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    _form.parse();
    if(_receiver.getInterpreter().getProgram(_program.value())!= null) {
      EvaluatorMenu menu = new EvaluatorMenu(_receiver.getInterpreter(), _receiver.getInterpreter().getProgram(_program.value()));
      menu.open();
    }
    else {
       _display.popup(Message.noSuchProgram(_program.value()));
    }
  }

}
