using DIDA_FAIL_DETECT;
using DIDA_Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Runtime.Remoting.Messaging;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using DU = DIDA_Resources.DIDAUtils;
namespace DIDA_API
{
    public class ClientApi
    {

        public delegate void AsyncAddDelegate(ITuple tuple, string id, int seq);
        public delegate ITuple AsyncReadDelegate(ISchema schema, string id, int seq);
        public delegate HashSet<ITuple> AsyncTakeSelectDelegate(ISchema schema, string id, int seq);
        public delegate ITuple AsyncTakeRemoveDelegate(ITuple tuple, HashSet<ITuple> locked, string id, int seq);
        public delegate void AsyncUnlockDelegate(HashSet<ITuple> locked, string id, int seq);
        public delegate List<string> AsyncViewDelegate();

        public string Id { get; set; }
        public int Seq { get; set; }
        private TcpChannel Channel { get; set; }

        Dictionary<string, Pingable> TupleSpaces = new Dictionary<string, Pingable>();
        string currentLeader = null;

        //TODO: change this url. it should be the client's
        public ClientApi(string id, string url, string scriptFile, List<string> serversUrls)
        {
            
            this.Id = id;
            this.Seq = 0;
            //TODO: REMOVE Channel = new TcpChannel(Int32.Parse(url.Split(':')[1]));
            Channel = new TcpChannel(DU.ExtractPortFromUrl(url));

            ChannelServices.RegisterChannel(Channel, false);
            Console.WriteLine(scriptFile);

            for(int i = 0; i < serversUrls.Count; i++)
            {
                TupleSpaces.Add(serversUrls[i], (Pingable) Activator.GetObject(typeof(TupleSpace), serversUrls[i]));
            }

        }

        public void Add(ITuple tuple, string id)
        {
            bool allAck = false;
            Seq++;
            while(!allAck)
            {
                try
                {
                    List<IAsyncResult> results = new List<IAsyncResult>();
                    List<WaitHandle> waits = new List<WaitHandle>();
                    foreach (var entry in GetViews())
                    //foreach (TupleSpace space in viewManager.RequestView().pingables)
                    {
                        IAsyncResult ar = RemoteAsyncCallAdd((TupleSpace)entry.Value, tuple, id, Seq);
                        //IAsyncResult ar = RemoteAsyncCallAdd(space, tuple, id, seq);
                        results.Add(ar);
                        waits.Add(ar.AsyncWaitHandle);
                    }

                    allAck = WaitHandle.WaitAll(waits.ToArray(), 2000);
                } catch(Exception e) {
                    continue;
                }
                
            }
        }

        private IAsyncResult RemoteAsyncCallAdd(TupleSpace space,  ITuple tuple, string id, int seq)
        {
            AsyncAddDelegate RemoteDel = new AsyncAddDelegate(space.Add);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(tuple, id, seq, RemoteAsyncCallBackAdd, null);
            return RemAr;
        }
        public static void RemoteAsyncCallBackAdd(IAsyncResult ar)
        {
            AsyncAddDelegate del = (AsyncAddDelegate)((AsyncResult)ar).AsyncDelegate;
            try { del.EndInvoke(ar); }
            catch(Exception e) { Console.WriteLine("(ADD) COULD NOT REACH"); }
        }

        public ITuple Read(ISchema schema, string id)
        {
            Seq++;
            while (true)
            {
                try {
                    List<IAsyncResult> results = new List<IAsyncResult>();
                    List<WaitHandle> waits = new List<WaitHandle>();
                    //foreach (TupleSpace space in viewManager.RequestView().pingables)
                    foreach (KeyValuePair<string, TupleSpace> entry in GetViews())
                    {
                        IAsyncResult ar = RemoteAsyncCallRead(entry.Value, schema, id, Seq);
                        results.Add(ar);
                        waits.Add(ar.AsyncWaitHandle);
                    }
                    int index = WaitHandle.WaitAny(waits.ToArray(), 2000);
                    if (index != WaitHandle.WaitTimeout)
                    {
                        AsyncReadDelegate del = (AsyncReadDelegate)((AsyncResult)results[index]).AsyncDelegate;
                        return del.EndInvoke(results[index]); //todo make function that endIvokes rest in another thread
                    }
                } catch(Exception e) {
                    continue;
                }
                
            }
        }

        private IAsyncResult RemoteAsyncCallRead(TupleSpace space, ISchema schema, string id, int seq)
        {
            AsyncReadDelegate RemoteDel = new AsyncReadDelegate(space.Read);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(schema, id, seq, null, null);
            return RemAr;
        }

        public ITuple Take(ISchema schema, string id)
        {
            bool allAck = false;
            ITuple result;
            HashSet<ITuple> intersection = null;
            Dictionary<string, HashSet<ITuple>> tupleSets = null;
            Dictionary<string, TupleSpace> view;
            view = GetViews();
            Seq++;
            Console.WriteLine("TakeSelect");
            while (true)
            {
                
                try
                {
                    //List<Pingable> view = viewManager.RequestView().pingables;
                    
                    Dictionary<string, KeyValuePair<IAsyncResult, WaitHandle>> results = new Dictionary<string, KeyValuePair<IAsyncResult, WaitHandle>> ();
                    List<WaitHandle> waits = new List<WaitHandle>();
                    foreach (KeyValuePair<string, TupleSpace> entry in view)
                    {
                        //IAsyncResult ar = RemoteAsyncCallTakeSelect(space, schema, id, seq);
                        IAsyncResult ar = RemoteAsyncCallTakeSelect(entry.Value, schema, id, Seq);
                        results.Add(entry.Key, new KeyValuePair<IAsyncResult, WaitHandle>(ar, ar.AsyncWaitHandle));
                        waits.Add(ar.AsyncWaitHandle);
                    }

                    //WaitHandle.WaitAll(waits.ToArray(), 2000);
                    WaitHandle.WaitAll(waits.ToArray());
                    tupleSets = new Dictionary<string, HashSet<ITuple>>();
                    foreach (var entry in results)
                    {
                        if(entry.Value.Value.WaitOne(0)) {
                            AsyncTakeSelectDelegate del = (AsyncTakeSelectDelegate)((AsyncResult) entry.Value.Key).AsyncDelegate;
                            tupleSets.Add(entry.Key, del.EndInvoke(entry.Value.Key));
                        }
                        
                    }
                    if (!tupleSets.Values.Any(item => item == null))
                    {
                        intersection = IntersectAll(tupleSets.Values.ToList());
                        if (intersection.Count() > 0)
                        {
                            break;
                        }
                    }
                    
                    foreach (var entry in tupleSets)
                    {
                        if (entry.Value != null)
                            RemoteAsyncCallUnlock((TupleSpace)TupleSpaces[entry.Key], entry.Value, id, Seq++);
                    }
                } catch(Exception e) {
                    continue;
                }
                Thread.Sleep(100);
            }
            result = intersection.First();
            Seq++;
            while (!allAck)
            {
                try
                {
                    //Dictionary<string, TupleSpace> view = GetViews();
                    List<IAsyncResult> results = new List<IAsyncResult>();
                    List<WaitHandle> waits = new List<WaitHandle>();
                    foreach(var entry in view)
                    {
                        IAsyncResult ar = RemoteAsyncCallTakeRemove(entry.Value, result, tupleSets[entry.Key], id, Seq);
                        results.Add(ar);
                        waits.Add(ar.AsyncWaitHandle);
                    }
                    allAck = WaitHandle.WaitAll(waits.ToArray(), 2000);
                } catch(Exception e)
                {
                    Console.WriteLine(e);
                    continue;
                }
                Thread.Sleep(100);
                view = GetViews();
            }
            return result;
        }

        private IAsyncResult RemoteAsyncCallTakeSelect(TupleSpace space, ISchema schema, string id, int seq)
        {
            AsyncTakeSelectDelegate RemoteDel = new AsyncTakeSelectDelegate(space.TakeSelect);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(schema, id, seq, null, null);
            return RemAr;
        }

        private IAsyncResult RemoteAsyncCallTakeRemove(TupleSpace space, ITuple tuple, HashSet<ITuple> locked, string id, int seq)
        {
            AsyncTakeRemoveDelegate RemoteDel = new AsyncTakeRemoveDelegate(space.TakeRemove);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(tuple, locked, id, seq, RemoteAsyncCallBackTakeRemove, null);
            return RemAr;
        }

        public static void RemoteAsyncCallBackTakeRemove(IAsyncResult ar)
        {
            AsyncTakeRemoveDelegate del = (AsyncTakeRemoveDelegate)((AsyncResult)ar).AsyncDelegate;
            try { del.EndInvoke(ar); }
            catch (Exception e) { Console.WriteLine("(TAKEREM) COULD NOT REACH"); }
        }

        private IAsyncResult RemoteAsyncCallUnlock(TupleSpace space, HashSet<ITuple> locked, string id, int seq)
        {
            AsyncUnlockDelegate RemoteDel = new AsyncUnlockDelegate(space.Unlock);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(locked, id, seq, RemoteAsyncCallBackUnlock, null);
            return RemAr;
        }

        public static void RemoteAsyncCallBackUnlock(IAsyncResult ar)
        {
            AsyncUnlockDelegate del = (AsyncUnlockDelegate)((AsyncResult)ar).AsyncDelegate;
            try { del.EndInvoke(ar); }
            catch (Exception e) { Console.WriteLine("(ADD) COULD NOT REACH"); }
        }

        private HashSet<ITuple> IntersectAll(List<HashSet<ITuple>> setList)
        {
            HashSet<ITuple> first = setList.First();
            foreach(HashSet<ITuple> set in setList.Skip(1))
            {
                first.IntersectWith(set);
            }
            return first;
        }

        private List<string> GetLeaderView()
        {
            List<string> result = null;
            if (currentLeader == null) return null;
            IAsyncResult ar = RemoteAsyncCallView((TupleSpace)TupleSpaces[currentLeader]);
            WaitHandle wait = ar.AsyncWaitHandle;
            if(wait.WaitOne(500))
            {
                AsyncViewDelegate del = (AsyncViewDelegate)((AsyncResult)ar).AsyncDelegate;
                try { result = del.EndInvoke(ar); } catch (Exception e) {/* Console.WriteLine(e);*/ }
            }
            return result;
        }

        private Dictionary<string, TupleSpace> GetViews()
        {
            List<string> leaderView = null;
            leaderView = GetLeaderView();
            Dictionary<string, TupleSpace> result = new Dictionary<string, TupleSpace>();
            if (leaderView != null)
            {
                foreach (string server in leaderView)
                {
                    result[server] = (TupleSpace)TupleSpaces[server];
                }
                //Console.WriteLine(String.Join(",", leaderView));
                return result;
            }
            while (leaderView == null)
            {
                Dictionary<string, KeyValuePair<IAsyncResult, WaitHandle>> results = new Dictionary<string, KeyValuePair<IAsyncResult, WaitHandle>>();
                List<WaitHandle> waits = new List<WaitHandle>();
                foreach (KeyValuePair<string, Pingable> entry in TupleSpaces)
                {
                    //IAsyncResult ar = RemoteAsyncCallTakeSelect(space, schema, id, seq);
                    IAsyncResult ar = RemoteAsyncCallView((TupleSpace) entry.Value);
                    results.Add(entry.Key, new KeyValuePair<IAsyncResult, WaitHandle>(ar, ar.AsyncWaitHandle));
                    waits.Add(ar.AsyncWaitHandle);
                }

                WaitHandle.WaitAll(waits.ToArray(), 500);
                List<List<string>> views = new List<List<string>>();
                foreach (var entry in results)
                {
                    if (entry.Value.Value.WaitOne(0))
                    {
                        AsyncViewDelegate del = (AsyncViewDelegate)((AsyncResult)entry.Value.Key).AsyncDelegate;
                        try { leaderView = del.EndInvoke(entry.Value.Key); } catch (Exception e){}
                        if (leaderView != null)
                        {
                            currentLeader = entry.Key;
                            break;
                        }
                        
                    }

                }
                
            }
            foreach(string server in new HashSet<string>(leaderView))
            {
                result[server] = (TupleSpace) TupleSpaces[server];
            }
            //Console.WriteLine(String.Join(",", leaderView));
            return result;
        }

        private IAsyncResult RemoteAsyncCallView(TupleSpace space)
        {
            AsyncViewDelegate RemoteDel = new AsyncViewDelegate(space.RequestView);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(null, null);
            return RemAr;
        }

    }
}