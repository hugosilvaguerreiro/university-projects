using DIDA_Lib;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Text;
using System.Threading.Tasks;
using DIDA_API;
using System;

namespace DIDA_Client
{
    class Client
    {
        string Id { get; set; }
        ClientApi Api { get; set; }
        public Client(string id, string url, string scriptFile )
        {
            Id = id;
            Api = new ClientApi(url, scriptFile);

        }
        static void Main(string[] args)
        {
            //args[0] - id
            //args[1] - url
            //args[2] - script file
            Client client = new Client(args[0], args[1], args[2]);
            client.Api.RunScript();
            System.Console.ReadLine();
        }
    }
}
