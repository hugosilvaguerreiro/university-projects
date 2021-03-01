using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_Lib
{
    [Serializable]
    public class SMRNotLeaderException : ApplicationException
    {
        public string leaderUrl;

        public SMRNotLeaderException(string leaderUrl)
        {
            this.leaderUrl = leaderUrl;
        }

        public SMRNotLeaderException(System.Runtime.Serialization.SerializationInfo info,
            System.Runtime.Serialization.StreamingContext context)
            : base(info, context)
        {
            leaderUrl = info.GetString("leaderUrl");
        }

        public override void GetObjectData(System.Runtime.Serialization.SerializationInfo info,
            System.Runtime.Serialization.StreamingContext context)
        {
            base.GetObjectData(info, context);
            info.AddValue("leaderUrl", leaderUrl);
        }

    }


    [Serializable]
    public class SMRCandidateException : ApplicationException
    {
        public SMRCandidateException()
        {
        }

        public SMRCandidateException(System.Runtime.Serialization.SerializationInfo info,
            System.Runtime.Serialization.StreamingContext context)
            : base(info, context)
        {
        }

        public override void GetObjectData(System.Runtime.Serialization.SerializationInfo info,
            System.Runtime.Serialization.StreamingContext context)
        {
            
        }

    }


}
