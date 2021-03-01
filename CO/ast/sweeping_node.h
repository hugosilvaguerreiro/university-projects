// $Id: sweeping_node.h,v 1.3 2018/05/10 08:45:10 ist424861 Exp $ -*- c++ -*-
#ifndef __GR8_SWEEPINGNODE_H__
#define __GR8_SWEEPINGNODE_H__

#include <cdk/ast/expression_node.h>
#include <cdk/ast/lvalue_node.h>
#include <cdk/ast/basic_node.h>

namespace gr8 {

  /**
   * Class sweeping describing sweeping-cycle nodes.
   */
  class sweeping_node: public cdk::basic_node {
    cdk::lvalue_node *_incrementableValue;
    cdk::expression_node *_start;
    cdk::expression_node *_end;
    cdk::basic_node *_block;
    cdk::expression_node *_increment;

  public:
    inline sweeping_node(int lineno,cdk::lvalue_node *incrementableValue, cdk::expression_node *start,
                    cdk::expression_node *end, cdk::basic_node *block, cdk::expression_node *increment) :
        basic_node(lineno), _incrementableValue(incrementableValue), _start(start), _end(end), _block(block), _increment(increment) {
    }

  public:
    inline cdk::lvalue_node *incrementableValue() {
      return _incrementableValue;
    }

    inline cdk::expression_node *start() {
      return _start;
    }

    inline cdk::expression_node *end() {
      return _end;
    }
    inline cdk::basic_node *block() {
      return _block;
    }
    inline cdk::expression_node *increment() {
      return _increment;
    }

    void accept(basic_ast_visitor *sp, int level) {
      sp->do_sweeping_node(this, level);
    }

  };

} // gr8

#endif
