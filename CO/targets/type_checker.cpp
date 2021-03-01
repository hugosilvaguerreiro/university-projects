#include <string>
#include "targets/type_checker.h"
#include "ast/all.h"  // automatically generated

#define ASSERT_UNSPEC \
    { if (node->type() != nullptr && \
          node->type()->name() != basic_type::TYPE_UNSPEC) return; }

//---------------------------------------------------------------------------

void gr8::type_checker::do_sequence_node(cdk::sequence_node * const node, int lvl) {
  //EMPTY
}

//---------------------------------------------------------------------------

void gr8::type_checker::do_nil_node(cdk::nil_node * const node, int lvl) {
  // EMPTY
}
void gr8::type_checker::do_data_node(cdk::data_node * const node, int lvl) {
  // EMPTY
}

//---------------------------------------------------------------------------

void gr8::type_checker::do_integer_node(cdk::integer_node * const node, int lvl) {
  ASSERT_UNSPEC;
  node->type(new basic_type(4, basic_type::TYPE_INT));
}

void gr8::type_checker::do_double_node(cdk::double_node * const node, int lvl) {
  ASSERT_UNSPEC;
  node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
}

void gr8::type_checker::do_string_node(cdk::string_node * const node, int lvl) {
  ASSERT_UNSPEC;
  node->type(new basic_type(4, basic_type::TYPE_STRING));
}

void gr8::type_checker::do_null_pointer_node(gr8::null_pointer_node * const node, int lvl) {
  ASSERT_UNSPEC
  node->type(new basic_type(4, basic_type::TYPE_POINTER));
  node->type()->_subtype = new basic_type(4, basic_type::TYPE_UNSPEC);
}


//---------------------------------------------------------------------------

void gr8::type_checker::processUnaryExpression(cdk::unary_expression_node * const node, int lvl) {
  ASSERT_UNSPEC;
  node->argument()->accept(this, lvl + 2);
  if(node->argument()->type()->name() == basic_type::TYPE_UNSPEC){
    node->argument()->type()->_name = basic_type::TYPE_INT;
    node->argument()->type()->_size = 4;
  }

  if (node->argument()->type()->name() != basic_type::TYPE_INT) throw std::string("wrong type in argument of unary expression");

  node->type(new basic_type(4, basic_type::TYPE_INT));
}

void gr8::type_checker::do_not_node(cdk::not_node * const node, int lvl) {
  processUnaryExpression(node, lvl);
}

void gr8::type_checker::do_neg_node(cdk::neg_node * const node, int lvl) {
  ASSERT_UNSPEC;
  node->argument()->accept(this, lvl + 2);

  if(node->argument()->type()->name() == basic_type::TYPE_UNSPEC){
    node->argument()->type()->_name = basic_type::TYPE_INT;
    node->argument()->type()->_size = 4;
  }

  if (node->argument()->type()->name() == basic_type::TYPE_INT )
    node->type(new basic_type(4, basic_type::TYPE_INT));
  else if( node->argument()->type()->name() == basic_type::TYPE_DOUBLE)
    node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
  else
    throw std::string("wrong type in argument of neg node");
}

void gr8::type_checker::do_identity_node(gr8::identity_node * const node, int lvl) {
  ASSERT_UNSPEC;
  node->argument()->accept(this, lvl + 2);

  if(node->argument()->type()->name() == basic_type::TYPE_UNSPEC){
    node->argument()->type()->_name = basic_type::TYPE_INT;
    node->argument()->type()->_size = 4;
  }

  if (node->argument()->type()->name() == basic_type::TYPE_INT )
    node->type(new basic_type(4, basic_type::TYPE_INT));
  else if( node->argument()->type()->name() == basic_type::TYPE_DOUBLE)
    node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
  else
    throw std::string("wrong type in argument of identity node");
}

//---------------------------------------------------------------------------

void gr8::type_checker::processBinaryExpression(cdk::binary_expression_node * const node, int lvl) {
  ASSERT_UNSPEC;
  node->left()->accept(this, lvl + 2);
  if(node->left()->type()->name() == basic_type::TYPE_UNSPEC)  {
    node->left()->type()->_name = basic_type::TYPE_INT;
    node->left()->type()->_size = 4;
  }

  if (node->left()->type()->name() != basic_type::TYPE_INT) throw std::string("wrong type in left argument of binary expression");

  node->right()->accept(this, lvl + 2);
  if(node->right()->type()->name() == basic_type::TYPE_UNSPEC)  {
    node->right()->type()->_name = basic_type::TYPE_INT;
    node->right()->type()->_size = 4;
  }
  if (node->right()->type()->name() != basic_type::TYPE_INT) throw std::string("wrong type in right argument of binary expression");

  node->type(new basic_type(4, basic_type::TYPE_INT));
}

void gr8::type_checker::do_and_node(cdk::and_node * const node, int lvl) {
  processBinaryExpression(node, lvl);
}
void gr8::type_checker::do_or_node(cdk::or_node * const node, int lvl) {
  processBinaryExpression(node, lvl);
}

void gr8::type_checker::do_add_node(cdk::add_node * const node, int lvl) {
  ASSERT_UNSPEC
  node->left()->accept(this, lvl + 2);
  node->right()->accept(this, lvl + 2);

  convert_unspec_types(node->left()->type(), node->right()->type());

  if (node->left()->type()->name() == basic_type::TYPE_INT ) {
    if(node->right()->type()->name() == basic_type::TYPE_INT)
      node->type(new basic_type(4, basic_type::TYPE_INT));
    else if(node->right()->type()->name() == basic_type::TYPE_DOUBLE)
      node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
    else if(node->right()->type()->name() == basic_type::TYPE_POINTER){
      basic_type* type = new basic_type(4, basic_type::TYPE_POINTER);
      copy_pointer_type(node->right()->type(), type);
      node->type(type);
    }
    else
      throw std::string("wrong type in right argument of add node");
  }
  else if( node->left()->type()->name() == basic_type::TYPE_DOUBLE) {
    if(node->right()->type()->name() == basic_type::TYPE_INT)
      node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
    else if(node->right()->type()->name() == basic_type::TYPE_DOUBLE)
      node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
    else
      throw std::string("wrong type in right argument of add node");
  }
  else if( node->left()->type()->name() == basic_type::TYPE_POINTER) {
    if(node->right()->type()->name() != basic_type::TYPE_INT)
        throw std::string("wrong type in right argument of add node");
    basic_type* type = new basic_type(4, basic_type::TYPE_POINTER);
    copy_pointer_type(node->left()->type(), type);
    node->type(type);
  }
  else
    throw std::string("wrong type in left argument of add node");
}

void gr8::type_checker::do_sub_node(cdk::sub_node * const node, int lvl) {
  ASSERT_UNSPEC
  node->left()->accept(this, lvl + 2);
  node->right()->accept(this, lvl + 2);

  convert_unspec_types(node->left()->type(), node->right()->type());

  if (node->left()->type()->name() == basic_type::TYPE_INT ) {
    if(node->right()->type()->name() == basic_type::TYPE_INT)
      node->type(new basic_type(4, basic_type::TYPE_INT));
    else if(node->right()->type()->name() == basic_type::TYPE_DOUBLE)
      node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
    else
      throw std::string("wrong type in right argument of sub node");
  }
  else if( node->left()->type()->name() == basic_type::TYPE_DOUBLE) {
    if(node->right()->type()->name() == basic_type::TYPE_INT)
      node->type(new basic_type(4, basic_type::TYPE_DOUBLE));
    else if(node->right()->type()->name() == basic_type::TYPE_DOUBLE)
      node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
    else
      throw std::string("wrong type in right argument of sub node");
  }
  else if( node->left()->type()->name() == basic_type::TYPE_POINTER) {
    if(node->right()->type()->name() != basic_type::TYPE_POINTER)
        throw std::string("wrong type in right argument of sub node");
    if(!confirm_pointer_types(node->left()->type(), node->right()->type()))
        throw std::string("Incompatible pointer types in sub node");
    node->type(new basic_type(4, basic_type::TYPE_INT));
  }
  else
    throw std::string("wrong type in left argument of add node");
}


void gr8::type_checker::do_mul_node(cdk::mul_node * const node, int lvl) {
  ASSERT_UNSPEC
  node->left()->accept(this, lvl + 2);
  node->right()->accept(this, lvl + 2);

  convert_unspec_types(node->left()->type(), node->right()->type());

  if (node->left()->type()->name() == basic_type::TYPE_INT ) {
    if(node->right()->type()->name() == basic_type::TYPE_INT)
      node->type(new basic_type(4, basic_type::TYPE_INT));
    else if(node->right()->type()->name() == basic_type::TYPE_DOUBLE)
      node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
    else
      throw std::string("wrong type in right argument of mul node");
  }
  else if( node->left()->type()->name() == basic_type::TYPE_DOUBLE) {
    if(node->right()->type()->name() == basic_type::TYPE_INT)
      node->type(new basic_type(4, basic_type::TYPE_DOUBLE));
    else if(node->right()->type()->name() == basic_type::TYPE_DOUBLE)
      node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
    else
      throw std::string("wrong type in right argument of mul node");
  }
  else
    throw std::string("wrong type in left argument of mul node");
}
void gr8::type_checker::do_div_node(cdk::div_node * const node, int lvl) {
  ASSERT_UNSPEC

  node->left()->accept(this, lvl + 2);
  node->right()->accept(this, lvl + 2);

  convert_unspec_types(node->left()->type(), node->right()->type());

  if (node->left()->type()->name() == basic_type::TYPE_INT ) {
    if(node->right()->type()->name() == basic_type::TYPE_INT)
      node->type(new basic_type(4, basic_type::TYPE_INT));
    else if(node->right()->type()->name() == basic_type::TYPE_DOUBLE)
      node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
    else
      throw std::string("wrong type in right argument of div node");
  }
  else if( node->left()->type()->name() == basic_type::TYPE_DOUBLE) {
    if(node->right()->type()->name() == basic_type::TYPE_INT)
      node->type(new basic_type(4, basic_type::TYPE_DOUBLE));
    else if(node->right()->type()->name() == basic_type::TYPE_DOUBLE)
      node->type(new basic_type(8, basic_type::TYPE_DOUBLE));
    else
      throw std::string("wrong type in right argument of div node");
  }
  else
    throw std::string("wrong type in left argument of div node");
}
void gr8::type_checker::do_mod_node(cdk::mod_node * const node, int lvl) {
  processBinaryExpression(node, lvl);
}
void gr8::type_checker::do_lt_node(cdk::lt_node * const node, int lvl) {
  ASSERT_UNSPEC
  node->left()->accept(this, lvl + 2);
  node->right()->accept(this, lvl + 2);

  convert_unspec_types(node->left()->type(), node->right()->type());
  if (node->left()->type()->name() == basic_type::TYPE_INT ||
      node->left()->type()->name() == basic_type::TYPE_DOUBLE) {
    if( node->right()->type()->name() == basic_type::TYPE_INT ||
        node->right()->type()->name() == basic_type::TYPE_DOUBLE )
          node->type(new basic_type(4, basic_type::TYPE_INT));
    else
      throw std::string("wrong type in right argument of lt node");
  }
  else
    throw std::string("wrong type in left argument of lt node");
}
void gr8::type_checker::do_le_node(cdk::le_node * const node, int lvl) {
  processBinaryExpression(node, lvl);
}
void gr8::type_checker::do_ge_node(cdk::ge_node * const node, int lvl) {
  processBinaryExpression(node, lvl);
}
void gr8::type_checker::do_gt_node(cdk::gt_node * const node, int lvl) {
  ASSERT_UNSPEC
  node->left()->accept(this, lvl + 2);
  node->right()->accept(this, lvl + 2);

  convert_unspec_types(node->left()->type(), node->right()->type());

  if (node->left()->type()->name() == basic_type::TYPE_INT ||
      node->left()->type()->name() == basic_type::TYPE_DOUBLE) {
    if( node->right()->type()->name() == basic_type::TYPE_INT ||
        node->right()->type()->name() == basic_type::TYPE_DOUBLE )
          node->type(new basic_type(4, basic_type::TYPE_INT));
    else
      throw std::string("wrong type in right argument of gt node");
  }
  else
    throw std::string("wrong type in left argument of gt node");
}
void gr8::type_checker::do_ne_node(cdk::ne_node * const node, int lvl) {
  processBinaryExpression(node, lvl);
}
void gr8::type_checker::do_eq_node(cdk::eq_node * const node, int lvl) {
  ASSERT_UNSPEC
  node->left()->accept(this, lvl + 2);
  node->right()->accept(this, lvl + 2);

  convert_unspec_types(node->left()->type(), node->right()->type());
  //TODO combinacoes com pointer tao mal
  if(node->left()->type()->name() == basic_type::TYPE_POINTER && node->right()->type()->name() == basic_type::TYPE_POINTER)
    if(!confirm_pointer_types(node->left()->type(), node->right()->type()))
      throw std::string("incompatible pointers in eq node");

  if (node->left()->type()->name() == basic_type::TYPE_INT ||
      node->left()->type()->name() == basic_type::TYPE_DOUBLE ) {
    if( node->right()->type()->name() == basic_type::TYPE_INT ||
        node->right()->type()->name() == basic_type::TYPE_DOUBLE )
          node->type(new basic_type(4, basic_type::TYPE_INT));
    else
      throw std::string("wrong type in right argument of eq node");
  }
  else
    throw std::string("wrong type in left argument of eq node");
}

//---------------------------------------------------------------------------

void gr8::type_checker::do_identifier_node(cdk::identifier_node * const node, int lvl) {
  ASSERT_UNSPEC;
  const std::string &id = node->name();
  std::shared_ptr<gr8::symbol> symbol = _symtab.find(id);

  if (symbol != nullptr) {
    node->type(symbol->type());
  } else {
    throw id;
  }
}

void gr8::type_checker::do_rvalue_node(cdk::rvalue_node * const node, int lvl) {
  ASSERT_UNSPEC;
  try {
    node->lvalue()->accept(this, lvl);
    node->type(node->lvalue()->type());
  } catch (const std::string &id) {
    throw "undeclared variable '" + id + "'";
  }
}

void gr8::type_checker::do_assignment_node(cdk::assignment_node * const node, int lvl) {
  ASSERT_UNSPEC;

  try {
    node->lvalue()->accept(this, lvl);
  } catch (const std::string &id) {
    throw "undeclared variable '" + id + "'";
  }

  node->rvalue()->accept(this, lvl + 2);


  if(node->rvalue()->type()->name() == basic_type::TYPE_UNSPEC && 
    (node->lvalue()->type()->name() == basic_type::TYPE_INT  || node->lvalue()->type()->name() == basic_type::TYPE_DOUBLE)){
    node->rvalue()->type()->_name = node->lvalue()->type()->name();
    node->rvalue()->type()->_size = node->lvalue()->type()->size();
  }

  if (node->lvalue()->type()->name() == basic_type::TYPE_INT){
    if(node->rvalue()->type()->name() == basic_type::TYPE_INT)
      node->type(new basic_type(4, basic_type::TYPE_INT));
    else
      throw std::string("wrong type in right argument of assignment expression");
  }
  else if(node->lvalue()->type()->name() == basic_type::TYPE_STRING){
    if(node->rvalue()->type()->name() == basic_type::TYPE_STRING)
      node->type(new basic_type(4, basic_type::TYPE_STRING));
    else
      throw std::string("wrong type in right argument of assignment expression");
  }
  else if(node->lvalue()->type()->name() == basic_type::TYPE_POINTER){
    if(node->rvalue()->type()->name() == basic_type::TYPE_POINTER) {
      if(node->rvalue()->type()->_subtype->name() == basic_type::TYPE_UNSPEC) {
        copy_pointer_type(node->lvalue()->type(), node->rvalue()->type());
      }
      if(!confirm_pointer_types(node->lvalue()->type(), node->rvalue()->type()))
        throw std::string("Incompatible pointer types in assignment operation");
      node->type(new basic_type(4, basic_type::TYPE_POINTER));
    }
    else {
      throw std::string("wrong type in right argument of assignment expression ");
    }
  }
  else if(node->lvalue()->type()->name() == basic_type::TYPE_DOUBLE){
    if(node->rvalue()->type()->name() == basic_type::TYPE_DOUBLE ||
      node->rvalue()->type()->name() == basic_type::TYPE_INT)
        node->type(new basic_type(4, basic_type::TYPE_DOUBLE));
    else
      throw std::string("wrong type in right argument of assignment expression ");
  }
  else
    throw std::string("wrong type in left argument of assignment expression");
}

void gr8::type_checker::do_memory_alloc_node(gr8::memory_alloc_node * const node, int lvl) {
  ASSERT_UNSPEC;

  node->argument()->accept(this, lvl+1);
  if(node->argument()->type()->name() == basic_type::TYPE_UNSPEC) {
    node->argument()->type()->_name = basic_type::TYPE_INT;
    node->argument()->type()->_size = 4;
  }
  if (node->argument()->type()->name() == basic_type::TYPE_INT ) {
    node->type(new basic_type(4, basic_type::TYPE_POINTER));
    node->type()->_subtype = new basic_type(4, basic_type::TYPE_UNSPEC);
  }
  else
    throw std::string("wrong type in right argument of memory alloc node");
}

void gr8::type_checker::do_reference_node(gr8::reference_node * const node, int lvl) {
  try {
    node->address()->accept(this, lvl);
    basic_type* type = new basic_type(4, basic_type::TYPE_POINTER);
    basic_type* subtype = new basic_type(4, basic_type::TYPE_UNSPEC);
    copy_pointer_type(node->address()->type(), subtype);
    type->_subtype = subtype;
    node->type(type);
  } catch (const std::string &id) {
    throw std::string("undeclared variable '" + id + "'");
  }
}

void gr8::type_checker::do_variable_declaration_node(gr8::variable_declaration_node * const node, int lvl) {
  const std::string &id = node->varName();
  std::shared_ptr<gr8::symbol> symbol = _symtab.find_local(id);

  if(symbol != nullptr && symbol->isFunction())
    throw std::string("'" + id + "' redeclared as different kind of symbol");

  if (symbol == nullptr) {
    symbol = std::make_shared<gr8::symbol>(node->type(), id, false, node->imported(), node->publ());
    _symtab.insert(id, symbol);
    _parent->set_new_symbol(symbol);
  } else{
    throw std::string("redeclared variable '" + id + "'");
  }
}

void gr8::type_checker::do_variable_initialization_node(gr8::variable_initialization_node * const node, int lvl) {
  const std::string &id = node->identifierName();
  std::shared_ptr<gr8::symbol> symbol = _symtab.find(id);
  std::shared_ptr<gr8::symbol> symbol_local = _symtab.find_local(id);

  if(symbol_local != nullptr && symbol_local->isFunction())
    throw std::string("'" + id + "' redeclared as different kind of symbol");

  if(symbol != nullptr && symbol->imported())
    throw std::string("initialization of imported variable '" + id + "'");

  if (symbol == nullptr) {
    symbol = std::make_shared<gr8::symbol>(node->type(), id, false, node->imported(), node->publ());
    _symtab.insert(id, symbol);
    _parent->set_new_symbol(symbol);
  }
  node->init_value()->accept(this, lvl+2);

  if(node->init_value()->type()->name() == basic_type::TYPE_UNSPEC){
    node->init_value()->type()->_name = basic_type::TYPE_INT;
    node->init_value()->type()->_size = 4;
  }


  if(symbol->type()->name() == node->init_value()->type()->name())
    node->type(symbol->type());
  else if(symbol->type()->name() == basic_type::TYPE_DOUBLE && node->init_value()->type()->name() == basic_type::TYPE_INT)
    node->type(symbol->type());
  else
    throw std::string("mismatched type in variable initialization node");

}

//---------------------------------------------------------------------------

void gr8::type_checker::do_function_call_node(gr8::function_call_node * const node, int lvl) {
  ASSERT_UNSPEC;
  const std::string &id = node->functionIdentifier();

  std::shared_ptr<gr8::symbol> symbol = _symtab.find(id);

  if(symbol == nullptr)
    throw std::string("'" + id + "' undeclared function");

  if(symbol->functionArgs().size() != node->arguments()->size())
    throw std::string("'" + id + "' invalid argument size");


  function_arg_t* expected;
  basic_type* actual;
  for(unsigned int i=0; i<symbol->functionArgs().size(); i++)  {
    expected = symbol->functionArgs().at(i);

    cdk::expression_node *var_node =  dynamic_cast<cdk::expression_node*>(node->arguments()->node(i));

    var_node->accept(this, lvl+1);
    actual =var_node->type();

    if(actual->name() == basic_type::TYPE_UNSPEC) {
      actual->_size = 4;
      actual->_name = basic_type::TYPE_INT;

    }

    function_arg_t actual_function_arg = { actual->name(), 0, basic_type::TYPE_UNSPEC };
    create_function_arg(actual, &actual_function_arg);

    if(!(expected->type_name == basic_type::TYPE_DOUBLE && actual_function_arg.type_name == basic_type::TYPE_INT)){
      if(expected->type_name != actual_function_arg.type_name)
        throw std::string("'" + id + "' invalid argument type1");
      if(expected->nr_subtypes != actual_function_arg.nr_subtypes)
        throw std::string("'" + id + "' invalid argument type2");
      if(expected->final_subtype_name != actual_function_arg.final_subtype_name)
        throw std::string("'" + id + "' invalid argument type3");
    }
  }

  if(symbol->type()->name() == basic_type::TYPE_POINTER){
    basic_type* type = new basic_type(4, basic_type::TYPE_POINTER);
    copy_pointer_type(symbol->type(), type);
    node->type(type);
  } else{
    node->type(new basic_type(symbol->type()->size(), symbol->type()->name()));
  }
}

void gr8::type_checker::do_function_definition_node(gr8::function_definition_node * const node, int lvl) {
  const std::string &id = node->funcName();
  std::shared_ptr<gr8::symbol> symbol = _symtab.find(id);

  if(symbol != nullptr && symbol->isFunction() && symbol->isDefined())
    throw std::string("'" + id + "' function redefinition");

  if(symbol != nullptr && !symbol->isFunction())
    throw std::string("'" + id + "' redeclared as different kind of symbol");

  if(symbol != nullptr && symbol->isFunction() && symbol->imported())
    throw std::string("'" + id + "' definition of imported function");

  if (symbol == nullptr) {
    if(node->funcName() == "covfefe"){
      node->funcName("_main");
    }
    std::vector<function_arg_t*> functionArgs;
    for (size_t i = 0; i < node->arguments()->nodes().size(); i++) {
      variable_declaration_node* var_decl = dynamic_cast<variable_declaration_node*>(node->arguments()->node(i));
    //  function_arg_t function_arg = { var_decl->type()->name(), 0, basic_type::TYPE_UNSPEC };
      function_arg_t *function_arg = new function_arg_t();
      function_arg->type_name = var_decl->type()->name();
      function_arg->nr_subtypes = 0;
      function_arg->final_subtype_name = basic_type::TYPE_UNSPEC ;

      create_function_arg(var_decl->type(), function_arg);
      functionArgs.push_back(function_arg);

    }
    symbol = std::make_shared<gr8::symbol>(node->type(), node->funcName(), true, node->imported(), node->publ(), functionArgs, false);
    _symtab.insert(node->funcName(), symbol);
    _parent->set_new_symbol(symbol);
  }

  symbol->isDefined(true);

}

void gr8::type_checker::do_function_declaration_node(gr8::function_declaration_node * const node, int lvl) {
  if(node->funcName() == "covfefe")
    node->funcName("_main");

  const std::string &id = node->funcName();
  std::shared_ptr<gr8::symbol> symbol = _symtab.find(id);

  if(symbol != nullptr && !symbol->isFunction())
    throw std::string("'" + id + "' redeclared as different kind of symbol");

  if (symbol == nullptr) {
    std::vector<function_arg_t*> functionArgs;
    for(auto arg : node->arguments()->nodes()){
      variable_declaration_node* var_decl = dynamic_cast<variable_declaration_node*>(arg);
      function_arg_t *function_arg = new function_arg_t();
      function_arg->type_name = var_decl->type()->name();
      function_arg->nr_subtypes = 0;
      function_arg->final_subtype_name = basic_type::TYPE_UNSPEC ;
      create_function_arg(var_decl->type(), function_arg);

      functionArgs.push_back(function_arg);
    }
    symbol = std::make_shared<gr8::symbol>(node->type(), id, true, node->imported(), node->publ(), functionArgs,false);
    _symtab.insert(id, symbol);
    _parent->set_new_symbol(symbol);
  } else{
    throw std::string("redeclared function '" + id + "'");
  }
}
void gr8::type_checker::do_return_node(gr8::return_node * const node, int lvl) {
  node->returnValue()->type(new basic_type(4, basic_type::TYPE_UNSPEC));
}

void gr8::type_checker::do_evaluation_node(gr8::evaluation_node * const node, int lvl) {
  node->argument()->accept(this, lvl + 2);
   if(node->argument()->type()->name() == basic_type::TYPE_UNSPEC){
        node->argument()->type()->_name = basic_type::TYPE_INT;
        node->argument()->type()->_size = 4;
      }
}

void gr8::type_checker::do_tweet_node(gr8::tweet_node * const node, int lvl) {

  node->argument()->accept(this, lvl +2);
  if(node->argument()->type()->name() == basic_type::TYPE_UNSPEC){
    node->argument()->type()->_name = basic_type::TYPE_INT;
    node->argument()->type()->_size = 4;
  }
  if ( !(node->argument()->type()->name() == basic_type::TYPE_INT ||
      node->argument()->type()->name() == basic_type::TYPE_DOUBLE ||
      node->argument()->type()->name() == basic_type::TYPE_STRING))
      throw std::string("wrong type in argument of tweet node");
}
void gr8::type_checker::do_post_node(gr8::post_node * const node, int lvl) {
  node->argument()->accept(this, lvl +2);

  if(node->argument()->type()->name() == basic_type::TYPE_UNSPEC){
    node->argument()->type()->_name = basic_type::TYPE_INT;
    node->argument()->type()->_size = 4;
  }

  if ( !(node->argument()->type()->name() == basic_type::TYPE_INT ||
      node->argument()->type()->name() == basic_type::TYPE_DOUBLE ||
      node->argument()->type()->name() == basic_type::TYPE_STRING))
      throw std::string("wrong type in argument of post node");
}

//---------------------------------------------------------------------------

void gr8::type_checker::do_read_node(gr8::read_node * const node, int lvl) {
  ASSERT_UNSPEC

  node->type(new basic_type(4, basic_type::TYPE_UNSPEC));
}

//---------------------------------------------------------------------------


void gr8::type_checker::do_sweeping_node(gr8::sweeping_node * const node, int lvl) {
  try{
    node->incrementableValue()->accept(this, lvl + 2);
    node->start()->accept(this, lvl+2);
    node->increment()->accept(this, lvl+2);
    node->end()->accept(this, lvl+2);
  }
  catch (const std::string &id) {
   throw std::string("undeclared variable '" + id + "'");
 }
}

void gr8::type_checker::do_stop_node(gr8::stop_node * const node, int lvl) {

}

void gr8::type_checker::do_again_node(gr8::again_node * const node, int lvl) {

}

//---------------------------------------------------------------------------

void gr8::type_checker::do_if_node(gr8::if_node * const node, int lvl) {
  node->condition()->accept(this, lvl + 4);

}

void gr8::type_checker::do_if_else_node(gr8::if_else_node * const node, int lvl) {
  node->condition()->accept(this, lvl + 4);
}

void gr8::type_checker::do_block_node(gr8::block_node * const node, int lvl) {

}

void gr8::type_checker::do_indexing_node(gr8::indexing_node * const node, int lvl) {
  ASSERT_UNSPEC;
  node->indexPosition()->accept(this, lvl + 2);

  if(node->indexPosition()->type()->name() == basic_type::TYPE_UNSPEC){
    node->indexPosition()->type()->_name = basic_type::TYPE_INT;
    node->indexPosition()->type()->_size = 4;
  }

  if (node->indexPosition()->type()->name() != basic_type::TYPE_INT)
    throw std::string("wrong type in argument of indexing node");

  node->pointer()->accept(this, lvl + 2);

  if(node->pointer()->type()->name() == basic_type::TYPE_UNSPEC){
    node->pointer()->type()->_name = basic_type::TYPE_INT;
    node->pointer()->type()->_size = 4;
  }

  if (node->pointer()->type()->name() != basic_type::TYPE_POINTER)
    throw std::string("wrong type in argument of indexing node");

  node->type(new basic_type(node->pointer()->type()->subtype()->size(), node->pointer()->type()->subtype()->name()));
}
