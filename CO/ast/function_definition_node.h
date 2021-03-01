// $Id: function_definition_node.h,v 1.9 2018/05/18 16:02:10 ist424861 Exp $
#ifndef __GR8_FUNCTIONDEFINITIONNODE_H__
#define __GR8_FUNCTIONDEFINITIONNODE_H__

#include <cdk/ast/sequence_node.h>
#include <cdk/ast/typed_node.h>
#include <cdk/basic_type.h>

namespace gr8 {

  /**
   * Class for describing function definition nodes.
   */
  class function_definition_node : public cdk::typed_node {
    block_node *_block;
    bool _publ;
    bool _imported;
    std::string _funcName;
    cdk::sequence_node *_arguments;
  public:
    inline function_definition_node(int lineno, block_node *block, bool publ,bool imported, std::string *funcName, cdk::sequence_node *arguments, basic_type *type) :
        cdk::typed_node(lineno),_block(block), _publ(publ), _imported(imported), _funcName(*funcName), _arguments(arguments) {
          cdk::typed_node::type(type);
    }

  public:
    inline block_node *block() {
      return _block;
    }
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
      sp->do_function_definition_node(this, level);
    }

  };

} // gr8

#endif
