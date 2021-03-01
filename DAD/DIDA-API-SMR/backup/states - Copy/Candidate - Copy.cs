using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting.Messaging;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using DIDA_Lib;

namespace DIDA_API_SMR.states
{
    public class Candidate : IState
    {
        public delegate bool AsyncStartDelegate(int term, string url);

        public Node node { get; set; }
        public bool active;

        public Candidate(Node node)
        {
            active = true;
            this.node = node;
        }

        public void Start()
        {
           Console.WriteLine("CANDIDATE");
           RunElection();
        }

        private IAsyncResult RemoteAsyncCallStart(Node otherNode, int term, string url)
        {
            AsyncStartDelegate RemoteDel = new AsyncStartDelegate(otherNode.Vote);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(term, url, null, null);
            return RemAr;
        }

        public void Stop()
        {
            active = false;
            Console.WriteLine("END CANDIDATE");
        }

        public void Add(ITuple tuple)
        {
            throw new SMRCandidateException();
        }

        public ITuple Read(ISchema schema)
        {
            throw new SMRCandidateException();

        }

        public ITuple Take(ISchema schema)
        {
            throw new SMRCandidateException();
        }

        public void Ping(int term, string url)
        {
            if(active)
            {
                //System.Console.WriteLine("(CANDIDATE) ping");
                this.node.SetState(new Follower(node));
            }
            
        }

        public bool Vote(int term, string url)
        {
            //Console.WriteLine("C - Got vote: " + term + ", " + url);
            if (term > node.term)
                this.node.SetState(new Follower(node));
            return node.GetVoteResult(term, url);
        }

        public void AddS(ITuple tuple)
        {

        }

        public ITuple ReadS(ISchema schema)
        {
            return null;
        }

        public ITuple TakeS(ISchema schema)
        {
            return null;
        }

        private void RunElection()
        {
            while(active) { 
                node.IncrementTerm();
                node.GetVoteResult(node.term, node.url);
                bool allAck = false;
                List<IAsyncResult> results = new List<IAsyncResult>();
                List<WaitHandle> waits = new List<WaitHandle>();
                foreach (Node otherNode in this.node.otherNodes)
                {
                    IAsyncResult ar = RemoteAsyncCallStart(otherNode, this.node.term, this.node.url);
                    results.Add(ar);
                    waits.Add(ar.AsyncWaitHandle);

                }
                allAck = WaitHandle.WaitAll(waits.ToArray(), 2000);
                List<bool> count = new List<bool>();
                foreach (IAsyncResult result in results)
                {
                    AsyncStartDelegate del = (AsyncStartDelegate)((AsyncResult)result).AsyncDelegate;
                    count.Add(del.EndInvoke(result));
                }
                int countTrues = count.Where(item => item == true).Count() + 1;
                Console.WriteLine("Got votes! " + countTrues);
                if (countTrues > (count.Count + 1) / 2)
                {
                    Console.WriteLine("I am the leader!");
                    this.node.SetState(new Leader(node));
                }
                else
                {
                    Console.WriteLine("election failed :c");
                    Thread.Sleep((new Random()).Next(500, 1000));
                }
            }
        }

        public ResponseMessage AppendEntry(AppendEntriesMessage message)
        {
            throw new NotImplementedException();
        }
    }
}
