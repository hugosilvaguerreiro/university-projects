#ifndef __GR8_SEMANTICS_SYMBOL_H__
#define __GR8_SEMANTICS_SYMBOL_H__

#include <string>
#include <cdk/basic_type.h>

namespace gr8 {

  struct function_arg_t{
    basic_type::type type_name;
    int nr_subtypes;
    basic_type::type final_subtype_name;
  };

  class symbol {
    basic_type *_type;
    std::string _name;
    bool _isFunction;
    bool _imported;
    bool _public;
    std::vector<function_arg_t*> _functionArgs;
    bool _isDefined;
    long unsigned int _localPosition;
    bool _isGlobal;
    bool _isReAssigned;
    bool _isFuncArg;

  public:
    symbol(basic_type *type, const std::string &name, bool isFunction, bool imported, bool publ, std::vector<function_arg_t*> functionArgs, bool isDefined) :
        _type(type), _name(name), _isFunction(isFunction), _imported(imported), _public(publ), _functionArgs(functionArgs), _isDefined(isDefined) {
      _isGlobal = false;
    }

    symbol(basic_type *type, const std::string &name, bool isFunction, bool imported, bool publ) :
        _type(type), _name(name), _isFunction(isFunction), _imported(imported), _public(publ)  {
          _isGlobal = false;
          _isDefined = false;
    }

    virtual ~symbol() {
      delete _type;
    }

    basic_type *type() const {
      return _type;
    }
    const std::string &name() const {
      return _name;
    }

    inline bool isFunction() {
      return _isFunction;
    }

    bool imported() {
      return _imported;
    }

    bool isPublic() {
      return _public;
    }

    bool isGlobal() {
      return _isGlobal;
    }

    bool isReAssigned() {
      return _isReAssigned;
    }

    bool isFuncArg(){
      return _isFuncArg;
    }

    bool isDefined(){
      return _isDefined;
    }

    inline void setisImported(bool status){
      _imported = status;
    }

    inline void setisFuncArg(bool status){
      _isFuncArg = status;
    }

    inline void setisReAssigned(bool status){
      _isReAssigned = status;
    }

    inline void isDefined(bool status){
      _isDefined = status;
    }
    inline void setisGlobal(bool status){
      _isGlobal = status;
    }
    inline void setLocalPosition(long unsigned int localPosition){
      _localPosition = localPosition;
    }
    inline size_t localPosition(){
      return _localPosition;
    }



    std::vector<function_arg_t*> functionArgs() {
      return _functionArgs;
    }


  };

} // gr8

#endif
