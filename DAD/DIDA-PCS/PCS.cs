using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_PCS
{
    class PCS
    {
        static void Main(string[] args)
        {
            TcpChannel channel = new TcpChannel(10000);
            ChannelServices.RegisterChannel(channel, false);
            ProcessManager mannager = new ProcessManager();

            RemotingServices.Marshal(mannager, "ProcessManager");

            Console.WriteLine("Enter to exit and close all created");
            Console.ReadLine();
            mannager.Kill();
        }
    }
}
