using DIDA_API.script_client.commands.specific_commands;
using DIDA_API.script_client.commands.visitor;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_API.script_client.commands
{
    class LoopCommand : CompositeCommand
    {
        public LoopStartCommand Start;
        public LoopEndCommand End;

        public LoopCommand(LoopStartCommand start, List<IScriptCommand> commands, LoopEndCommand end) : base(commands)
        {
            Start = start;
            End = end;
        }
        public override void Accept(IDidaVisitor visitor)
        {
            visitor.VisitLoop(this);
        }
    }
}
