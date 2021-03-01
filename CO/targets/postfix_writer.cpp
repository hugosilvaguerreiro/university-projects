#include <string>
#include <sstream>
#include "targets/type_checker.h"
#include "targets/variable_counter.h"
#include "targets/postfix_writer.h"
#include "ast/all.h"  // all.h is automatically generated

//---------------------------------------------------------------------------

void gr8::postfix_writer::do_nil_node(cdk::nil_node * const node, int lvl) {
  // EMPTY
}
void gr8::postfix_writer::do_data_node(cdk::data_node * const node, int lvl) {
  // EMPTY
}

void gr8::postfix_writer::do_not_node(cdk::not_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  int lbl1, lbl2;

  node->argument()->accept(this, lvl);

  _pf.JZ(mklbl(lbl1 = ++_lbl));
  _pf.INT(0);
  _pf.JMP(mklbl(lbl2 = ++_lbl));
  _pf.LABEL(mklbl(lbl1));
  _pf.INT(1);
  _pf.LABEL(mklbl(lbl2));
}
void gr8::postfix_writer::do_and_node(cdk::and_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  int lbl1, lbl2;

  node->left()->accept(this, lvl);
  _pf.DUP32();
  _pf.JZ(mklbl(lbl1 = ++_lbl)); //jz fim1
  _pf.TRASH(4);
  _pf.INT(1);

  node->right()->accept(this, lvl);
  _pf.DUP32();
  _pf.JZ(mklbl(lbl2 = ++_lbl)); //jz fim1
  _pf.TRASH(4);
  _pf.INT(1);

  _pf.LABEL(mklbl(lbl2));
  _pf.AND();

  _pf.LABEL(mklbl(lbl1)); // fim1


}
void gr8::postfix_writer::do_or_node(cdk::or_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  int lbl1, lbl2, lbl3;

  node->left()->accept(this, lvl);
  _pf.DUP32();
  _pf.JZ(mklbl(lbl1 = ++_lbl));

  _pf.TRASH(4);
  _pf.INT(1);
  _pf.JMP(mklbl(lbl2 = ++_lbl));

  _pf.LABEL(mklbl(lbl1));
  node->right()->accept(this, lvl);
  _pf.DUP32();
  _pf.JZ(mklbl(lbl3 = ++_lbl));
  _pf.TRASH(4);
  _pf.INT(1);

  _pf.OR();
  _pf.JMP(mklbl(lbl2));

  _pf.LABEL(mklbl(lbl3));
  _pf.TRASH(4);

  _pf.LABEL(mklbl(lbl2));


}

//---------------------------------------------------------------------------

void gr8::postfix_writer::do_sequence_node(cdk::sequence_node * const node, int lvl) {
  for (size_t i = 0; i < node->size(); i++) {
    node->node(i)->accept(this, lvl);
  }
}

//---------------------------------------------------------------------------

void gr8::postfix_writer::do_integer_node(cdk::integer_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  _pf.INT(node->value()); // push an integer*/
}

void gr8::postfix_writer::do_double_node(cdk::double_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  _pf.DOUBLE(node->value());
}

void gr8::postfix_writer::do_string_node(cdk::string_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;

  int lbl1;

  /* generate the string */
  _pf.RODATA(); // strings are DATA readonly
  _pf.ALIGN(); // make sure we are aligned
  _pf.LABEL(mklbl(lbl1 = ++_lbl)); // give the string a name
  _pf.SSTRING(node->value()); // output string characters
  /* leave the address on the stack */
  _pf.TEXT(); // return to the TEXT segment
  _pf.ADDR(mklbl(lbl1));
}

//---------------------------------------------------------------------------

void gr8::postfix_writer::do_neg_node(cdk::neg_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  node->argument()->accept(this, lvl); // determine the value
  if(node->argument()->type()->name() == basic_type::TYPE_INT)
    _pf.NEG(); // 2-complement
  if(node->argument()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.DNEG(); // 2-complement
}

//---------------------------------------------------------------------------

void gr8::postfix_writer::do_add_node(cdk::add_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  
  if(node->type()->name() == basic_type::TYPE_POINTER) {
    if(node->left()->type()->name() == basic_type::TYPE_POINTER) {
      node->left()->accept(this, lvl+1);
      node->right()->accept(this,  lvl+1);
      _pf.INT(node->type()->subtype()->size());
      _pf.MUL();
    }else {
      node->left()->accept(this,  lvl+1);
      _pf.INT(node->type()->subtype()->size());
      _pf.MUL();
      node->right()->accept(this, lvl+1);
    }
    _pf.ADD();
  }else {
    node->left()->accept(this, lvl);
    if(node->type()->name() == basic_type::TYPE_DOUBLE && node->left()->type()->name() == basic_type::TYPE_INT)
      _pf.I2D();

    node->right()->accept(this, lvl);

    if(node->type()->name() == basic_type::TYPE_DOUBLE && node->right()->type()->name() == basic_type::TYPE_INT)
      _pf.I2D();

    if(node->type()->name() == basic_type::TYPE_INT )
      _pf.ADD();
    else if(node->type()->name() == basic_type::TYPE_DOUBLE)
      _pf.DADD();
  }


}
void gr8::postfix_writer::do_sub_node(cdk::sub_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;

  if(node->left()->type()->name() == basic_type::TYPE_POINTER && node->right()->type()->name() == basic_type::TYPE_POINTER) {
    node->left()->accept(this, lvl);
    node->right()->accept(this, lvl);
    _pf.SUB();
    _pf.INT(node->left()->type()->subtype()->size());
    _pf.DIV();
    
  }else {
    node->left()->accept(this, lvl);
    if(node->type()->name() == basic_type::TYPE_DOUBLE && node->left()->type()->name() == basic_type::TYPE_INT)
      _pf.I2D();

    node->right()->accept(this, lvl);
    if(node->type()->name() == basic_type::TYPE_DOUBLE && node->right()->type()->name() == basic_type::TYPE_INT)
      _pf.I2D();

    if(node->type()->name() == basic_type::TYPE_INT)
      _pf.SUB();
    else if(node->type()->name() == basic_type::TYPE_DOUBLE)
      _pf.DSUB();
    
  }



}
void gr8::postfix_writer::do_mul_node(cdk::mul_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  node->left()->accept(this, lvl);
  if(node->type()->name() == basic_type::TYPE_DOUBLE && node->left()->type()->name() == basic_type::TYPE_INT)
    _pf.I2D();

  node->right()->accept(this, lvl);
  if(node->type()->name() == basic_type::TYPE_DOUBLE && node->right()->type()->name() == basic_type::TYPE_INT)
    _pf.I2D();

  if(node->type()->name() == basic_type::TYPE_INT)
    _pf.MUL();
  else if(node->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.DMUL();
}
void gr8::postfix_writer::do_div_node(cdk::div_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  node->left()->accept(this, lvl);
  if(node->type()->name() == basic_type::TYPE_DOUBLE && node->left()->type()->name() == basic_type::TYPE_INT)
    _pf.I2D();

  node->right()->accept(this, lvl);
  if(node->type()->name() == basic_type::TYPE_DOUBLE && node->right()->type()->name() == basic_type::TYPE_INT)
    _pf.I2D();

  if(node->type()->name() == basic_type::TYPE_INT)
    _pf.DIV();
  else if(node->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.DDIV();
}
void gr8::postfix_writer::do_mod_node(cdk::mod_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  node->left()->accept(this, lvl);
  node->right()->accept(this, lvl);
  _pf.MOD();
}
void gr8::postfix_writer::do_lt_node(cdk::lt_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;

  node->left()->accept(this, lvl);
  if(node->left()->type()->name() == basic_type::TYPE_INT && node->right()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.I2D();

  node->right()->accept(this, lvl);
  if(node->right()->type()->name() == basic_type::TYPE_INT && node->left()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.I2D();

  if(node->left()->type()->name() == basic_type::TYPE_INT && node->right()->type()->name() == basic_type::TYPE_INT)
    _pf.LT();
  else{
    _pf.DCMP();
    _pf.INT(0);
    _pf.LT();
  }

}
void gr8::postfix_writer::do_le_node(cdk::le_node * const node, int lvl) {
  /*EMPTY*/
}
void gr8::postfix_writer::do_ge_node(cdk::ge_node * const node, int lvl) {
  /*EMPTY*/
}
void gr8::postfix_writer::do_gt_node(cdk::gt_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;

  node->left()->accept(this, lvl);
  if(node->left()->type()->name() == basic_type::TYPE_INT && node->right()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.I2D();

  node->right()->accept(this, lvl);
  if(node->right()->type()->name() == basic_type::TYPE_INT && node->left()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.I2D();

  if(node->left()->type()->name() == basic_type::TYPE_INT && node->right()->type()->name() == basic_type::TYPE_INT)
    _pf.GT();
  else{
    _pf.DCMP();
    _pf.INT(0);
    _pf.GT();
  }
}
void gr8::postfix_writer::do_ne_node(cdk::ne_node * const node, int lvl) {
  /*EMPTY*/
}
void gr8::postfix_writer::do_eq_node(cdk::eq_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;

  node->left()->accept(this, lvl);
  if(node->left()->type()->name() == basic_type::TYPE_INT && node->right()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.I2D();

  node->right()->accept(this, lvl);
  if(node->right()->type()->name() == basic_type::TYPE_INT && node->left()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.I2D();

  if(node->left()->type()->name() == basic_type::TYPE_INT && node->right()->type()->name() == basic_type::TYPE_INT)
    _pf.EQ();
  else{
    _pf.DCMP();
    _pf.INT(0);
    _pf.EQ();
  }
}

//---------------------------------------------------------------------------

void gr8::postfix_writer::do_identifier_node(cdk::identifier_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  if(isInsideFuction()) {
    const std::string &id = node->name();
    std::shared_ptr<gr8::symbol> symbol = _symtab.find(id);
    if(symbol->isGlobal() && !symbol->isReAssigned()) {
      _pf.ADDR(node->name());
    }
    else if(symbol->isFuncArg()){
      _pf.LOCAL(symbol->localPosition());
    }
    else {
      _pf.LOCAL(-(symbol->localPosition()));
    }
  } else {
    _pf.ADDR(node->name());
  }
}

void gr8::postfix_writer::do_rvalue_node(cdk::rvalue_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  node->lvalue()->accept(this, lvl);
  if(node->type()->name() == basic_type::TYPE_INT || node->type()->name() == basic_type::TYPE_POINTER)
    _pf.LDINT(); // depends on type size
  else if(node->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.LDDOUBLE(); // depends on type size
  else if(node->type()->name() == basic_type::TYPE_STRING){
    _pf.LDINT();
  }
}

void gr8::postfix_writer::do_assignment_node(cdk::assignment_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  node->rvalue()->accept(this, lvl); // determine the new value


  if(node->rvalue()->type()->name() == basic_type::TYPE_INT && node->type()->name() == basic_type::TYPE_DOUBLE) {
    _pf.I2D();
  } 

  node->lvalue()->accept(this, lvl); // where to store the value
  if(node->type()->name() == basic_type::TYPE_STRING) {
      _pf.STINT();
      _pf.TEXT();
  }
  if(node->type()->name() == basic_type::TYPE_INT || node->type()->name() == basic_type::TYPE_POINTER)
    _pf.STINT(); // store the value at address*/
  else if(node->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.STDOUBLE();
}

void gr8::postfix_writer::do_memory_alloc_node(gr8::memory_alloc_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS

  node->argument()->accept(this, lvl+1);
  _pf.INT(node->type()->subtype()->size());
  _pf.MUL();
  _pf.ALLOC();
  _pf.SP();


}

void gr8::postfix_writer::do_reference_node(gr8::reference_node * const node, int lvl) {
    ASSERT_SAFE_EXPRESSIONS
    node->address()->accept(this, lvl+1);

}


void gr8::postfix_writer::do_variable_declaration_node(gr8::variable_declaration_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;

  if(isInsideFuction()){
      set_currentOffset(currentOffset() + new_symbol()->type()->size());
      new_symbol()->setLocalPosition(currentOffset());
  } else {
    new_symbol()->setisGlobal(true);
    _pf.BSS();
    _pf.ALIGN();
    _pf.GLOBAL(new_symbol()->name(), _pf.OBJ());
    _pf.LABEL(new_symbol()->name());
    if(new_symbol()->type()->name() == basic_type::TYPE_INT || new_symbol()->type()->name() == basic_type::TYPE_POINTER)
      _pf.SINT(0);
    else if(new_symbol()->type()->name() == basic_type::TYPE_DOUBLE)
      _pf.SDOUBLE(0);
    reset_new_symbol();
  }

  _pf.TEXT();
  _pf.ALIGN();
}

void gr8::postfix_writer::do_variable_initialization_node(gr8::variable_initialization_node * const node, int lvl) {
    ASSERT_SAFE_EXPRESSIONS;

    if(isInsideFuction()){

        const std::string &id = node->identifierName();
        std::shared_ptr<gr8::symbol> symbol = _symtab.find(id);

        if(symbol == nullptr)
          symbol = new_symbol();
        else{
          if(symbol->isGlobal()){
            symbol->setisReAssigned(true);
            _globalVariablesReInitialized.push_back(symbol);
          }
        }

        set_currentOffset(currentOffset() + symbol->type()->size());
        symbol->setLocalPosition(currentOffset());

      if(symbol->type()->name() == basic_type::TYPE_STRING) {

        node->init_value()->accept(this, lvl+1);

        _pf.LOCAL(-(symbol->localPosition()));
        _pf.STINT();
        _pf.TEXT();

      }else {
        if(symbol->type()->name() == basic_type::TYPE_INT || symbol->type()->name() == basic_type::TYPE_POINTER) {
            node->init_value()->accept(this, lvl +1);
            _pf.LOCAL(-(symbol->localPosition()));
            _pf.STINT();
        }
        else if(symbol->type()->name() == basic_type::TYPE_DOUBLE) {
          if(node->init_value()->type()->name() == basic_type::TYPE_INT) {
            node->init_value()->accept(this, lvl+1);
            _pf.I2D();
            _pf.LOCAL(-(symbol->localPosition()));
            _pf.STDOUBLE();
          } else {
            node->init_value()->accept(this, lvl+1);
            _pf.LOCAL(-(symbol->localPosition()));
            _pf.STDOUBLE();
          }
        }
      }
    } else {
        new_symbol()->setisGlobal(true);
        new_symbol()->setisReAssigned(false);
      if(new_symbol()->type()->name() == basic_type::TYPE_STRING){

        node->init_value()->accept(this, lvl+1);
        _pf.TRASH(4); //need to delete trash from the stack
        _pf.DATA();
        _pf.ALIGN();
        _pf.GLOBAL(new_symbol()->name(), _pf.OBJ());
        _pf.LABEL(new_symbol()->name());
        _pf.SADDR(mklbl(_lbl));
      } else {

        _pf.DATA();
        _pf.ALIGN();
        _pf.GLOBAL(new_symbol()->name(), _pf.OBJ());
        _pf.LABEL(new_symbol()->name());


        if(new_symbol()->type()->name() == basic_type::TYPE_INT) {
          cdk::integer_node *init = dynamic_cast<cdk::integer_node*>(node->init_value());
          _pf.SINT(init->value());
        }
        else if(new_symbol()->type()->name() == basic_type::TYPE_DOUBLE) {
          if(node->init_value()->type()->name() == basic_type::TYPE_INT) {
            cdk::integer_node *init = dynamic_cast<cdk::integer_node*>(node->init_value());              
            _pf.SDOUBLE((double)init->value());
          } else {
            cdk::double_node *init = dynamic_cast<cdk::double_node*>(node->init_value());              
            _pf.SDOUBLE(init->value());
          }
        }
      }
      _pf.TEXT();
      reset_new_symbol();
    }
}

//---------------------------------------------------------------------------

void gr8::postfix_writer::do_function_call_node(gr8::function_call_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS

  const std::string &id = node->functionIdentifier();
  std::shared_ptr<gr8::symbol> symbol = _symtab.find(id);

  if(symbol->imported() && symbol->isDefined()) //Note: se é importada tu não sabes se está definida, só o linker é que vai saber
    _pf.EXTERN(symbol->name());


  for (size_t i = node->arguments()->size(); i > 0; i--) {
    node->arguments()->node(i-1)->accept(this, lvl);
    cdk::expression_node* expr = dynamic_cast<cdk::expression_node*>(node->arguments()->node(i-1));
    if(expr->type()->name() == basic_type::TYPE_INT && symbol->functionArgs().at(i-1)->type_name == basic_type::TYPE_DOUBLE)
      _pf.I2D(); 
  }

  _pf.CALL(node->functionIdentifier());

  for (size_t i = 0; i < node->arguments()->size(); i++) {
    if(symbol->functionArgs().at(i)->type_name == basic_type::TYPE_INT ||
      symbol->functionArgs().at(i)->type_name == basic_type::TYPE_STRING ||
      symbol->functionArgs().at(i)->type_name == basic_type::TYPE_POINTER)
      _pf.TRASH(4);
    else if(symbol->functionArgs().at(i)->type_name == basic_type::TYPE_DOUBLE)
      _pf.TRASH(8);
  }


  if(symbol->type()->name() == basic_type::TYPE_INT ||
    symbol->type()->name() == basic_type::TYPE_STRING ||
    symbol->type()->name() == basic_type::TYPE_POINTER)
    _pf.LDFVAL32();
  else if(symbol->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.LDFVAL64();

}

void gr8::postfix_writer::do_function_definition_node(gr8::function_definition_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS
  _pf.TEXT();
  //contar o numero de variaveis definidas na funcao
  gr8::variable_counter varCounter(_compiler);
  node->accept(&varCounter, lvl);

  _symtab.push();
  set_currentOffset(8);
  for (size_t i = 0; i < node->arguments()->size(); i++) {
    variable_declaration_node* var_decl = dynamic_cast<variable_declaration_node*>(node->arguments()->node(i));
    const std::string &id = var_decl->varName();
    std::shared_ptr<gr8::symbol> symbol = std::make_shared<gr8::symbol>(var_decl->type(), id, false, false, false);
    symbol->setisFuncArg(true);
    symbol->setLocalPosition(_currentOffset);
    _symtab.insert(id, symbol);
    set_currentOffset(_currentOffset + var_decl->type()->size());
  }

  set_currentOffset(0);

  const std::string &id = node->funcName();
  std::shared_ptr<gr8::symbol> symbol = _symtab.find(id);

  _currentFunctionSymbol =  symbol;

  //se for publica entao e global logo  queremos inserir a tag global
  if(node->publ())
    _pf.GLOBAL(node->funcName(), _pf.FUNC());
  _pf.LABEL(node->funcName());
  _pf.ALIGN();
  _pf.ENTER(varCounter.get_counter());

  if(node->funcName() == "_main") { //inserir as variaveis globais no main
    while (!_globalFuncsNotInitialized.empty()) {
      _globalFuncsNotInitialized.back()->setisImported(true);
      _globalFuncsNotInitialized.back()->isDefined(true);
      _globalFuncsNotInitialized.pop_back();
    }
  } else {
    for(unsigned int i=0; i< _globalFuncsNotInitialized.size(); i++) {
      if(_currentFunctionSymbol->name() == _globalFuncsNotInitialized.at(i)->name()){
        _globalFuncsNotInitialized.erase(_globalFuncsNotInitialized.begin()+i);
      }
    }
  }

  set_is_inside_function(true);

  node->block()->accept(this, lvl);

  if(node->type()->name() == basic_type::TYPE_VOID){
    _pf.LEAVE();
    _pf.RET();
  }
  _currentFunctionSymbol = nullptr;


  while (!_globalVariablesReInitialized.empty()) {
    _globalVariablesReInitialized.back()->setisReAssigned(false);
    _globalVariablesReInitialized.pop_back();
  }

  _currentFunctionSymbol = nullptr;

  _symtab.pop();

  set_is_inside_function(false);
}

void gr8::postfix_writer::do_function_declaration_node(gr8::function_declaration_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS

  if(new_symbol()->isPublic()){
    new_symbol()->isDefined(false);
    _globalFuncsNotInitialized.push_back(new_symbol());
  }
}

void gr8::postfix_writer::do_return_node(gr8::return_node * const node, int lvl) {

  if(_currentFunctionSymbol->type()->name() != basic_type::TYPE_VOID) {
    node->returnValue()->accept(this, lvl);
    if(node->returnValue()->type()->name() == basic_type::TYPE_INT && _currentFunctionSymbol->type()->name() == basic_type::TYPE_DOUBLE)
      _pf.I2D();


    if(_currentFunctionSymbol->type()->name() != basic_type::TYPE_DOUBLE)
      _pf.STFVAL32();
    else 
      _pf.STFVAL64();
  }

  _pf.LEAVE();
  _pf.RET();

}


//---------------------------------------------------------------------------

void gr8::postfix_writer::do_evaluation_node(gr8::evaluation_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  node->argument()->accept(this, lvl); // determine the value
  if(node->argument()->type()->name() != basic_type::TYPE_VOID){
    if (node->argument()->type()->name() == basic_type::TYPE_INT) {
      _pf.TRASH(4); // delete the evaluated value
    } else if (node->argument()->type()->name() == basic_type::TYPE_STRING) {
      _pf.TRASH(4); // delete the evaluated value's address
    } else if (node->argument()->type()->name() == basic_type::TYPE_DOUBLE) {
      _pf.TRASH(8); // delete the evaluated value's address
    } else if (node->argument()->type()->name() == basic_type::TYPE_POINTER) {
      _pf.TRASH(4); // delete the evaluated value's address
    } else {
      std::cerr << "ERROR: CANNOT HAPPEN!" << std::endl;
      exit(1);
    }
  }
}


void gr8::postfix_writer::do_tweet_node(gr8::tweet_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS

  node->argument()->accept(this, lvl);

  if (node->argument()->type()->name() == basic_type::TYPE_INT) {
    _pf.EXTERN("printi");
    _pf.CALL("printi");
    _pf.TRASH(4); // delete the printed value
  } else if (node->argument()->type()->name() == basic_type::TYPE_STRING) {
    _pf.EXTERN("prints");
    _pf.CALL("prints");
    _pf.TRASH(4); // delete the printed value's address
  } else if (node->argument()->type()->name() == basic_type::TYPE_DOUBLE) {
    _pf.EXTERN("printd");
    _pf.CALL("printd");
    _pf.TRASH(8); // delete the printed value's address
  } else {
    std::cerr << "ERROR: CANNOT HAPPEN!" << std::endl;
    exit(1);
  }
}

void gr8::postfix_writer::do_post_node(gr8::post_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS

  node->argument()->accept(this, lvl);

  if (node->argument()->type()->name() == basic_type::TYPE_INT) {
    _pf.EXTERN("printi");
    _pf.CALL("printi");
    _pf.TRASH(4); // delete the printed value
  } else if (node->argument()->type()->name() == basic_type::TYPE_STRING) {
    _pf.EXTERN("prints");
    _pf.CALL("prints");
    _pf.TRASH(4); // delete the printed value's address
  } else if (node->argument()->type()->name() == basic_type::TYPE_DOUBLE) {
    _pf.EXTERN("printd");
    _pf.CALL("printd");
    _pf.TRASH(8); // delete the printed value's address
  } else {
    std::cerr << "ERROR: CANNOT HAPPEN!" << std::endl;
    exit(1);
  }
  _pf.EXTERN("println");
  _pf.CALL("println"); // print a newline
}

//---------------------------------------------------------------------------

void gr8::postfix_writer::do_read_node(gr8::read_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  if(node->type()->name() == basic_type::TYPE_INT) {
    _pf.EXTERN("readi");
    _pf.CALL("readi");
    _pf.LDFVAL32();
  }else {
    _pf.EXTERN("readd");
    _pf.CALL("readd");
    _pf.LDFVAL64();
  }
}

//---------------------------------------------------------------------------


void gr8::postfix_writer::do_sweeping_node(gr8::sweeping_node * const node, int lvl) {
  //NOTE funciona com variaveis globais apenas

  int goingDown, end, increment, end2, firstOne, instructionZone;

  cdk::assignment_node* init_sweep_assignment = new cdk::assignment_node(lvl, node->incrementableValue(), node->start());
  init_sweep_assignment->accept(this, lvl);

  _pf.JMP(mklbl(firstOne = ++_lbl));
  //increment
  //load var
  _pf.LABEL(mklbl(increment = ++_lbl));
  node->incrementableValue()->accept(this, lvl);
  if(node->incrementableValue()->type()->name() == basic_type::TYPE_INT)
    _pf.LDINT();
  else if(node->incrementableValue()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.LDDOUBLE();

  //load increment
  node->increment()->accept(this, lvl);

  //var + increment
  if(node->increment()->type()->name() == basic_type::TYPE_INT && node->incrementableValue()->type()->name() == basic_type::TYPE_DOUBLE){
    _pf.I2D();
    _pf.DADD();
  } else if (node->increment()->type()->name() != basic_type::TYPE_DOUBLE) {
    _pf.ADD();
  } else {
    _pf.DADD();
  }

  //var = var + increment
  node->incrementableValue()->accept(this, lvl);
  if(node->incrementableValue()->type()->name() == basic_type::TYPE_INT)
    _pf.STINT();
  else if(node->incrementableValue()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.STDOUBLE();

  _pf.LABEL(mklbl(firstOne));
  node->increment()->accept(this, lvl);

  //verifica se incremento e' positivo (e altera o tipo de comparacao)
  _pf.INT(0);
  _pf.GE();
  _pf.JZ(mklbl(goingDown = ++_lbl));

  //goingUP
  node->incrementableValue()->accept(this, lvl);
  if(node->incrementableValue()->type()->name() == basic_type::TYPE_INT)
    _pf.LDINT();
  else if(node->incrementableValue()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.LDDOUBLE();
  node->end()->accept(this, lvl);
  _pf.LE(); //compara o valor em var com o fim
  _pf.JZ(mklbl(end2 = ++_lbl));
  node->block()->accept(this, lvl);
  _pf.JMP(mklbl(increment)); // repeat

  //goingDown
  _pf.LABEL(mklbl(goingDown));
  node->incrementableValue()->accept(this, lvl);
  if(node->incrementableValue()->type()->name() == basic_type::TYPE_INT)
    _pf.LDINT();
  else if(node->incrementableValue()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.LDDOUBLE();
  node->end()->accept(this, lvl);
  _pf.GE(); //compara o valor em var com o fim
  _pf.JZ(mklbl(end2));

  
  _pf.LABEL(mklbl(instructionZone = ++_lbl));
  
  _stopLabels.push_back(end2);
  _againLabels.push_back(instructionZone);

  node->block()->accept(this, lvl);
  
  _stopLabels.pop_back();
  _againLabels.pop_back();

  _pf.JMP(mklbl(increment)); // repeat

  _pf.LABEL(mklbl(end = ++_lbl));
  if(node->incrementableValue()->type()->name() == basic_type::TYPE_INT)
    _pf.TRASH(4);
  else if(node->incrementableValue()->type()->name() == basic_type::TYPE_DOUBLE)
    _pf.TRASH(8);

  _pf.LABEL(mklbl(end2));
}

void gr8::postfix_writer::do_stop_node(gr8::stop_node * const node, int lvl) {
  
  if(_stopLabels.size() != 0) {
    for(int i=0; i < node->nr_of_cicles_to_stop()-1; i++) {
      _stopLabels.pop_back();
      _againLabels.pop_back();
    }
    _pf.JMP(mklbl(_stopLabels.back()));
  }//NOTE: lançar exceção
}

void gr8::postfix_writer::do_again_node(gr8::again_node * const node, int lvl) {
  if(_againLabels.size() != 0) {
    for(int i=0; i < node->nr_of_cicles_to_skip()-1; i++) {
      _stopLabels.pop_back();
      _againLabels.pop_back();
    }
    _pf.JMP(mklbl(_againLabels.back()));
  }//NOTE: lançar exceção
}

//---------------------------------------------------------------------------

void gr8::postfix_writer::do_if_node(gr8::if_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  int lbl1;
  node->condition()->accept(this, lvl);
  _pf.JZ(mklbl(lbl1 = ++_lbl));
  node->block()->accept(this, lvl + 2);
  _pf.LABEL(mklbl(lbl1));
}

//---------------------------------------------------------------------------

void gr8::postfix_writer::do_if_else_node(gr8::if_else_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  int lbl1, lbl2;
  node->condition()->accept(this, lvl);
  _pf.JZ(mklbl(lbl1 = ++_lbl));
  node->thenblock()->accept(this, lvl + 2);
  _pf.JMP(mklbl(lbl2 = ++_lbl));
  _pf.LABEL(mklbl(lbl1));
  node->elseblock()->accept(this, lvl + 2);
  _pf.LABEL(mklbl(lbl1 = lbl2));
}

void gr8::postfix_writer::do_block_node(gr8::block_node * const node, int lvl) {
  _symtab.push();
  if(node->varDeclarationZone() != nullptr)
    node->varDeclarationZone()->accept(this, lvl);
  if(node->instructionZone() != nullptr)
    node->instructionZone()->accept(this, lvl);
  _symtab.pop();
}

void gr8::postfix_writer::do_indexing_node(gr8::indexing_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;

  node->indexPosition()->accept(this, lvl);
  _pf.INT(node->pointer()->type()->subtype()->size());
  _pf.MUL();
  node->pointer()->accept(this, lvl);
  _pf.ADD();
}


void gr8::postfix_writer::do_identity_node(gr8::identity_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  node->argument()->accept(this, lvl);
}

void gr8::postfix_writer::do_null_pointer_node(gr8::null_pointer_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  _pf.INT(0);
}
