using System;
using System.Collections.Generic;

namespace DIDA_Lib
{
    [Serializable]
    public abstract class DADTest { }

    [Serializable]
    public class DADTestA : DADTest
    {
        public int i1;
        public string s1;

        public DADTestA(int pi1, string ps1)
        {
            i1 = pi1;
            s1 = ps1;
        }
        public override string ToString()
        {
            return String.Format("DADTestA({0}, {1})", i1, s1);
        }

        public override bool Equals(object o)
        {
            if (o == null) return false;
            if (o.GetType() != this.GetType()) return false;
            DADTestA otherObj = (DADTestA)o;
            return ((this.i1 == otherObj.i1) && (this.s1.Equals(otherObj.s1)));
        }

        public override int GetHashCode()
        {
            return i1.GetHashCode() ^ s1.GetHashCode();
        }
    }
    [Serializable]
    public class DADTestB : DADTest
    {
        public int i1;
        public string s1;
        public int i2;

        public DADTestB(int pi1, string ps1, int pi2)
        {
            i1 = pi1;
            s1 = ps1;
            i2 = pi2;
        }

        public override string ToString()
        {
            return String.Format("DADTestB({0}, {1}, {2})", i1, s1, i2);
        }

        public override bool Equals(object o)
        {
            if (o == null) return false;
            if (o.GetType() != this.GetType()) return false;
            DADTestB otherObj = (DADTestB)o;
            return ((this.i1 == otherObj.i1) && (this.s1.Equals(otherObj.s1)) && (this.i2 == otherObj.i2));
        }
        public override int GetHashCode()
        {
            return i1.GetHashCode() ^ s1.GetHashCode() ^ i2.GetHashCode();
        }
    }
    [Serializable]
    public class DADTestC : DADTest
    {
        public int i1;
        public string s1;
        public string s2;

        public DADTestC(int pi1, string ps1, string ps2)
        {
            i1 = pi1;
            s1 = ps1;
            s2 = ps2;
        }

        public override bool Equals(object o)
        {
            if (o == null) return false;
            if (o.GetType() != this.GetType()) return false;
            DADTestC otherObj = (DADTestC)o;
            return ((this.i1 == otherObj.i1) && (this.s1.Equals(otherObj.s1)) && (this.s2.Equals(otherObj.s2)));
        }

        public override string ToString()
        {
            return String.Format("DADTestC({0}, {1}, {2})", i1, s1, s2);
        }

        public override int GetHashCode()
        {
            return i1.GetHashCode() ^ s1.GetHashCode() ^ s2.GetHashCode();
        }
    }

}