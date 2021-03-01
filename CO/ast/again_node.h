// $Id: again_node.h,v 1.2 2018/04/19 00:23:56 ist424778 Exp $
#ifndef __GR8_AGAINNODE_H__
#define __GR8_AGAINNODE_H__


#include <cdk/ast/basic_node.h>

namespace gr8 {

  /**
   * Class for describing again nodes.
   */
  class again_node: public cdk::basic_node {
    int _nr_of_cicles_to_skip;

  public:
    inline again_node(int lineno, int nr_of_cicles_to_skip ) :
        cdk::basic_node(lineno), _nr_of_cicles_to_skip(nr_of_cicles_to_skip) {
    }

  public:
    inline int nr_of_cicles_to_skip() {
      return _nr_of_cicles_to_skip;
    }

    void accept(basic_ast_visitor *sp, int level) {
      sp->do_again_node(this, level);
    }

  };

} // gr8

#endif
