// $Id: identity_node.h,v 1.1 2018/03/22 16:22:22 ist424778 Exp $ -*- c++ -*-
#ifndef __GR8_IDENTITYNODE_H__
#define __GR8_IDENTITYNODE_H__

#include <cdk/ast/unary_expression_node.h>

namespace gr8 {

  /**
   * Class for describing identity nodes.
   */
  class identity_node: public cdk::unary_expression_node {
  public:
    inline identity_node(int lineno, cdk::expression_node *number) :
        cdk::unary_expression_node(lineno, number){
    }

  public:
    void accept(basic_ast_visitor *sp, int level) {
      sp->do_identity_node(this, level);
    }

  };

} // gr8

#endif