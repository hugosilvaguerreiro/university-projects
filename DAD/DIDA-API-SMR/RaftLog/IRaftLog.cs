using DIDA_API_SMR.states;

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_API_SMR.Log
{
    interface IRaftLog
    {

        bool Match(int previousLogIndex, int previousLogTerm);

        bool Conflict(int LogIndex, int Term);

        void DeleteEntryAndFollowingEntries(int logIndex);

        void AppendNewEntries(List<Entry> entry);

        void UpdateCommitIndex(int leaderCommit);

        bool UpToDate(int lastLogIndex, int lastLogTerm);
    }
}
