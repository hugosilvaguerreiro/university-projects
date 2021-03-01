// $Id: variable_initialization_node.h,v 1.5 2018/04/19 18:09:36 ist424778 Exp $
#ifndef __GR8_VARIABLEINITIALIZATIONNODE_H__
#define __GR8_VARIABLEINITIALIZATIONNODE_H__

#include <cdk/ast/typed_node.h>
#include <cdk/ast/expression_node.h>
#include <cdk/basic_type.h>

namespace gr8 {

  /**
   * Class for describing variable initialization nodes.
   */
  class variable_initialization_node : public cdk::typed_node {
    bool _publ;
    bool _imported;
    std::string _identifierName;
    cdk::expression_node *_init_value;

  public:
    inline variable_initialization_node(int lineno, bool publ,  bool imported,  basic_type *type, std::string *identifierName, cdk::expression_node *init_value) :
        cdk::typed_node(lineno), _publ(publ),_imported(imported), _identifierName(*identifierName), _init_value(init_value) {
        cdk::typed_node::type(type);
    }

  public:

    inline bool publ() {
      return _publ;
    }

    inline bool imported() {
      return _imported;
    }

    inline std::string identifierName()  {
      return _identifierName;
    }

    inline cdk::expression_node *init_value() {
      return _init_value;
    }

    void accept(basic_ast_visitor *sp, int level) {
      sp->do_variable_initialization_node(this, level);
    }

  };

} // gr8

#endif
