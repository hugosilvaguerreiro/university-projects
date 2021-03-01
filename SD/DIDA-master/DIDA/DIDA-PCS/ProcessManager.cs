using System;
using System.Diagnostics;

namespace DIDA_PCS
{
    public class ProcessManager : MarshalByRefObject
    {
        static readonly string serverLocation = @"C:\Users\Hugo Guerreiro\source\repos\DIDAPROJ\DIDA\DIDA-Server\bin\Debug\DIDA-Server";
        static readonly string clientLocation = @"C:\Users\Hugo Guerreiro\source\repos\DIDAPROJ\DIDA\DIDA-Client\bin\Debug\DIDA-Client";

        public ProcessManager()
        {

        }

        public void NewServer(string id, string port, int minDelay, int maxDelay)
        {
            Process.Start(serverLocation, String.Format("{0} {1} {2} {3}", id, port, minDelay, maxDelay));
            Console.WriteLine("Server Added");
        }

        public void NewClient(string id, string url, string file)
        {
            Process.Start(clientLocation, String.Format("{0} {1} {2}", id, url, file));
            Console.WriteLine("Client Added");
        }

        public void Status()
        {

        }
    }
}
