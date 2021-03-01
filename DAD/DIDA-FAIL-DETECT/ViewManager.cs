using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting.Messaging;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Timers;
using DF = DIDA_Resources.DIDAFlags;
namespace DIDA_FAIL_DETECT
{
    public class ViewManager
    {
        View MostRecentView;
        Dictionary<int, Pingable> Pingables;
        private System.Timers.Timer PingTimer;

        public int Interval = 100;
        public int Timeout = DF.TIMEOUT;

        public delegate void AsyncPingDelegate();
        
        public ViewManager(Dictionary<int, Pingable> pingables)
        {
            MostRecentView = new View(pingables);
            Pingables = pingables;
            SetTimer();
        }

        public View RequestView()
        {
            //lock(MostRecentView)
            //{
                View returnView = new View(MostRecentView);
                return returnView;
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

            IAsyncResult[] results = new IAsyncResult[Pingables.Count];
            WaitHandle[] wait_handles = new WaitHandle[Pingables.Count];
            Pingable[] pingables = new Pingable[Pingables.Count];
            int i = 0;
            foreach (KeyValuePair<int, Pingable> entry in Pingables)
            {
                results[i] = CheckAliveServer(entry.Value);
                wait_handles[i] = results[i].AsyncWaitHandle;
                pingables[i] = entry.Value;
                i++;
            }

            WaitHandle.WaitAll(wait_handles, Timeout);

            Dictionary<int,Pingable> new_view = AssertAliveServers(results, wait_handles, pingables);
            //lock(MostRecentView)
            //{
                MostRecentView.SwapPingables(new_view);
            //}
            //Console.WriteLine("VIEW MANAGER: ALIVE- " + new_view.pingables.Count);
            //Start();
        }
        private Dictionary<int,Pingable> AssertAliveServers(IAsyncResult[] results, WaitHandle[] wait_handles, Pingable[] pingables)
        {
            Dictionary<int, Pingable> new_view = new Dictionary<int, Pingable>();
            for (int i = 0; i < Pingables.Count; i++)
            {
                if (wait_handles[i].WaitOne(0))
                {
                    AsyncPingDelegate del = (AsyncPingDelegate)((AsyncResult)results[i]).AsyncDelegate;
                    try
                    {
                        del.EndInvoke(results[i]);
                        new_view.Add(i, pingables[i]);
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
