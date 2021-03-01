using DIDA_Resources;
using DF = DIDA_Resources.DIDAFlags;

namespace DIDA_Client
{
    class Client
    {
        static void Main(string[] args)
        {
            //args[0] - id
            //args[1] - url
            //args[2] - script file -> script file name
            //args[3] - server urls -> not needed
            IClient client;
            int currentCliType = DF.CurrentProjectType;
            switch(currentCliType)
            {
                case (int)DF.ProjectTypes.SMR:
                    //client = new ClientSMR(args[0], args[1], args[2], args[3]);
                    client = new ClientSMR(args[0], args[1], args[2]);
                    break;
                case (int)DF.ProjectTypes.XL:
                    //client = new ClientXL(args[0], args[1], args[2], args[3]);
                    client = new ClientXL(args[0], args[1], args[2]);
                    break;
                default:
                    //client = new ClientXL(args[0], args[1], args[2]);
                    client = new ClientXL(args[0], args[1], args[2]);
                    break;
            }
            client.RunScript();
            System.Console.ReadLine();
        }
    }
}
