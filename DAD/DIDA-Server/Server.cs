using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Text;
using System.Threading.Tasks;
using DIDA_API;
using DIDA_API_SMR;
using DF = DIDA_Resources.DIDAFlags;
using DS = DIDA_Resources.DIDAStrings;
using DU = DIDA_Resources.DIDAUtils;

namespace DIDA_Server
{
    class Server
    {
        private static string ServersListLocation = DU.JoinPaths(DS.CommonResources, DS.ServersListDefaultName);

        static void Main(string[] args)
        {
            Console.WriteLine(String.Join(" ", args));

            object serverApi;

            if(args.Length == 4)
            {
                int currentCliType = DF.CurrentProjectType;
                string[] lines;
                switch (currentCliType)
                {
                    case (int)DF.ProjectTypes.SMR:
                        lines = File.ReadAllLines(ServersListLocation);
                        serverApi = new ServerApiSmr(args[0], args[1], Int32.Parse(args[2]), Int32.Parse(args[3]), new List<string>(lines));
                        break;

                    case (int)DF.ProjectTypes.XL:
                        lines = File.ReadAllLines(ServersListLocation);
                        serverApi = new ServerApi(args[0], args[1], Int32.Parse(args[2]), Int32.Parse(args[3]), new List<string>(lines));
                        break;

                    default:
                        lines = File.ReadAllLines(ServersListLocation);
                        serverApi = new ServerApi(args[0], args[1], Int32.Parse(args[2]), Int32.Parse(args[3]), new List<string>(lines));
                        break;
                }
                Console.ReadLine();
            }
            else { 
            
                Console.WriteLine("Error, invalid arguments");
                Console.ReadLine();
            }
        }
    }
}