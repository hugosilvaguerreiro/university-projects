using System;
using System.Collections;
using System.Collections.Generic;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using DU = DIDA_Resources.DIDAUtils;

namespace DIDA_API_SMR
{
    public class ServerApiSmr
    {
        public string Id { get; set; }
        private TcpChannel Channel { get; set; }
        private List<Node> Nodes;

        public ServerApiSmr(string id, string url, int minDelay, int maxDelay, List<string> serversUrls)
        {
            BinaryServerFormatterSinkProvider provider = new BinaryServerFormatterSinkProvider();
            this.Id = id;
            Hashtable props = new Hashtable();
            int port = DU.ExtractPortFromUrl(url);
            props["port"] = port;
            props["timeout"] = 1000;
            Channel = new TcpChannel(props, null, provider);
            Nodes = new List<Node>();

            ChannelServices.RegisterChannel(Channel, false);

            foreach (string serverUrl in serversUrls)
            {
                if (port != DU.ExtractPortFromUrl(serverUrl))
                {
                    Nodes.Add((Node)Activator.GetObject(typeof(Node), serverUrl));
                }
                    
            }

            Node node = new Node(id, url, Nodes, minDelay, maxDelay);
            RemotingServices.Marshal(node, DU.ExtractObjectNameFromUrl(url));
        }
    }
}
