using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_Lib
{
    public interface ITupleSpace
    {
        void Add(ITuple tuple);

        ITuple Read(ISchema schema);

        ITuple Take(ISchema schema);
    }
}
