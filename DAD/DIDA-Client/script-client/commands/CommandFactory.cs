using DIDA_Lib;
using DIDA_API.script_client.commands.specific_commands;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_API.script_client.commands
{

    static class CommandFactory
    {

        public static IScriptCommand BuildCommand(RawCommand command) {
            if (command.GetType() == typeof(BasicCommand)) {
                return BuildBasicCommand((BasicCommand)command);
            }
            if (command.GetType() == typeof(BasicCommand)) {
                return BuildComplexCommand((ComplexCommand)command);
            }
            return null;
        }
        private static IScriptCommand BuildBasicCommand(BasicCommand command) {
            switch (command.Name)
            {
                case "add":
                    IField[] addFields = command.Fields.ToArray<IField>();
                    return new AddCommand(new Tuple(addFields));
                case "take":
                    IField[] takeFields = command.Fields.ToArray<IField>();
                    return new TakeCommand(new Schema(takeFields));
                case "read":
                    IField[] readFields = command.Fields.ToArray<IField>();
                    return new ReadCommand(new Schema(readFields));
                case "wait":
                    IntegerField waitField = (IntegerField)command.Fields[0];
                    return new WaitCommand(waitField);
                case "begin-repeat":
                    IntegerField beginField = (IntegerField)command.Fields[0];
                    return new LoopStartCommand(beginField);
                case "end-repeat":
                    return new LoopEndCommand();
            }
            return null;
        }

        private static IScriptCommand BuildComplexCommand(ComplexCommand command)
        {

            return null;
        }

       
        private static IScriptCommand BuildComplexCommand(string CommandName, params object[] args)
        {
            switch (CommandName)
            {
                case "loop":
                    LoopStartCommand start = (LoopStartCommand)args[0];
                    List<IScriptCommand> commands = (List<IScriptCommand>)args[1];
                    LoopEndCommand end = (LoopEndCommand)args[2];
                    return new LoopCommand(start, commands, end);
                case "script":
                    List<IScriptCommand> scriptsCommands = (List<IScriptCommand>)args[0];
                    return new CompositeCommand(scriptsCommands);
                case "composite":
                    List<IScriptCommand> compositeCommands = (List<IScriptCommand>)args[0];
                    return new CompositeCommand(compositeCommands);
            }
            return null;
        }
    }
}
