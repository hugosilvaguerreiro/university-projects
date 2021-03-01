// $Id: reference_node.h,v 1.5 2018/04/14 17:41:05 ist424778 Exp $ -*- c++ -*-
#ifndef __GR8_REFERENCENODE_H__
#define __GR8_REFERENCENODE_H__

#include <cdk/ast/lvalue_node.h>
#include <cdk/ast/expression_node.h>
namespace gr8 {

  /**
   * Class for describing reference nodes i.e get the address of .
   */
  class reference_node : public cdk::expression_node {
    cdk::lvalue_node *_address;

  public:
    inline reference_node(int lineno, cdk::lvalue_node *address) :
        cdk::expression_node(lineno), _address(address) {
    }

  public:
    inline cdk::lvalue_node *address() {
      return _address;
    }

    void accept(basic_ast_visitor *sp, int level) {
      sp->do_reference_node(this, level);
    }

  };

} // gr8

#endif
