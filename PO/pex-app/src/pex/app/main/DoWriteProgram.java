/** @version  */
package pex.app.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import pex.InterpreterHandler;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.Input;
import pex.app.OperatorsPrintVisitor;
import pex.Interpreter;
import pex.operators.Program;
import pex.atomic.StringLiteral;

/**
 * Write (save) program to file.
 */
public class DoWriteProgram extends Command<InterpreterHandler> {
  /** Input field. */
  Input<String> _programName;
  /** Input field. */
  Input<String> _filename;

  /**
   * @param receiver
   */
  public DoWriteProgram(InterpreterHandler receiver) {
    super(Label.WRITE_PROGRAM, receiver);
    _programName = _form.addStringInput(Message.requestProgramId());
    _filename = _form.addStringInput(Message.programFileName());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    _form.parse();
    if(_receiver.getInterpreter().getProgram(_programName.value()) != null) {
      try {
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_filename.value())));
        Interpreter i = _receiver.getInterpreter();
        Program p = i.getProgram(_programName.value());
        StringLiteral output = (StringLiteral)p.accept(new OperatorsPrintVisitor());
        out.write(output.getValue());
        out.close();
      }catch(IOException e) {e.printStackTrace();}
    }
    else {
      _display.popup(Message.noSuchProgram( _programName.value()));
    }
  }
}
