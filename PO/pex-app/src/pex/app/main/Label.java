/*  */
package pex.app.main;

/** Menu entries for the main menu. */
@SuppressWarnings("nls")
public interface Label {

	/** Menu title. */
	String TITLE = "Menu Principal";

  /** Create new interpreter. */
  String NEW = "Criar";

  /** Open existing interpreter. */
  String OPEN = "Abrir";

  /** Save state in an existing file. */
  String SAVE = "Guardar";

  /** Create new program. */
  String NEW_PROGRAM = "Criar Programa";

  /** Read existing program. */
  String READ_PROGRAM = "Ler Programa";

  /** Write program. */
  String WRITE_PROGRAM = "Escrever Programa";

  /** Edit program.. */
	String MANAGE_PROGRAM = "Manipular Programa";

}