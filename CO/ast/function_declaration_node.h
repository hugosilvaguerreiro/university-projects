// $Id: function_declaration_node.h,v 1.9 2018/05/18 16:02:10 ist424861 Exp $ -*- c++ -*-
#ifndef __GR8_FUNCTIONDECLARATIONNODE_H__
#define __GR8_FUNCTIONDECLARATIONNODE_H__

#include <cdk/ast/sequence_node.h>
#include <cdk/basic_type.h>
#include <cdk/ast/typed_node.h>


namespace gr8 {

  /**
   * Class for function declaration nodes.
   */
  class function_declaration_node: public cdk::typed_node {
    bool _imported;
    bool _publ;
    std::string _funcName;
    cdk::sequence_node *_arguments;

  public:
    inline function_declaration_node(int lineno, basic_type *type, bool imported, bool publ, std::string *funcName, cdk::sequence_node *arguments) :
        typed_node(lineno), _imported(imported), _publ(publ), _funcName(*funcName), _arguments(arguments) {

        cdk::typed_node::type(type);
    }

  public:
    inline bool imported() {
      return _imported;
    }

    inline bool publ() {
      return _publ;
    }

    inline std::string funcName() {
      return _funcName;
    }

    inline void funcName(const char *s){
      _funcName = s;
    }

    inline cdk::sequence_node *arguments() {
      return _arguments;
    }

    void accept(basic_ast_visitor *sp, int level) {
      sp->do_function_declaration_node(this, level);
    }

  };

} // gr8

#endif
