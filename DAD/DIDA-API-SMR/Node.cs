using DIDA_API_SMR.Log;
using DIDA_API_SMR.states;
using DIDA_FAIL_DETECT;
using DIDA_Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using DF = DIDA_Resources.DIDAFlags;
namespace DIDA_API_SMR
{
    public class Node : MarshalByRefObject, Pingable, IManageable
    {
        public string url;
        public string id;
        public readonly int N = 4;
        public static Dictionary<int, string> VotesPerTerm;
        private IState currentState;
        public TupleSpace TupleSpace;
        public Dictionary<int, Node> otherNodes = new Dictionary<int, Node>();
        public ViewManager manager;
        private readonly int minDelay;
        private readonly int maxDelay;
        private readonly Random random = new Random();

        public Node(string id, string url, List<Node> other, int minDelay, int maxDelay)
        {
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;
            this.url = url;
            this.id = id;
            VotesPerTerm = new Dictionary<int, string>();
            currentState = new Follower(this, 0, new RaftLog(this, 0));
            Dictionary<int, Pingable> ping = new Dictionary<int, Pingable>();

            TupleSpace = new TupleSpace();
            int count = 0;
            foreach(Node node in other)
            {
                ping.Add(count, node);
                otherNodes.Add(count, node);
                count++;
                N++;
            }
            manager = new ViewManager(ping);
            manager.Start();
            currentState.Start();
        }

        public void SetState(IState newState)
        {
                currentState.Stop();
                currentState = newState;
                currentState.Start();
        }


        public void Add(ITuple tuple) {
            AddDelay();
                currentState.Add(tuple);}
        public ITuple Read(ISchema schema) {
            AddDelay();
            return currentState.Read(schema); }
        public ITuple Take(ISchema schema) {
            AddDelay();
            return currentState.Take(schema); }
        public void AddS(ITuple tuple) {
            AddDelay();
            currentState.AddS(tuple);}
        public ITuple ReadS(ISchema schema)
        {
            AddDelay();
            return currentState.ReadS(schema); }
        public ITuple TakeS(ISchema schema)
        {
            AddDelay();
            return currentState.TakeS(schema); }
        public ResponseMessage AppendEntry(AppendEntriesMessage message)
        {
            AddDelay();
            return currentState.AppendEntry(message); }
        public ResponseMessage RequestVote(RequestVoteMessage message)
        {
            AddDelay();
            return currentState.RequestVote(message); }

        public RequestLogResponse RequestLog()
        {
            AddDelay();
            return currentState.RequestLog(); }
        public bool GetVoteResult(int term, string url)
        {
            AddDelay();
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
            AddDelay();
            return;
        }

        //PuppetFunctions

        public void Status()
        {
            Console.WriteLine("<<<<<<<<<<<<< NODE "+this.id+" >>>>>>>>>>>>>>>>>");
            Console.WriteLine("Term: "+this.currentState.term);
            if (currentState.Frozen)
            {
                Console.WriteLine("Server is Frozen");
            }
            else
            {
                Console.WriteLine("Server is Unfrozen");
            }
            currentState.Status();
        }

        public void Freeze()
        {
            Console.WriteLine("Freezing");
            currentState.Frozen = true;

        }

        public void Unfreeze()
        {
            Console.WriteLine("Unfreezing");
            if (DF.CurrentProjectVersion == (int)DF.ProjectVersion.BASIC)
            {
                currentState.Frozen = false;
                this.SetState(new Follower(this, this.currentState.term, new RaftLog(this, 0)));
            }
            else { currentState.Frozen = false; }
        }

        public void AddDelay()
        {
            Thread.Sleep(random.Next(minDelay, maxDelay));
        }
    }
}
