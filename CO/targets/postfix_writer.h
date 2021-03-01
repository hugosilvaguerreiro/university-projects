#ifndef __GR8_SEMANTICS_POSTFIX_WRITER_H__
#define __GR8_SEMANTICS_POSTFIX_WRITER_H__

#include <string>
#include <iostream>
#include <cdk/symbol_table.h>
#include <cdk/emitters/basic_postfix_emitter.h>
#include "targets/basic_ast_visitor.h"
#include "targets/symbol.h"

namespace gr8 {


  //!
  //! Traverse syntax tree and generate the corresponding assembly code.
  //!
  class postfix_writer: public basic_ast_visitor {
    cdk::symbol_table<gr8::symbol> &_symtab;
    cdk::basic_postfix_emitter &_pf;
    int _lbl;
    bool _isInsideFuction = false;
    long unsigned int _currentOffset;
    std::vector<std::shared_ptr<gr8::symbol>> _globalVariablesReInitialized;
    std::vector<std::shared_ptr<gr8::symbol>> _globalFuncsNotInitialized;
    std::shared_ptr<gr8::symbol> _currentFunctionSymbol;

    std::vector<int> _stopLabels;
    std::vector<int> _againLabels;

  public:
    postfix_writer(std::shared_ptr<cdk::compiler> compiler, cdk::symbol_table<gr8::symbol> &symtab,
                   cdk::basic_postfix_emitter &pf) :
        basic_ast_visitor(compiler), _symtab(symtab), _pf(pf), _lbl(0), _currentFunctionSymbol(nullptr) {
    }

  public:
    ~postfix_writer() {
      os().flush();
    }
    void set_is_inside_function(bool status){
      _isInsideFuction = status;
    }

    void set_currentOffset(long unsigned int offset){
      _currentOffset = offset;
    }

    long unsigned int currentOffset() {
      return _currentOffset;
    }

    bool isInsideFuction(){
      return _isInsideFuction;
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
