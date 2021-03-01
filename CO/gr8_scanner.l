%option c++ prefix="gr8_scanner_" outfile="gr8_scanner.cpp"
%option stack noyywrap yylineno 8bit
%{
// make relevant includes before including the parser's tab file
#include <string>
#include <stack>
#include <cdk/ast/sequence_node.h>
#include <cdk/ast/expression_node.h>
#include <cdk/ast/lvalue_node.h>
#include "gr8_parser.tab.h"
#include <limits.h>

std::stack<int> block_indent;
int number_indent=0;
// don't change this
#define yyerror LexerError
%}
%x X_STRING X_INDENT X_REMOVEINDENT X_ENDL X_INITLINE X_COMMENT
%%


"!!".*                 ; /* ignore comments */

<X_INDENT>.                                       yyless(0); yy_pop_state(); return '{';
<X_REMOVEINDENT>.                                 yyless(0); yy_pop_state(); return '}';
<X_REMOVEINDENT><<EOF>>                           {
                                                    yy_pop_state();
                                                    if(!block_indent.empty()){

                                                      block_indent.pop();
                                                      yy_push_state(X_REMOVEINDENT);
                                                      return '}';
                                                    }
                                                    else{ yyterminate(); }
                                                  }
<X_ENDL>.                                          yyless(0); yy_pop_state();return ';';
<X_ENDL><<EOF>>                                   {
                                                    yy_pop_state();
                                                    yy_push_state(X_REMOVEINDENT);
                                                    return ';';
                                                  }

<X_STRING>\"           yy_pop_state(); return tSTRING;
<X_STRING>~t           *yylval.s += "\t";
<X_STRING>~n           *yylval.s += "\n";
<X_STRING>~r           *yylval.s += "\r";
<X_STRING>~\"          *yylval.s += "\"";
<X_STRING>~0           *yylval.s += "\0";
<X_STRING>~~           *yylval.s += "~";
<X_STRING>~[0-6][0-6]?[0-6]?  {
                                int decimalValue = strtol(yytext+1, nullptr, 7);
                                if(decimalValue > 255) {yyerror("overflow");}
                                std::string charFromInt(1, decimalValue);
                                *yylval.s += charFromInt;
                              }
<X_STRING>~            yyerror("no special char identified");
<X_STRING>.            *yylval.s += *yytext;
<X_STRING>\n           yyerror("newline in string");

<X_COMMENT>"<<"          yy_push_state(X_COMMENT);
<X_COMMENT>">>"          yy_pop_state();
<X_COMMENT>.|\n          ;

[ \t]                                             number_indent++;
.                                     {
                                                    yy_push_state(X_INITLINE);
                                                    if(block_indent.empty() || block_indent.top() < number_indent){
                                                      block_indent.push(number_indent);
                                                      yy_push_state(X_INDENT);
                                                    }
                                                    else if(block_indent.top() > number_indent){
                                                      while (!block_indent.empty() && block_indent.top() > number_indent){
                                                         block_indent.pop();
                                                         yy_push_state(X_REMOVEINDENT);
                                                      }
                                                      yy_push_state(X_ENDL);
                                                    }
                                                    else if(block_indent.top() == number_indent){
                                                      yy_push_state(X_ENDL);
                                                    }
                                                    number_indent = 0;
                                                    yyless(0);
                                                  }
<<EOF>>                                           {
                                                    if(!block_indent.empty()){
                                                      yy_push_state(X_ENDL);
                                                   }
                                                    else{ yyterminate(); }
                                                  }
<X_INITLINE>{
  "!!".*                 ; /* ignore comments */

  "small"                return tINTTYPE;
  "huge"                 return tREALTYPE;
  "news"                 return tSTRINGTYPE;


  "initially"            return tINITIALIZE;


  "null"                 return tNULL;
  "fake"                 return tPOINTERTYPE;

  "define"               return tDEFINE;
  "use"                  return tUSE;
  "for"                  return tFOR;
  "public"               return tPUBLIC;
  "procedure"            return tPROCEDURE;
  "function"             return tFUNCTION;

  "on"                   return tON;
  "uses"                 return tUSES;
  "as"                   return tAS;
  "do"                   return tDO;

  "return"               return tRETURN;

  "minus"                return '-';
  "plus"                 return '+';
  "times"                return '*';
  "over"                 return '/';
  "modulus"              return '%';


  "not"                  return tNOT;
  "and"                  return tAND;
  "or"                   return tOR;

  "if"                   return tIF;
  "then"                 return tTHEN;
  "elsif"                return tELSEIF;
  "else"                 return tELSE;

  "stop"                 return tSTOP;
  "again"                return tAGAIN;


  "assign"               return tASSIGN;
  "to"                   return tTO;

  "cell"                 return tINDEX;
  "at"                   return tAT;

  "above"                return '>';
  "below"                return '<';
  "equals"               return tEQ;


  "input"                return tINPUT;
  "objects"			         return tMEMORYALLOC;
  "post"                 return tPOST;
  "tweet"                return tTWEET;
  "sweeping"             return tSWEEPCYCLE;
  "from"                 return tFROM;
  "by"                   return tBY;

  [1-9][0-9]*                        {long int result = strtol(yytext, nullptr, 10); if(errno == ERANGE || result > INT_MAX) yyerror("overflow");yylval.i = result; return tINTEGER;}
  0[0-6]*                            {long int result = strtol(yytext, nullptr, 7); if(errno == ERANGE || result > INT_MAX) yyerror("overflow"); yylval.i = result;  return tINTEGER;}
  [0-9]*"."[0-9]+([eE][+-]?[0-9]+)?  {yylval.d = strtod(yytext, nullptr); if(errno == ERANGE ) yyerror("overflow"); return tDOUBLE;}
  [0-9]+"."([eE][+-]?[0-9]+)?        {yylval.d = strtod(yytext, nullptr); if(errno == ERANGE) yyerror("overflow");  return tDOUBLE;}


  [A-Za-z][-A-Za-z0-9]*   yylval.s = new std::string(yytext); return tIDENTIFIER;

  \"                      yy_push_state(X_STRING); yylval.s = new std::string("");
  "<<"                    yy_push_state(X_COMMENT);

  [(),?]                  return *yytext;

  [ \t]+                  ; /* ignore whitespace */

  "..."\n                 ; /* ignore ... before new line */

  \n                      yy_pop_state();

  .                      yyerror("Unknown character");
}

%%
