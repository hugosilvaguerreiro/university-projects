using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_API
{
    public class ServerApi
    { 
        public ServerApi(int port)
        {
            TcpChannel Channel = new TcpChannel(port);
            ChannelServices.RegisterChannel(Channel, false);
            RemotingConfiguration.RegisterWellKnownServiceType(
                typeof(TupleSpace),
                "TupleSpace",
                WellKnownObjectMode.Singleton);
            Console.WriteLine("Enter to exit");
            Console.WriteLine(port);
            Console.ReadLine();
        }

    }
}
