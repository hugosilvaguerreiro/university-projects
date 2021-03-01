// $Id: function_call_node.h,v 1.5 2018/05/17 17:52:51 ist424778 Exp $ -*- c++ -*-
#ifndef __GR8_FUNCTIONCALLNODE_H__
#define __GR8_FUNCTIONCALLNODE_H__

#include <cdk/ast/expression_node.h>
#include <cdk/ast/sequence_node.h>


namespace gr8 {

  /**
   * Class for describing function call nodes.
   */
  class function_call_node: public cdk::expression_node {
    cdk::sequence_node *_arguments;
    std::string _functionIdentifier;

  public:
    inline function_call_node(int lineno, cdk::sequence_node *arguments, std::string *functionIdentifier) :
        cdk::expression_node(lineno),_arguments(arguments), _functionIdentifier(*functionIdentifier) {
    }

  public:
    inline cdk::sequence_node *arguments() {
      return _arguments;
    }
    inline std::string functionIdentifier() {
      return _functionIdentifier;
    }

    void accept(basic_ast_visitor *sp, int level) {
      sp->do_function_call_node(this, level);
    }

  };

} // gr8

#endif
