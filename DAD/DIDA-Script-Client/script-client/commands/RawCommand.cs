using DIDA_Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_SCRIPT_CLIENT.script_client.commands
{
    abstract class RawCommand { }

    static class RawCommandInfo
    {
        public static string GetTypeOfCommand(string commandName)
        {
            string[] basicCommands = new string[] { "add", "take", "read", "wait", "begin-repeat", "end-repeat"};
            string[] complexCommands = new string[] { "loop", "composite", "script" };
            int pos = Array.IndexOf(complexCommands, commandName);
            if(pos == -1)
            {
                return "basic";
            }else { return "complex"; }
        }
    }

    class BasicCommand : RawCommand
    {
        public string Name { get; set; }
        public List<IField> Fields { get; set; }
        public BasicCommand(string commandName, List<IField> fields)
        {
            Name = commandName;
            Fields = fields;
        }
    }

    class ComplexCommand : RawCommand
    {
        public string Name { get; set; }
        public List<IField> Fields { get; set; }
        public ComplexCommand(string commandName, List<IField> fields)
        {
            Name = commandName;
            Fields = fields;
        }
    }

}
