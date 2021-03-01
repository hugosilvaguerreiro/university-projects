using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_Lib
{
    public interface ITuple
    {
        IField[] Fields { get; }

        int Size { get; }

        IField this[int i] { get; }

        bool MatchesSchema(ISchema schema);
    }
}
