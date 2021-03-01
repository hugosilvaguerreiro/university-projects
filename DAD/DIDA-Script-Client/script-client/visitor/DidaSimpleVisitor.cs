using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DIDA_API;
using DIDA_Lib;
using DIDA_SCRIPT_CLIENT.script_client.commands.specific_commands;

namespace DIDA_SCRIPT_CLIENT.script_client.commands.visitor
{
    class DidaSimpleVisitor : IDidaVisitor
    {
        private ClientApi CliApi;
        public DidaSimpleVisitor(ClientApi Api)
        {
            CliApi = Api;
        }

        public void VisitAdd(AddCommand Add)
        {
            System.Console.WriteLine("Sending add: " + Add.Tuple);
            CliApi.Add(Add.Tuple);
        }

        public void VisitComposite(CompositeCommand Composite)
        {
            foreach (IScriptCommand command in Composite.Commands)
            {
                command.Accept(this);
            }
        }

        public void VisitLoop(LoopCommand Loop)
        {
            Loop.Start.Accept(this);
            for (int i = 0; i < Loop.Start.Loops.Value; i++)
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
            System.Console.WriteLine("loop-start " + Start.Loops);
        }

        public void VisitRead(ReadCommand Read)
        {
            System.Console.WriteLine("Sending read: " + Read.Schema);
            ITuple tuple = CliApi.Read(Read.Schema);
            System.Console.WriteLine("Got: " + tuple);
        }

        public void VisitTake(TakeCommand Take)
        {
            System.Console.WriteLine("Sending take: " + Take.Schema);
            ITuple tuple = CliApi.Take(Take.Schema);
            System.Console.WriteLine("Got: " + tuple);
        }

        public void VisitScript(Script script)
        {
            foreach (IScriptCommand command in script.Commands)
            {
                command.Accept(this);
            }
        }

        public void VisitWait(WaitCommand Wait)
        {
            System.Console.WriteLine("wait: " + Wait.MiliSeconds.Value);
            System.Threading.Thread.Sleep(Wait.MiliSeconds.Value);
        }
    }
}
