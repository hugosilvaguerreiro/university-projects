using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DIDA_Lib;
using DIDA_SCRIPT_CLIENT.script_client.commands.visitor;

namespace DIDA_SCRIPT_CLIENT.script_client.commands
{
    class TakeCommand : IScriptCommand
    {
        public ISchema Schema;
        public TakeCommand(ISchema schema) : base("basic")
        {
            Schema = schema;
        }
        public override void Accept(IDidaVisitor visitor)
        {
            visitor.VisitTake(this);
        }
    }
}
