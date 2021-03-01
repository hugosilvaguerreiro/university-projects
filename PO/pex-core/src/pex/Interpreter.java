package pex;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.io.Serializable;
import pex.operators.Program;
import pex.atomic.Identifier;

/** Class for describing interpreters. */
public class Interpreter implements Serializable{

  /** Map containing the programs loaded in the interpreter */
  Map<String, Program> _programs;
  /** Map containing the identifiers saved in the interpreter */
  Map<String, Value<?>> _identifiers;

  /** Parser. */
  Parser _parser;

  /** name of the file associated to the Interpreter. */
  String _fileName = null;

  /**
  * default constructor
  */
  public Interpreter() {
    _programs = new HashMap<String, Program>();
    _identifiers = new HashMap<String, Value<?>>();
    _parser = new Parser();
  }

  /**
   * @param name
   * @param program
   */
  public void put(String name, Program program) {
    _programs.put(name, program);
    program.setName(name);
  }

  /**
   * @param name
   * returns a program from within the interpreter
   */
  public Program getProgram(String name) {
    return _programs.get(name);
  }

  /**
   * @param name
   * @param identifier
   */
  public void putIdentifier(String name, Value<?> identifierValue) {
    _identifiers.put(name, identifierValue);
  }

  /**
   * @param name
   * @param identifier
   */
  public void putIdentifier(String name) {
    _identifiers.put(name, null);
  }

  /**
   * @param name
   * @return a program from within the interpreter
   */
  public Value<?> getIdentifierValue(String identifierName) {
    return _identifiers.get(identifierName);
  }

  /**
   * @param identifierName
   * @return  boolean that says if the interpreter contains tha given identifierName
   */
  public boolean containsIdentifier(String identifierName) {
    return _identifiers.containsKey(identifierName);
  }

  /**
   * @param fileName
   */
  public void setFileName(String fileName) {
    _fileName = fileName;
  }

  /**
   * @return The name of the interpreter
   */
  public String getFileName() {
    return _fileName;
  }

  /**
   * @param programName
   * @return The program to be shown
   */
  public Program showProgram(String programName) {
    return getProgram(programName);
  }

  /**
   * @param fileName
   * @throws ParserException
   */
  public void interprete(String fileName) throws ParserException{
      put(fileName, _parser.parseProgramFile(fileName));
  }

  /**
   * @param fileName
   * @throws ParserException
   */
  public void interpreteImportFile(String fileName) throws ParserException{
      put("import", _parser.parseProgramFile(fileName));
  }

  /**
   * @param expression
   * @return The expression that was parsed
   * @throws ParserException
   */
   public Expression interpreteExpression(String expression) throws ParserException{
    return _parser.parse(expression);
   }
}
