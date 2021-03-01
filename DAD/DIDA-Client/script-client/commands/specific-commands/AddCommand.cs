using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DIDA_Lib;
using DIDA_API.script_client.commands.visitor;

namespace DIDA_API.script_client.commands
{
    class AddCommand : IScriptCommand
    {
        public ITuple Tuple;
        public AddCommand(ITuple tuple) : base("basic")
        {
            Tuple = tuple;
        }
        public override void Accept(IDidaVisitor visitor)
        {
            visitor.VisitAdd(this);
        }
    }
}
