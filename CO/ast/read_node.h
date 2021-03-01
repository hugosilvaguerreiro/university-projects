// $Id: read_node.h,v 1.2 2018/03/21 22:11:12 ist424861 Exp $ -*- c++ -*-
#ifndef __GR8_READNODE_H__
#define __GR8_READNODE_H__

namespace gr8 {

  /**
   * Class for describing read nodes.
   */
  class read_node : public cdk::expression_node {

  public:
    inline read_node(int lineno) :
        cdk::expression_node(lineno) {
    }

  public:
    void accept(basic_ast_visitor *sp, int level) {
      sp->do_read_node(this, level);
    }

  };

} // gr8

#endif
