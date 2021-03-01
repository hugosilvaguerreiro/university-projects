using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using DIDA_PCS;

namespace DIDA_PuppetMaster
{
    class PuppetMaster
    {
        static Dictionary<string, ProcessManager> Pcss = new Dictionary<string, ProcessManager>();
        static readonly string configlocation = @"C:\Users\Hugo Guerreiro\Desktop\cfgfile.txt";
        static readonly string scriptlocation = @"C:\Users\Hugo Guerreiro\Desktop\scriptfile.txt";

        static void Main(string[] args)
        {
            TcpChannel Channel = new TcpChannel();
            ChannelServices.RegisterChannel(Channel, false);
                
            string[] lines = File.ReadAllLines(configlocation);
            Console.WriteLine("Reading cfgfile.txt = ");
            foreach (string line in lines)
            {
                Console.WriteLine("\t Creating reference for remote object at: " + line);
                ProcessManager pcs = (ProcessManager)System.Activator.GetObject(
                    typeof(ProcessManager),
                    String.Format("tcp://{0}/ProcessManager", line));
                if (pcs == null)
                {
                    Console.WriteLine("Could not locate server at " + line);
                }
                else
                {
                    Pcss.Add(line.Split(':')[0], pcs);                
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
                    Console.WriteLine("\t Executing Command: " + line);
                    ExecuteCommand(line);
                }
            }
            else
            {
                Console.WriteLine("Script Not found, write command manually");
                bool exit = false;
                while (!exit)
                {
                    string command = Console.ReadLine();
                    if (command != "exit")
                    {
                        ExecuteCommand(command);
                    }
                    else
                    {
                        exit = true;
                    }
                }
            }
            Console.ReadLine();
        }


        static void ExecuteCommand(string command)
        {
            string[] args = command.Split(null);
            ProcessManager pcs;
            switch (args[0])
            {
                case "Server":
                    Console.WriteLine("Creating new Server");
                    pcs = Pcss[args[2].Split(':')[0]];
                    string port = args[2].Split(':')[1];
                    pcs.NewServer(args[1], port, Int32.Parse(args[3]), Int32.Parse(args[4]));
                    Console.WriteLine("Server Created "+ args[2].Split(':')[1]);
                    break;
                case "Client":
                    Console.WriteLine("Creating new Client");
                    pcs = Pcss[args[2].Split(':')[0]];
                    pcs.NewClient(args[1], args[2], args[3]);
                    Console.WriteLine("Client Created " + args[2].Split(':')[1]);
                    break;
                case "Status":
                    //TODO
                    break;
                case "Crash":
                    //TODO
                    break;
                case "Freeze":
                    //TODO
                    break;
                case "Unfreeze":
                    //TODO
                    break;
                default:
                    Console.WriteLine("Invalid command");
                    break;
            }
        }
    }
}

