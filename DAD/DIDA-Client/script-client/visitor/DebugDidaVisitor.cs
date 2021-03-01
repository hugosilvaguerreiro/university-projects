using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DIDA_Lib;
using DIDA_API.script_client.commands.specific_commands;

namespace DIDA_API.script_client.commands.visitor
{
    class DebugDidaVisitor : IDidaVisitor
    {
        public void VisitAdd(AddCommand Add)
        {
            System.Console.WriteLine("Add");
        }

        public void VisitComposite(CompositeCommand Composite)
        {
            System.Console.WriteLine("Composite");
            foreach(IScriptCommand command in Composite.Commands)
            {
                command.Accept(this);
            }
        }

        public void VisitLoop(LoopCommand Loop)
        {
            Loop.Start.Accept(this);
            for(int i = 0; i < Loop.Start.Loops.Value; i++)
            {
                foreach (IScriptCommand command in Loop.Commands)
                {
                    command.Accept(this);
                }
            }
            Loop.End.Accept(this);

        }

        public void VisitLoopEnd(LoopEndCommand End)
        {
            System.Console.WriteLine("loop-end");
        }

        public void VisitLoopStart(LoopStartCommand Start)
        {
            System.Console.WriteLine("loop-start "+Start.Loops);
        }

        public void VisitRead(ReadCommand Read)
        {
            System.Console.WriteLine("read ");
            foreach(IField field in Read.Schema.Fields)
            {
                System.Console.WriteLine(field.ToString().Trim('"'));
            }
        }

        public void VisitScript(Script script)
        {
            foreach(IScriptCommand command in script.Commands)
            {
                command.Accept(this);
            }
        }

        public void VisitTake(TakeCommand Take)
        {
            System.Console.WriteLine("take" + Take.Schema.Fields);
        }

        public void VisitWait(WaitCommand Wait)
        {
            System.Console.WriteLine("wait");
        }
    }
}
