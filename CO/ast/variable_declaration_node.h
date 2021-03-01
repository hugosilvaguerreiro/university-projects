// $Id: variable_declaration_node.h,v 1.6 2018/04/17 14:41:52 ist424778 Exp $
#ifndef __GR8_VARIABLEDECLARATIONNODE_H__
#define __GR8_VARIABLEDECLARATIONNODE_H__

#include <cdk/ast/typed_node.h>
#include <cdk/basic_type.h>

namespace gr8 {

  /**
   * Class for describing variable declaration nodes.
   */
  class variable_declaration_node : public cdk::typed_node {
    bool _publ;
    bool _imported;
    std::string _varName;

  public:
    inline variable_declaration_node(int lineno, bool publ, bool imported, basic_type *type, std::string *varName) :
        cdk::typed_node(lineno),_publ(publ), _imported(imported), _varName(*varName) {

          cdk::typed_node::type(type);
    }

  public:
    
    inline bool imported() {
      return _imported;
    }

    inline bool publ() {
      return _publ;
    }

    inline std::string varName() {
      return _varName;
    }

    void accept(basic_ast_visitor *sp, int level) {
      sp->do_variable_declaration_node(this, level);
    }

  };

} // gr8

#endif
