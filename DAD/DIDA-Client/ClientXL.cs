using DIDA_API;
using DIDA_Resources;
using DS = DIDA_Resources.DIDAStrings;
using DU = DIDA_Resources.DIDAUtils;
using System.Collections.Generic;
using System.IO;

namespace DIDA_Client
{
    class ClientXL : IClient
    {
        ClientApi Api { get; set; }
        private string ScriptFileLocation;
        private string ServersListLocation = DU.JoinPaths(DS.CommonResources, DS.ServersListDefaultName);

        public ClientXL(string id, string url, string scriptFileName)
        {
            ScriptFileLocation = DU.JoinPaths(DS.ClientResources, scriptFileName);
            string[] lines = File.ReadAllLines(ServersListLocation);
            Api = new ClientApi(id, url, ScriptFileLocation, new List<string>(lines));

        }

        public void RunScript()
        {
            ScriptClient.RunScript(ScriptFileLocation, Api);
        }
    }
}
