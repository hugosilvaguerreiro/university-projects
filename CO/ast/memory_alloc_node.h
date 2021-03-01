// $Id: memory_alloc_node.h,v 1.1 2018/03/22 16:22:22 ist424778 Exp $ -*- c++ -*-
#ifndef __GR8_MEMORYALLOCNODE_H__
#define __GR8_MEMORYALLOCNODE_H__

#include <cdk/ast/unary_expression_node.h>

namespace gr8 {

  /**
   * Class for describing memory alloc nodes.
   */
  class memory_alloc_node: public cdk::unary_expression_node {
  public:
    inline memory_alloc_node(int lineno, cdk::expression_node *nrObjsToAlloc) :
        cdk::unary_expression_node(lineno, nrObjsToAlloc){
    }

  public:
    void accept(basic_ast_visitor *sp, int level) {
      sp->do_memory_alloc_node(this, level);
    }

  };

} // gr8

#endif