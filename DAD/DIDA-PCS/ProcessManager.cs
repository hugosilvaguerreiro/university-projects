using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using DIDA_API;
using DS = DIDA_Resources.DIDAStrings;
using DU = DIDA_Resources.DIDAUtils;
using DF = DIDA_Resources.DIDAFlags;
using DIDA_API_SMR;
using DIDA_Lib;

namespace DIDA_PCS
{
    public class ProcessManager : MarshalByRefObject
    {
        private static readonly string serverLocation = DS.ServerExecutableLocation;
        private static readonly string clientLocation = DS.ClientExecutableLocation;
        private static readonly string serverList = DU.JoinPaths(DS.CommonResources, DS.ServersListDefaultName);

        private TcpChannel Channel { get; set; }

        private static Dictionary<string, Process> Processes;
        private Dictionary<string, IManageable> Servers;


        public ProcessManager()
        {
            Servers = new Dictionary<string, IManageable>();
            
            Processes = new Dictionary<string, Process>();
        }

        public void NewServer(string id, string url, int minDelay, int maxDelay)
        {
            string domain = DU.ExtractDomainFromUrl(url, true);
            Process p = Process.Start(serverLocation, String.Format("{0} {1} {2} {3}", id, url, minDelay, maxDelay));
            Processes.Add(id, p);



            Servers.Add(id, (IManageable)Activator.GetObject(typeof(IManageable), url));

            Console.WriteLine("Server Added");
        }



        public void NewClient(string id, string url, string file)
        {
            Process p = Process.Start(clientLocation, String.Format("{0} {1} {2} {3}", id, url, file, serverList));
            Processes.Add(id, p);
            
            Console.WriteLine("Client Added");
        }

        public void Status()
        {
            try
            {
                foreach (KeyValuePair<string, IManageable> server in Servers)
                {
                    server.Value.Status();
                }
            }catch(Exception ex) { }

        }

        public void Freeze(string id)
        {
            IManageable server = Servers[id];
            server.Freeze();
        }

        public void Unfreeze(string id)
        {
            IManageable server = Servers[id];
            server.Unfreeze();
        }
    

        public void Crash(string id)
        {
            Console.WriteLine("Killing ", id);
            Processes[id].Kill();
        }

        public void Kill()
        {
            foreach(KeyValuePair<string, Process> process in Processes)
            {
                process.Value.Kill();
            }
        }
    }
}
