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
        Node node { get; set;  }
        void Start();
        void Stop();

        ResponseMessage AppendEntry(AppendEntriesMessage message);


        void Add(ITuple tuple);
        void AddS(ITuple tuple);
        ITuple Read(ISchema schema);
        ITuple ReadS(ISchema schema);
        ITuple Take(ISchema schema);
        ITuple TakeS(ISchema schema);

        void Ping(int term, string url);
        bool Vote(int term, string url);

    }
}
