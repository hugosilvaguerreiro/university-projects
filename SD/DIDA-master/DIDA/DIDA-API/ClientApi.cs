using DIDA_Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_API
{
    public class ClientApi
    {
        private TcpChannel Channel { get; set; }
        private ITupleSpace TupleSpace { get; set; }
        private string ScriptFileName;
        
        //TODO: change this url. it should be the client's
        public ClientApi(string url, string scriptFile)
        {
            Channel = new TcpChannel();
            ChannelServices.RegisterChannel(Channel, false);
            TupleSpace = (ITupleSpace)Activator.GetObject(
             typeof(ITupleSpace),
             String.Format("tcp://{0}/TupleSpace", url));

            if (TupleSpace == null)
            {
                System.Console.WriteLine("Could not locate server");
                return;
            }
            ScriptFileName = scriptFile;
        }
        public void Add(ITuple tuple)
        {
            TupleSpace.Add(tuple);
        }

        public ITuple Read(ISchema schema)
        {
            // TODO: this must be blocking
            return TupleSpace.Read(schema);
        }
        
        public ITuple Take(ISchema schema)
        {
            // TODO: this must be blocking
            return TupleSpace.Take(schema);
        }

        public void RunScript()
        {
            ScriptClient.RunScript(ScriptFileName, this);
        }
    }
}