using DIDA_SCRIPT_CLIENT.script_client.commands.visitor;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_SCRIPT_CLIENT
{
    abstract class IScriptCommand
    {
        public readonly string Type;
        public IScriptCommand(string type)
        {
            Type = type;
        }
        abstract public void Accept(IDidaVisitor visitor);
    }
}
