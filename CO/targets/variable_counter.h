#ifndef __GR8_SEMANTICS_VARIABLE_COUNTER_H__
#define __GR8_SEMANTICS_VARIABLE_COUNTER_H__

#include <string>
#include <iostream>
#include <cdk/symbol_table.h>
#include "targets/basic_ast_visitor.h"
#include "targets/symbol.h"

namespace gr8 {


  //!
  //! Traverse syntax tree and generate the corresponding assembly code.
  //!
  class variable_counter: public basic_ast_visitor {
    size_t _countValue  = 0;

  public:
    variable_counter(std::shared_ptr<cdk::compiler> compiler) :
        basic_ast_visitor(compiler) {
    }

  public:
    ~variable_counter() {
      os().flush();
    }
    void reset_counter() {
      _countValue = 0;
    }

    size_t get_counter() {
      return _countValue;
    }

  private:
    /** Method used to generate sequential labels. */
    inline std::string mklbl(int lbl) {
      std::ostringstream oss;
      if (lbl < 0)
        oss << ".L" << -lbl;
      else
        oss << "_L" << lbl;
      return oss.str();
    }

  public:
  // do not edit these lines
#define __IN_VISITOR_HEADER__
#include "ast/visitor_decls.h"       // automatically generated
#undef __IN_VISITOR_HEADER__
  // do not edit these lines: end

  };

} // gr8


#endif
