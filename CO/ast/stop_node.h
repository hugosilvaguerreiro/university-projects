// $Id: stop_node.h,v 1.2 2018/04/19 00:23:56 ist424778 Exp $
#ifndef __GR8_STOPNODE_H__
#define __GR8_STOPNODE_H__

#include <cdk/ast/basic_node.h>

namespace gr8 {

  /**
   * Class for describing stop nodes.
   */
  class stop_node: public cdk::basic_node {
    int _nr_of_cicles_to_stop;

  public:
    inline stop_node(int lineno, int nr_of_cicles_to_stop = 1) :
        cdk::basic_node(lineno), _nr_of_cicles_to_stop(nr_of_cicles_to_stop) {
    }

  public:

    inline int nr_of_cicles_to_stop() {
      return _nr_of_cicles_to_stop;
    }

    void accept(basic_ast_visitor *sp, int level) {
      sp->do_stop_node(this, level);
    }

  };

} // gr8

#endif
