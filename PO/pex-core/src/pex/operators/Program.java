/*  */
package pex.operators;


import pex.OperatorsVisitor;
import pex.Expression;
import pex.Value;
import pex.Value;
import java.util.List;
/**
 * Class for describing programs.
 */
public class Program extends Sequence{
  /** Program name */
  String _name = "";


  /**
   * Default constructor (empty program)
   */
  public Program() {
    super();
  }

  /**
  * Constructor for single-expression programs
  * @param expression
   */
  public Program(Expression expression) {
    super(expression);
  }

  /**
   * Constructor for appending expressions to program
   *
   * @param previous
   * @param expression
   */
  public Program(Sequence previous, Expression expression) {
    super(previous, expression);
  }

  /**
   * @param expressions
   */
  public Program(List<Expression> expressions) {
    super(expressions);
  }

  /**
   * @param name
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * @return The name of the program
   */
  public String getName() {
    return _name;
  }

  /**
   * @param position
   * @param expression
   */
   public void addExpression(int position, Expression expression) {
     add(position, expression);
   }

   /**
    * @param position
    * @param expression
    */
    public void replaceExpression(int position, Expression expression) {
      set(position, expression);
    }

      /**
       * @param visitor
       * @return A value depending of the type of the visitor
       */
      public Value<?> accept(OperatorsVisitor visitor) {
        return visitor.visitProgram(this);
      }
    }
