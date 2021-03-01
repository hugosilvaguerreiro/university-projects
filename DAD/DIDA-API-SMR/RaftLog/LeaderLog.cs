using System;
using System.Collections.Generic;
using DF = DIDA_Resources.DIDAFlags;

namespace DIDA_API_SMR.Log
{
    public class LeaderLog : IRaftLog
    {
        public Node node;
        public RaftLog log;
        public Dictionary<int, int> FollowersCommitIndex = new Dictionary<int, int>();
        //private Dictionary<int, int> FollowersPreviousIndex;
        public Dictionary<int, int> FollowersPreviousTerm = new Dictionary<int, int>();
        //public Dictionary<int, List<Entry>> FollowersLogList = new Dictionary<int, int>();
        public int term;
        public int CommitIndex;
        // The leader maintains a nextIndex for each follower.
        // This is the index of the log entry that the leader will send to that follower.

        public LeaderLog(Node node, RaftLog log, int term)
        {
            this.term = term;
            this.log = log;
            this.node = node;
            if (log.LogList.Count == 0)
                CommitIndex = 0;
            else
                CommitIndex = log.LogList.Count - 1;
            for(int i=0; i < DF.N-1; i++)
            {
                FollowersCommitIndex[i] = 0;
                //FollowersPreviousTerm[i] = this.term;
            }

        }

        public bool Conflict(int previousLogTerm1, int previousLogTerm2)
        {
            return this.log.Conflict(previousLogTerm1, previousLogTerm2);
        }

        public void AppendNewEntries(List<Entry> entry)
        {
            this.log.AppendNewEntries(entry);
        }

        public void UpdateCommitIndex(int leaderCommit)
        {
            this.log.UpdateCommitIndex(leaderCommit);
        }

        public bool UpToDate(int lastLogIndex, int lastLogTerm)
        {
            return this.log.UpToDate(lastLogIndex, lastLogTerm);
        }


        public bool Match(int previousLogIndex, int previousLogTerm)
        {
            return this.log.Match(previousLogIndex, previousLogTerm);
        }

        public void DeleteEntryAndFollowingEntries(int logIndex)
        {
            this.log.DeleteEntryAndFollowingEntries(logIndex);
        }
    }
}

