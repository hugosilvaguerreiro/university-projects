using DIDA_API.script_client.commands.specific_commands;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_API.script_client.commands.visitor
{
    interface IDidaVisitor
    {
        void VisitAdd(AddCommand Add);
        void VisitTake(TakeCommand Take);
        void VisitRead(ReadCommand Read);
        void VisitLoopStart(LoopStartCommand Start);
        void VisitLoopEnd(LoopEndCommand End);
        void VisitLoop(LoopCommand Loop);
        void VisitWait(WaitCommand Wait);
        void VisitComposite(CompositeCommand Composite);
        void VisitScript(Script script);
    }
}
