﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DIDA_Lib
{
    public interface IManageable
    {
        void Status();
        void Freeze();
        void Unfreeze();
    }
}
