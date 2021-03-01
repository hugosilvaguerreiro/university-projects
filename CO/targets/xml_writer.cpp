#include <string>
#include "targets/xml_writer.h"
#include "targets/type_checker.h"
#include "ast/all.h"  // automatically generated

//---------------------------------------------------------------------------

void gr8::xml_writer::do_nil_node(cdk::nil_node * const node, int lvl) {
  os() << std::string(lvl, ' ') << "<nil_node/>" << std::endl;
}
void gr8::xml_writer::do_data_node(cdk::data_node * const node, int lvl) {
  os() << std::string(lvl, ' ') << "<data_node size='" << node->size() << "'/>" << std::endl;
}
void gr8::xml_writer::do_double_node(cdk::double_node * const node, int lvl) {
  process_literal(node, lvl);
}
void gr8::xml_writer::do_not_node(cdk::not_node * const node, int lvl) {
  do_unary_expression(node, lvl);
}
void gr8::xml_writer::do_and_node(cdk::and_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}
void gr8::xml_writer::do_or_node(cdk::or_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}

//---------------------------------------------------------------------------

void gr8::xml_writer::do_sequence_node(cdk::sequence_node * const node, int lvl) {
  os() << std::string(lvl, ' ') << "<sequence_node size='" << node->size() << "'>" << std::endl;
  for (size_t i = 0; i < node->size(); i++)
    node->node(i)->accept(this, lvl + 2);
  closeTag(node, lvl);
}

//---------------------------------------------------------------------------

void gr8::xml_writer::do_integer_node(cdk::integer_node * const node, int lvl) {
  process_literal(node, lvl);
}

void gr8::xml_writer::do_string_node(cdk::string_node * const node, int lvl) {
  process_literal(node, lvl);
}

//---------------------------------------------------------------------------

void gr8::xml_writer::do_unary_expression(cdk::unary_expression_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);
  node->argument()->accept(this, lvl + 2);
  closeTag(node, lvl);
}

void gr8::xml_writer::do_neg_node(cdk::neg_node * const node, int lvl) {
  do_unary_expression(node, lvl);
}

//---------------------------------------------------------------------------

void gr8::xml_writer::do_binary_expression(cdk::binary_expression_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);
  node->left()->accept(this, lvl + 2);
  node->right()->accept(this, lvl + 2);
  closeTag(node, lvl);
}

void gr8::xml_writer::do_add_node(cdk::add_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}
void gr8::xml_writer::do_sub_node(cdk::sub_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}
void gr8::xml_writer::do_mul_node(cdk::mul_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}
void gr8::xml_writer::do_div_node(cdk::div_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}
void gr8::xml_writer::do_mod_node(cdk::mod_node * const node, int lvl) {
 do_binary_expression(node, lvl);
}
void gr8::xml_writer::do_lt_node(cdk::lt_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}
void gr8::xml_writer::do_le_node(cdk::le_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}
void gr8::xml_writer::do_ge_node(cdk::ge_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}
void gr8::xml_writer::do_gt_node(cdk::gt_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}
void gr8::xml_writer::do_ne_node(cdk::ne_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}
void gr8::xml_writer::do_eq_node(cdk::eq_node * const node, int lvl) {
  do_binary_expression(node, lvl);
}

//---------------------------------------------------------------------------

void gr8::xml_writer::do_identifier_node(cdk::identifier_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  os() << std::string(lvl, ' ') << "<" << node->label() << ">" << node->name() << "</" << node->label() << ">" << std::endl;
}

void gr8::xml_writer::do_rvalue_node(cdk::rvalue_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);
  node->lvalue()->accept(this, lvl + 4);
  closeTag(node, lvl);
}

void gr8::xml_writer::do_assignment_node(cdk::assignment_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);

  node->lvalue()->accept(this, lvl + 4);

  node->rvalue()->accept(this, lvl + 4);
  closeTag(node, lvl);
}
void gr8::xml_writer::do_memory_alloc_node(gr8::memory_alloc_node * const node, int lvl) {
  do_unary_expression(node, lvl);
}
void gr8::xml_writer::do_reference_node(gr8::reference_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);
  node->address()->accept(this, lvl + 2);
  closeTag(node, lvl);
}

void gr8::xml_writer::do_variable_declaration_node(gr8::variable_declaration_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;

  os() << std::string(lvl, ' ') << "<variable_declaration_node public='" << node->publ() << "' ";
  os() << "imported='" << node->imported() << "' ";
  os() << "name='" << node->varName() << "'>" << std::endl;
  closeTag(node, lvl);
}

//---------------------------------------------------------------------------


void gr8::xml_writer::do_function_definition_node(gr8::function_definition_node * const node, int lvl) {

  ASSERT_SAFE_EXPRESSIONS;

  os() << std::string(lvl, ' ') << "<function_definition_node public='" << node->publ() << "' ";
  os() << "imported='" << node->imported() << "' ";
  os() << "name='" << node->funcName() << "'>" << std::endl;
  _symtab.push();
  if(node->arguments() != nullptr){
    openTag("arguments", lvl+2);
    node->arguments()->accept(this, lvl + 4);
    closeTag("arguments", lvl+2);
  }
  openTag("block", lvl+2);
  node->block()->accept(this, lvl + 4);
  closeTag("block", lvl+2);
  _symtab.pop();
  closeTag(node, lvl);
}

void gr8::xml_writer::do_function_declaration_node(gr8::function_declaration_node * const node, int lvl) {

  ASSERT_SAFE_EXPRESSIONS;

  os() << std::string(lvl, ' ') << "<function_declaration_node public='" << node->publ() << "' ";
  os() << "imported='" << node->imported() << "' ";
  os() << "name='" << node->funcName() << "'>" << std::endl;
  if(node->arguments() != nullptr){
    openTag("arguments", lvl+2);
    node->arguments()->accept(this, lvl + 4);
    closeTag("arguments", lvl+2);
  }
  closeTag(node, lvl);
}

void gr8::xml_writer::do_function_call_node(gr8::function_call_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;

  os() << std::string(lvl, ' ') << "<function_call_node identifier='" << node->functionIdentifier() << "'>" << std::endl;
  if(node->arguments() != nullptr){
    openTag("arguments", lvl+2);
    node->arguments()->accept(this, lvl + 4);
    closeTag("arguments", lvl+2);
  }
  closeTag(node, lvl);
}
void gr8::xml_writer::do_return_node(gr8::return_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);
  node->returnValue()->accept(this, lvl + 2);
  closeTag(node, lvl);
}

//---------------------------------------------------------------------------

void gr8::xml_writer::do_evaluation_node(gr8::evaluation_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);
  node->argument()->accept(this, lvl + 2);
  closeTag(node, lvl);
}

void gr8::xml_writer::do_tweet_node(gr8::tweet_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);
  node->argument()->accept(this, lvl+2);
  closeTag(node, lvl);
}
void gr8::xml_writer::do_post_node(gr8::post_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);
  node->argument()->accept(this, lvl+2);
  closeTag(node, lvl);
}

//---------------------------------------------------------------------------

void gr8::xml_writer::do_read_node(gr8::read_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  os() << std::string(lvl, ' ') << "<read_node/>" << std::endl;
}

//---------------------------------------------------------------------------

void gr8::xml_writer::do_sweeping_node(gr8::sweeping_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);
  openTag("incrementable_value", lvl+2);
  node->incrementableValue()->accept(this, lvl+4);
  closeTag("incrementable_value", lvl+2);
  openTag("start_from", lvl+2);
  node->start()->accept(this, lvl+4);
  closeTag("start_from", lvl+2);
  openTag("end_in", lvl+2);
  node->end()->accept(this, lvl+4);
  closeTag("end_in", lvl+2);
  openTag("increment_by", lvl+2);
  node->increment()->accept(this, lvl+4);
  closeTag("increment_by", lvl+2);
  openTag("execute_block", lvl+2);
  node->block()->accept(this, lvl+4);
  closeTag("execute_block", lvl+2);
  closeTag(node, lvl);
}

void gr8::xml_writer::do_stop_node(gr8::stop_node * const node, int lvl) {
  os() << std::string(lvl, ' ') << "<stop_node nr_of_cicles_to_stop='" << node->nr_of_cicles_to_stop() << "'/>" << std::endl;
}

void gr8::xml_writer::do_again_node(gr8::again_node * const node, int lvl) {
  os() << std::string(lvl, ' ') << "<again_node nr_of_cicles_to_skip='" << node->nr_of_cicles_to_skip() << "'/>" << std::endl;
}

//---------------------------------------------------------------------------

void gr8::xml_writer::do_if_node(gr8::if_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);
  openTag("condition", lvl + 2);
  node->condition()->accept(this, lvl + 4);
  closeTag("condition", lvl + 2);
  openTag("then", lvl + 2);
  node->block()->accept(this, lvl + 4);
  closeTag("then", lvl + 2);
  closeTag(node, lvl);
}

void gr8::xml_writer::do_if_else_node(gr8::if_else_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);
  openTag("condition", lvl + 2);
  node->condition()->accept(this, lvl + 4);
  closeTag("condition", lvl + 2);
  openTag("then", lvl + 2);
  node->thenblock()->accept(this, lvl + 4);
  closeTag("then", lvl + 2);
  openTag("else", lvl + 2);
  node->elseblock()->accept(this, lvl + 4);
  closeTag("else", lvl + 2);
  closeTag(node, lvl);
}

void gr8::xml_writer::do_block_node(gr8::block_node * const node, int lvl) {
  _symtab.push();
  openTag(node, lvl);
  if(node->varDeclarationZone() != nullptr){
    openTag("declaration_zone", lvl+2);
    node->varDeclarationZone()->accept(this, lvl+4);
    closeTag("declaration_zone", lvl+2);
  }
  if(node->instructionZone() != nullptr){
    openTag("instruction_zone", lvl+2);
    node->instructionZone()->accept(this, lvl+4);
    closeTag("instruction_zone", lvl+2);
  }
  closeTag(node, lvl);
  _symtab.pop();
}

void gr8::xml_writer::do_indexing_node(gr8::indexing_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  openTag(node, lvl);

  openTag("pointer", lvl+2);
  node->pointer()->accept(this, lvl+4);
  closeTag("pointer", lvl+2);
  openTag("index_position", lvl+2);
  node->indexPosition()->accept(this, lvl+4);
  closeTag("index_position", lvl+2);

  closeTag(node, lvl);
}

void gr8::xml_writer::do_identity_node(gr8::identity_node * const node, int lvl) {
  do_unary_expression(node, lvl);
}
void gr8::xml_writer::do_null_pointer_node(gr8::null_pointer_node * const node, int lvl) {
  os() << std::string(lvl, ' ') << "<null_pointer_node/>" << std::endl;
}
void gr8::xml_writer::do_variable_initialization_node(gr8::variable_initialization_node * const node, int lvl) {
  ASSERT_SAFE_EXPRESSIONS;
  os() << std::string(lvl, ' ') << "<variable_initialization_node public='" << node->publ() << "' ";
  os() << "imported='" << node->imported() << "' ";
  os() << "name='" << node->identifierName() << "'>" << std::endl;
  openTag("init_value", lvl+2);
  node->init_value()->accept(this, lvl + 4);
  closeTag("init_value", lvl+2);
  closeTag(node, lvl);
}
