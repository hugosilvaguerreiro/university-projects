using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using DIDA_Lib;

namespace DIDA_API_SMR.states
{
    class Follower : IState
    {
        public Node node { get; set; }
        public bool pinged = true;
        public bool active;
        public string leaderUrl = null;
        public Follower(Node node) {
            active = true;
            this.node = node;
        }

        public void Start()
        {
            Console.WriteLine("FOLLOWER");
            _ = Timer(); // "_ = Timer..." to supress await warning
        }

        public void Stop()
        {
            active = false;
            Console.WriteLine("END FOLLOWER");
        }

        public async Task Timer()
        {
            int interval;
            Random r = new Random();
            while (active)
            {
                interval = r.Next(500, 2000);
                if (!pinged)
                {
                    node.SetState(new Candidate(node));
                    break;
                }
                else
                {
                    pinged = false;
                }
               // Console.WriteLine("waiting: " + interval);
                await Task.Delay(interval);
            }
        }


        public void Add(ITuple tuple)
        {
            throw new SMRNotLeaderException(leaderUrl);
        }

        public ITuple Read(ISchema schema)
        {
            throw new SMRNotLeaderException(leaderUrl);
        }

        public ITuple Take(ISchema schema)
        {
            throw new SMRNotLeaderException(leaderUrl);
        }

        public void Ping(int term, string url)
        {
          //  Console.WriteLine("(Follower) Got ping: " + url);
            node.term = term;
            leaderUrl = url;
            pinged = true;
        }

        public bool Vote(int term, string url)
        {
            Console.WriteLine("(Follower) Got vote: " + term + ", " + url);
            pinged = true;
            return node.GetVoteResult(term, url);
        }

        public void AddS(ITuple tuple)
        {
            // System.Console.WriteLine("ADD: " + tuple.ToString());
            this.node.TupleSpace.Add(tuple);
        }

        public ITuple ReadS(ISchema schema)
        {
            throw new NotImplementedException();
        }

        public ITuple TakeS(ISchema schema)
        {
            // System.Console.WriteLine("Take: " + schema.ToString());
            return this.node.TupleSpace.Take(schema);
        }

        public ResponseMessage AppendEntry(AppendEntriesMessage message)
        {
            throw new NotImplementedException();
        }
    }
}
