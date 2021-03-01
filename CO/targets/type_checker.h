// $Id: type_checker.h,v 1.6 2018/05/21 02:18:32 ist424778 Exp $ -*- c++ -*-
#ifndef __GR8_SEMANTICS_TYPE_CHECKER_H__
#define __GR8_SEMANTICS_TYPE_CHECKER_H__

#include <string>
#include <iostream>
#include <cdk/symbol_table.h>
#include <cdk/ast/basic_node.h>
#include "targets/symbol.h"
#include "targets/basic_ast_visitor.h"

namespace gr8 {

  /**
   * Print nodes as XML elements to the output stream.
   */
  class type_checker: public basic_ast_visitor {
    cdk::symbol_table<gr8::symbol> &_symtab;

    basic_ast_visitor *_parent;

  public:
    type_checker(std::shared_ptr<cdk::compiler> compiler, cdk::symbol_table<gr8::symbol> &symtab, basic_ast_visitor *parent) :
        basic_ast_visitor(compiler), _symtab(symtab), _parent(parent) {
    }

  inline void create_function_arg(basic_type *type, function_arg_t* function_arg){
    if(type->subtype() == nullptr){
      function_arg->final_subtype_name = type->name();
    }
    else{
      function_arg->nr_subtypes += 1;
      create_function_arg(type->subtype(), function_arg);
    }
  }

  inline void convert_unspec_types(basic_type *type1, basic_type *type2) {
    if(type1->name() == basic_type::TYPE_UNSPEC && type2->name() == basic_type::TYPE_UNSPEC) {
      type1->_name = basic_type::TYPE_INT;
      type2->_name = basic_type::TYPE_INT;
      type1->_size = 4;
      type2->_size = 4;
    }
    else if(type1->name() == basic_type::TYPE_UNSPEC) {
      if(type2->name() != basic_type::TYPE_POINTER) {
        type1->_name = type2->name();
        type1->_size = type2->size();
      }else if(type2->name() == basic_type::TYPE_POINTER) {
        type1->_name = basic_type::TYPE_INT;
        type1->_size = 4;
      }
    }
    else if(type2->name() == basic_type::TYPE_UNSPEC ) {
      if(type1->name() != basic_type::TYPE_POINTER) {
        type2->_name = type1->name();
        type2->_size = type1->size();
      }else if(type1->name() == basic_type::TYPE_POINTER) {
        type2->_name = basic_type::TYPE_INT;
        type2->_size = 4;
      }
    }
  }
  inline bool confirm_pointer_types(basic_type *type1, basic_type* type2) {
      
      if( type1->name() != type2->name() ||
         type1->size() != type2->size() ||
         (type1->subtype() != nullptr && type2->subtype() != nullptr &&
         type1->subtype()->name() != type2->subtype()->name())){
          return false;
      }
      if(type1->subtype() != nullptr && type2->subtype() != nullptr) {
        return confirm_pointer_types(type1->subtype(), type2->subtype());
      }else {
        return true;
      }

  }
  void print_type(basic_type * type) {
    if(type->subtype() == nullptr) {
      std::cout << type->name() << " hey \n";
      return;
    }
    else {
      std::cout << type->name() << " hey \n";
      print_type(type->subtype());
    }
  }
  inline void copy_pointer_type(basic_type *typeToCopy, basic_type* type) {
      if(typeToCopy->subtype() == nullptr){
        type->_name = typeToCopy->name();
        type->_size = typeToCopy->size();
      }
      else{
       // std::cout << "type to copy:\n ";
       // print_type(typeToCopy);
       // std::cout << "new type: \n";
       // print_type(type);
        type->_name = basic_type::TYPE_POINTER;
        type->_size = 4;
        type->_subtype = new basic_type(4, basic_type::TYPE_UNSPEC);
        copy_pointer_type(typeToCopy->subtype(), type->subtype());
      }
  }

  public:
    ~type_checker() {
      os().flush();
    }

  protected:
    void processUnaryExpression(cdk::unary_expression_node * const node, int lvl);
    void processBinaryExpression(cdk::binary_expression_node * const node, int lvl);
    template<typename T>
    void process_literal(cdk::literal_node<T> * const node, int lvl) {
    }

  public:
  // do not edit these lines
#define __IN_VISITOR_HEADER__
#include "ast/visitor_decls.h"       // automatically generated
#undef __IN_VISITOR_HEADER__
  // do not edit these lines: end

  };

} // gr8

//---------------------------------------------------------------------------
//     HELPER MACRO FOR TYPE CHECKING
//---------------------------------------------------------------------------

#define CHECK_TYPES(compiler, symtab, node) { \
  try { \
    gr8::type_checker checker(compiler, symtab, this); \
    (node)->accept(&checker, 0); \
  } \
  catch (const std::string &problem) { \
    std::cerr << (node)->lineno() << ": " << problem << std::endl; \
    return; \
  } \
}

#define ASSERT_SAFE_EXPRESSIONS CHECK_TYPES(_compiler, _symtab, node)

#endif
