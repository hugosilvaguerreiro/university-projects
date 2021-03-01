using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DIDA_SCRIPT_CLIENT.script_client.commands.visitor;

namespace DIDA_SCRIPT_CLIENT.script_client.commands.specific_commands
{
    class LoopEndCommand : IScriptCommand
    {
        public LoopEndCommand() : base("basic") { }

        public override void Accept(IDidaVisitor visitor)
        {
            visitor.VisitLoopEnd(this);
        }
    }
}
