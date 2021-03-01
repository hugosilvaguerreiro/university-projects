/** @version  */
package pex.app;

import static pt.tecnico.po.ui.Dialog.IO;
import pex.InterpreterHandler;
import pex.ParserException;
import pex.app.main.MainMenu;
import pt.tecnico.po.ui.Menu;
import pex.ParserException;
/**
 * This is a sample client for the expression evaluator.
 * It uses a text-based user interface.
 */
public class App {
  /**
   * @param args
   */
  public static void main(String[] args) {
    InterpreterHandler interpreterHandler = new InterpreterHandler();

    String datafile = System.getProperty("import"); //$NON-NLS-1$
    if (datafile != null) {
      try {
          interpreterHandler.readImportProgram(datafile);
      } catch (ParserException e) {
        // no behavior described: just present the problem
        e.printStackTrace();
      }
    }
    Menu menu = new MainMenu(interpreterHandler);
    menu.open();
    IO.close();
  }

}
