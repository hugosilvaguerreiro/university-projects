package pex.operators;

import pex.Value;
import pex.OperatorsVisitor;
import pex.Expression;
import pex.Value;

/**
 * Class for describing the If conditional operator
 */
public class If extends Expression {
  /** First operand. */
  private Expression _first;

  /** Second operand. */
  private Expression _second;

  /** Third operand. */
  private Expression _third;

  /** String that holds the identation of the operator in a program */
  private String _identation = "";

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608281352L;

  /**
   * @param first
   * @param second
   */
  public If(Expression first, Expression second, Expression third) {
    _first = first;
    _second = second;
    _third = third;
  }

  /**
   * @return Expression
   */
  public Expression first() {
    return _first;
  }

  /**
   * @return Expression
   */
  public Expression second() {
    return _second;
  }

  /**
   * @return Expression
   */
  public Expression third() {
    return _third;
  }

  /**
   * @return String
   */
  public String getIdentation() {
    return _identation;
  }

  /**
   * @param identation
   */
  public void setIdentation(String identation) {
    _identation = identation;
  }

  /**
   * @param visitor
   * @return A value depending of the type of the visitor
   */
  public Value<?> accept(OperatorsVisitor visitor) {
    return visitor.visitIf(this);
  }
}
