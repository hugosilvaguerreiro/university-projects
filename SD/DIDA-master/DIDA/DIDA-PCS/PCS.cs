using System;
using System.Collections.Generic;
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
            RemotingConfiguration.RegisterWellKnownServiceType(
                typeof(ProcessManager),
                "ProcessManager",
                WellKnownObjectMode.Singleton);
            Console.WriteLine("Enter to exit");
            Console.ReadLine();
        }
    }
}
