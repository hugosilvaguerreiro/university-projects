using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DIDA_Lib;
using DIDA_API.script_client.commands.visitor;

namespace DIDA_API.script_client.commands
{
    class WaitCommand : IScriptCommand
    {
        public IntegerField MiliSeconds;
        public WaitCommand(IntegerField miliSeconds) : base("basic")
        {
            MiliSeconds = miliSeconds;
        }

        public override void Accept(IDidaVisitor visitor)
        {
            visitor.VisitWait(this);
        }
    }
}
