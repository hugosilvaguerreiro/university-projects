using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Threading;
using System.Threading.Tasks;
using DIDA_PCS;
using DS = DIDA_Resources.DIDAStrings;
using DU = DIDA_Resources.DIDAUtils;
namespace DIDA_PuppetMaster
{
    class PuppetMaster
    {
        static Dictionary<string, ProcessManager> Pcss = new Dictionary<string, ProcessManager>();
        static Dictionary<string, string> idPcs = new Dictionary<string, string>();

        static readonly string configlocation = DU.JoinPaths(DS.PCSResources, "PCSconfig.txt");
        static readonly string scriptlocation = DU.JoinPaths(DS.PuppetMasterResources, "puppetscript.txt");

        static void Main(string[] args)
        {
            TcpChannel Channel = new TcpChannel();
            ChannelServices.RegisterChannel(Channel, false);
                
            string[] lines = File.ReadAllLines(configlocation);
            Console.WriteLine("Reading cfgfile.txt = ");
            foreach (string line in lines)
            {
                if (line.StartsWith(@"//")) continue;

                Console.WriteLine("\t Creating reference for remote object at: " + line);
                ProcessManager pcs = (ProcessManager)Activator.GetObject(
                    typeof(ProcessManager),
                    line);
                if (pcs == null)
                {
                    Console.WriteLine("Could not locate server at " + line);
                }
                else
                {
                    Pcss.Add(DU.ExtractDomainFromUrl(line), pcs);
                    Console.WriteLine("\t reference added");
                }
                
            }
        
            Console.WriteLine();

            if (File.Exists(scriptlocation))
            {
                Console.WriteLine("Executing Script");
                lines = File.ReadAllLines(scriptlocation);
                foreach (string line in lines)
                {
                    if (line.StartsWith("//")) continue;
                    Console.WriteLine("\t Executing Command: " + line);
                    ExecuteCommand(line);
                }
            }
           
            Console.WriteLine("Write command manually, Exit to quit");
            bool exit = false;
            while (!exit)
            {
                string command = Console.ReadLine();
                if (command.StartsWith("//")) continue;
                if (command != "Exit")
                {
                        
                    ExecuteCommand(command);
                }
                else
                {
                    exit = true;
                }
            }
            
        }

        static async void ExecuteCommand(string command)
        {
            string[] args = command.Split(null);
            ProcessManager pcs;
            string url;
            switch (args[0])
            {
                case "Server":
                    Console.WriteLine("Creating new Server");
                    url = DU.ExtractDomainFromUrl(args[2]);
                    pcs = Pcss[url];
                    idPcs.Add(args[1], url);
                    await Task.Run(() =>
                    {

                    pcs.NewServer(args[1], args[2], Int32.Parse(args[3]), Int32.Parse(args[4]));

                    });
                    Console.WriteLine("Server Created "+ args[2]);
                    break;

                case "Client":
                    Console.WriteLine("Creating new Client");
                    url = DU.ExtractDomainFromUrl(args[2]);
                    pcs = Pcss[url];
                    idPcs.Add(args[1], url);
                    await Task.Run(() =>
                    {
                        pcs.NewClient(args[1], args[2], args[3]);
                    });
                    Console.WriteLine("Client Created " + args[2]);
                    break;

                case "Status":
                    foreach (KeyValuePair<string, ProcessManager> pm in Pcss)
                    {
                        await Task.Run(() =>
                        {
                            pm.Value.Status();

                        });
                    }
                    break;

                case "Crash":
                    url = idPcs[args[1]];
                    pcs = Pcss[url];
                    Console.WriteLine("Crashing Process " + args[1]);
                    await Task.Run(() =>
                    {
                        pcs.Crash(args[1]);
                    });
                    
                    break;

                case "Freeze":
                    url = idPcs[args[1]];
                    pcs = Pcss[url];

                    await Task.Run(() =>
                    {
                        pcs.Freeze(args[1]);
                    });
                    
                    Console.WriteLine("Freezing Process ", args[1]);
                    break;

                case "Unfreeze":
                    url = idPcs[args[1]];
                    pcs = Pcss[url];
                    await Task.Run(() =>
                    {
                        pcs.Unfreeze(args[1]);
                    });
                    Console.WriteLine("Unfreezing Process ", args[1]);
                    break;

                case "Wait":
                    Thread.Sleep(Int32.Parse(args[1]));
                    break;

                default:
                    Console.WriteLine("Invalid command");
                    break;
            }
        }
    }
}

