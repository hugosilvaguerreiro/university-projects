using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace DIDA_SCRIPT_CLIENT
{
    class Program
    {
        static void bla2(string[] args)
        {
            //string BaseDir = "C:\Users\Hugo Guerreiro\Desktop";
            string sentence = "add 'oi bla', MyClass(1,'2')";
            string[] digits = Regex.Split(sentence, @"\D+");
            System.Console.WriteLine(digits);
            
        }
    }
}
