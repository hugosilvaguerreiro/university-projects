/*  */
package pex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Pattern;
import pex.atomic.Identifier;
import pex.atomic.IntegerLiteral;
import pex.atomic.StringLiteral;
import pex.exceptions.BadExpressionException;
import pex.exceptions.BadNumberException;
import pex.exceptions.ExtraneousDataAtEndOfInputException;
import pex.exceptions.MissingClosingParenthesisException;
import pex.exceptions.EndOfInputException;
import pex.exceptions.UnknownOperationException;
import pex.operators.Add;
import pex.operators.And;
import pex.operators.Call;
import pex.operators.Div;
import pex.operators.Eq;
import pex.operators.Ge;
import pex.operators.Gt;
import pex.operators.If;
import pex.operators.Le;
import pex.operators.Lt;
import pex.operators.Mod;
import pex.operators.Mul;
import pex.operators.Ne;
import pex.operators.Neg;
import pex.operators.Not;
import pex.operators.Or;
import pex.operators.Print;
import pex.operators.Program;
import pex.operators.Readi;
import pex.operators.Reads;
import pex.operators.Sequence;
import pex.operators.Set;
import pex.operators.Sub;
import pex.operators.While;

/**
 * Create expressions from text descriptions.
 */
@SuppressWarnings("nls")
public class Parser implements Serializable {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201608281352L;

  /**
   * @param description
   * @return expression
   * @throws UnknownOperationException
   * @throws MissingClosingParenthesisException
   * @throws BadNumberException
   * @throws BadExpressionException
   * @throws EndOfInputException
   * @throws ExtraneousDataAtEndOfInputException
   */
  public Expression parse(String description)
      throws BadNumberException, MissingClosingParenthesisException, UnknownOperationException,
      BadExpressionException, EndOfInputException, ExtraneousDataAtEndOfInputException {
    try {
      return parse(new PushbackReader(new StringReader(description)));
    } catch (IOException ioe) {
      throw new BadExpressionException(description, ioe);
    }
  }

  /**
   * @param filename
   * @return expression
   * @throws ExtraneousDataAtEndOfInputException
   * @throws EndOfInputException
   * @throws MissingClosingParenthesisException
   * @throws UnknownOperationException
   * @throws BadNumberException
   * @throws BadExpressionException
   */
  public Program parseProgramFile(String filename) throws ExtraneousDataAtEndOfInputException,
      EndOfInputException, MissingClosingParenthesisException, UnknownOperationException,
      BadNumberException, BadExpressionException {
    try {
      PushbackReader input = new PushbackReader(new BufferedReader(new FileReader(filename)));
      Program program = new Program();
      try {
        while (true)
          program.add(parse(input));
      } catch (EndOfInputException e) {
        // expected end of input
      }
      input.close();
      return program;
    } catch (IOException ioe) {
      throw new BadExpressionException(filename, ioe);
    }
  }

  /**
   * @param input
   * @return expression
   * @throws ExtraneousDataAtEndOfInputException
   * @throws EndOfInputException
   * @throws UnknownOperationException
   * @throws MissingClosingParenthesisException
   * @throws BadNumberException
   * @throws IOException
   */
  private Expression parse(PushbackReader input)
      throws ExtraneousDataAtEndOfInputException, EndOfInputException, IOException,
      BadNumberException, MissingClosingParenthesisException, UnknownOperationException {
    trim(input);

    int c = input.read();

    if (c == -1)
      throw new EndOfInputException(); // special case: end of input

    if (c == '(') {
      Expression expression = parseCompositeExpression(input);
      trim(input);
      if (input.read() != ')')
        throw new MissingClosingParenthesisException();
      return expression;
    }

    input.unread(c);

    if (c >= '0' && c <= '9')
      return parseNumberLiteral(input);

    if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')
      return parseIdentifier(input);

    if (c == '"')
      return parseStringLiteral(input);

    throw new ExtraneousDataAtEndOfInputException(c);
  }

  /**
   * @param input
   * @return expression
   * @throws IOException
   * @throws MissingClosingParenthesisException
   * @throws BadNumberException
   * @throws UnknownOperationException
   * @throws EndOfInputException
   * @throws ExtraneousDataAtEndOfInputException
   */
  private Expression parseCompositeExpression(PushbackReader input)
      throws IOException, BadNumberException, MissingClosingParenthesisException,
      UnknownOperationException, EndOfInputException, ExtraneousDataAtEndOfInputException {
    trim(input);

    int c;
    String operation = "";
    while ((c = input.read()) >= 'a' && c <= 'z')
      operation += (char) c;
    input.unread(c); // return first unmatched char

    // Regular expressions patterns to match specific groups.
    Pattern patSimple = Pattern.compile(
        "^(lt|le|eq|ge|gt|ne|add|sub|mul|div|mod|if|while|set|neg|readi|reads|call|and|or|not)");
    Pattern patMultiArg = Pattern.compile("^(print|seq)");

    if (patSimple.matcher(operation).matches())
      return parseSimpleOperation(operation, input);

    if (patMultiArg.matcher(operation).matches()) {
      return parseVarArgOperation(operation, input);
    }

    throw new UnknownOperationException(operation);
  }

  /**
   * @param operation
   * @param input
   * @return the new expression
   * @throws ExtraneousDataAtEndOfInputException
   * @throws EndOfInputException
   * @throws MissingClosingParenthesisException
   * @throws UnknownOperationException
   * @throws IOException
   * @throws BadNumberException
   */
  private Expression parseSimpleOperation(String operation, PushbackReader input)
      throws ExtraneousDataAtEndOfInputException, EndOfInputException,
      MissingClosingParenthesisException, UnknownOperationException, IOException,
      BadNumberException {
    switch (operation) {
    case "add":
      return new Add(parse(input), parse(input));
    case "mul":
      return new Mul(parse(input), parse(input));
    case "not":
      return new Not(parse(input));
    case "and":
      return new And(parse(input), parse(input));
    case "call":
      return new Call(parse(input));
    case "div":
      return new Div(parse(input), parse(input));
    case "eq":
      return new Eq(parse(input), parse(input));
    case "ge":
      return new Ge(parse(input), parse(input));
    case "gt":
      return new Gt(parse(input), parse(input));
    case "if":
      return new If(parse(input), parse(input), parse(input));
    case "le":
      return new Le(parse(input), parse(input));
    case "lt":
      return new Lt(parse(input), parse(input));
    case "mod":
      return new Mod(parse(input), parse(input));
    case "ne":
      return new Ne(parse(input), parse(input));
    case "neg":
      return new Neg(parse(input));
    case "or":
      return new Or(parse(input), parse(input));
    case "readi":
      return new Readi();
    case "reads":
      return new Reads();
    case "set":
      return new Set(parse(input), parse(input));
    case "sub":
      return new Sub(parse(input), parse(input));
    case "while":
      return new While(parse(input), parse(input));
    default:
      return null; // cannot happen
    }
  }

  /**
   * @param operation
   * @param input
   * @return the new expression
   * @throws EndOfInputException
   * @throws MissingClosingParenthesisException
   * @throws UnknownOperationException
   * @throws IOException
   * @throws BadNumberException
   * @throws ExtraneousDataAtEndOfInputException
   */
  private Expression parseVarArgOperation(String operation, PushbackReader input)
      throws EndOfInputException, MissingClosingParenthesisException,
      UnknownOperationException, IOException, BadNumberException,
      ExtraneousDataAtEndOfInputException {
    ArrayList<Expression> expressions = new ArrayList<>();
    try {
      while (true)
        expressions.add(parse(input));
    } catch (ExtraneousDataAtEndOfInputException e) {
      // this exception makes sense here, but it may not be a real exception
      // in an outer context
      if (e.getC() != ')')
        throw new ExtraneousDataAtEndOfInputException(e);
    }

    switch (operation) {
    case "seq":
      return new Sequence(expressions);
    case "print":
      return new Print(expressions);
    default:
      return null; // cannot happen
    }
  }

  /**
   * @param input
   * @return expression
   * @throws IOException
   * @throws BadNumberException
   * @throws EndOfInputException
   */
  private Expression parseNumberLiteral(PushbackReader input)
      throws IOException, BadNumberException, EndOfInputException {
    trim(input); // just in case

    int c;
    String token = "";

    do {
      c = input.read();
      if (c == -1)
        throw new EndOfInputException(); // special case: end of input
      token += (char) c;
    } while (c >= '0' && c <= '9');

    c = token.charAt(token.length() - 1);
    token = token.substring(0, token.length() - 1);

    input.unread(c); // return first unmatched char

    return new IntegerLiteral(Integer.parseInt(token));
  }

  /**
   * @param input
   * @return expression
   * @throws IOException
   * @throws EndOfInputException
   */
  private Expression parseIdentifier(PushbackReader input)
      throws IOException, EndOfInputException {
    trim(input); // just in case

    int c;
    String token = "";

    do {
      c = input.read();
      if (c == -1)
        throw new EndOfInputException(); // special case: end of input
      token += (char) c;
    } while (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9');

    c = token.charAt(token.length() - 1);
    token = token.substring(0, token.length() - 1);
    input.unread(c); // return first unmatched char

    return new Identifier(token);
  }

  /**
   * @param input
   * @return expression
   * @throws EndOfInputException
   * @throws IOException
   */
  private Expression parseStringLiteral(PushbackReader input)
      throws EndOfInputException, IOException {
    trim(input); // just in case

    int c = input.read();
    String token = "";
    boolean afterSpecialChar = false;

    do {
      afterSpecialChar = false;
      c = input.read();

      if (c == -1)
        throw new EndOfInputException(); // special case: end of input

      if (c == '\\') {
        afterSpecialChar = true;
        switch (c = input.read()) {
        case 'n':
          c = '\n';
          break;
        case 't':
          c = '\t';
          break;
        case 'r':
          c = '\r';
          break;
        default:
          // do nothing
        }
      }

      token += (char) c;
    } while (afterSpecialChar || c != '"');

    return new StringLiteral(token.substring(0, token.length() - 1));
  }

  /**
   * Strip whitespace.
   *
   * @param input
   * @throws IOException
   */
  private void trim(PushbackReader input) throws IOException {
    int c;
    while ((c = input.read()) == ' ' || c == '\t' || c == '\n')
      ;
    if (c == -1)
      return;
    input.unread(c); // return first unmatched char
  }

}
