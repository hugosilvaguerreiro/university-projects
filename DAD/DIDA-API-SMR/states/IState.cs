using DIDA_Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_API_SMR.states
{
    public interface IState
    {
        int term { get; set; }
        Node node { get; set;  }
        bool Frozen { get; set; }
        void Start();
        void Stop();

        ResponseMessage AppendEntry(AppendEntriesMessage message);
        ResponseMessage RequestVote(RequestVoteMessage message);
        RequestLogResponse RequestLog();

        void Add(ITuple tuple);
        void AddS(ITuple tuple);
        ITuple Read(ISchema schema);
        ITuple ReadS(ISchema schema);
        ITuple Take(ISchema schema);
        ITuple TakeS(ISchema schema);

        void Status();
    }
}
