using DIDA_Lib;
using DIDA_API.script_client;
using DIDA_API.script_client.commands;
using DIDA_API.script_client.commands.specific_commands;
using DIDA_API.script_client.commands.visitor;
using System.Collections.Generic;
using System.Linq;
using DIDA_API_SMR;

namespace DIDA_API
{
    class ScriptClient
    {
        static Tokenizer ScriptTokenizer = new Tokenizer();


        static Script BuildScriptTree(List<IScriptCommand> commands)
        {
            Stack<List<IScriptCommand>> context = new Stack<List<IScriptCommand>>();
            foreach(IScriptCommand command in commands)
            {
                if(context.Count() == 0) { context.Push(new List<IScriptCommand>()); };
                if (command.Type.Equals("basic") && 
                    command.GetType() != typeof(LoopStartCommand) &&
                    command.GetType() != typeof(LoopEndCommand)) {
                    context.Peek().Add(command);
                }
                if(command.GetType() == typeof(LoopStartCommand))
                {
                        List<IScriptCommand> newContext = new List<IScriptCommand>();
                        newContext.Add(command);
                        context.Push(newContext);
                }
                if(command.GetType() == typeof(LoopEndCommand))
                {
                    List<IScriptCommand> currentContext = context.Pop();
                    LoopStartCommand loopStart = (LoopStartCommand) currentContext[0];
                    currentContext.RemoveAt(0);
                    LoopCommand loop = new LoopCommand(loopStart, currentContext, (LoopEndCommand)command);
                    context.Peek().Add(loop);
                }
            }
            return new Script(context.Pop());
        }

        static RawCommand ParseScriptLine(string line)
        {
            List<Token> tokens =  ScriptTokenizer.Tokenize(line);
            string commandName = null;
            List<IField> fields = new List<IField>();
            foreach (Token token in tokens)
            {
                if(token.Name == "command_name")
                {
                    commandName = (string)token.Value;
                }else
                {
                    fields.Add((IField)token.Value);
                }                
            }
            string type = RawCommandInfo.GetTypeOfCommand(commandName);
            if(type.Equals("basic")) {return new BasicCommand(commandName, fields); }
            else {return new ComplexCommand(commandName, fields);}
        }

        static Script ParseScript(string ScriptName)
        {
            List<IScriptCommand> Commands = new List<IScriptCommand>();
            string UnparsedCommand;
            System.IO.StreamReader file = 
                new System.IO.StreamReader(ScriptName);

            while ((UnparsedCommand = file.ReadLine()) != null)
            {
                if(UnparsedCommand.StartsWith("//")) { continue; }
                RawCommand Raw = ParseScriptLine(UnparsedCommand);
                IScriptCommand Command = CommandFactory.BuildCommand(Raw);
                Commands.Add(Command);
            }
            return BuildScriptTree(Commands);
        }

        static void RunScript(Script script, IDidaVisitor visitor)
        {
            script.Accept(visitor);
        }


        public static void RunScript(string scriptName, ClientSmrApi client)
        {
            Script script = ParseScript(scriptName);
            RunScript(script, new DidaSmrVisitor(client));
        }

        public static void RunScript(string scriptName, ClientApi client)
        {
            Script script = ParseScript(scriptName);
            RunScript(script , new DidaSimpleVisitor(client));
        }
        /*static void Main(string[] args)
         {
             string sentence = "add <\"bla\",\"hey\",myclass(1,2,\"bla\")>";
             Tokenizer tokenizer = new Tokenizer();
             List<Token> tokens = tokenizer.Tokenize(sentence);
             System.Console.WriteLine("Original Command:");
             System.Console.WriteLine(sentence);
             System.Console.WriteLine("Tokens:");
             foreach(Token token in tokens)
             {
                System.Console.WriteLine("bla");
                System.Console.WriteLine(token.ToString());
              }
             string url = @"localhost:10000";
             Script script = ParseScript(@"D:\Downloads\DAD", "test2.txt");
             ClientApi api = new ClientApi(url);
             RunScript(script, new DidaSimpleVisitor(api));
             //RunScript(script, new DebugDidaVisitor());
             System.Console.ReadLine();

         }*/

    }
}
