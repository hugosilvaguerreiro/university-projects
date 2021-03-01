%{
//-- don't change *any* of these: if you do, you'll break the compiler.
#include <cdk/compiler.h>
#include "ast/all.h"
#define LINE               compiler->scanner()->lineno()
#define yylex()            compiler->scanner()->scan()
#define yyerror(s)         compiler->scanner()->error(s)
#define YYPARSE_PARAM_TYPE std::shared_ptr<cdk::compiler>
#define YYPARSE_PARAM      compiler
//-- don't change *any* of these --- END!

%}

%union {
  int                   i;	/* integer value */
  double                d;
  std::string          *s;	/* symbol name or string literal */
  cdk::basic_node      *node;	/* node pointer */
  gr8::block_node      *blocknode;
  cdk::sequence_node   *sequence;
  cdk::expression_node *expression; /* expression nodes */
  cdk::lvalue_node     *lvalue;
  basic_type           *basictype;

};

%token <i> tINTEGER
%token <d> tDOUBLE
%token <s> tIDENTIFIER tSTRING
%token <basictype>  tPOINTERTYPE tINTTYPE tREALTYPE tSTRINGTYPE

%token tIF tMEMORYALLOC tINITIALIZE tNULL tDEFINE tUSE tFOR tPUBLIC tPROCEDURE tFUNCTION
%token tUSES tON tAS tDO  tRETURN tNOT tAND tOR  tIF tTHEN tELSE tELSEIF
%token tSTOP tAGAIN  tASSIGN tTO tINDEX tAT tEQ tINPUT tPOST tTWEET tSWEEPCYCLE tFROM tBY

%nonassoc tIFX
%nonassoc tELSE
%left tOR
%left tAND
%nonassoc tNOT
%left tEQ
%left '>' '<'
%left '+' '-'
%left '*' '/' '%'
%nonassoc tLVALX
%nonassoc tUNARY tMEMORYALLOC 


%type <s> stringLit
%type <lvalue> lval
%type <expression> expr lit funcCall indexingExpr
%type <basictype> pointerType stringTypeSeq realTypeSeq intTypeSeq type funcT
%type <node> var funcVar declaration function funcDef funcDecl instruction attribution printInstr againInstr stopInstr arg
%type <node> returnInstr conditionalInstr elseif iterationInstr
%type <blocknode> block funcBody
%type <sequence> functionArgs exprList blockDecl blockInstrs declarations funcInstrs funcDeclZone



%{
//-- The rules below will be included in yyparse, the main parsing function.
%}
%%

file                  :  '{' declarations '}'                                                     { compiler->ast($2); }
                      | /*EMPTY*/                                                                 { compiler->ast(new cdk::sequence_node(LINE)); }
                      ;

declarations          : declaration                                                               { $$ = new cdk::sequence_node(LINE, $1); }
                      | declarations declaration                                                  { $$ = new cdk::sequence_node(LINE, $2, $1); }
                      ;

declaration           : var ';'                                                                   { $$ = $1;}
                      | function                                                                  { $$= $1;}
                      ;

function              : funcDef                                                                   {$$=$1;}
                      | funcDecl                                                                  {$$=$1;}
                      ;

funcDef               : tDEFINE funcT tIDENTIFIER tAS funcBody                                    {$$ = new gr8::function_definition_node(LINE, $5, false, false, $3, new cdk::sequence_node(LINE), $2);}
                      | tDEFINE funcT tIDENTIFIER tON functionArgs tAS  funcBody                  {$$ = new gr8::function_definition_node(LINE, $7, false, false, $3, $5, $2);}
                      | tDEFINE tPUBLIC funcT tIDENTIFIER tAS  funcBody                           {$$ = new gr8::function_definition_node(LINE, $6, true, false, $4, new cdk::sequence_node(LINE), $3);}
                      | tDEFINE tPUBLIC funcT tIDENTIFIER tON functionArgs tAS  funcBody          {$$ = new gr8::function_definition_node(LINE, $8, true, false, $4, $6, $3);}
                      | tDEFINE tUSE funcT tIDENTIFIER tAS  funcBody                              {$$ = new gr8::function_definition_node(LINE, $6, true, true, $4, new cdk::sequence_node(LINE), $3);}
                      | tDEFINE tUSE funcT tIDENTIFIER tON functionArgs tAS  funcBody             {$$ = new gr8::function_definition_node(LINE, $8, true, true, $4, $6, $3);}
                      ;

funcDecl              : funcT tIDENTIFIER ';'                                                     {$$ = new gr8::function_declaration_node(LINE, $1, false, false, $2, new cdk::sequence_node(LINE));}
                      | funcT tIDENTIFIER tUSES functionArgs ';'                                  {$$ = new gr8::function_declaration_node(LINE, $1, false, false, $2, $4);}
                      | tPUBLIC funcT tIDENTIFIER';'                                              {$$ = new gr8::function_declaration_node(LINE, $2, false, true, $3, new cdk::sequence_node(LINE));}
                      | tPUBLIC funcT tIDENTIFIER tUSES functionArgs ';'                          {$$ = new gr8::function_declaration_node(LINE, $2, false, true, $3, $5);}
                      | tUSE funcT tIDENTIFIER';'                                                 {$$ = new gr8::function_declaration_node(LINE, $2, true, true, $3, new cdk::sequence_node(LINE));}
                      | tUSE funcT tIDENTIFIER tUSES functionArgs ';'                             {$$ = new gr8::function_declaration_node(LINE, $2, true, true, $3, $5);}
                      ;

funcCall              : tUSE exprList tFOR tIDENTIFIER                                            {$$ = new gr8::function_call_node(LINE, $2,$4 );}
                      | tDO tIDENTIFIER                                                           {$$ = new gr8::function_call_node(LINE, new cdk::sequence_node(LINE), $2 );}
                      ;

funcT                 : type tFUNCTION                                                            {$$ = $1;}
                      | tPROCEDURE                                                                {$$ = new basic_type(4, basic_type::TYPE_VOID);}
                      ;

functionArgs          : arg                                                                       { $$ = new cdk::sequence_node(LINE, $1); }
                      | functionArgs ',' arg                                                      { $$ = new cdk::sequence_node(LINE, $3, $1);}
                      ;

arg                   : type tIDENTIFIER                                                          {$$ = new gr8::variable_declaration_node(LINE, false, false, $1, $2);}
                      ;


funcBody              : '{' funcInstrs '}'                                 {$$ = new gr8::block_node(LINE, nullptr, $2);} /* no declaration zone */
                      | '{' funcDeclZone '}'                               {$$ = new gr8::block_node(LINE, $2, nullptr);} /* no instruction zone */
                      | '{' funcDeclZone funcInstrs '}'                    {$$ = new gr8::block_node(LINE, $2, $3);}
                      ;

funcInstrs            : instruction                                        { $$ = new cdk::sequence_node(LINE, $1);}
                      | funcInstrs instruction                             { $$ = new cdk::sequence_node(LINE, $2, $1);}
                      ;

funcDeclZone          : funcVar  ';'                                       { $$ = new cdk::sequence_node(LINE, $1);}
                      | funcDeclZone funcVar ';'                           { $$ = new cdk::sequence_node(LINE, $2, $1);}
                      ;


funcVar               : type tIDENTIFIER                                   { $$ = new gr8::variable_declaration_node(LINE, false, false, $1, $2);}
                      | type tIDENTIFIER '(' tINITIALIZE expr ')'          { $$ = new gr8::variable_initialization_node(LINE, false, false, $1, $2, $5);}
                      ;


block                 : '{' blockInstrs '}'                                { $$ = new gr8::block_node(LINE, nullptr, $2);} /* no declaration zone */
                      | '{' blockDecl '}'                                  { $$ = new gr8::block_node(LINE, $2, nullptr);} /* no instruction zone */
                      | '{' blockDecl blockInstrs '}'                      { $$ = new gr8::block_node(LINE, $2, $3);}
                      ;


blockDecl             : var ';'                                            { $$ = new cdk::sequence_node(LINE, $1);}
                      | blockDecl var ';'                                  { $$ = new cdk::sequence_node(LINE, $2, $1);}
                      ;


blockInstrs           : instruction                                        { $$ = new cdk::sequence_node(LINE, $1);}
                      | blockInstrs instruction                            { $$ = new cdk::sequence_node(LINE, $2, $1);}
                      ;

instruction           : expr ';'                                           { $$ = new gr8::evaluation_node(LINE, $1); }
                      | attribution ';'                                    { $$=$1; }
                      | printInstr ';'                                     { $$=$1; }
                      | againInstr  ';'                                    { $$=$1; }
                      | stopInstr ';'                                      { $$=$1; }
                      | returnInstr ';'                                    { $$=$1; }
                      | conditionalInstr                                   { $$=$1; }
                      | iterationInstr                                     { $$=$1; }
                      | block                                              { $$=$1; }
                      ;



attribution           : tASSIGN expr tTO lval                              {$$ = new cdk::assignment_node(LINE,$4, $2);}
                      ;

printInstr            : tTWEET expr                                        {$$ = new gr8::tweet_node(LINE, $2);}
                      | tPOST expr                                         {$$ = new gr8::post_node(LINE, $2);}
                      ;

againInstr            : tAGAIN                                             {$$ = new gr8::again_node(LINE, 1);}
                      | tAGAIN tINTEGER                                    {$$ = new gr8::again_node(LINE, $2);}
                      ;

stopInstr             : tSTOP                                              {$$ = new gr8::stop_node(LINE, 1);}
                      | tSTOP tINTEGER                                     {$$ = new gr8::stop_node(LINE, $2);}
                      ;

returnInstr           : tRETURN expr                                       {$$ = new gr8::return_node(LINE, $2);}
                      ;

conditionalInstr      : tIF expr tTHEN block                               {$$ = new gr8::if_node(LINE, $2, $4);}
                      | tIF expr tTHEN block tELSE block                   {$$ = new gr8::if_else_node(LINE, $2, $4, $6);}
                      | tIF expr tTHEN block elseif                        {$$ = new gr8::if_else_node(LINE, $2, $4, $5);}
                      ;

elseif                : tELSEIF expr tTHEN block tELSE block               { $$ = new gr8::if_else_node(LINE, $2, $4, $6);}
                      | tELSEIF expr tTHEN block                           { $$ = new gr8::if_node(LINE, $2, $4);}
                      | tELSEIF expr tTHEN block elseif                    { $$ = new gr8::if_else_node(LINE, $2, $4, $5);}
                      ;

iterationInstr        : tSWEEPCYCLE lval tFROM expr tTO expr tDO block          { $$ = new gr8::sweeping_node(LINE, $2, $4, $6, $8, new cdk::integer_node(LINE, 1));}
                      | tSWEEPCYCLE lval tFROM expr tTO expr tBY expr tDO block { $$ = new gr8::sweeping_node(LINE, $2, $4, $6, $10, $8);}
                      ;


var                   : type tIDENTIFIER                                   { $$ = new gr8::variable_declaration_node(LINE, false, false, $1, $2);}
                      | tPUBLIC type tIDENTIFIER                           { $$ = new gr8::variable_declaration_node(LINE, true, false, $2, $3);}
                      | tUSE type tIDENTIFIER                              { $$ = new gr8::variable_declaration_node(LINE, true, true, $2, $3);} // If it is imported then it has to be public too
                      | tPUBLIC type tIDENTIFIER '(' tINITIALIZE expr ')'  { $$ = new gr8::variable_initialization_node(LINE, true, false, $2, $3, $6);}
                      | type tIDENTIFIER '(' tINITIALIZE expr ')'          { $$ = new gr8::variable_initialization_node(LINE, false, false, $1, $2, $5);}
                      ;


exprList              : expr                                               { $$ = new cdk::sequence_node(LINE, $1);}
                      | exprList ',' expr                                  { $$ = new cdk::sequence_node(LINE, $3, $1); }
                      ;
expr                  : lit                                                { $$ = $1;}
                      | '-' expr %prec tUNARY                              { $$ = new cdk::neg_node(LINE, $2);}
                      | '+' expr %prec tUNARY                              { $$ = new gr8::identity_node(LINE, $2);}                      
                      | expr '*' expr                                      { $$ = new cdk::mul_node(LINE, $1, $3);}
                      | expr '/' expr                                      { $$ = new cdk::div_node(LINE, $1, $3);}
                      | expr '%' expr                                      { $$ = new cdk::mod_node(LINE, $1, $3);}
                      | expr '+' expr                                      { $$ = new cdk::add_node(LINE, $1, $3);}
                      | expr '-' expr                                      { $$ = new cdk::sub_node(LINE, $1, $3);}
                      | expr '<' expr                                      { $$ = new cdk::lt_node(LINE, $1, $3);}
                      | expr '>' expr                                      { $$ = new cdk::gt_node(LINE, $1, $3);}
                      | tINPUT                                             { $$ = new gr8::read_node(LINE);}                      
                      | expr tMEMORYALLOC                                  { $$ = new gr8::memory_alloc_node(LINE, $1);}      
                      | expr tEQ expr                                      { $$ = new cdk::eq_node(LINE, $1, $3);}
                      | tNOT expr                                          { $$ = new cdk::not_node(LINE, $2);}
                      | expr tAND expr                                     { $$ = new cdk::and_node(LINE, $1, $3);}
                      | expr tOR expr                                      { $$ = new cdk::or_node(LINE, $1, $3); }
                      | '(' expr ')'                                       { $$ = $2; }
                      | lval '?'                                           { $$ = new gr8::reference_node(LINE, $1);}                                          
                      | lval                                               { $$ = new cdk::rvalue_node(LINE, $1);}
                      | funcCall                                           { $$=$1; }
                      ;

indexingExpr          : lit                                                { $$ = $1;}
                      | '+' indexingExpr %prec tUNARY                      { $$ = new gr8::identity_node(LINE, $2);}
                      | indexingExpr '+' indexingExpr                      { $$ = new cdk::add_node(LINE, $1, $3);}                      
                      | '(' indexingExpr ')'                               { $$ = $2; }
                      | tINPUT                                             { $$ = new gr8::read_node(LINE);}
                      | indexingExpr tMEMORYALLOC                          { $$ = new gr8::memory_alloc_node(LINE, $1);}
                      | lval                                               { $$ = new cdk::rvalue_node(LINE, $1);}
                      | funcCall                                           { $$=$1; }
                      ;

lval                  : tIDENTIFIER                                        { $$ = new cdk::identifier_node(LINE, $1); }
                      | tINDEX expr tAT indexingExpr %prec tLVALX               {$$ = new gr8::indexing_node(LINE, $2, $4);}
                      ;
                      
lit                   : tINTEGER                                           { $$ = new cdk::integer_node(LINE, $1); }
                      | stringLit                                          { $$ = new cdk::string_node(LINE, $1); }
                      | tDOUBLE                                            { $$ = new cdk::double_node(LINE, $1); }
                      | tNULL                                              { $$ = new gr8::null_pointer_node(LINE); }
                      ;


stringLit             : tSTRING                                            {$$ = $1;}
                      | stringLit tSTRING                                  {$$ = new std::string(*$1 + *$2); delete $1; delete $2;}
                      ;


type                  : tINTTYPE                                           {$$ = new basic_type(4,  basic_type::TYPE_INT);}
                      | tSTRINGTYPE                                        {$$ = new basic_type(4,  basic_type::TYPE_STRING);} //4 porque vai ser um ponteiro para um vetor de caracteres
                      | tREALTYPE                                          {$$ = new basic_type(8,  basic_type::TYPE_DOUBLE);}
                      | pointerType                                        {$$ = $1;}
                      ;


pointerType           : stringTypeSeq tSTRINGTYPE                          {$$=new basic_type(4, basic_type::TYPE_POINTER); $$->_subtype = $1;}
                      | realTypeSeq tPOINTERTYPE                           {$$=new basic_type(4, basic_type::TYPE_POINTER); $$->_subtype = $1;}
                      | intTypeSeq tPOINTERTYPE                            {$$=new basic_type(4, basic_type::TYPE_POINTER); $$->_subtype = $1;}
                      ;

stringTypeSeq         : tPOINTERTYPE                                       {$$ = new basic_type(4, basic_type::TYPE_STRING);}
                      | stringTypeSeq tPOINTERTYPE                         {$$ = new basic_type(4, basic_type::TYPE_POINTER); $$->_subtype = $1;}
                      ;

realTypeSeq           : tREALTYPE                                          {$$ = new basic_type(8,  basic_type::TYPE_DOUBLE);}
                      | realTypeSeq tREALTYPE                              {$$ = new basic_type(4, basic_type::TYPE_POINTER); $$->_subtype = $1;}
                      ;

intTypeSeq            : tINTTYPE                                           {$$ = new basic_type(4,  basic_type::TYPE_INT);}
                      | intTypeSeq tINTTYPE                                {$$ = new basic_type(4, basic_type::TYPE_POINTER); $$->_subtype = $1;}
                      ;

%%
