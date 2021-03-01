using DIDA_SCRIPT_CLIENT.script_client.commands.visitor;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_SCRIPT_CLIENT.script_client.commands.specific_commands
{
    class Script : CompositeCommand
    {
        public Script(List<IScriptCommand> commands) : base(commands) { }
        public override void Accept(IDidaVisitor visitor)
        {
            visitor.VisitScript(this);
        }
    }
}
