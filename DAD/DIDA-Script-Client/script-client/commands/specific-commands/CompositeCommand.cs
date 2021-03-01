using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DIDA_SCRIPT_CLIENT.script_client.commands.visitor;

namespace DIDA_SCRIPT_CLIENT.script_client.commands.specific_commands
{
    class CompositeCommand : IScriptCommand
    {
        public List<IScriptCommand> Commands;
        public CompositeCommand(List<IScriptCommand> commands) : base("complex")
        {
            Commands = commands;
        }

        public override void Accept(IDidaVisitor visitor)
        {
            visitor.VisitComposite(this);
        }
    }
}
