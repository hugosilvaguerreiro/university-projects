using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_Resources
{
    public static class DIDAUtils
    {
        public static int ExtractPortFromUrl(string url)
        {
            return Int32.Parse(url.Split(':')[2].Split('/')[0]);
        }

        public static string ExtractDomainFromUrl(string url)
        {
            return ExtractDomainFromUrl(url, false);
        }

        public static string ExtractDomainFromUrl(string url, Boolean keepPort)
        {
            //CONN_TYPE://HOST:PORT/OBJ_NAME
            string[] token = new string[] { "//" };
            if (keepPort)
                return url.Split(token, StringSplitOptions.None)[1].Split('/')[0];
            else
                return url.Split(token, StringSplitOptions.None)[1].Split(':')[0];
        }

        public static string ExtractObjectNameFromUrl(string url)
        {
            return url.Split(':')[2].Split('/')[1];
        }

        public static string ExtractConnectionTypeFromUrl(string url) {
            return url.Split(':')[0];
        }


        public static string JoinPaths(string path1, string path2, params string[] args)
        {
            string result = path1 + @"\" + path2;
            foreach (string path in args)
            {
                result += @"\" + path;
            }
            return result;
        }

    }
}
