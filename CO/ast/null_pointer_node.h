// $Id: null_pointer_node.h,v 1.1 2018/03/23 13:34:37 ist424778 Exp $ -*- c++ -*-
#ifndef __GR8_NULLPOINTERNODE_H__
#define __GR8_NULLPOINTERNODE_H__

#include <cdk/ast/expression_node.h>

namespace gr8 {

  /**
   * Class for describing null pointer nodes i.e accesses to memory addresses.
   */
  class null_pointer_node : public cdk::expression_node {

  public:
    inline null_pointer_node(int lineno) :
        cdk::expression_node(lineno){
    }

  public:
    void accept(basic_ast_visitor *sp, int level) {
      sp->do_null_pointer_node(this, level);
    }

  };

} // gr8

#endif
