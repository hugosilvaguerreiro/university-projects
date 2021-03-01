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
    class Leader : IState
    {
        public Node node { get; set; }
        public CancellationTokenSource tsource = new CancellationTokenSource();
        public delegate void AsyncPingDelegate(int term, string url);
        public delegate void AsyncAddDelegate(ITuple tuple);
        public delegate ITuple AsyncTakeDelegate(ISchema schema);

        public Leader(Node node)
        {
            this.node = node;
        }

        public void Add(ITuple tuple)
        {
            this.node.TupleSpace.Add(tuple);
            bool allAck = false;
            while (!allAck)
            {
                List<IAsyncResult> results = new List<IAsyncResult>();
                List<WaitHandle> waits = new List<WaitHandle>();
                foreach (Node node in this.node.otherNodes)
                {
                    IAsyncResult ar = RemoteAsyncCallAdd(node, tuple);
                    results.Add(ar);
                    waits.Add(ar.AsyncWaitHandle);

                }

                allAck = WaitHandle.WaitAll(waits.ToArray(), 2000);
                System.Console.WriteLine(allAck);
            }

        }

        public void Ping(int term, string url)
        {
            throw new NotImplementedException();
        }

        public ITuple Read(ISchema schema)
        {
            // System.Console.WriteLine("Read: " + schema.ToString());
            return this.node.TupleSpace.Read(schema);
        }

        public void Start()
        {
            Console.WriteLine("LEADER");
            _ = Pinger(tsource.Token);
        }

        public async Task Pinger(CancellationToken cancellationToken)
        {
            while (true)
            {
                //Console.WriteLine("ping!");
                foreach (Node otherNode in this.node.otherNodes)
                {
                    IAsyncResult ar = RemoteAsyncCallPing(otherNode, this.node.term, this.node.url);

                }
                await Task.Delay(200, cancellationToken);
            }
        }


        private IAsyncResult RemoteAsyncCallPing(Node otherNode, int term, string url)
        {
            AsyncPingDelegate RemoteDel = new AsyncPingDelegate(otherNode.Ping);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(term, url, RemoteAsyncCallBackPing, null);
            return RemAr;
        }

        public static void RemoteAsyncCallBackPing(IAsyncResult ar)
        {
            AsyncPingDelegate del = (AsyncPingDelegate)((AsyncResult)ar).AsyncDelegate;
            del.EndInvoke(ar);
        }

        public void Stop()
        {
            Console.WriteLine("END LEADER");
        }

        public ITuple Take(ISchema schema)
        {
            ITuple result = this.node.TupleSpace.Take(schema);

            bool allAck = false;
            while (!allAck)
            {
                List<IAsyncResult> results = new List<IAsyncResult>();
                List<WaitHandle> waits = new List<WaitHandle>();
                foreach (Node node in this.node.otherNodes)
                {
                    IAsyncResult ar = RemoteAsyncCallTake(node, schema);
                    results.Add(ar);
                    waits.Add(ar.AsyncWaitHandle);

                }

                allAck = WaitHandle.WaitAll(waits.ToArray(), 2000);
                System.Console.WriteLine(allAck);
            }
            return result;
        }

        public bool Vote(int term, string url)
        {
            return false;
        }

        private IAsyncResult RemoteAsyncCallAdd(Node node, ITuple tuple)
        {
            AsyncAddDelegate RemoteDel = new AsyncAddDelegate(node.AddS);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(tuple,RemoteAsyncCallBackAdd, null);
            return RemAr;
        }
        private IAsyncResult RemoteAsyncCallTake(Node node, ISchema schema)
        {
            AsyncTakeDelegate RemoteDel = new AsyncTakeDelegate(node.TakeS);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(schema, RemoteAsyncCallBackAdd, null);
            return RemAr;
        }

        public static void RemoteAsyncCallBackAdd(IAsyncResult ar)
        {
            AsyncAddDelegate del = (AsyncAddDelegate)((AsyncResult)ar).AsyncDelegate;
            del.EndInvoke(ar);
        }
        public static void RemoteAsyncCallBackTake(IAsyncResult ar)
        {
            AsyncTakeDelegate del = (AsyncTakeDelegate)((AsyncResult)ar).AsyncDelegate;
            del.EndInvoke(ar);
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

        public ResponseMessage AppendEntry(AppendEntriesMessage message)
        {
            throw new NotImplementedException();
        }
    }
}
