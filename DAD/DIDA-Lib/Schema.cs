using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_Lib
{
    [Serializable]
    public class Schema : ISchema
    {

        public IField[] Fields { get; }

        public Schema(params IField[] fields)
        {
            Fields = fields;
        }

        public IField this[int i] => Fields[i];

        public int Size { get { return Fields.Length; } }

        public override string ToString()
        {
            return string.Format("[{0}]", String.Join(", ", Fields.Select(item => item.ToString())));
        }
    }
}
