using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Timers;

namespace DIDA_FAIL_DETECT
{
    public static class TimerHelper
    {
        public static System.Timers.Timer SetTimer(int timeout, ElapsedEventHandler callback)
        {
            System.Timers.Timer timer = new System.Timers.Timer(timeout);
            timer.Elapsed += callback;
            timer.AutoReset = true;
            return timer;
        }

        public static  void StartTimer(System.Timers.Timer timer)
        {
            timer.Enabled = true;
        }

        public static void PauseTimer(System.Timers.Timer timer)
        {
            timer.Stop();
        }

        public static void StopTimer(System.Timers.Timer timer)
        {
            timer.Stop();
            timer.Dispose();
        }

        public static void RestartTimer(int timeout, System.Timers.Timer timer)
        {
            timer.Stop();
            timer.Interval = timeout;
            timer.Start();

        }
    }
}
