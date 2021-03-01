using DIDA_Client.script_client;
using DIDA_Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;

namespace DIDA_API.script_client
{
    class DummyClass //TODO: REMOVE THIS CLASS
    { 
        string Name { get; set; }
        List<Object> Arguments { get; set; }
        public DummyClass(string ClassName, List<Object> arguments) {
            Name = ClassName;
            Arguments = arguments;
        }
        public override string ToString()
        {
            string result = Name + " ";
  
            System.Console.WriteLine(Arguments.Count());
            foreach(object i in Arguments)
            {
                result += i.ToString() + " ";
            }
            return result;
        }
    }

    class Token
    {
        public string Name { get; set; }
        public object Value { get; set; }
        public Token(string TokenName, object TokenValue)
        {
            Name = TokenName;
            Value = TokenValue;
        }

        public override string ToString()
        {
            return Name + " "+Value.ToString();
        }
    }

    class Tokenizer
    {
        private readonly string IntegerField;
        private readonly string StringField;
        private readonly string ObjectField;
        private readonly string ObjectArgumentRegex;
        private readonly string FieldRegex;
        private readonly string TypeField;
        private readonly string FieldSeparator;
        private readonly string MultipleArgumentsRules;
        private readonly string SingleArgumentRules;
        private readonly string NoArgumentRules;

        public Tokenizer()
        {
            IntegerField = "[0-9]+";
            StringField = "\"(([^\"])|";//select anything that isnt a quote
            StringField += "((?<=\\\\)\")|"; //select a quote that is preceeded by a slash
            StringField += "(\\\\(?=\"))(?!\\\\))*\""; //select a slash that is proceeded by a quote

            ObjectArgumentRegex = "((" + StringField + ")" + "|" + "(" + IntegerField + ")),{0,1}";

            ObjectField = ObjectArgumentRegex;
            ObjectField = "\\w+\\((" + ObjectField + ")+\\)";
            TypeField = "DADTestA|DADTestB|DADTestC|null";
            FieldRegex = "(" + StringField + "|" + ObjectField + "|" + TypeField + "),{0,1}";

            FieldSeparator = "<( )*.*( )*>";
            MultipleArgumentsRules = "^([\t ])*add |^([\t ])*read |^([\t ])*take";
            SingleArgumentRules = "^([\t ])*wait |^([\t ])*begin-repeat ";
            NoArgumentRules = "^([\t ])*end-repeat";
        }

        public List<Token> Tokenize(string line)
        {
            //Handle multiple arguments commands
            Match matchMultipleMatch = Regex.Match(line, MultipleArgumentsRules);
            if (matchMultipleMatch.Success)
            {
                string CommandName = matchMultipleMatch.ToString().Trim(' ').Trim('\t');
                List<string> separatedTokens = TokenizeMultipleArgumentsCommand(line);
                return ParseMultipleArgumentsCommandTokens(CommandName, separatedTokens);
            }
            List<Token> result = new List<Token>();
            //Handle single argument commands
            Match MatchSingleMatch = Regex.Match(line, SingleArgumentRules);
            if (MatchSingleMatch.Success)
            {
                string CommandName = MatchSingleMatch.ToString().Trim(' ').Trim('\t');
                string Argument = TokenizeSingleArgumentCommand(line);
                if (Argument != null)
                {
                    result.Add(new Token("command_name", CommandName));
                    result.Add(new Token("argument", new IntegerField(Int32.Parse(Argument))));
                    return result;
                }
            }
            //Handle no arguments commands
            Match NoArgumentMatch = Regex.Match(line, NoArgumentRules);
            if (NoArgumentMatch.Success)
            {
                string CommandName = NoArgumentMatch.ToString().Trim(' ').Trim('\t');
                result.Add(new Token("command_name", CommandName));
                return result;
            }
            result.Add(new Token("ERROR", null)); //The line is invalid
            return result;
        }

        private List<string> TokenizeMultipleArgumentsCommand(string line)
        {
            Match MatchFields = Regex.Match(line, FieldSeparator);
            if (MatchFields.Success)
            {
                string Fields = MatchFields.ToString().Trim('<').Trim('>');
                Match MatchIndividualFields = Regex.Match(Fields, FieldRegex);
                List<string> ExtractedFields = new List<string>();
                
                while (MatchIndividualFields.Success)
                {

                    ExtractedFields.Add(MatchIndividualFields.ToString().Trim(','));
                    Fields = Fields.Substring(MatchIndividualFields.Length, Fields.Length - MatchIndividualFields.Length);
                    MatchIndividualFields = Regex.Match(Fields, FieldRegex);
                }
               // foreach(string i in ExtractedFields)
               // {
                //    System.Console.WriteLine(i);
               // }
                
                return ExtractedFields;
            }
            return null;
        }

        private string TokenizeSingleArgumentCommand(string line)
        {
            Match MatchIntegerField = Regex.Match(line, " " + IntegerField);
            if (MatchIntegerField.Success)
            {
                return MatchIntegerField.ToString();
            }
            return null;
        }

        private List<Token> ParseMultipleArgumentsCommandTokens(string commandName, List<string> tokens)
        {
            List<Token> result = new List<Token>();
            result.Add(new Token("command_name", commandName));
            foreach(string token in tokens)
            {
                Match MatchObjectField = Regex.Match(token, ObjectField);
                if (MatchObjectField.Success)
                {
                    string className = Regex.Match(MatchObjectField.ToString().Trim(' '), "^\\w+\\(").ToString().Trim('(');
                    List<object> classFields = ExtractClassFields(MatchObjectField.ToString());
                    DADTest obj = ClassFactory.GetClass(className, classFields);
                    result.Add(new Token("object_field", new ObjectField(obj)));
                    continue;
                }
                Match MatchStringField = Regex.Match(token, StringField);
                if(MatchStringField.Success)
                {
                    result.Add(new Token("field_string", new StringField(MatchStringField.ToString().Trim('"'))));
                    continue;
                }
                Match MatchTypeField = Regex.Match(token, TypeField);
                if (MatchTypeField.Success)
                {
                    string typeName = MatchTypeField.ToString().Trim(' ');
                    Type obj = ClassFactory.GetClassType(typeName);
                    result.Add(new Token("object_field", new ObjectField(obj)));
                    continue;
                }


            }
            return result;
        }

        private List<object> ExtractClassFields(string objectField)
        {
            List<object> result = new List<object>();
            string objectFieldStart = "^\\w+\\(";
            string fields = string.Join("",Regex.Split(objectField.Trim(' ').Trim('\t'), objectFieldStart));
            System.Console.WriteLine(fields);
            Match MatchIndividualFields = Regex.Match(fields, ObjectArgumentRegex);
            while (MatchIndividualFields.Success)
            {
                Match IsInteger = Regex.Match(MatchIndividualFields.ToString(), IntegerField);
                if(IsInteger.Success) {
                    result.Add(Int32.Parse(IsInteger.ToString().Trim(',')));
                }
                else
                {
                    result.Add(MatchIndividualFields.ToString().Trim(',').Trim('"'));
                }
                fields = fields.Substring(MatchIndividualFields.Length, fields.Length - MatchIndividualFields.Length);
                MatchIndividualFields = Regex.Match(fields, ObjectArgumentRegex);
            }
            System.Console.WriteLine(result.Count());
            return result;

        }
    }
}