using DIDA_API_SMR.Log;
using DIDA_Lib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_API_SMR
{
    [Serializable]
    public abstract class IMessage {};

    [Serializable]
    public class AppendEntriesMessage : IMessage {
        public int Term { get; set; } //leader’s term
        //String leaderId; //probably not needed
        public int PreviousLogIndex { get; set; }//index of log entry immediately preceding new ones
        public int PreviousLogTerm { get; set; } //term of prevLogIndex entry
        public int LeaderCommit { get; set; }
        public int SharedId { get; set; }
        public List<Entry> Entry { get; set; }
        public string url { get; set; }

        public AppendEntriesMessage(int term, int previousLogIndex, 
                                    int previousLogTerm, int leaderCommit, 
                                    List<Entry> entry, int sharedId, string url)
        {
            Term = term;
            PreviousLogIndex = previousLogIndex;
            PreviousLogTerm = previousLogTerm;
            Entry = entry;
            LeaderCommit = leaderCommit;
            SharedId = SharedId;
        }
    }

    [Serializable]
    public class RequestVoteMessage : IMessage
    {
        public int Term;
        public int LogSize;
        public String CandidateId;
        public int LastLogIndex;
        public int LastLogTerm;
        public int SharedId;

        public RequestVoteMessage(int term, int logSize, String candidateId, int sharedId)
        {
            Term = term;
            LogSize = logSize;
            CandidateId = candidateId;
            SharedId = sharedId;
        }
    }
    [Serializable]
    public class RequestLogResponse : IMessage
    {
        public RaftLog Log;
        public TupleSpace tupleSpace;

        public RequestLogResponse(RaftLog l, TupleSpace t) {
            Log = l;
            tupleSpace = t;

        }

    }

    [Serializable]
    public class ResponseMessage : IMessage
    {
        public int Term { get; set; }
        public bool Success { get; set; }
        public int CommitIndex { get; set; }
        public int SharedId { get; set; }

        public ResponseMessage(int term, bool success, int SharedId)
        {
            Term = term;
            Success = success;
            this.SharedId = SharedId;
            CommitIndex = 0;
        }
        public ResponseMessage(int term, bool success, int SharedId, int commitIndex)
        {
            Term = term;
            Success = success;
            this.SharedId = SharedId;
            CommitIndex = commitIndex;
        }
    }

    [Serializable]
    public class Entry
    {
        public String Command { get; set; }
        public ITuple Tuple { get; set; }
        public ISchema Schema { get; set; }
        public int Term { get; set; }
        public int LogIndex { get; set; }

        public Entry(String Command, ITuple tuple, int term, int logIndex) {
            this.Command = Command;
            this.Tuple = tuple;
            this.Term = term;
            LogIndex = logIndex;
        }

        public Entry(String Command, ISchema schema, int term, int logIndex) {
            this.Command = Command;
            this.Schema = schema;
            this.Term = term;
            LogIndex = logIndex;
        }

        public override string ToString()
        {
            String result = "";
            result += "----------- ENTRY " + LogIndex + " -----------\n";
            result += "Term    : " + Term+"\n";
            result += "Index   : " + LogIndex+"\n";
            switch (Command)
            {
                case "add":
                    result += "Command : ADD " + Tuple.ToString()+"\n";
                    //result += "Rollback: TAKE"+ Schema.ToString() + "\n";
                    break;
                case "take":
                    result += "Command : TAKE " + Schema.ToString() + "\n";
                    //result += "Rollback: ADD " + Tuple.ToString() + "\n";
                    break;
            }
            result += "---------------------------------\n";
            return result;

        }
    }
}
