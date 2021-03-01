// $Id: block_node.h,v 1.1 2018/03/21 23:03:25 ist424778 Exp $ -*- c++ -*-
#ifndef __GR8_BLOCKNODE_H__
#define __GR8_BLOCKNODE_H__

#include <cdk/ast/basic_node.h>
#include <cdk/ast/sequence_node.h>

namespace gr8 {

  /**
   * Class for describing block nodes.
   */
  class block_node: public cdk::basic_node {
    cdk::sequence_node *_varDeclarationZone;
    cdk::sequence_node *_instructionZone;

  public:
    inline block_node(int lineno, cdk::sequence_node *varDeclarationZone, cdk::sequence_node *instructionZone) :
        cdk::basic_node(lineno), _varDeclarationZone(varDeclarationZone),
        _instructionZone(instructionZone)  {
    }

  public:
    inline cdk::sequence_node *varDeclarationZone() {
      return _varDeclarationZone;
    }
    inline cdk::sequence_node *instructionZone() {
      return _instructionZone;
    }

    void accept(basic_ast_visitor *sp, int level) {
      sp->do_block_node(this, level);
    }

  };

} // gr8

#endif
