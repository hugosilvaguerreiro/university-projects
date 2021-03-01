using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Tcp;
using System.Text;
using System.Threading.Tasks;
using DIDA_API;


namespace DIDA_Server
{
    class Server
    {
        static void Main(string[] args)
        {
            if(args.Length == 4)
            {
                System.Console.WriteLine(args);
                ServerApi serverApi = new ServerApi(Int32.Parse(args[1]));
                System.Console.ReadLine();
            }else
            {
                System.Console.WriteLine("Error, invalid arguments");
                System.Console.ReadLine();
            }

        }
    }
}