/** @version  */
package pex.app.evaluator;

import pex.operators.Program;
import pex.Interpreter;
import pex.atomic.StringLiteral;
import pex.app.OperatorsPrintVisitor;
/**
 * Show program (present code).
 */
public class DoShowProgram extends ProgramCommand {

  /**
   * @param interpreter
   * @param receiver
   */
  public DoShowProgram(Interpreter interpreter, Program receiver) {
    super(Label.SHOW_PROGRAM, interpreter, receiver);
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    Program p = _interpreter.getProgram(_receiver.getName());
    StringLiteral print =  (StringLiteral)p.accept(new OperatorsPrintVisitor());
    _display.popup(print.getValue());
  }
}
