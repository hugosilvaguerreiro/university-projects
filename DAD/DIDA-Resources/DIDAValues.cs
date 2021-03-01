using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_Resources
{
    public static class DIDAFlags
    {
        public enum ProjectTypes : int { XL, SMR };
        public enum ProjectVersion : int { BASIC, ADVANCED };
        public static int CurrentProjectType = (int)ProjectTypes.XL;
        public static int CurrentProjectVersion = (int)ProjectVersion.BASIC;
        public static readonly int FreezeStep = 50;

        //FAIL DETECT
        public static int TIMEOUT = 900;
        
        //LEADER
        public static int HEARTBEATTIMEOUT = 100;
        
        //FOLLOWER
        public static int REQUESTTIMEOUT = 900;
        public static int SMRFOLLOWERTIMEOUT(){ return new Random(
                                            Guid.NewGuid().GetHashCode()).Next(
                                            1500, 3000); }
        //CANDIDATE
        public static int REQUESTVOTESTIMEOUT = 100;
        public static int SMRCANDIDATETIMEOUT(){return new Random(
                                                Guid.NewGuid().GetHashCode()).Next(
                                               1000, 2000);}
        public static int N = 4;

    }

    public static class DIDAStrings
    {
        public readonly static string CommonResources = @"..\..\..\DIDA-Resources\Common";
        public readonly static string ClientResources = @"..\..\..\DIDA-Resources\ClientResources";
        public readonly static string PCSResources = @"..\..\..\DIDA-Resources\PCSResources";
        public readonly static string PuppetMasterResources = @"..\..\..\DIDA-Resources\PuppetMasterResources";
        public readonly static string ServerResources = @"..\..\..\DIDA-Resources\ServerResources";

        public readonly static string ServersListDefaultName = "serversList.txt";

        public readonly static string ServerExecutableLocation = @"..\..\..\DIDA-Server\bin\Debug\DIDA-Server";
        public readonly static string ClientExecutableLocation = @"..\..\..\DIDA-Client\bin\Debug\DIDA-Client";

    }
}
