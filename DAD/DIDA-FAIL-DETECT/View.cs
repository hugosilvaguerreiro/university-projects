using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting.Messaging;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace DIDA_FAIL_DETECT
{
    public class View
    {
        public Dictionary<int, Pingable> pingables;

        public View(Dictionary<int, Pingable> pingables)
        {
            this.pingables = new Dictionary<int, Pingable>(pingables);
        }
        public View(View v)
        {
            this.pingables = new Dictionary<int, Pingable>(v.pingables);
        }
        public void SwapPingables(Dictionary<int,Pingable> ping)
        {
            this.pingables = ping;
        }

    }
}
