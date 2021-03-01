using DIDA_API;
using DIDA_API_SMR;
using System.Collections.Generic;
using System.IO;
using DS = DIDA_Resources.DIDAStrings;
using DU = DIDA_Resources.DIDAUtils;

namespace DIDA_Client
{
    class ClientSMR : IClient
    {
        ClientSmrApi Api { get; set; }
        private string ScriptFileName;
        private string ServersListLocation = DU.JoinPaths(DS.CommonResources, DS.ServersListDefaultName);

        public ClientSMR(string id, string url, string scriptFileName)
        {
            ScriptFileName = DU.JoinPaths(DS.ClientResources, scriptFileName);
            string[] lines = File.ReadAllLines(ServersListLocation);
            Api = new ClientSmrApi(id, url, ScriptFileName, new List<string>(lines));

        }

        public void RunScript()
        {
            ScriptClient.RunScript(ScriptFileName, Api);
        }
    }
}
