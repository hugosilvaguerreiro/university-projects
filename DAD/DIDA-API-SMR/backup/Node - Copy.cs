using DIDA_API_SMR.raft;
using DIDA_API_SMR.states;
using DIDA_FAIL_DETECT;
using DIDA_Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_API_SMR
{
    public class Node : MarshalByRefObject, Pingable
    {
        public string url;
        public int term;
        public static Dictionary<int, string> VotesPerTerm;
        private IState currentState;
        public TupleSpace TupleSpace;
        public List<Node> otherNodes;

        public Node(string url, List<Node> other)
        {
            this.term = 0;
            this.url = url;
            VotesPerTerm = new Dictionary<int, string>();
            currentState = new Follower(this);
            TupleSpace = new TupleSpace();
            otherNodes = other;
            currentState.Start();
            
        }

        public void SetState(IState newState)
        {
            currentState.Stop();
            currentState = newState;
            currentState.Start();
        }

        public void Add(ITuple tuple) { currentState.Add(tuple); }
        public ITuple Read(ISchema schema) { return currentState.Read(schema); }
        public ITuple Take(ISchema schema) { return currentState.Take(schema); }
        public void AddS(ITuple tuple) { currentState.AddS(tuple); }
        public ITuple ReadS(ISchema schema) { return currentState.ReadS(schema); }
        public ITuple TakeS(ISchema schema) { return currentState.TakeS(schema); }
        public bool Vote(int term, string url) { return currentState.Vote(term, url); }
        public void Ping(int term, string url) { currentState.Ping(term, url); }
        public ResponseMessage AppendEntry(AppendEntriesMessage message) { return currentState.AppendEntry(message); }

        public void IncrementTerm()
        {
            term++;
        }

        public bool GetVoteResult(int term, string url)
        {
            bool result;

            //Console.WriteLine(term.ToString() + " " + url);
            if (!VotesPerTerm.ContainsKey(term))
            {
                VotesPerTerm.Add(term, url);
                result = true;
            }
            else if (VotesPerTerm.ContainsKey(term))
            {
                result = VotesPerTerm[term].Equals(url);
            }
            else
            {
                result = false;
            }

            return result;
            
        }

        public void IsAlive()
        {
            throw new NotImplementedException();
        }
    }
}
