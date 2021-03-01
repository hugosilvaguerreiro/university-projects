using DIDA_FAIL_DETECT;
using DIDA_Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Runtime.Remoting.Messaging;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using DU = DIDA_Resources.DIDAUtils;
namespace DIDA_API_SMR
{
    public class ClientSmrApi
    {

        public delegate void AsyncAddDelegate(ITuple tuple, string id, int seq);
        public delegate ITuple AsyncReadDelegate(ISchema schema, string id, int seq);
        public delegate HashSet<ITuple> AsyncTakeSelectDelegate(ISchema schema, string id, int seq);
        public delegate ITuple AsyncTakeRemoveDelegate(ITuple tuple, HashSet<ITuple> locked, string id, int seq);
        public delegate void AsyncUnlockDelegate(HashSet<ITuple> locked, string id, int seq);

        public string Id { get; set; }
        public int Seq { get; set; }
        private TcpChannel Channel { get; set; }
        private Dictionary<string, Node> Nodes;
        private string leaderUrl = null;
        private List<string> urls;
        public ViewManager manager;
        
        //TODO: change this url. it should be the client's
        public ClientSmrApi(string id, string url, string scriptFile, List<string> serversUrls)
        {
            
            this.Id = id;
            this.Seq = 0;
            int port = DU.ExtractPortFromUrl(url);
            Channel = new TcpChannel(port);
            Nodes = new Dictionary<string, Node>();
            urls = new List<string>();
            
            ChannelServices.RegisterChannel(Channel, false);
            Console.WriteLine(scriptFile);
            Dictionary<int, Pingable> ping = new Dictionary<int, Pingable>();
            int i = 0;
            foreach(string serverUrl in serversUrls)
            {
                Nodes[serverUrl] = ((Node)Activator.GetObject(
                                            typeof(Node),
                                            serverUrl));
                urls.Add(serverUrl);
                ping.Add(i,Nodes[serverUrl]);
                i++;
            }
            manager = new ViewManager(ping);
            manager.Start();


        }

        public void Add(ITuple tuple)
        {
            
        /*    //if(leaderUrl == null)
                {*/
                bool sent = false;
                while (sent == false)
                {
                    try
                    {
                        //System.Console.WriteLine("leader null-BEGIN");
                        if(leaderUrl == null)
                        {
                            Random rnd = new Random();
                            int r = rnd.Next(urls.Count);
                            ((Node)(manager.RequestView().pingables[r])).Add(tuple);
                            sent = true;
                        }
                        else
                        {
                            Nodes[leaderUrl].Add(tuple);
                            sent = true;
                        }
                        
                        //System.Console.WriteLine("leader null-END");
                        System.Console.WriteLine(tuple.ToString());
                    }
                    catch (SMRNotLeaderException e)
                    {
                        leaderUrl = e.leaderUrl;
                    }
                    catch (SMRCandidateException)
                    {
                        //System.Console.WriteLine("Candidate");
                        leaderUrl = null;
                        System.Threading.Thread.Sleep(300);
                    }
                    catch(Exception c)
                    {
                        //System.Console.WriteLine(c);
                    }
                }
            }
            /*else {
                //TODO: HANDLE SERVER EXCEPTION
                try {
                    Nodes[leaderUrl].Add(tuple);
                }catch(Exception e)
                {
                    //System.Console.WriteLine("THERE WAS AN EXCEPTION");
                }
                
            }*/



        public ITuple Read(ISchema schema)
        {
            ITuple result;
           /* // if (leaderUrl == null)
            {*/
                bool sent = false;
                while (sent == false)
                {
                    try
                    {
                        //System.Console.WriteLine("leader read null-BEGIN");
                        Random rnd = new Random();
                        int r = rnd.Next(Nodes.Count);
                        
                        result= ((Node)manager.RequestView().pingables[r]).Read(schema);
                        leaderUrl = urls[r];
                        //System.Console.WriteLine("leader null-END");
                        System.Console.WriteLine(result.ToString());
                        sent = true;
                    }
                    catch (SMRNotLeaderException e)
                    {
                        //System.Console.WriteLine("Not leader");
                        //System.Console.WriteLine("Leader URL: " + e.leaderUrl);
                        leaderUrl = e.leaderUrl;
                    }
                    catch (SMRCandidateException)
                    {
                        //System.Console.WriteLine("Candidate");
                        leaderUrl = null;
                        System.Threading.Thread.Sleep(300);
                    }
                    catch(Exception x) { }
                }
            return null;
        }
            /*else
            {
                //TODO: HANDLE SERVER EXCEPTION
                try
                {
                    return Nodes[leaderUrl].Read(schema);
                }
                catch (Exception e)
                {
                    //System.Console.WriteLine("THERE WAS AN EXCEPTION");
                }

            }*/
           // return null;
        //}
        public ITuple Take(ISchema schema)
        {
            ITuple result = null;
            //if (leaderUrl == null)
               /* if (true)
                {*/
                bool sent = false;
                while (sent == false)
                {
                    try
                    {
                        Random rnd = new Random();
                        int r = rnd.Next(urls.Count);
                        result = ((Node)manager.RequestView().pingables[r]).Take(schema);
                        leaderUrl = urls[r];
                        sent = true;
                    }
                    catch (SMRNotLeaderException e)
                    {
                        //System.Console.WriteLine("Not leader");
                        //System.Console.WriteLine("Leader URL: " + e.leaderUrl);
                        leaderUrl = e.leaderUrl;
                    }
                    catch (SMRCandidateException)
                    {
                        //System.Console.WriteLine("Candidate");
                        leaderUrl = null;
                        System.Threading.Thread.Sleep(1000);
                    }catch(Exception ex) { }
                }
           /* }
            else
            {
                //TODO: HANDLE SERVER EXCEPTION
                try
                {
                    result = Nodes[leaderUrl].Take(schema);
                }
                catch (Exception e)
                {
                    //System.Console.WriteLine(e);
                }

            }*/
            return result;
        }

    }
}