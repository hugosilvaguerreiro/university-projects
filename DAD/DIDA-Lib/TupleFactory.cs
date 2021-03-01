using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_Lib
{
    public static class TupleFactory
    {
        public static Tuple MakeTuple(params object[] parameters)
        {
            IField[] fields = new IField[parameters.Length];
            for(int i = 0; i < parameters.Length; i++)
            {
                if (parameters[i] == null) throw new ArgumentNullException();
                else if(parameters[i].GetType() == typeof(string))
                {
                    string str = (string)parameters[i];
                    if (str.Contains("*")) throw new ArgumentException("'*' is not allowed");
                    fields[i] = new StringField(((string) parameters[i]).ToLower());
                }
                else
                {
                    fields[i] = new ObjectField(parameters[i]);
                }
            }
            return new Tuple(fields);
        }

        public static Schema MakeSchema(params object[] parameters)
        {
            IField[] fields = new IField[parameters.Length];
            for (int i = 0; i < parameters.Length; i++)
            {
                if (parameters[i] == null) fields[i] = new ObjectField(null);
                else if (parameters[i].GetType() == typeof(string))
                {
                    fields[i] = new StringField(((string)parameters[i]).ToLower());
                }
                else
                {
                    fields[i] = new ObjectField(parameters[i]);
                }
            }
            return new Schema(fields);
        }
    }
}
