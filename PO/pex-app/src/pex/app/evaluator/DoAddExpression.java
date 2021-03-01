/** @version  */
package pex.app.evaluator;

import pex.Interpreter;
import pex.app.BadExpressionException;
import pex.app.BadPositionException;
import pex.operators.Program;
import pt.tecnico.po.ui.DialogException;
import pt.tecnico.po.ui.Input;
import pex.Expression;
import pex.ParserException;

/**
 * Add expression.
 */
public class DoAddExpression extends ProgramCommand {
  /** Input field. */
  Input<Integer> _position;

  /** Input field. */
  Input<String> _description;

  /**
   * @param interpreter
   * @param receiver
   */
  public DoAddExpression(Interpreter interpreter, Program receiver) {
    super(Label.ADD_EXPRESSION, interpreter, receiver);
    _position = _form.addIntegerInput(Message.requestPosition());
    _description = _form.addStringInput(Message.requestExpression());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() throws DialogException, BadPositionException {
    _form.parse();
    if(_position.value() > _receiver.size() || _position.value() < 0) 
      throw new BadPositionException(_position.value());
    try {
      Expression expression = _interpreter.interpreteExpression(_description.value());
      _receiver.addExpression(_position.value(), expression);
    }catch( ParserException exception) {
      exception.printStackTrace();
    }
  }
}
