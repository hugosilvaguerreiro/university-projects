using DIDA_Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_API
{
    [Serializable]
    public class TupleSpaceData
    {
        public List<ITuple> tuples;
        public HashSet<KeyValuePair<string, int>> recievedMsg;
        public HashSet<ITuple> lockedTuples;

        public TupleSpaceData(List<ITuple> tuples, HashSet<KeyValuePair<string, int>> recievedMsg, HashSet<ITuple> lockedTuples)
        {
            this.tuples = tuples;
            this.recievedMsg = recievedMsg;
            this.lockedTuples = lockedTuples;
        }

    }
}
