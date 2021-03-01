using DIDA_FAIL_DETECT;
using DIDA_Lib;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting.Messaging;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Timers;
using DU = DIDA_Resources.DIDAUtils;

namespace DIDA_API
{
    public class TupleSpace : MarshalByRefObject, Pingable, IManageable
    {
        public delegate TupleSpaceData AsyncAskDelegate();
        public delegate bool AsyncStopDelegate();
        public delegate void AsyncContinueDelegate();

        private string selfUrl;
        private List<ITuple> tuples;
        private ReaderWriterLockSlim rw_lock;
        private HashSet<KeyValuePair<string, int>> recievedMsg;
        private HashSet<ITuple> lockedTuples;
        private readonly int minDelay;
        private readonly int maxDelay;
        readonly Random rnd = new Random();

        private Dictionary<string, bool> clientsInTake;

        public bool Stopped = false;
        public bool isNew = true;

        public bool Frozen { get; set; }

        public ViewManagerXL viewManager;
        Dictionary<string, Pingable> otherTupleSpaces;

        public TupleSpace(string selfUrl, int minDelay, int maxDelay, Dictionary<string, Pingable> otherTupleSpaces)
        {
            this.selfUrl = selfUrl;
            Frozen = false;
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;

            tuples = new List<ITuple>();
            rw_lock = new ReaderWriterLockSlim();
            recievedMsg = new HashSet<KeyValuePair<string, int>>();
            lockedTuples = new HashSet<ITuple>();
            clientsInTake = new Dictionary<string, bool>();

            viewManager = new ViewManagerXL(otherTupleSpaces);
            viewManager.Start();
            this.otherTupleSpaces = otherTupleSpaces;
        }

        public void Add(ITuple tuple, string id, int seq)
        {
            CheckFreeze();
            Thread.Sleep(rnd.Next(minDelay, maxDelay));
            WaitForPrevious(id, seq);
            isNew = false;
            KeyValuePair<string, int> msgSeq = new KeyValuePair<string, int>(id, seq);
            Monitor.Enter(tuples);
            if (!recievedMsg.Contains(msgSeq))
            {
                tuples.Add(tuple);
                AddMsgAndNotify(id, seq);
                Console.WriteLine(DateTime.Now.ToString("h:mm:ss.fff")+" - " + id + ": " + seq + " -ADD: " + tuple);
                Monitor.PulseAll(tuples);
            }
            Monitor.Exit(tuples);
    }

        public ITuple Read(ISchema schema, string id, int seq)
        {
            CheckFreeze();
            Thread.Sleep(rnd.Next(minDelay, maxDelay));
            WaitForPrevious(id, seq);
            ITuple t = BlockingFind(schema);
            Console.WriteLine(DateTime.Now.ToString("h:mm:ss.fff") + " - " + id + ": " + seq + " -READ: " + t);
            AddMsgAndNotify(id, seq);
            return t;
        }

        public ITuple TakeRemove(ITuple tuple, HashSet<ITuple> locked, string id, int seq)
        {
            CheckOnlyFreeze();
            Thread.Sleep(rnd.Next(minDelay, maxDelay));
            KeyValuePair<string, int> msgSeq = new KeyValuePair<string, int>(id, seq);
            if (recievedMsg.Contains(msgSeq))
            {
                return tuple;
            }
            isNew = false;
            rw_lock.EnterWriteLock();
            bool ok = tuples.Remove(tuple);
            AddMsgAndNotify(id, seq);
            rw_lock.ExitWriteLock();
            if (ok)
            {
                Console.WriteLine(DateTime.Now.ToString("h:mm:ss.fff") + " - " + id + ": " + seq + " -TAKE REMOVE: " + tuple);
                foreach (ITuple t in locked)
                {
                    lockedTuples.Remove(t);
                }
                Console.WriteLine(DateTime.Now.ToString("h:mm:ss.fff") + " - " + id + ": " + seq + " -UNLOCK: {" + String.Join(",", locked) + "}");
                AddMsgAndNotify(id, seq);
                clientsInTake[id] = false;
                return tuple;
            }
            else
            {
                return TakeRemove(tuple, locked, id, seq);
            }
        }

        public HashSet<ITuple> TakeSelect(ISchema schema, string id, int seq)
        {
            try
            {
                CheckFreeze();
                Thread.Sleep(rnd.Next(minDelay, maxDelay));
                WaitForPrevious(id, seq);
                bool rejected = false;
                HashSet<ITuple> selected = new HashSet<ITuple>(FindAllUnlocked(schema));
                if (selected.Count == 0) rejected = true;
                foreach (ITuple tuple in selected)
                {
                    if (!lockedTuples.Add(tuple))
                    {
                        rejected = true;
                    }
                }
                if (rejected)
                {
                    foreach (ITuple tuple in selected)
                    {
                        lockedTuples.Remove(tuple);
                    }
                    Console.WriteLine(DateTime.Now.ToString("h:mm:ss.fff") + " - " + id + ": " + seq + " -TAKE SELECT: FAIL");
                    return null;
                }
                else
                {
                    clientsInTake[id] = true;
                    isNew = false;
                    Console.WriteLine(DateTime.Now.ToString("h:mm:ss.fff") + " - " + id + ": " + seq + " -TAKE SELECT: {" + String.Join(",", selected) + "}");
                    return selected;
                }
            } catch(Exception e) { Console.WriteLine(e); }
            return null;
        }

        private ITuple BlockingFind(ISchema schema)
        {
            ITuple tuple;
            while ((tuple = FindFirst(schema)) == null)
            {
                Monitor.Enter(tuples);
                Monitor.Wait(tuples);
                Monitor.Exit(tuples);
            }
            return tuple;
        }

        private IEnumerable<ITuple> BlockingFindAll(ISchema schema)
        {
            IEnumerable<ITuple> tuples;
            while ((tuples = FindAllUnlocked(schema)).Count() == 0)
            {
                Monitor.Enter(tuples);
                Monitor.Wait(tuples);
                Monitor.Exit(tuples);
            }
            return tuples;
        }

        private ITuple FindFirst(ISchema schema)
        {
            rw_lock.EnterReadLock();
            ITuple tuple = new HashSet<ITuple>(tuples).Except(lockedTuples).FirstOrDefault(item => item.MatchesSchema(schema));
            rw_lock.ExitReadLock();
            return tuple;
        }

        private IEnumerable<ITuple> FindAllUnlocked(ISchema schema)
        {
            rw_lock.EnterReadLock();
            IEnumerable<ITuple> tuplesList = new HashSet<ITuple>(tuples).Except(lockedTuples)
                                                                        .Where(item => item.MatchesSchema(schema));
            rw_lock.ExitReadLock();
            return tuplesList;
        }

        public void Unlock(HashSet<ITuple> locked, string id, int seq)
        {
            CheckFreeze();
            foreach (ITuple tuple in locked)
            {
                lockedTuples.Remove(tuple);
            }
            Monitor.PulseAll(tuples);
            Console.WriteLine(id + ": " + seq + " -UNLOCK: {" + String.Join(",", locked) + "}");
        }

        private void WaitForPrevious(string id, int seq)
        {
            if (seq == 1) recievedMsg.Add(new KeyValuePair<string, int>(id, 0)); 
            KeyValuePair<string, int> msgSeq = new KeyValuePair<string, int>(id, seq-1);
            while (!recievedMsg.Contains(msgSeq))
            {
                Monitor.Enter(recievedMsg);
                Monitor.Wait(recievedMsg);
                Monitor.Exit(recievedMsg);
            }
        }

        private void AddMsgAndNotify(string id, int seq)
        {
            recievedMsg.Add(new KeyValuePair<string, int>(id, seq));
            Monitor.Enter(recievedMsg);
            Monitor.PulseAll(recievedMsg);
            Monitor.Exit(recievedMsg);
        }

        public void IsAlive()
        {
            CheckOnlyFreeze();
            //Console.WriteLine("i am alive");
        }

        public TupleSpaceData RequestData()
        {
            CheckOnlyFreeze();
            return new TupleSpaceData(this.tuples, this.recievedMsg, this.lockedTuples);            
        }

        public bool Stop()
        {
            if(!isNew)
            {
                while (clientsInTake.ContainsValue(true))
                {
                    Console.WriteLine("Waiting to stop");
                    Thread.Sleep(50);
                }
                Console.WriteLine("Stop");
                Stopped = true;
                return true;
            }
            return false;
            
        }

        public void Continue()
        {
            Console.WriteLine("Continue");
            Stopped = false;
        }

        public List<string> RequestView()
        {
            CheckOnlyFreeze();
            List<string> view = viewManager.RequestView();
            foreach(string url in view)
            {
                if(DU.ExtractPortFromUrl(url) < DU.ExtractPortFromUrl(selfUrl))
                {
                    return null;
                }
            }
            view.Add(selfUrl);
            return view;
        }

        public void Update()
        {
            string copyUrl = null;
            while(copyUrl == null)
            {
                copyUrl = viewManager.RequestView().First();
            }
            TupleSpace copySpace = (TupleSpace)otherTupleSpaces[copyUrl];

            bool Stopped = copySpace.Stop();
            if (!Stopped) return;
            UpdateLatest(copySpace.RequestData());
            copySpace.Continue();
        }

        private IAsyncResult RemoteAsyncCallAsk(TupleSpace space)
        {
            AsyncAskDelegate RemoteDel = new AsyncAskDelegate(space.RequestData);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(null, null);
            return RemAr;
        }

        private IAsyncResult RemoteAsyncCallStop(TupleSpace space)
        {
            AsyncStopDelegate RemoteDel = new AsyncStopDelegate(space.Stop);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(null, null);
            return RemAr;
        }

        private IAsyncResult RemoteAsyncCallContinue(TupleSpace space)
        {
            AsyncContinueDelegate RemoteDel = new AsyncContinueDelegate(space.Continue);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(null, null);
            return RemAr;
        }

        private void UpdateLatest(TupleSpaceData data)
        {

            Console.WriteLine("Updating...");
            this.tuples = data.tuples;
            Console.WriteLine(String.Join(",", this.tuples));
            this.recievedMsg = data.recievedMsg;
            this.lockedTuples = data.lockedTuples;
            Console.WriteLine(String.Join(",", this.lockedTuples));
            Monitor.Enter(recievedMsg);
            Monitor.PulseAll(recievedMsg);
            Monitor.Exit(recievedMsg);

            
        }

        //PuppetMaster Functions

        public void Freeze()
        {
            Frozen = true;
        }

        public void Unfreeze()
        {
            List<TupleSpace> others = new List<TupleSpace>();
            foreach(var tupleSpace in otherTupleSpaces.Values)
            {
                others.Add((TupleSpace) tupleSpace);
            }

            //Update(others);
            Frozen = false;
        }

        public void Status()
        {
            if (Frozen)
            {
                Console.WriteLine("Server is Frozen");
            }
            else
            {
                Console.WriteLine("Server is Unfrozen");
            }
        }

        public void CheckFreeze()
        {
            while (Frozen || Stopped)
            {
                System.Threading.Thread.Sleep(50);
            }
        }

        public void CheckOnlyFreeze()
        {
            while (Frozen)
            {
                System.Threading.Thread.Sleep(50);
            }
        }
    }
}
