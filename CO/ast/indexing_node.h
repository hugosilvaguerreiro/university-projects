// $Id: indexing_node.h,v 1.5 2018/04/18 22:40:35 ist424778 Exp $ -*- c++ -*-
#ifndef __GR8_INDEXINGNODE_H__
#define __GR8_INDEXINGNODE_H__

#include <cdk/ast/expression_node.h>
#include <cdk/ast/lvalue_node.h>

namespace gr8 {

  /**
   * Class for describing indexing nodes.
   */
  class indexing_node: public cdk::lvalue_node {
    cdk::expression_node *_indexPosition;
    cdk::expression_node   *_pointer;

  public:
    inline indexing_node(int lineno, cdk::expression_node *indexPosition, cdk::expression_node * pointer) :
        cdk::lvalue_node(lineno), _indexPosition(indexPosition), _pointer(pointer) {
    }

  public:
    inline cdk::expression_node *indexPosition() {
      return _indexPosition;
    }

    inline cdk::expression_node *pointer() {
      return _pointer;
    }

    void accept(basic_ast_visitor *sp, int level) {
      sp->do_indexing_node(this, level);
    }

  };

} // gr8

#endif
