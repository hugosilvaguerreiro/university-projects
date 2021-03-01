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
    class Leader : IState
    {
        public Node node { get; set; }
        public bool Frozen { get; set; }

 
        bool changedState = false;
        public delegate void AsyncAddDelegate(ITuple tuple);
        public delegate ITuple AsyncTakeDelegate(ISchema schema);
        public Dictionary<int, bool> AlreadyVoted;
        public Dictionary<int, String> votedFor;


        public int term { get; set; }

        int HeartbeatTimeout = DF.HEARTBEATTIMEOUT;
        System.Timers.Timer HeartBeatTimer;
        LeaderLog leaderLog;

        public Leader(Node node, int term, RaftLog log)
        {
            Frozen = false;
            this.node = node;
            this.term = term;
            leaderLog = new LeaderLog(this.node, log, this.term);
            AlreadyVoted = new Dictionary<int, bool>();
            votedFor = new Dictionary<int, string>();
            System.Console.WriteLine("LEADER");
            HeartBeatTimer = TimerHelper.SetTimer(HeartbeatTimeout, 
                                                (ElapsedEventHandler)HeartBeatOrEntryUpdate);

        }

        //------------------ STATE MANAGEMENT FUNCTIONS ------------------------------

        public void Start()
        {
            CheckFreeze();
            TimerHelper.StartTimer(HeartBeatTimer);

        }

        public void Stop()
        {
            CheckFreeze();
            TimerHelper.StopTimer(HeartBeatTimer);
        }
        //----------------------------------------------------------------------

        //-------------------------- RAFT SPECIFIC FUNCTIONS ------------------------- 
        public delegate ResponseMessage RemoteAsyncCallAppendEntryDelegate(AppendEntriesMessage message);
        private IAsyncResult RemoteAsyncCallAppendEntry(Node otherNode, AppendEntriesMessage message)
        {
            RemoteAsyncCallAppendEntryDelegate RemoteDel = new RemoteAsyncCallAppendEntryDelegate(otherNode.AppendEntry);
            AsyncCallback callback = new AsyncCallback(VerifyHeartBeatsMessages);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(message, callback, null);
            return RemAr;
        }
        public void VerifyHeartBeatsMessages(IAsyncResult result)
        {
            try
            {
                if (changedState) return;
                RemoteAsyncCallAppendEntryDelegate del = (RemoteAsyncCallAppendEntryDelegate)((AsyncResult)result).AsyncDelegate;
                ResponseMessage r = del.EndInvoke(result);
                if (r.Term > this.term)
                {
                    changedState = true;
                    this.node.SetState(new Follower(this.node, r.Term, this.leaderLog.log));
                }
                this.leaderLog.FollowersCommitIndex[r.SharedId] = r.CommitIndex;
            }
            catch (Exception ex){}
            
        }

        private void HeartBeatOrEntryUpdate(Object source, ElapsedEventArgs e)
        {
            CheckFreeze();

            //UPDATE ENTRIES IN NODES THAT NEED TO APPEND AN ENTRY 
            //OR JUST SEND AN HEARTBEAT (empty entry parameter)
            try
            {
                Random r = new Random();
                TimerHelper.PauseTimer(HeartBeatTimer);
                View v = this.node.manager.RequestView();
                foreach (KeyValuePair<int, Pingable> entry in v.pingables)
                {
                    List<Entry> l = new List<Entry>();
                    
                    int ind = leaderLog.FollowersCommitIndex[entry.Key];
                    l = this.leaderLog.log.LogList.GetRange(ind, this.leaderLog.log.LogList.Count - ind);
                    
                    RemoteAsyncCallAppendEntry((Node)entry.Value, 
                        new AppendEntriesMessage(this.term, 0, 0, ind, l, entry.Key, node.url));
                }
                TimerHelper.StartTimer(HeartBeatTimer);
            }
            catch(Exception ex) { }
        }

        public ResponseMessage AppendEntry(AppendEntriesMessage message)
        {
            CheckFreeze();

            bool shouldBecomeFollower = false;
            TimerHelper.PauseTimer(HeartBeatTimer);
            if ((message.Term < this.term) ||
                !(this.leaderLog.Match(message.PreviousLogIndex, message.PreviousLogTerm)))
            {
                TimerHelper.StartTimer(HeartBeatTimer);
                return new ResponseMessage(this.term, false, message.SharedId);
            }else if(message.Term > this.term)
            {
                this.term = message.Term;
                shouldBecomeFollower = true;
            }
            leaderLog.AppendNewEntries(message.Entry);

            leaderLog.UpdateCommitIndex(message.LeaderCommit);

            if (shouldBecomeFollower) node.SetState(new Follower(node, this.term, this.leaderLog));

            return new ResponseMessage(this.term, true, message.SharedId);
        }

        public ResponseMessage RequestVote(RequestVoteMessage message)
        {
            CheckFreeze();

            try
            {
                if (message.Term < this.term)
                {
                    return new ResponseMessage(this.term, false, message.SharedId);
                }
                else if (message.Term > this.term)
                {
                    this.term = message.Term;
                    node.SetState(new Follower(node, this.term, this.leaderLog.log));
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
            return new ResponseMessage(this.term, false, message.SharedId);
        }

        //-----------------------------------------------------------------------------

        //-------------------------------- TUPLE SPACE SPECIFIC FUNCTIONS ----------------------

        public void Add(ITuple tuple)
        {

           this.node.TupleSpace.Add(tuple);
            List<Entry> l = new List<Entry>();
            lock (this.leaderLog)
            {
                l.Add(new Entry("add", tuple, this.term, this.leaderLog.CommitIndex));
                this.leaderLog.CommitIndex++;
                this.leaderLog.AppendNewEntries(l);
            }
            if (DF.N == 1)
                return;
            bool allAck = false;
            int consensus = 0;
            Dictionary<int, bool> alreadyTried = new Dictionary<int, bool>(DF.N);
            for (int i = 0; i < DF.N; i++) alreadyTried[i] = false;
            while (!allAck)
            {
                List<IAsyncResult> results = new List<IAsyncResult>();
                List<WaitHandle> waits = new List<WaitHandle>();
                View v = this.node.manager.RequestView();
                foreach (KeyValuePair<int, Pingable> entry in v.pingables)
                {
                    if(!alreadyTried[entry.Key])
                    {
                        AppendEntriesMessage message = new AppendEntriesMessage(this.term, 0, 0, this.leaderLog.CommitIndex, l, entry.Key, node.url);
                        RemoteAsyncCallAppendEntryDelegate RemoteDel = new RemoteAsyncCallAppendEntryDelegate(node.otherNodes[entry.Key].AppendEntry);
                        IAsyncResult RemAr = RemoteDel.BeginInvoke(message, null, null);
                        results.Add(RemAr);
                        waits.Add(RemAr.AsyncWaitHandle);
                    }

                }

                allAck = WaitHandle.WaitAll(waits.ToArray(), DF.TIMEOUT);//Change to a more reliable value because of timeouts
                if (!allAck)
                {
                    for(int i = 0; i < waits.Count; i++)
                    {try
                        {
                            if (waits[i].WaitOne(0))
                            {
                                
                                RemoteAsyncCallAppendEntryDelegate d = (RemoteAsyncCallAppendEntryDelegate)((AsyncResult)results[i]).AsyncDelegate;
                                ResponseMessage r = d.EndInvoke(results[i]);
                                if (r.Success && !alreadyTried[r.SharedId])
                                {
                                    //leaderLog.FollowersCommitIndex[r.SharedId]++;
                                    alreadyTried[r.SharedId] = true;
                                    consensus++;
                                }
                            }
                        }catch(Exception ex) { }
                        if (consensus > DF.N / 2)
                            allAck = true;
                    }
                } 
            }
            
            foreach (KeyValuePair<int, bool> entry in alreadyTried)
            {
                if(entry.Value)
                {
                    leaderLog.FollowersCommitIndex[entry.Key]++;
                }
            }
            return;
        }

        public ITuple Take(ISchema schema)
        {
            ITuple result;
            Console.WriteLine("start take");
            result = this.node.TupleSpace.Take(schema);
            Console.WriteLine("end take");
            List<Entry> l;
            lock (this.leaderLog)
            {
                

                l = new List<Entry>();
                l.Add(new Entry("take", schema, this.term, this.leaderLog.CommitIndex));
                this.leaderLog.CommitIndex++;
                this.leaderLog.AppendNewEntries(l);
                
            }
            if (DF.N == 1)
                return result;

            bool allAck = false;
            int consensus = 0;
            Dictionary<int, bool> alreadyTried = new Dictionary<int, bool>(DF.N);
            for (int i = 0; i < DF.N; i++) alreadyTried[i] = false;
            while (!allAck)
            {
                List<IAsyncResult> results = new List<IAsyncResult>();
                List<WaitHandle> waits = new List<WaitHandle>();
                //foreach (Node node in this.node.otherNodes)


                View v = this.node.manager.RequestView();
                List<Entry> a = new List<Entry>();
                a.Add(new Entry("take", schema, this.term, this.leaderLog.CommitIndex));
                //foreach (KeyValuePair<int, Node> entry in nodesLeftToTry)
                foreach (KeyValuePair<int, Pingable> entry in v.pingables)
                {
                    if (!alreadyTried[entry.Key])
                    {
                        AppendEntriesMessage message = new AppendEntriesMessage(this.term, 0, 0, this.leaderLog.CommitIndex, l, entry.Key, node.url);
                        RemoteAsyncCallAppendEntryDelegate RemoteDel = new RemoteAsyncCallAppendEntryDelegate(node.otherNodes[entry.Key].AppendEntry);
                        IAsyncResult RemAr = RemoteDel.BeginInvoke(message, null, null);
                        results.Add(RemAr);
                        waits.Add(RemAr.AsyncWaitHandle);
                    }

                }

                allAck = WaitHandle.WaitAll(waits.ToArray(), DF.TIMEOUT);//Change to a more reliable value because of timeouts
                if (!allAck)
                {
                    //                   foreach(WaitHandle h in waits)
                    for (int i = 0; i < waits.Count; i++)
                    {
                        try
                        {
                            if (waits[i].WaitOne(0))
                            {

                                RemoteAsyncCallAppendEntryDelegate d = (RemoteAsyncCallAppendEntryDelegate)((AsyncResult)results[i]).AsyncDelegate;
                                ResponseMessage r = d.EndInvoke(results[i]);
                                if (r.Success && !alreadyTried[r.SharedId])
                                {
                                    //leaderLog.FollowersCommitIndex[r.SharedId]++;
                                    alreadyTried[r.SharedId] = true;
                                    consensus++;
                                }
                            }
                        }
                        catch (Exception ex) { }
                        if (consensus > DF.N / 2)
                            allAck = true;
                    }
                }
            }

            foreach (KeyValuePair<int, bool> entry in alreadyTried)
            {
                if (entry.Value)
                {
                    //leaderLog.FollowersCommitIndex[entry.Key]++;
                }
            }
            return result;
        }

        public ITuple Read(ISchema schema)
        {
            System.Console.WriteLine("Read: " + schema.ToString());
            return this.node.TupleSpace.Read(schema);
            
        }

        private IAsyncResult RemoteAsyncCallAdd(Node node, ITuple tuple)
        {
             AsyncAddDelegate RemoteDel = new AsyncAddDelegate(node.AddS);
             IAsyncResult RemAr = RemoteDel.BeginInvoke(tuple,RemoteAsyncCallBackAdd, null);
             return RemAr;
            //throw new NotImplementedException();
        }
        private IAsyncResult RemoteAsyncCallTake(Node node, ISchema schema)
        {
               AsyncTakeDelegate RemoteDel = new AsyncTakeDelegate(node.TakeS);
               IAsyncResult RemAr = RemoteDel.BeginInvoke(schema, RemoteAsyncCallBackAdd, null);
               return RemAr;
            //throw new NotImplementedException();
        }

        public static void RemoteAsyncCallBackAdd(IAsyncResult ar)
        {
            AsyncAddDelegate del = (AsyncAddDelegate)((AsyncResult)ar).AsyncDelegate;
            try
            {
                del.EndInvoke(ar);
            }catch(Exception ex) { return; }
            
            //throw new NotImplementedException();
        }
        public static void RemoteAsyncCallBackTake(IAsyncResult ar)
        {
             AsyncTakeDelegate del = (AsyncTakeDelegate)((AsyncResult)ar).AsyncDelegate;
             del.EndInvoke(ar);
            throw new NotImplementedException();
        }


        public void AddS(ITuple tuple)
        {
            //throw new NotImplementedException();
        }

        public ITuple ReadS(ISchema schema)
        {
            return null;
            //throw new NotImplementedException();
        }

        public ITuple TakeS(ISchema schema)
        {
            return null;
            //throw new NotImplementedException();
        }

        public RequestLogResponse RequestLog()
        {
            //return new RequestLogResponse(this.leaderLog.);
            return null;
        }


        public void CheckFreeze()
        {
            while (Frozen)
            {
                System.Threading.Thread.Sleep(DF.FreezeStep);
            }

        }

        public void Status() {
            Console.WriteLine("################## LEADER #######################");
            Console.WriteLine(leaderLog.log.ToString());
            Console.WriteLine("####################################################");
        }
    }
}
