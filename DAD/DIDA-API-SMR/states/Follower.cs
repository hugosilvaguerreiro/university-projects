using System;
using System.Collections.Generic;
using System.Threading;
using System.Timers;
using DIDA_API_SMR.Log;
using DIDA_FAIL_DETECT;
using DIDA_Lib;
using DF = DIDA_Resources.DIDAFlags;


namespace DIDA_API_SMR.states
{
    class Follower : IState
    {
        public Node node { get; set; }
        public bool Frozen { get; set; }

        //Raft
        public int term { get; set; }
        public Dictionary<int, String> votedFor;
        public string leaderUrl = null;
        public RaftLog FollowerLog;
        //Timers
        private System.Timers.Timer ElectionTimer;
        public int GetElectionTimeout() { return DF.SMRFOLLOWERTIMEOUT(); } 
        public int RequestTimeout = DF.REQUESTTIMEOUT;
        System.Timers.Timer DebugTimer;

        public Follower(Node node, int term, IRaftLog log)
        {
            this.node = node;
            this.term = term;
            this.votedFor = new Dictionary<int, string>();
            FollowerLog = (RaftLog)log;
            ElectionTimer = TimerHelper.SetTimer(GetElectionTimeout(),
                                                 (ElapsedEventHandler)ElectionTimeout);


            System.Console.WriteLine("FOLLOWER");
        }
        public Follower(Node node, int term, IRaftLog log, Dictionary<int, String> voted)
        {
            this.node = node;
            this.term = term;
            this.votedFor = voted;
            FollowerLog = (RaftLog)log;
            ElectionTimer = TimerHelper.SetTimer(GetElectionTimeout(),
                                                 (ElapsedEventHandler)ElectionTimeout);

            System.Console.WriteLine("FOLLOWER");

        }

        //------------------ STATE MANAGEMENT FUNCTIONS ------------------------------

        public void Start()
        {
            CheckFreeze();
            TimerHelper.StartTimer(ElectionTimer);
        }

        public void Stop()
        {
            TimerHelper.StopTimer(ElectionTimer);
        }
        // --------------------------------------------------------------------------


        //-------------------------- RAFT SPECIFIC FUNCTIONS ------------------------- 

        private void ElectionTimeout(Object source, ElapsedEventArgs e)
        {
            CheckFreeze();

            //If election timeout elapses without receiving AppendEntries
            //RPC from current leader or granting vote to candidate:
            //convert to candidate
            leaderUrl = null;
            TimerHelper.StopTimer(ElectionTimer);
            this.node.SetState(new Candidate(this.node, this.term, this.FollowerLog));
        }

        public ResponseMessage AppendEntry(AppendEntriesMessage message)
        {
            try
            {
                TimerHelper.PauseTimer(ElectionTimer);
                //1.Reply false if term < currentTerm
                if (message.Term < this.term)
                {
                    return new ResponseMessage(this.term, false, message.SharedId, this.FollowerLog.CommitIndex);
                }
                this.term = message.Term;
                try
                {
                    this.FollowerLog.AppendNewEntries(message.Entry);
                }
                catch (Exception ex){}

                leaderUrl = message.url;
                //Restart timer to a new random value
                TimerHelper.RestartTimer(GetElectionTimeout(), ElectionTimer);

                return new ResponseMessage(this.term, true, message.SharedId, this.FollowerLog.CommitIndex);
            }
            catch (Exception ec){}
            return new ResponseMessage(this.term, true, message.SharedId, this.FollowerLog.CommitIndex);
        }


        public ResponseMessage RequestVote(RequestVoteMessage message)
        {
            CheckFreeze();
            if (message.Term < this.term)
            {
                return new ResponseMessage(this.term, false, message.SharedId);
            }else if (message.Term == this.term && message.LogSize > this.FollowerLog.LogList.Count)
            {
                return new ResponseMessage(this.term, true, message.SharedId);
            }
            this.term = message.Term;
            lock (votedFor)
            {
                if (!this.votedFor.ContainsKey(this.term))
                    this.votedFor.Add(this.term, null);
                if (votedFor[this.term] == null || message.CandidateId.Equals(votedFor[this.term]))
                {
                    votedFor[this.term] = message.CandidateId;
                    Console.WriteLine("Voted for: " + message.CandidateId + " " + this.term);
                    TimerHelper.RestartTimer(GetElectionTimeout(), ElectionTimer);
                    return new ResponseMessage(this.term, true, message.SharedId);
                }
            }
            TimerHelper.RestartTimer(GetElectionTimeout(), ElectionTimer);

            return new ResponseMessage(this.term, false, message.SharedId);

        }
        // -------------------------------------------------------------------------------------

        //-------------------------------- TUPLE SPACE SPECIFIC FUNCTIONS ----------------------

        public void Add(ITuple tuple)
        {
            throw new SMRNotLeaderException(leaderUrl);
            //throw new NotImplementedException();
            Console.WriteLine("NODE: Add");
        }

        public ITuple Read(ISchema schema)
        {
            throw new SMRNotLeaderException(leaderUrl);
            //throw new NotImplementedException();
            //Console.WriteLine("NODE: Read");
            // return null;
        }

        public ITuple Take(ISchema schema)
        {
            throw new SMRNotLeaderException(leaderUrl);
            //throw new NotImplementedException();
            //Console.WriteLine("NODE: Take");
            //return null;
        }

        public void AddS(ITuple tuple)
        {
            // System.Console.WriteLine("ADD: " + tuple.ToString());
            this.node.TupleSpace.Add(tuple);
            //throw new NotImplementedException();
        }

        public ITuple ReadS(ISchema schema)
        {
            throw new NotImplementedException();
        }

        public ITuple TakeS(ISchema schema)
        {
            // System.Console.WriteLine("Take: " + schema.ToString());
            return this.node.TupleSpace.Take(schema);
            //throw new NotImplementedException();
        }

        public RequestLogResponse RequestLog()
        {
            throw new NotImplementedException();
        }

        public void CheckFreeze()
        {
            while (Frozen)
            {
                System.Threading.Thread.Sleep(DF.FreezeStep);
            }
        }
        public void Status()
        {
            Console.WriteLine("################## FOLLOWER ########################");
            Console.WriteLine(FollowerLog.ToString());
            Console.WriteLine("####################################################");
        }
    }
}
