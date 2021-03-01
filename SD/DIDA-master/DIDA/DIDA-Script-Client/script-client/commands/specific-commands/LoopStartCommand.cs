using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DIDA_Lib;
using DIDA_SCRIPT_CLIENT.script_client.commands.visitor;

namespace DIDA_SCRIPT_CLIENT.script_client.commands.specific_commands
{
    class LoopStartCommand : IScriptCommand
    {
        public IntegerField Loops;
        public LoopStartCommand(IntegerField nrOfLoops) : base("basic")
        {
            Loops = nrOfLoops;
        }

        public override void Accept(IDidaVisitor visitor)
        {
            visitor.VisitLoopStart(this);
        }
    }
}
