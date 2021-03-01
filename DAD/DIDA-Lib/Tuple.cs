﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_Lib
{
    [Serializable]
    public class Tuple : ITuple
    {
        public IField[] Fields { get; }

        public Tuple(params IField[] fields)
        {
            Fields = fields;
        }

        public IField this[int i] => Fields[i];
        public int Size { get { return Fields.Length; } }

        public override string ToString()
        {
            return string.Format("({0})", String.Join(", ", Fields.Select(item => item.ToString())));
        }

        public bool MatchesSchema(ISchema schema)
        {
            if (this.Size != schema.Size) { return false; }

            bool match = true;
            for (int i = 0; i < this.Fields.Length; i++)
            {
                match &= this[i].Compare(schema[i]);
                if (!match) return match;
            }
            return match;
        }

        public override bool Equals(object obj)
        {
            if (obj.GetType() != this.GetType()) return false;
            else
            {
                ITuple other = (ITuple) obj;
                if (this.Size != other.Size) { return false; }

                bool match = true;
                for (int i = 0; i < this.Fields.Length; i++)
                {
                    match &= this[i].Equals(other[i]);
                    if (!match) return match;
                }
                return match;
            }
        }

        public override int GetHashCode()
        {
            int res = 0;
            for (int i = 0; i < this.Fields.Length; i++)
            {
                res ^= this[i].GetHashCode();
            }
            return res;
        }
    }
}