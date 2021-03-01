using DIDA_Lib;
using System;
using System.Collections.Generic;

namespace DIDA_Client.script_client
{
    public static class ClassFactory
    {
        public static DADTest GetClass(string className, List<object> classFields)
        {
            switch (className)
            {
                case "DADTestA":
                    return new DADTestA((int)classFields[0], (string)classFields[1]);
                case "DADTestB":
                    return new DADTestB((int)classFields[0], (string)classFields[1], (int)classFields[2]);
                case "DADTestC":
                    return new DADTestC((int)classFields[0], (string)classFields[1], (string)classFields[2]);
            }
            return null;
        }

        public static Type GetClassType(string TypeName)
        {
            switch (TypeName)
            {
                case "DADTestA":
                    return typeof(DADTestA);
                case "DADTestB":
                    return typeof(DADTestB);
                case "DADTestC":
                    return typeof(DADTestC);
                case "null":
                    return null;
            }
            return null;
        }
    }
}
