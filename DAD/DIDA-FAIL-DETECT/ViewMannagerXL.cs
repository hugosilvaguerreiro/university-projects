using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting.Messaging;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Timers;

namespace DIDA_FAIL_DETECT
{
    public class ViewManagerXL
    {
        List<String> MostRecentView;
        Dictionary<string, Pingable> Pingables;
        private System.Timers.Timer PingTimer;

        public int Interval = 100;
        public int Timeout = 1000;

        public delegate void AsyncPingDelegate();

        public ViewManagerXL(Dictionary<string, Pingable> pingables)
        {
            MostRecentView = new List<string>(pingables.Keys);
            Pingables = pingables;
            SetTimer();
        }

        public List<string> RequestView()
        {
            //lock(MostRecentView)
            //{
            return MostRecentView;
            //}
        }

        public void Start()
        {
            PingTimer.Enabled = true;
        }

        public void Pause()
        {
            PingTimer.Stop();
        }

        public void Stop()
        {
            PingTimer.Stop();
            PingTimer.Dispose();
        }

        private void SetTimer()
        {
            PingTimer = new System.Timers.Timer(Interval);
            PingTimer.Elapsed += UpdateView;
            PingTimer.AutoReset = true;
        }

        private void UpdateView(Object source, ElapsedEventArgs e)
        {
            //Pause();

            Dictionary<string, KeyValuePair<IAsyncResult, WaitHandle>> responseDict = new Dictionary<string, KeyValuePair<IAsyncResult, WaitHandle>>();

            IAsyncResult result;
            WaitHandle wait_handle;
            List<WaitHandle> waits = new List<WaitHandle>();

            foreach (KeyValuePair<string, Pingable> entry in Pingables)
            {
                result = CheckAliveServer(entry.Value);
                wait_handle = result.AsyncWaitHandle;
                waits.Add(wait_handle);
                responseDict[entry.Key] = new KeyValuePair<IAsyncResult, WaitHandle>(result, wait_handle);
            }

            WaitHandle.WaitAll(waits.ToArray(), Timeout);

            List<string> new_view = AssertAliveServers(responseDict);
            lock(MostRecentView)
            {
                MostRecentView = new_view;
            }
            //Console.WriteLine("VIEW MANAGER: ALIVE- " + new_view.pingables.Count);
            //Start();
        }
        private List<string> AssertAliveServers(Dictionary<string, KeyValuePair<IAsyncResult, WaitHandle>> responseDict)
        {
            List<string> new_view = new List<string>();
            //Console.WriteLine("Current View: ");
            foreach(var entry in responseDict) {
                if(entry.Value.Value.WaitOne(0))
                {
                    AsyncPingDelegate del = (AsyncPingDelegate)((AsyncResult)entry.Value.Key).AsyncDelegate;
                    try
                    {
                        del.EndInvoke(entry.Value.Key);
                        //Console.WriteLine(entry.Key);
                        new_view.Add(entry.Key);
                    }
                    catch (Exception error)
                    {
                        //Do nothing, the server is really dead or is behaving badly.
                        //Console.WriteLine("VIEW MANAGER: A pingable is behaving weirdly, check it out");
                        //Console.WriteLine(error.ToString());
                    }
                }
                else
                {
                    //Console.WriteLine("Ups");
                    //Server is probably alive but he just timed out or is not currently responding
                    //Assume that it died, he will come back eventually if he really is alive.
                    //When you don't use EndInvoke you simply dont get the return value.
                }
            }
            return new_view;
        }

        private IAsyncResult CheckAliveServer(Pingable p)
        {
            AsyncPingDelegate RemoteDel = new AsyncPingDelegate(p.IsAlive);
            IAsyncResult RemAr = RemoteDel.BeginInvoke(null, null);
            WaitHandle wait = RemAr.AsyncWaitHandle;
            return RemAr;
        }
    }
}
