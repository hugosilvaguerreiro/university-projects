/*  */
package pex.app.evaluator;

/** Menu entries for the section editing menu. */
@SuppressWarnings("nls")
public interface Label {

	/** Menu title. */
	String TITLE = "Menu de Edição";

  /** 3.2.1 Show program. */
  String SHOW_PROGRAM = "Listar programa";

  /** 3.2.2 Run program. */
  String RUN_PROGRAM = "Executar programa";

	/** 3.2.3 Add expression. */
	String ADD_EXPRESSION = "Adicionar expressão ao programa";

	/** 3.2.4 Replace expression. */
	String REPLACE_EXPRESSION = "Substituir expressão no programa";

  /** 3.2.5 Show all identifiers. */
  String SHOW_ALL_IDENTIFIERS = "Mostrar os identificadores presentes no programa";

  /** 3.2.6 Show uninitialized identifiers. */
  String SHOW_UNINITIALIZED_IDENTIFIERS = "Mostrar os identificadores do programa sem inicialização explícita";

}
