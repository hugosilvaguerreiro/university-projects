using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_Lib
{
    public interface IField
    {
        bool Compare(IField other);
    }

    [Serializable]
    public class StringField : IField
    {
        public string Value { get; set; }

        public StringField(string str)
        {
            Value = str;
        }

        public bool Compare(IField other)
        {
            if (other.GetType() != this.GetType()) { return false; }
            StringField otherStr = (StringField) other;
            if (otherStr.Value.Equals("*")) { return true; }
            else if (otherStr.Value.EndsWith("*"))
            {
                return this.Value.StartsWith(otherStr.Value.TrimEnd('*'));
            }
            else if (otherStr.Value.StartsWith("*"))
            {
                return this.Value.EndsWith(otherStr.Value.TrimStart('*'));
            }
            else
            {
                return this.Value.Equals(otherStr.Value);
            }
        }

        public override string ToString()
        {
            return "\"" + this.Value + "\"";
        }

        public override bool Equals(object obj)
        {
            if (obj.GetType() != this.GetType()) return false;
            StringField other = (StringField) obj;
            return Value.Equals(other.Value);
        }

        public override int GetHashCode()
        {
            return Value.GetHashCode();
        }
    }

    [Serializable]
    public class ObjectField : IField
    {
        public object Value { get; set; }

        public ObjectField(object obj)
        {
            Value = obj;
        }

        public bool Compare(IField other)
        {
            if (other.GetType() != this.GetType()) { return false; }
            ObjectField otherObj = (ObjectField) other;
            if (otherObj.Value == null) { return true; }
            if (otherObj.Value is Type) { return this.Value.GetType() == (Type)otherObj.Value; }
            return this.Value.Equals(otherObj.Value);
        }

        public override string ToString()
        {
            if (this.Value == null) return "null";
            return this.Value.ToString();
        }

        public override bool Equals(object obj)
        {
            if (obj.GetType() != this.GetType()) return false;
            ObjectField other = (ObjectField)obj;
            return Value.Equals(other.Value);
        }

        public override int GetHashCode()
        {
            return Value.GetHashCode();
        }
    }

    [Serializable]
    public class IntegerField : IField
    {
        public int Value { get; set; }

        public IntegerField(int value)
        {
            Value = value;
        }

        public bool Compare(IField other)
        {
            if (other.GetType() != this.GetType()) { return false; }
            IntegerField otherInt = (IntegerField)other;
            return otherInt.Value == this.Value;
        }

        public override string ToString()
        {
            return this.Value.ToString();
        }
    }
}
