using DIDA_FAIL_DETECT;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Text;
using System.Threading.Tasks;
using DU = DIDA_Resources.DIDAUtils;
namespace DIDA_API
{
    public class ServerApi
    {
        public ServerApi(string id, string url, int minDelay, int maxDelay, List<String> urls)
        { 
            int port = DU.ExtractPortFromUrl(url);
            TcpChannel Channel = new TcpChannel(DU.ExtractPortFromUrl(url));

            ChannelServices.RegisterChannel(Channel, false);

            Dictionary<string, Pingable> TupleSpaces = new Dictionary<string, Pingable>();
            string selfURL = "";

            foreach (string serverUrl in urls)
            {
                if (port != DU.ExtractPortFromUrl(serverUrl))
                {
                    Console.WriteLine(serverUrl);
                    TupleSpaces.Add(serverUrl, (TupleSpace) Activator.GetObject(typeof(TupleSpace), serverUrl));
                }
                else
                {
                    selfURL = serverUrl;
                }
            }

            TupleSpace tupleSpace = new TupleSpace(selfURL, minDelay, maxDelay, TupleSpaces);
            RemotingServices.Marshal(tupleSpace, DU.ExtractObjectNameFromUrl(url));
            tupleSpace.Stopped = true;
            List<TupleSpace> tupleSpaces = new List<TupleSpace>();
            foreach(var entry in TupleSpaces)
            {
                tupleSpaces.Add((TupleSpace) entry.Value);
            }
            tupleSpace.Update();
            tupleSpace.Stopped = false;
            Console.WriteLine("Enter to exit");
            Console.ReadLine();
        }
    }
}
