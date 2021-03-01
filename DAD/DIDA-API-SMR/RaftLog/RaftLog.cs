using DIDA_API_SMR.states;
using DIDA_Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace DIDA_API_SMR.Log
{
    [Serializable]
    public class RaftLog : IRaftLog
    {
        public List<Entry> LogList;
        public Dictionary<int, int> contains;
        public int CommitIndex;
        public int PreviousIndex;
        public int PreviousTerm;
        public int term;
        public Node node;
        private ReaderWriterLockSlim rw_lock;



        public RaftLog(Node node, int term) {
            rw_lock = new ReaderWriterLockSlim();
            LogList = new List<Entry>();
            contains = new Dictionary<int, int>();
            this.term = term;
            CommitIndex = 0;
            PreviousIndex = 0;
            PreviousTerm = 0;
            this.node = node;
        }
        public RaftLog(Node node,  List<Entry> logList, Dictionary<int,int> contains, int commitIndex, int previousIndex, int previousTerm)
        {
            rw_lock = new ReaderWriterLockSlim();
            this.contains = contains;
            LogList = logList;
            CommitIndex = commitIndex;
            PreviousIndex = previousIndex;
            PreviousTerm = previousTerm;
            this.node = node;
        }

        public bool Match(int LogIndex, int LogTerm)
        {
            rw_lock.EnterReadLock();
            if (LogIndex <= LogList.Count - 1)
            {
                bool result =  LogList[LogIndex].Term == LogTerm;
                rw_lock.ExitReadLock();
                return result;
            }
            rw_lock.ExitReadLock();
            return false;
        }

        public bool Conflict(int LogIndex, int LogTerm)
        {
            rw_lock.EnterReadLock();
            if (LogIndex <= LogList.Count - 1)
            {
                bool result =  LogList[LogIndex].Term != LogTerm;
                rw_lock.ExitReadLock();
                return result;
                
            }
            rw_lock.ExitReadLock();
            return false; //return false because we dont have this entry, therefore theres no conflict
        }

        public void DeleteEntryAndFollowingEntries(int LogEntry)
        {
            rw_lock.EnterWriteLock();
            for (int i = LogList.Count-1; i >=LogEntry; i--)//start removing from the back
            {
                Rollback(LogList[i]);
                LogList.RemoveAt(i);
            }
            rw_lock.ExitWriteLock();
        }

        public void Rollback(Entry entry) {
            //Do the oposite of the entry;
            //The schema and the tuple are saved when we commit a new entry.
            //We calculate the oposite action on commiting action
            switch (entry.Command)
            {
                case "add":
                    node.TakeS(entry.Schema);
                    break;
                case "take":
                    node.AddS(entry.Tuple);
                    break;
            }
        }

        public void CommitEntry(Entry entry)
        //this is where we execute the entry
        {
            switch(entry.Command)
            {
                case "add":
                    node.AddS(entry.Tuple);
                    //entry.Schema = new Schema(entry.Tuple.Fields); //Doing this because i need to know exactly 
                   break;                                          //what query was made to the system for rollback purposes
                case "take":
                    DIDA_Lib.Tuple t = (DIDA_Lib.Tuple)node.TakeS(entry.Schema);
                    //entry.Tuple = new DIDA_Lib.Tuple(t.Fields); //The same thing goes for the taken tuple;
                   break;
            }
        }

        public void AppendNewEntries(List<Entry> entry)
        {
            rw_lock.EnterWriteLock();

            foreach(Entry e in entry)
            {

                //) && contains[e.LogIndex] == e.Term)
                if (contains.ContainsKey(e.LogIndex))
                    continue;
                contains[e.LogIndex] = e.Term;
                LogList.Add(e);
                if (e.LogIndex > CommitIndex && (CommitIndex +1) == e.LogIndex)
                {
                    CommitIndex = e.LogIndex;
                }
                    
                CommitEntry(e);
            }
            rw_lock.ExitWriteLock();
        }

        public void UpdateCommitIndex(int leaderCommit)
        {
            //If leaderCommit > commitIndex, set commitIndex =
            //min(leaderCommit, index of last new entry)
            rw_lock.EnterReadLock();
            if (leaderCommit <= LogList.Count - 1)
                CommitIndex = leaderCommit;
            else
                CommitIndex = LogList.Count - 1;
            rw_lock.ExitReadLock();
        }

        public bool UpToDate(int lastLogIndex, int lastLogTerm)
        {
            //throw new NotImplementedException();
            //candidate’s log is at least as up - to - date as receiver’s log
            //this is the same as verifying if the last log index of the candidate is at least the same as 
            // the receiver's commit index;

            throw new NotImplementedException();
            return true;
        }
        public override string ToString()
        {
            string r = "Commit Index: "+this.CommitIndex+"\n";
            foreach (Entry e in LogList)
                r += e.ToString();
            return r;
        }
    }
}
