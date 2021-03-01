package pex;

import pex.Interpreter;
import pex.ParserException;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import pex.operators.Program;

/** Class for describing the Manager of all the interpreters */
public class InterpreterHandler{
  /** current Interpreter of the InterpreterHandler.  */
  Interpreter _interpreter;

  /** Boolean variable to check whether there was or not a change in the current interpreter   */
  boolean _Modified = true;

  /** Default constructor */
  public InterpreterHandler() {
    _interpreter = new Interpreter();
  }

  /**
  * returns the current loaded interpreter.
  */
  public Interpreter getInterpreter() {
    return _interpreter;
  }

  /**
  * returns the Modified state of the Interpreter.
  */
  public boolean getModifiedState() {
    return _Modified;
  }

  /**
  * creates a new interpreter and Loads it into the Handler.
  */
  public void newInterpreter() {
    _interpreter = new Interpreter();
    _Modified = true;
  }

  /**
  * @param file
  * opens the interpreter with the name give in the parameter.
  * @throws IOException
  * @throws ClassNotFoundException
  */
  public void open(String file) throws IOException, ClassNotFoundException {
      ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
      Interpreter interpreter = (Interpreter) ois.readObject();
      ois.close();
      _interpreter = interpreter;
      _Modified = true;
  }

  /**
  * Saves a interpreter under a file
  * @throws IOException
  */
  public void save() throws IOException{
    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(_interpreter.getFileName())));
    oos.writeObject(_interpreter);
    oos.close();
    _Modified = false;
  }

  /**
  * @param fileName
  * Loads a new program into the currently loaded interpreter
  */
  public void newProgram(String fileName) {
    _interpreter.put(fileName, new Program());
    _Modified = true;
  }

  /**
  * @param filename
  * Associates a name with the currently loaded interpreter
  */
  public void associateFile(String fileName) {
    _interpreter.setFileName(fileName);
    _Modified = true;
  }

  /**
  * @param fileName
  * @throws ParserException
  * reads a program file, gets it interpreted and loads it into the interpreter
  */
  public void readProgram(String fileName) throws ParserException{
      _interpreter.interprete(fileName);
      _Modified = true;
  }

  /**
  * @param fileName
  * @throws ParserException
  * reads a special import file, gets it interpreted and loads it into the interpreter
  */
  public void readImportProgram(String fileName) throws ParserException{
      _interpreter.interpreteImportFile(fileName);
      _Modified = true;
  }
}
