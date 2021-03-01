using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DIDA_Lib;
using DIDA_API.script_client.commands.visitor;

namespace DIDA_API.script_client.commands
{
    class ReadCommand : IScriptCommand
    {
        public ISchema Schema;
        public ReadCommand(ISchema tuple) : base("basic")
        {
            Schema = tuple;
        }

        public override void Accept(IDidaVisitor visitor)
        {
            visitor.VisitRead(this);
        }
    }
}
