using System;
using System.Collections.Generic;
using System.Runtime.Remoting.Messaging;
using System.Threading;
using System.Timers;
using DIDA_API_SMR.Log;
using DIDA_FAIL_DETECT;
using DIDA_Lib;
using DF = DIDA_Resources.DIDAFlags;

namespace DIDA_API_SMR.states
{
    public class Candidate : IState
    {
        public delegate bool AsyncStartDelegate(int term, string url);
        public bool Frozen { get; set; }
        public Node node { get; set; }

        //raft
        public RaftLog CandidateLog { get; set; }
        public int term { get; set; }
        public bool leaderFound;
        public Dictionary<int, bool> AlreadyVoted;
        public int VotedForMeCount = 0;

        public Dictionary<int, String> votedFor;

        //timers
        public System.Timers.Timer ElectionTimer;
        public System.Timers.Timer RequestVotesTimer;
        public int RequestVotesTimeout = DF.REQUESTVOTESTIMEOUT;
        public int GetElectionTimeout() { return DF.SMRCANDIDATETIMEOUT(); }

        public Candidate(Node node, int term, RaftLog log)
        {
            Frozen = false;
            this.node = node;
            this.term = term;
            this.leaderFound = false;
            votedFor = new Dictionary<int, string>();
            AlreadyVoted = new Dictionary<int, bool>();
            for (int i = 0; i < this.node.otherNodes.Count; i++)
            {
                AlreadyVoted.Add(i, false);
            }
            ElectionTimer = TimerHelper.SetTimer(GetElectionTimeout(),
                                                 (ElapsedEventHandler)ElectionTimeout);
            TimerHelper.PauseTimer(ElectionTimer);

            RequestVotesTimer = TimerHelper.SetTimer(RequestVotesTimeout, (ElapsedEventHandler)RetryVoteRequestTimer);
            TimerHelper.PauseTimer(RequestVotesTimer);

            CandidateLog = new Log.RaftLog(this.node, this.term); //TODO CHANGE THIS
            Console.WriteLine("CANDIDATE");
            CandidateLog = (RaftLog)log;
        }

        //------------------ STATE MANAGEMENT FUNCTIONS ------------------------------
        public void Start()
        {
            CheckFreeze();
            StartElection();
        }

        public void Stop()
        {
            CheckFreeze();
            TimerHelper.StopTimer(ElectionTimer);
            TimerHelper.StopTimer(RequestVotesTimer);
        }
        // ---------------------------------------------------------------------------


        //-------------------------- RAFT SPECIFIC FUNCTIONS ------------------------- 

        /* Explanation of candidate election system:
         * 
         * There are 2 timers running simultaneously. 
         * The election timeout timer and a timer to request votes.
         * 
         * When a batch of vote requests is sent, the timer does not wait for each response
         * he simply sets a callback for the eventual responses. 
         * In this callback we will verify if he already has enough votes to become a leader.
         */

        private void ElectionTimeout(Object source, ElapsedEventArgs e)
        {
            CheckFreeze();

            try
            {
                if (leaderFound) return;
                System.Console.WriteLine("(CANDIDATE) Election failed");
                this.term++;
                TimerHelper.PauseTimer(RequestVotesTimer);
                TimerHelper.PauseTimer(ElectionTimer);
                lock (AlreadyVoted)
                {
                    for (int i = 0; i < AlreadyVoted.Count; i++)
                        AlreadyVoted[i] = false;
                }
                
                VotedForMeCount = 1;
                TimerHelper.RestartTimer(GetElectionTimeout(), ElectionTimer);
                TimerHelper.RestartTimer(RequestVotesTimeout, RequestVotesTimer);
            }
            catch (Exception ex)
            {
                //Console.WriteLine(ex.ToString());
            }
        }

        private void RetryVoteRequestTimer(Object source, ElapsedEventArgs e)
        {
            CheckFreeze();
            if (DF.N == 1)
                this.node.SetState(new Leader(node, this.term, this.CandidateLog));
            try
            {
                if (leaderFound) return;
                TimerHelper.PauseTimer(RequestVotesTimer);
                View v = this.node.manager.RequestView();
                foreach (KeyValuePair<int, Pingable> entry in v.pingables)
                {
                    if (!this.AlreadyVoted[entry.Key])
                    {
                        RemoteAsyncCallRequestVote(node.otherNodes[entry.Key], new RequestVoteMessage(this.term, this.CandidateLog.LogList.Count, this.node.id, entry.Key));
                    }
                }
                TimerHelper.RestartTimer(RequestVotesTimeout, RequestVotesTimer);
            }catch(Exception ex)
            {
                //Console.WriteLine(ex.ToString());
            }
                
            
        }

        public delegate ResponseMessage AsyncRequestVoteDelegate(RequestVoteMessage message);
        private IAsyncResult RemoteAsyncCallRequestVote(Node otherNode, RequestVoteMessage message)
        {
            AsyncRequestVoteDelegate RemoteDel = new AsyncRequestVoteDelegate(otherNode.RequestVote);
            AsyncCallback callback = new AsyncCallback(VerifyVotes);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(message, callback, null);
            return RemAr;
        }

        public void VerifyVotes(IAsyncResult result)
        {
            //TODO VERIFY EXCEPTION
            CheckFreeze();
            try
            {
                if (leaderFound) return;
                AsyncRequestVoteDelegate del = (AsyncRequestVoteDelegate)((AsyncResult)result).AsyncDelegate;
                ResponseMessage message;
                try
                {
                   message  = del.EndInvoke(result);
                }catch(Exception ex) { return; }
                

            Console.WriteLine("(CANDIDATE) verifying votes for: " + message.SharedId);

                lock(AlreadyVoted)
                {
                    if (message.Term >= this.term && !AlreadyVoted[message.SharedId])
                    {
                        AlreadyVoted[message.SharedId] = true;
                        if (message.Success)
                            VotedForMeCount++;
                    }
                    else if (message.Term < this.term) { return; }
                    else { AlreadyVoted[message.SharedId] = false; }
                    if (VotedForMeCount > DF.N / 2)
                    {
                        leaderFound = true;
                        TimerHelper.PauseTimer(ElectionTimer);
                        TimerHelper.PauseTimer(RequestVotesTimer);
                        this.node.SetState(new Leader(this.node, this.term, this.CandidateLog));
                    }
                }
               
            }catch(Exception ex) {}
                
        }
        

        public ResponseMessage AppendEntry(AppendEntriesMessage message)
        {
            CheckFreeze();
            if (message.Term < this.term)
            {
                return new ResponseMessage(this.term, false, message.SharedId);
            }
            this.term = message.Term;

            if (message.LeaderCommit < this.CandidateLog.CommitIndex)
            {
                return new ResponseMessage(this.term, false, message.SharedId, this.CandidateLog.CommitIndex);
            }
            try
            {
                this.CandidateLog.AppendNewEntries(message.Entry);
            }
            catch (Exception ex) {
            }

            leaderFound = true;
            TimerHelper.StopTimer(ElectionTimer);
            TimerHelper.StopTimer(RequestVotesTimer);
            this.node.SetState(new Follower(this.node, this.term, this.CandidateLog, this.votedFor));

            return new ResponseMessage(this.term, true, message.SharedId, this.CandidateLog.CommitIndex);
        }



        public ResponseMessage RequestVote(RequestVoteMessage message)
        {
            CheckFreeze();

            try
            {
                Console.WriteLine("Received request from: " + message.CandidateId + " " + term);
                //1.Reply false if term < currentTerm

                if (message.Term < this.term)
                {
                    return new ResponseMessage(this.term, false, message.SharedId);
                }else if(message.Term == this.term && message.LogSize > this.CandidateLog.LogList.Count)
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
                        leaderFound = true;
                        this.node.SetState(new Follower(this.node, this.term, this.CandidateLog, votedFor));
                        return new ResponseMessage(this.term, true, message.SharedId);
                    }
                    return new ResponseMessage(this.term, false, message.SharedId);
                }
            }
            catch (Exception e){}
            return new ResponseMessage(this.term, false, message.SharedId);
        }

        private void StartElection()
        {
            CheckFreeze();

            Console.WriteLine("Starting new election");
            this.term++;
            this.VotedForMeCount++;
            TimerHelper.RestartTimer(GetElectionTimeout(), ElectionTimer);
            TimerHelper.RestartTimer(RequestVotesTimeout, RequestVotesTimer);
        }




        // -------------------------------------------------------------------------------------

        //-------------------------------- TUPLE SPACE SPECIFIC FUNCTIONS ----------------------


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

        public void AddS(ITuple tuple)
        {
            throw new NotImplementedException();
        }

        public ITuple ReadS(ISchema schema)
        {
            throw new NotImplementedException();
        }

        public ITuple TakeS(ISchema schema)
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
        public RequestLogResponse RequestLog()
        {
            throw new NotImplementedException();
        }

        public void Status() {
            Console.WriteLine("################## CANDIDATE #######################");
            Console.WriteLine(CandidateLog.ToString());
            Console.WriteLine("####################################################");
        }
    }
}
