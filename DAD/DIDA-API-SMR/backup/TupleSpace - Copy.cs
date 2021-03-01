using DIDA_Lib;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace DIDA_API_SMR
{
    public class TupleSpace
    {
        private List<ITuple> tuples;
        private ReaderWriterLockSlim rw_lock;

        public TupleSpace()
        {
            tuples = new List<ITuple>();
            rw_lock = new ReaderWriterLockSlim();
        }

        public void Add(ITuple tuple)
        {
            Monitor.Enter(tuples);
            tuples.Add(tuple);
            Console.WriteLine("ADD: " + tuple);
            Monitor.PulseAll(tuples);
            Monitor.Exit(tuples);
        }

        public ITuple Read(ISchema schema)
        {
            ITuple t = BlockingFind(schema);
            Console.WriteLine("READ: " + t);
            return t;
        }

        public ITuple Take(ISchema schema)
        {
            ITuple tuple = BlockingFind(schema);

            rw_lock.EnterWriteLock();
            bool ok = tuples.Remove(tuple);
            rw_lock.ExitWriteLock();
            if (ok)
            {
                Console.WriteLine("TAKE: " + tuple);
                return tuple;
            }
            else
            {
                return Take(schema);
            }
        }


        private ITuple BlockingFind(ISchema schema)
        {
            ITuple tuple;
            while ((tuple = FindFirst(schema)) == null)
            {
                Monitor.Enter(tuples);
                Monitor.Wait(tuples);
                Monitor.Exit(tuples);
            }
            return tuple;
        }

        private ITuple FindFirst(ISchema schema)
        {
            rw_lock.EnterReadLock();
            ITuple tuple = tuples.FirstOrDefault(item => item.MatchesSchema(schema));
            rw_lock.ExitReadLock();
            return tuple;
        }
    }
}
