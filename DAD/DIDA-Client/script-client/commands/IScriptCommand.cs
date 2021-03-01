using DIDA_API.script_client.commands.visitor;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_API
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
